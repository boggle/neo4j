package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.SlotFactory;

public interface RowPoolBuilder<C extends Pos<C> & Cursor<C>> extends SlotFactory<C>
{
    RowPool<C> builder();
}
