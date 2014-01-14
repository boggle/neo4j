package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.RowPool;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;

public interface Operator<P extends Pos<P>, C extends Cursor<P, C>, E>
{
    Object key();

    Activation<P, C, E> newActivation(RowPool<P, C> pool, Scheduler<P, C, E> scheduler);
}
