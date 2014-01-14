package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.proc;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.SlotReader;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.SlotWriter;

import java.util.Set;

public interface Proc<P extends Pos<P>>
{
    String name();

    Set<SlotReader<P>> reads();
    Set<SlotWriter<P>> writes();

    void run( P pos );
}
