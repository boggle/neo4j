package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;

public interface Activation<P extends Pos<P>, C extends Cursor<P, C>, E>
{
    void activate( Router<P, C, E> router, Event<?> event );
}
