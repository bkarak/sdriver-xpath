/*
 * Copyright (c) 2009, 2026 Vassilios Karakoidas, Dimitrios Mitropoulos.
 * Licensed under the BSD 3-Clause License; see LICENSE.
 */
package org.sdriver.xpath.registry;

/**
 * The set of query identifiers an application accepts. Implementations must be safe to use
 * from several threads, since every XPath a factory creates shares its registry.
 *
 * @author Vassilios Karakoidas (bkarak@aueb.gr)
 */
public interface Registry {
    void addID(String id);

    boolean exists(String id);

    String[] getIDs();
}
