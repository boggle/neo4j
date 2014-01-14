package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.operator;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Event;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Router;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;

public abstract
class RowActivation<P extends Pos<P>, C extends Cursor<P, C>, E, O extends ChainingOperator<P, C, E>>
        extends ChainingActivation<P, C, E, O>
{
    public RowActivation( O operator )
    {
        super( operator );
    }

    @Override
    public void activate( Router<P, C, E> router, Event<?> event )
    {
        C cursor = (C) event.payload();
        while ( cursor.hasNext() )
        {
            cursor.next();
            process( cursor );
        }
        emit( router, cursor, event.isLast() );
    }

    public abstract void process( Pos<P> row );
}
