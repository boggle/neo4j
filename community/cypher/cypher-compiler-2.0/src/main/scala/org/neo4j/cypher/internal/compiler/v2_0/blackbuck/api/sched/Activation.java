package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

public interface Activation<C extends Cursor<C>>
{
    void activate( Router<C> router, Event<C> event );
}
