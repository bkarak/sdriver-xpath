/*
 * Copyright (c) 2009, 2026 Vassilios Karakoidas, Dimitrios Mitropoulos.
 * Licensed under the BSD 3-Clause License; see LICENSE.
 */
package org.sdriver.xpath.registry;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A registry held in memory, lost when the application stops.
 *
 * @author Vassilios Karakoidas (bkarak@aueb.gr)
 */
public class MemoryRegistry implements Registry {
    private final Set<String> ids = ConcurrentHashMap.newKeySet();

    @Override
    public void addID(String id) {
        ids.add(id);
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
