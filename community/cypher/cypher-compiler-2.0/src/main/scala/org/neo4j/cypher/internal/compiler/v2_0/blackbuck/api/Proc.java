package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api;

import java.util.Set;

public interface Proc
{
    String name();

    Set<SlotReader> reads();
    Set<SlotWriter> writes();

    void run( int row );
}
