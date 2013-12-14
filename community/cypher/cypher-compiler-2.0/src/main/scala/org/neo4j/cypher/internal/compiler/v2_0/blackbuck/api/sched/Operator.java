package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.RowPool;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

public interface Operator<C extends Cursor<C>>
{
    Object key();
    void install( RowPool<C> pool, Scheduler<C> scheduler );
}
