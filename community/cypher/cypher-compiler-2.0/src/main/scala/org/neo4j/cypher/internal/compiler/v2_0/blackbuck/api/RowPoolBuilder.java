package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api;

public interface RowPoolBuilder extends SlotFactory {
    RowPool builder();
}
