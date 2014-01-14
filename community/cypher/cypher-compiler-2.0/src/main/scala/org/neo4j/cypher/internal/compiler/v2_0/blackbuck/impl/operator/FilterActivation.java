package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.operator;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.ChunkType;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.RowPool;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Event;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Router;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;

public abstract
class FilterActivation<P extends Pos<P>, C extends Cursor<P, C>, E, O extends ChainingOperator<P, C, E>>
        extends ChainingActivation<P, C, E, O>
{
    C output;

    public FilterActivation( RowPool<P, C> pool, O operator )
    {
        super( operator );
        output = pool.newCursor();

    }

    @Override
    public void activate( Router<P, C, E> router, Event<?> event )
    {
        C cursor = (C) event.payload();

        while ( cursor.hasNext() )
        {
            cursor.next();
            if ( filter( cursor ) )
            {
                cursor.appendTo( output );
                if ( output.hasFullChunk( ChunkType.HEAD ) )
                {
                    boolean last = event.isLast() && cursor.isLast();
                    if ( ! last )
                    {
                        router.submit( event );
                    }
                    emit( router, output.chop( ChunkType.HEAD ), last );
                    return;
                }
            }
        }

        if ( event.isLast() )
        {
            emit( router, output, true );
        }

        cursor.close();
    }

    public abstract boolean filter( Pos<P> row );
}
