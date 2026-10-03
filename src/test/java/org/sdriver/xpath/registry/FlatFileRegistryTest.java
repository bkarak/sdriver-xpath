package org.sdriver.xpath.registry;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FlatFileRegistryTest {
    @TempDir
    Path dir;

    @Test
    void missingFileIsAnEmptyRegistry() {
        FlatFileRegistry r = new FlatFileRegistry(dir.resolve("ids.registry"));
        assertEquals(0, r.getIDs().length);
        assertFalse(Files.exists(r.getFile()));
    }

    @Test
    void identifiersSurviveARestart() throws Exception {
        Path file = dir.resolve("ids.registry");
        FlatFileRegistry first = new FlatFileRegistry(file);
        first.addID("b");
        first.addID("a");
        first.addID("a");
        assertEquals(2, Files.readAllLines(file).size());

        FlatFileRegistry second = new FlatFileRegistry(file);
        assertTrue(second.exists("a"));
        assertTrue(second.exists("b"));
        assertFalse(second.exists("c"));
        assertArrayEquals(new String[] {"a", "b"}, second.getIDs());
    }

    @Test
    void anIdentifierThatCannotBeWrittenIsNotRecorded() throws Exception {
        Path file = dir.resolve("ids.registry");
        Files.writeString(file, "a\n");
        Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("r--r--r--"));
        FlatFileRegistry broken = new FlatFileRegistry(file);
        assertTrue(broken.exists("a"));

        assertThrows(UncheckedIOException.class, () -> broken.addID("b"));
        // 2.0.0 had it in memory by now, so it passed this run and a retry never wrote it.
        assertFalse(broken.exists("b"));
        assertThrows(UncheckedIOException.class, () -> broken.addID("b"));
    }
}
