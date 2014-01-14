package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.operator;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Event;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Router;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;

public abstract
class ChainingActivation<P extends Pos<P>, C extends Cursor<P, C>, E, O extends ChainingOperator<P, C, E>>
        extends AbstractActivation<P, C, E, O>
{
    public ChainingActivation( O operator )
    {
        super( operator );
    }

    protected void emit( Router<P, C, E> router, C cursor, boolean last )
    {
        router.submit(Event.message( operator().key(), operator().dest().key(), cursor, last ) );
    }
}
