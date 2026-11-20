package com.example.bandb.service;

import com.example.bandb.model.Admin;
import com.example.bandb.model.Customer;
import com.example.bandb.model.Person;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * PRIVATE CUSTOMER DATABASE
 *
 *  - The ArrayList of accounts is private and there is NO method that
 *    returns it, a copy of it, or lets anyone browse other customers.
 *  - Passwords are never stored: only a salted PBKDF2 hash is kept.
 *  - The only things the rest of the app can do are register() and
 *    login(); login() returns just the logged-in person's own profile.
 *  - The data file is readable only by the current OS user
 *    (see SecureFiles) and lives outside the project / Git repository.
 */
public final class CustomerData {

    private static final int ITERATIONS = 210000;
    private static final int KEY_LENGTH = 256;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_MILLIS = 60_000;

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final ArrayList<AccountRecord> accounts =
            new ArrayList<>();

    private static final Map<String, long[]> failedLogins =
            new HashMap<>();

    private static boolean loaded = false;

    private CustomerData() {
    }

    private static Path file() {
        return SecureFiles.dataDirectory().resolve("customers.properties");
    }

    private static final class AccountRecord {
        private final Person person;
        private final String salt;
        private final String passwordHash;

        private AccountRecord(Person person, String salt, String hash) {
            this.person = person;
            this.salt = salt;
            this.passwordHash = hash;
        }
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    // ------------------------------------------------------------ create

    /**
     * @return false when the username or e-mail is already taken
     * @throws IllegalArgumentException for invalid input
     */
    public static synchronized boolean register(
            Customer customer, String password
    ) {
        load();

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer information is required.");
        }

        validatePassword(password);

        if (isTaken(customer.getUsername(), customer.getEmail())) {
            return false;
        }

        AccountRecord record = newRecord(customer, password);
        accounts.add(record);

        try {
            save();
        } catch (RuntimeException exception) {
            accounts.remove(record);
            throw exception;
        }

        return true;
    }

    /**
     * Creates the shop's admin account the very first time the app runs.
     * The random password is returned ONCE so it can be shown to the
     * owner; it is never written anywhere in readable form.
     *
     * @return {username, password}, or null when an admin already exists
     */
    public static synchronized String[] ensureAdminAccount() {
        load();

        for (AccountRecord record : accounts) {
            if (record.person.canManageInventory()) {
                return null;
            }
        }

        String username = "admin";
        int suffix = 1;

        while (isTaken(username, username + "@blushandbloom.local")) {
            username = "admin" + suffix++;
        }

        String alphabet =
                "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            password.append(
                    alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }

        Admin admin = new Admin(
                "Shop Administrator",
                username,
                username + "@blushandbloom.local");

        AccountRecord record = newRecord(admin, password.toString());
        accounts.add(record);

        try {
            save();
        } catch (RuntimeException exception) {
            accounts.remove(record);
            throw exception;
        }

        return new String[]{username, password.toString()};
    }

    // -------------------------------------------------------------- read

    /**
     * @return the logged-in person's own profile, or null when the
     *         username / password is wrong
     * @throws IllegalStateException when the account is temporarily locked
     */
    public static synchronized Person login(
            String username, String password
    ) {
        load();

        if (username == null || password == null
                || username.isBlank() || password.isEmpty()) {
            return null;
        }

        String key = normalize(username);
        long[] state = failedLogins.get(key);
        long now = System.currentTimeMillis();

        if (state != null && state[1] > now) {
            throw new IllegalStateException(
                    "Too many failed attempts. Please try again in "
                            + ((state[1] - now) / 1000 + 1)
                            + " seconds.");
        }

        AccountRecord found = null;

        for (AccountRecord record : accounts) {
            if (record.person.getUsername().equalsIgnoreCase(key)) {
                found = record;
                break;
            }
        }

        // Always hash, even for unknown users, so response time does
        // not reveal which usernames exist.
        byte[] salt = found == null
                ? new byte[16]
                : Base64.getDecoder().decode(found.salt);

        byte[] actual = hashPassword(password, salt);
        boolean correct = found != null && MessageDigest.isEqual(
                Base64.getDecoder().decode(found.passwordHash), actual);

        if (!correct) {
            long[] updated = state == null ? new long[2] : state;
            updated[0]++;

            if (updated[0] >= MAX_FAILED_ATTEMPTS) {
                updated[0] = 0;
                updated[1] = now + LOCK_MILLIS;
            }

            failedLogins.put(key, updated);
            return null;
        }

        failedLogins.remove(key);
        return found.person;
    }

    // ---------------------------------------------------------- helpers

    private static void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException(
                    "Password must contain at least 6 characters.");
        }
    }

    private static boolean isTaken(String username, String email) {
        for (AccountRecord record : accounts) {
            if (record.person.getUsername().equalsIgnoreCase(username)
                    || record.person.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    private static AccountRecord newRecord(Person person, String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);

        return new AccountRecord(
                person,
                Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(
                        hashPassword(password, salt)));
    }

    private static byte[] hashPassword(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

        try {
            return SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to process password.", exception);
        } finally {
            spec.clearPassword();
        }
    }

    // ------------------------------------------------------ persistence

    private static void load() {
        if (loaded) {
            return;
        }

        Properties data = SecureFiles.readProperties(file());
        ArrayList<AccountRecord> restored = new ArrayList<>();

        try {
            int count = Integer.parseInt(data.getProperty("count", "0"));

            if (count < 0) {
                throw new IllegalArgumentException("Invalid account file.");
            }

            for (int i = 0; i < count; i++) {
                String key = "customer." + i + ".";

                String fullName = data.getProperty(key + "fullName");
                String username = data.getProperty(key + "username");
                String email = data.getProperty(key + "email");
                String salt = data.getProperty(key + "salt");
                String hash = data.getProperty(key + "hash");
                String role = data.getProperty(
                        key + "role", Customer.ROLE);

                if (fullName == null || username == null
                        || email == null || salt == null || hash == null) {
                    throw new IllegalArgumentException(
                            "Incomplete customer record.");
                }

                if (Base64.getDecoder().decode(salt).length != 16
                        || Base64.getDecoder().decode(hash).length != 32) {
                    throw new IllegalArgumentException(
                            "Invalid password record.");
                }

                Person person = Admin.ROLE.equals(role)
                        ? new Admin(fullName, username, email)
                        : new Customer(fullName, username, email);

                for (AccountRecord existing : restored) {
                    if (existing.person.getUsername()
                            .equalsIgnoreCase(username)
                            || existing.person.getEmail()
                            .equalsIgnoreCase(email)) {
                        throw new IllegalArgumentException(
                                "Duplicate saved account.");
                    }
                }

                restored.add(new AccountRecord(person, salt, hash));
            }

        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    "Unable to load saved customer accounts.", exception);
        }

        accounts.clear();
        accounts.addAll(restored);
        loaded = true;
    }

    private static void save() {
        Properties data = new Properties();
        data.setProperty("count", String.valueOf(accounts.size()));

        for (int i = 0; i < accounts.size(); i++) {
            AccountRecord record = accounts.get(i);
            String key = "customer." + i + ".";

            data.setProperty(key + "fullName", record.person.getFullName());
            data.setProperty(key + "username", record.person.getUsername());
            data.setProperty(key + "email", record.person.getEmail());
            data.setProperty(key + "role", record.person.getRole());
            data.setProperty(key + "salt", record.salt);
            data.setProperty(key + "hash", record.passwordHash);
        }

        SecureFiles.writeProperties(
                file(), data, "Blush and Bloom Customer Accounts");
    }
}
