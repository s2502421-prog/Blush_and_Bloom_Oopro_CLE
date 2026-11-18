
package com.example.bandb;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Locale;
import java.util.Properties;

public class CustomerData {

    private static final ArrayList<AccountRecord> customers =
            new ArrayList<>();

    private static final Path FILE = Paths.get(
            System.getProperty("user.home"),
            "BlushAndBloom",
            "customers.properties"
    );

    private static final SecureRandom RANDOM =
            new SecureRandom();

    private static final int ITERATIONS = 210000;
    private static final int KEY_LENGTH = 256;

    private static boolean loaded = false;

    private static class AccountRecord {

        private final Customer customer;
        private final String salt;
        private final String passwordHash;

        private AccountRecord(
                Customer customer,
                String salt,
                String passwordHash
        ) {
            this.customer = customer;
            this.salt = salt;
            this.passwordHash = passwordHash;
        }
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public static synchronized boolean register(
            Customer customer
    ) {
        load();

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer information is required."
            );
        }

        String fullName = customer.getFullName();
        String username = customer.getUsername();
        String email = customer.getEmail();
        String password = customer.getPassword();

        if (fullName == null || fullName.isBlank()
                || username == null || username.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isEmpty()) {

            throw new IllegalArgumentException(
                    "Please fill in all fields."
            );
        }

        fullName = fullName.trim();
        username = username.trim();
        email = email.trim();

        if (!username.matches("[A-Za-z0-9_]{3,30}")) {
            throw new IllegalArgumentException(
                    "Username must contain 3 to 30 letters, " +
                            "numbers, or underscores."
            );
        }

        if (!email.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )) {
            throw new IllegalArgumentException(
                    "Please enter a valid email address."
            );
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException(
                    "Password must contain at least 6 characters."
            );
        }

        for (AccountRecord record : customers) {

            if (record.customer.getUsername()
                    .equalsIgnoreCase(username)) {
                return false;
            }

            if (record.customer.getEmail()
                    .equalsIgnoreCase(email)) {
                return false;
            }
        }

        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);

        String encodedSalt =
                Base64.getEncoder().encodeToString(salt);

        String encodedHash =
                Base64.getEncoder().encodeToString(
                        hashPassword(password, salt)
                );

        Customer safeCustomer = new Customer(
                fullName,
                username,
                email,
                ""
        );

        AccountRecord record = new AccountRecord(
                safeCustomer,
                encodedSalt,
                encodedHash
        );

        customers.add(record);

        try {
            save();
        } catch (RuntimeException exception) {
            customers.remove(record);
            throw exception;
        }

        return true;
    }

    public static synchronized Customer login(
            String username,
            String password
    ) {
        load();

        if (username == null || password == null
                || username.isBlank() || password.isEmpty()) {
            return null;
        }

        for (AccountRecord record : customers) {

            if (record.customer.getUsername()
                    .equalsIgnoreCase(username.trim())) {

                byte[] salt = Base64.getDecoder().decode(
                        record.salt
                );

                byte[] expected =
                        Base64.getDecoder().decode(
                                record.passwordHash
                        );

                byte[] actual = hashPassword(
                        password,
                        salt
                );

                if (MessageDigest.isEqual(expected, actual)) {
                    return record.customer;
                }

                return null;
            }
        }

        return null;
    }

    public static synchronized boolean usernameExists(
            String username
    ) {
        load();

        if (username == null || username.isBlank()) {
            return false;
        }

        for (AccountRecord record : customers) {
            if (record.customer.getUsername()
                    .equalsIgnoreCase(username.trim())) {
                return true;
            }
        }

        return false;
    }

    public static synchronized boolean emailExists(
            String email
    ) {
        load();

        if (email == null || email.isBlank()) {
            return false;
        }

        for (AccountRecord record : customers) {
            if (record.customer.getEmail()
                    .equalsIgnoreCase(email.trim())) {
                return true;
            }
        }

        return false;
    }

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
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            return factory.generateSecret(spec)
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

    private static synchronized void load() {

        if (loaded) {
            return;
        }

        if (!Files.exists(FILE)) {
            loaded = true;
            return;
        }

        Properties data = new Properties();
        ArrayList<AccountRecord> restored =
                new ArrayList<>();

        try (InputStream input =
                     Files.newInputStream(FILE)) {

            data.load(input);

            int count = Integer.parseInt(
                    data.getProperty("count", "-1")
            );

            if (count < 0) {
                throw new IllegalArgumentException(
                        "Invalid account file."
                );
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

                if (fullName == null
                        || username == null
                        || email == null
                        || salt == null
                        || hash == null) {
                    throw new IllegalArgumentException(
                            "Incomplete customer record."
                    );
                }

                if (fullName.isBlank()
                        || username.isBlank()
                        || email.isBlank()) {
                    throw new IllegalArgumentException(
                            "Invalid customer record."
                    );
                }

                byte[] decodedSalt =
                        Base64.getDecoder().decode(salt);

                byte[] decodedHash =
                        Base64.getDecoder().decode(hash);

                if (decodedSalt.length != 16
                        || decodedHash.length != 32) {
                    throw new IllegalArgumentException(
                            "Invalid password record."
                    );
                }

                for (AccountRecord existing : restored) {

                    if (normalize(
                            existing.customer.getUsername()
                    ).equals(normalize(username))
                            || normalize(
                            existing.customer.getEmail()
                    ).equals(normalize(email))) {

                        throw new IllegalArgumentException(
                                "Duplicate saved account."
                        );
                    }
                }

                Customer customer = new Customer(
                        fullName,
                        username,
                        email,
                        ""
                );

                restored.add(
                        new AccountRecord(
                                customer,
                                salt,
                                hash
                        )
                );
            }

            customers.clear();
            customers.addAll(restored);
            loaded = true;

        } catch (IOException | RuntimeException exception) {

            throw new IllegalStateException(
                    "Unable to load saved customer accounts.",
                    exception
            );
        }
    }

    private static synchronized void save() {

        Properties data = new Properties();

        data.setProperty(
                "count",
                String.valueOf(customers.size())
        );

        for (int i = 0; i < customers.size(); i++) {

            AccountRecord record = customers.get(i);

            String key = "customer." + i + ".";

            data.setProperty(
                    key + "fullName",
                    record.customer.getFullName()
            );

            data.setProperty(
                    key + "username",
                    record.customer.getUsername()
            );

            data.setProperty(
                    key + "email",
                    record.customer.getEmail()
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

        Path temporary = FILE.resolveSibling(
                "customers.properties.tmp"
        );

        try {
            Files.createDirectories(FILE.getParent());

            try (OutputStream output =
                         Files.newOutputStream(temporary)) {

                data.store(
                        output,
                        "Blush and Bloom Customer Accounts"
                );
            }

            try {
                Files.move(
                        temporary,
                        FILE,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(
                        temporary,
                        FILE,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to save customer account.",
                    exception
            );
        }
    }
}
