package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

public interface Operator<C extends Cursor<C>>
{
    Activation<C> newActivation( Object key, Router<C> router );
}
