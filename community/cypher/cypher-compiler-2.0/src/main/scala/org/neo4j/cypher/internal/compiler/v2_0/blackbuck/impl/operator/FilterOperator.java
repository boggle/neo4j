package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.operator;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.RowPool;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.*;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

public abstract class FilterOperator<C extends Cursor<C>> extends ChainingOperator<C>
{

    public FilterOperator(Object key, Operator<C> destination)
    {
        super( key, destination );
    }

    @Override
    public void install( final RowPool<C> pool, Scheduler<C> scheduler )
    {
        Activation<C> activation = new AbstractActivation<C>() {
            C output = pool.newCursor();

            @Override
            public void activate( Router<C> router, Event<C> event )
            {
                if ( operateOrShutdown(router, key()) )
                {
                    C cursor = event.cursor();
                    while ( cursor.hasNext() )
                    {
                        cursor.next();
                        if ( filter( cursor ) )
                        {
                            cursor.appendTo( output );
                            if ( output.hasChunk() )
                            {
                                emit( router, output.nextChunk() );
                            }
                        }
                    }

                    if ( cursor.isLastChunk() )
                    {
                        if ( output.hasNext() )
                        {
                            emit( router, output );
                        }
                        else
                        {
                            output.close();
                        }
                    }
                    cursor.close();
                }
            }

            @Override
            protected void shutdown( Router<C> router, Object key ) {
                output.close();
                super.shutdown( router, key );
            }
        };

        scheduler.register( key(), activation );
    }

    protected abstract boolean filter( C cursor );
}
