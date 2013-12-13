package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

public interface Scheduler<C extends Cursor<C>>
{
    void emit( Object key, C cursor );

    void register( Object key, Operator<C> operator );
    // Object unRegister( Object key );

    C execute();
}
