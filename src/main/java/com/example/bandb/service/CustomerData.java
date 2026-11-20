
package com.example.bandb.service;

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
 * Customer-only account management for Blush & Bloom.
 *
 * - Customers can register and log in.
 * - Passwords are stored as salted PBKDF2 hashes.
 * - Accounts are saved outside the project folder.
 * - Existing customer accounts can be restored.
 * - Previously saved admin accounts are ignored.
 */
public final class CustomerData {

    private static final int ITERATIONS = 210000;
    private static final int KEY_LENGTH = 256;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_MILLIS = 60_000;

    private static final SecureRandom RANDOM =
            new SecureRandom();

    private static final ArrayList<AccountRecord> accounts =
            new ArrayList<>();

    private static final Map<String, long[]> failedLogins =
            new HashMap<>();

    private static boolean loaded = false;

    private CustomerData() {
    }

    private static Path file() {
        return SecureFiles.dataDirectory()
                .resolve("customers.properties");
    }

    private static final class AccountRecord {

        private final Person person;
        private final String salt;
        private final String passwordHash;

        private AccountRecord(
                Person person,
                String salt,
                String hash
        ) {
            this.person = person;
            this.salt = salt;
            this.passwordHash = hash;
        }
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    // ------------------------------------------------------------
    // CUSTOMER REGISTRATION
    // ------------------------------------------------------------

    public static synchronized boolean register(
            Customer customer,
            String password
    ) {
        load();

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer information is required.");
        }

        validatePassword(password);

        if (isTaken(
                customer.getUsername(),
                customer.getEmail()
        )) {
            return false;
        }

        AccountRecord record =
                newRecord(customer, password);

        accounts.add(record);

        try {
            save();
        } catch (RuntimeException exception) {
            accounts.remove(record);
            throw exception;
        }

        return true;
    }

    // ------------------------------------------------------------
    // CUSTOMER LOGIN
    // ------------------------------------------------------------

    public static synchronized Person login(
            String username,
            String password
    ) {
        load();

        if (username == null
                || password == null
                || username.isBlank()
                || password.isEmpty()) {
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
            if (record.person.getUsername()
                    .equalsIgnoreCase(key)) {
                found = record;
                break;
            }
        }

        // Always perform password hashing,
        // even when the username does not exist.

        byte[] salt = found == null
                ? new byte[16]
                : Base64.getDecoder().decode(found.salt);

        byte[] actual = hashPassword(password, salt);

        boolean correct = found != null
                && MessageDigest.isEqual(
                Base64.getDecoder().decode(
                        found.passwordHash),
                actual);

        if (!correct) {

            long[] updated =
                    state == null ? new long[2] : state;

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

    // ------------------------------------------------------------
    // PASSWORD VALIDATION
    // ------------------------------------------------------------

    private static void validatePassword(String password) {

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException(
                    "Password must contain at least 6 characters.");
        }
    }

    // ------------------------------------------------------------
    // CHECK EXISTING CUSTOMER
    // ------------------------------------------------------------

    private static boolean isTaken(
            String username,
            String email
    ) {

        for (AccountRecord record : accounts) {

            if (record.person.getUsername()
                    .equalsIgnoreCase(username)
                    || record.person.getEmail()
                    .equalsIgnoreCase(email)) {

                return true;
            }
        }

        return false;
    }

    // ------------------------------------------------------------
    // CREATE CUSTOMER RECORD
    // ------------------------------------------------------------

    private static AccountRecord newRecord(
            Person person,
            String password
    ) {

        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);

        return new AccountRecord(
                person,
                Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(
                        hashPassword(password, salt))
        );
    }

    // ------------------------------------------------------------
    // PASSWORD HASHING
    // ------------------------------------------------------------

    private static byte[] hashPassword(
            String password,
            byte[] salt
    ) {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        try {

            return SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to process password.",
                    exception
            );

        } finally {
            spec.clearPassword();
        }
    }

    // ------------------------------------------------------------
    // LOAD SAVED CUSTOMER ACCOUNTS
    // ------------------------------------------------------------

    private static void load() {

        if (loaded) {
            return;
        }

        Properties data =
                SecureFiles.readProperties(file());

        ArrayList<AccountRecord> restored =
                new ArrayList<>();

        try {

            int count = Integer.parseInt(
                    data.getProperty("count", "0"));

            if (count < 0) {
                throw new IllegalArgumentException(
                        "Invalid account file.");
            }

            for (int i = 0; i < count; i++) {

                String key = "customer." + i + ".";

                String fullName =
                        data.getProperty(key + "fullName");

                String username =
                        data.getProperty(key + "username");

                String email =
                        data.getProperty(key + "email");

                String salt =
                        data.getProperty(key + "salt");

                String hash =
                        data.getProperty(key + "hash");

                String role = data.getProperty(
                        key + "role",
                        Customer.ROLE
                );

                // Only restore customer accounts.
                // Ignore previously saved admin accounts.

                if (!Customer.ROLE.equals(role)) {
                    continue;
                }

                if (fullName == null
                        || username == null
                        || email == null
                        || salt == null
                        || hash == null) {

                    throw new IllegalArgumentException(
                            "Incomplete customer record.");
                }

                if (Base64.getDecoder()
                        .decode(salt).length != 16
                        || Base64.getDecoder()
                        .decode(hash).length != 32) {

                    throw new IllegalArgumentException(
                            "Invalid password record.");
                }

                Person person = new Customer(
                        fullName,
                        username,
                        email
                );

                for (AccountRecord existing : restored) {

                    if (existing.person.getUsername()
                            .equalsIgnoreCase(username)
                            || existing.person.getEmail()
                            .equalsIgnoreCase(email)) {

                        throw new IllegalArgumentException(
                                "Duplicate saved account.");
                    }
                }

                restored.add(
                        new AccountRecord(person, salt, hash)
                );
            }

        } catch (RuntimeException exception) {

            throw new IllegalStateException(
                    "Unable to load saved customer accounts.",
                    exception
            );
        }

        accounts.clear();
        accounts.addAll(restored);

        loaded = true;
    }

    // ------------------------------------------------------------
    // SAVE CUSTOMER ACCOUNTS
    // ------------------------------------------------------------

    private static void save() {

        Properties data = new Properties();

        data.setProperty(
                "count",
                String.valueOf(accounts.size())
        );

        for (int i = 0; i < accounts.size(); i++) {

            AccountRecord record = accounts.get(i);

            String key = "customer." + i + ".";

            data.setProperty(
                    key + "fullName",
                    record.person.getFullName()
            );

            data.setProperty(
                    key + "username",
                    record.person.getUsername()
            );

            data.setProperty(
                    key + "email",
                    record.person.getEmail()
            );

            data.setProperty(
                    key + "role",
                    record.person.getRole()
            );

            data.setProperty(
                    key + "salt",
                    record.salt
            );

            data.setProperty(
                    key + "hash",
                    record.passwordHash
            );
        }

        SecureFiles.writeProperties(
                file(),
                data,
                "Blush and Bloom Customer Accounts"
        );
    }
}
