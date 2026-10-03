/*
 * Copyright (c) 2009, 2026 Vassilios Karakoidas, Dimitrios Mitropoulos.
 * Licensed under the BSD 3-Clause License; see LICENSE.
 */
package org.sdriver.xpath;

/**
 * The feature names {@link SecureXPathFactory#setFeature} understands.
 *
 * <ul>
 * <li>{@code TrainingMode} — record identifiers instead of checking them.</li>
 * <li>{@code MemoryRegistry} — keep identifiers in memory (the default).</li>
 * <li>{@code FlatFileRegistry} — keep identifiers in a text file, one per line; the file is
 * {@code ids.registry} in the working directory unless the {@code sdriver.xpath.registry}
 * system property names another.</li>
 * </ul>
 *
 * @author Vassilios Karakoidas (bkarak@aueb.gr)
 */
public enum SecureXPathFeature {
    TRAINING_MODE("TrainingMode"),
    MEMORY_REGISTRY("MemoryRegistry"),
    FLATFILE_REGISTRY("FlatFileRegistry"),
    UNKNOWN_FEATURE("Unknown");

    private final String name;

    SecureXPathFeature(String s) {
        this.name = s;
    }

    @Override
    public String toString() {
        return name;
    }

    public static SecureXPathFeature getFeature(String s) {
        for (SecureXPathFeature sxpf : values()) {
            if (sxpf != UNKNOWN_FEATURE && sxpf.name.equals(s)) {
                return sxpf;
            }
        }
        return UNKNOWN_FEATURE;
    }
}
