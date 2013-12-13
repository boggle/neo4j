package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

public interface Scheduler<C extends Cursor<C>> extends Router<C>
{
    Event<C> execute();
}
