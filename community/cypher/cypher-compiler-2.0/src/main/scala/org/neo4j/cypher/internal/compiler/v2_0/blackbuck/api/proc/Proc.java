package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.proc;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.SlotReader;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.SlotWriter;

import java.util.Set;

public interface Proc<C extends Cursor<C>>
{
    String name();

    Set<SlotReader<C>> reads();
    Set<SlotWriter<C>> writes();

    void run( C cursor );
}
