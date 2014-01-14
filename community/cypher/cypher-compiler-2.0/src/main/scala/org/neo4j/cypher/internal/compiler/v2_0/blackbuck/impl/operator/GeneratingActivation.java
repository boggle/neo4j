package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.operator;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.ChunkType;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.RowPool;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Event;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Router;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;

public abstract
class GeneratingActivation<P extends Pos<P>, C extends Cursor<P, C>, E, S, O extends ChainingOperator<P, C, E>>
        extends ChainingActivation<P, C, E, O>
{
    private C output;
    private S cont;

    public GeneratingActivation( RowPool<P, C> pool, O operator )
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
            if ( hasCont( false) )
            {
                // no continuation? process next row
                cursor.next();
                cont = newState( cursor, output, router.environment() );
            }
            else
            {
                // cont? produce more output for current row
                cont = updateState( cont, cursor, output, router.environment() );
            }

            // produced at least a chunk of output?
            if ( output.hasFullChunk( ChunkType.HEAD ) )
            {
                break;
            }
        }

        boolean maybeLast = event.isLast();
        if ( hasCont( true ) || atEnd( cursor, false ) )
        {
            // if there is a continuation or more rows in this cursor,
            // continue processing of this event after processing output
            router.submit( event );
            maybeLast = false;
        }

        // produce output chunks
        while ( output.hasFullChunk( ChunkType.HEAD ) )
        {
            chopOutput( router, maybeLast && output.isEmpty() );
        }

        // drain output if this is the last event
        if ( maybeLast && !output.isEmpty() )
        {
            chopOutput( router, true );
        }
    }

    private boolean hasCont( boolean expected )
    {
        return expected == (null != cont);
    }

    private boolean atEnd( C cursor, boolean expected )
    {
        return expected == cursor.isLast();
    }

    protected void chopOutput(Router<P, C, E> router, boolean last)
    {
        emit( router, output.chop( ChunkType.HEAD ), last );
        if ( last )
        {
            output.close();
        }
    }

    protected abstract S newState( Pos<P> pos, C output, E environment );
    protected abstract S updateState( S state, Pos<P> pos, C output, E environment );
}
