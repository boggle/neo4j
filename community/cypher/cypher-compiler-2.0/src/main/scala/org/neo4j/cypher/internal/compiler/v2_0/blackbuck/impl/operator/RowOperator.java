package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.operator;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.RowPool;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.*;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

public abstract class RowOperator<C extends Cursor<C>> extends ChainingOperator<C>
{
    protected RowOperator( Object key, Operator<C> destination ) {
        super( key, destination );
    }

    @Override
    public void install( RowPool<C> pool, Scheduler<C> scheduler )
    {
        Activation<C> activation = new Activation<C>()
        {
            @Override
            public void activate( Router<C> router, Event<C> event )
            {
                if ( operateOrClose( router ) )
                {
                    C cursor = event.cursor();
                    while ( cursor.hasNext() )
                    {
                        cursor.next();
                        process( cursor );
                    }
                    cursor.rewind();
                    router.submit( event );
                }
            }
        };

        scheduler.register( key(), activation );
    }

    protected abstract void process( C cursor );
}
