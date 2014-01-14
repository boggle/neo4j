package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.operator;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.RowPool;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Event;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Router;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;

public abstract
class UnionActivation<P extends Pos<P>, C extends Cursor<P, C>, E, O extends FusingOperator<P, C, E>>
        extends ChainingActivation<P, C, E, O>
{
    C output;

    public UnionActivation( RowPool<P, C> pool, O operator  )
    {
        super( operator );
        this.output = pool.newCursor();
    }

    @Override
    public void activate( Router<P, C, E> router, Event<?> event )
    {
        C cursor = (C) event.payload();

        if ( event.isFrom( operator().lhs().key() ) )
        {
            emit( router, cursor, event.isLast() );
            return;
        }

        if ( event.isFrom( operator().rhs().key() ) )
        {
            // TODO
        }
    }
}
