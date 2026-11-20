package com.example.bandb.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Properties;

/**
 * Package-private helper (not visible outside the service package).
 * Every data file lives in the user's own BlushAndBloom folder and is
 * restricted to the current OS user, so other accounts on the same
 * computer cannot read the customer records.
 */
final class SecureFiles {

    private SecureFiles() {
    }

    static Path dataDirectory() {
        String custom = System.getProperty("bandb.data");

        if (custom != null && !custom.isBlank()) {
            return Paths.get(custom);
        }

        return Paths.get(System.getProperty("user.home"), "BlushAndBloom_Merged");
    }

    static void ensurePrivateDirectory(Path directory) throws IOException {
        Files.createDirectories(directory);
        restrict(directory, true);
    }

    private static void restrict(Path path, boolean directory) {
        try {
            Files.setPosixFilePermissions(
                    path,
                    PosixFilePermissions.fromString(
                            directory ? "rwx------" : "rw-------"));
        } catch (UnsupportedOperationException | IOException exception) {
            // Windows: fall back to the basic owner-only flags.
            java.io.File file = path.toFile();
            file.setReadable(false, false);
            file.setWritable(false, false);
            file.setReadable(true, true);
            file.setWritable(true, true);
            if (directory) {
                file.setExecutable(false, false);
                file.setExecutable(true, true);
            }
        }
    }

    static Properties readProperties(Path file) {
        Properties data = new Properties();

        if (Files.notExists(file)) {
            return data;
        }

        try (InputStream input = Files.newInputStream(file)) {
            data.load(input);
            return data;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to read saved data.", exception);
        }
    }

    static void writeProperties(
            Path file, Properties data, String description
    ) {
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");

        try {
            ensurePrivateDirectory(file.getParent());

            try (OutputStream output = Files.newOutputStream(temporary)) {
                data.store(output, description);
            }

            restrict(temporary, false);

            try {
                Files.move(temporary, file,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, file,
                        StandardCopyOption.REPLACE_EXISTING);
            }

            restrict(file, false);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to save data.", exception);
        }
    }
}
