/*
 * Copyright (c) 2009, 2026 Vassilios Karakoidas, Dimitrios Mitropoulos.
 * Licensed under the BSD 3-Clause License; see LICENSE.
 */
package org.sdriver.xpath.registry;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A registry kept in a text file, one identifier per line, so that a training run survives
 * into production. The file is read once when the registry is created; a missing file is an
 * empty registry. Each new identifier is appended to it.
 *
 * @author Vassilios Karakoidas (bkarak@aueb.gr)
 */
public class FlatFileRegistry implements Registry {
    public static final String DEFAULT_FILE = "ids.registry";

    private final Path file;
    private final Set<String> ids = ConcurrentHashMap.newKeySet();

    public FlatFileRegistry() {
        this(Path.of(DEFAULT_FILE));
    }

    public FlatFileRegistry(Path file) {
        this.file = file;
        if (Files.exists(file)) {
            try {
                for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                    String id = line.trim();
                    if (!id.isEmpty()) {
                        ids.add(id);
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Could not read registry " + file, e);
            }
        }
    }

    public Path getFile() {
        return file;
    }

    @Override
    public void addID(String id) {
        if (ids.add(id)) {
            synchronized (this) {
                try {
                    Files.writeString(file, id + System.lineSeparator(), StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    throw new UncheckedIOException("Could not write registry " + file, e);
                }
            }
        }
    }

    @Override
    public boolean exists(String id) {
        return ids.contains(id);
    }

    @Override
    public String[] getIDs() {
        return ids.stream().sorted().toArray(String[]::new);
    }
}
