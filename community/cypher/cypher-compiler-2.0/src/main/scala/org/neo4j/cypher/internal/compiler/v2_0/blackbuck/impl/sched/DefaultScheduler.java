package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.RowPool;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Activation;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Event;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;

import java.util.ArrayDeque;
import java.util.Deque;

public class DefaultScheduler<P extends Pos<P>, C extends Cursor<P, C>, E> extends AbstractScheduler<P, C, E>
{
    private final E environment;
    private final Deque<Event<C>> events;
    private final RowPool<P, C> pool;
    private int numActivations = 0;

    public DefaultScheduler( E environment, RowPool<P, C> pool )
    {
        this( environment, pool, new ArrayDeque<Event<C>>() );
    }

    public DefaultScheduler( E environment, RowPool<P, C> pool, int eventQueueSizeHint )
    {
        this( environment, pool, new ArrayDeque<Event<C>>( eventQueueSizeHint ) );
    }

    private DefaultScheduler( E environment, RowPool<P, C> pool, Deque<Event<C>> events )
    {
        this.environment = environment;
        this.events = events;
        this.pool = pool;
    }

    @Override
    public void submit( Event<C> event )
    {
        if ( event.isInput() )
        {
            events.addLast( event );
        }
        else
        {
            events.addFirst( event );
        }
    }

    @Override
    protected RowPool<P, C> pool() {
        return pool;
    }

    @Override
    public E environment() {
        return environment;
    }

    public int numActivations()
    {
        return numActivations;
    }

    @Override
    public Event<C> execute() {
        for ( ;;numActivations++ )
        {
            Event<C> event = events.poll();
            if ( null == event || event.isOutput() )
            {
                return event;
            }

            Activation<P, C, E> activation = lookup( event.destination() );
            if ( null == activation )
            {

                throw new IllegalStateException( "No activation found for event: " + event.toString() );
            }

            activation.activate( this, event );
        }
    }
}
