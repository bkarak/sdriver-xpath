package org.sdriver.xpath.registry;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

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
}
