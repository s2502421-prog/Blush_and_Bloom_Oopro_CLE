
package com.example.bandb.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Base64;

public class AccountStore {

    private static final int ITERATIONS = 210000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM =
            new SecureRandom();

    private static final Path FILE = Paths.get(
            System.getProperty("user.home"),
            "BlushAndBloom",
            "accounts.properties"
    );

    private static final Map<String, Account> accounts =
            new HashMap<>();

    private static boolean loaded = false;

    private static class Account {
        String username;
        String salt;
        String passwordHash;

        Account(String username, String salt,
                String passwordHash) {
            this.username = username;
            this.salt = salt;
            this.passwordHash = passwordHash;
        }
    }

    private static String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    public static synchronized boolean register(
            String username,
            String password
    ) {
        load();

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter a username."
            );
        }

        username = username.trim();

        if (!username.matches("[A-Za-z0-9_]{3,30}")) {
            throw new IllegalArgumentException(
                    "Username must contain 3 to 30 letters, " +
                            "numbers, or underscores."
            );
        }

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException(
                    "Password must have at least 6 characters."
            );
        }

        String key = normalize(username);

        if (accounts.containsKey(key)) {
            throw new IllegalArgumentException(
                    "This username is already registered."
            );
        }

        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);

        String encodedSalt =
                Base64.getEncoder().encodeToString(salt);

        String hashedPassword = hashPassword(
                password, salt
        );

        accounts.put(
                key,
                new Account(
                        username,
                        encodedSalt,
                        hashedPassword
                )
        );

        try {
            save();
        } catch (RuntimeException exception) {
            accounts.remove(key);
            throw exception;
        }

        return true;
    }

    public static synchronized boolean login(
            String username,
            String password
    ) {
        load();

        if (username == null || password == null) {
            return false;
        }

        Account account = accounts.get(
                normalize(username)
        );

        if (account == null) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(
                account.salt
        );

        byte[] expected = Base64.getDecoder().decode(
                account.passwordHash
        );

        byte[] actual = hashBytes(
                password, salt
        );

        return MessageDigest.isEqual(
                expected, actual
        );
    }

    public static synchronized boolean usernameExists(
            String username
    ) {
        load();

        if (username == null) {
            return false;
        }

        return accounts.containsKey(
                normalize(username)
        );
    }

    public static synchronized String getUsername(
            String username
    ) {
        load();

        if (username == null) {
            return null;
        }

        Account account = accounts.get(
                normalize(username)
        );

        return account == null
                ? null
                : account.username;
    }

    private static String hashPassword(
            String password,
            byte[] salt
    ) {
        return Base64.getEncoder().encodeToString(
                hashBytes(password, salt)
        );
    }

    private static byte[] hashBytes(
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

            return factory.generateSecret(
                    spec
            ).getEncoded();

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
        Map<String, Account> restored =
                new HashMap<>();

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
                String key = "account." + i + ".";

                String username = data.getProperty(
                        key + "username"
                );

                String salt = data.getProperty(
                        key + "salt"
                );

                String hash = data.getProperty(
                        key + "hash"
                );

                if (username == null ||
                        salt == null || hash == null) {
                    throw new IllegalArgumentException(
                            "Incomplete account record."
                    );
                }

                String normalized = normalize(username);

                if (restored.containsKey(normalized)) {
                    throw new IllegalArgumentException(
                            "Duplicate account record."
                    );
                }

                restored.put(
                        normalized,
                        new Account(username, salt, hash)
                );
            }

            accounts.clear();
            accounts.putAll(restored);
            loaded = true;

        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException(
                    "Unable to load saved accounts.",
                    exception
            );
        }
    }

    private static synchronized void save() {

        Properties data = new Properties();

        data.setProperty(
                "count",
                String.valueOf(accounts.size())
        );

        int index = 0;

        for (Account account : accounts.values()) {
            String key = "account." + index + ".";

            data.setProperty(
                    key + "username", account.username
            );

            data.setProperty(
                    key + "salt", account.salt
            );

            data.setProperty(
                    key + "hash", account.passwordHash
            );

            index++;
        }

        Path temporary = FILE.resolveSibling(
                "accounts.properties.tmp"
        );

        try {
            Files.createDirectories(FILE.getParent());

            try (OutputStream output =
                         Files.newOutputStream(temporary)) {

                data.store(
                        output,
                        "Blush and Bloom Accounts"
                );
            }

            try {
                Files.move(
                        temporary,
                        FILE,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException e) {
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
