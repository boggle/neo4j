package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;

public interface Scheduler<P extends Pos<P>, C extends Cursor<P, C>, E> extends Router<P, C, E>
{
    int numActivations();

    void install( Operator<P, C, E> operator );

    Event<C> execute();
}
