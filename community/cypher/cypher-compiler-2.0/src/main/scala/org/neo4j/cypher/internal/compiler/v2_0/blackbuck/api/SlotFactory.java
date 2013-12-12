package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api;

public interface SlotFactory {
    Slot newSlot( SlotType type );
    Slot newAnySlot( Object initialValue );
}
