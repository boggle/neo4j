package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Activation;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Event;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

import java.util.ArrayDeque;
import java.util.Deque;

public class DefaultScheduler<C extends Cursor<C>> extends AbstractScheduler<C>
{
    private final Deque<Event<C>> events;
    private int numActivations = 0;

    public DefaultScheduler()
    {
        events = new ArrayDeque<>();
    }

    public DefaultScheduler( int eventQueueSizeHint )
    {
        events = new ArrayDeque<>( eventQueueSizeHint );
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

            Activation<C> activation = lookup(event.destination());
            if ( null == activation )
            {

                throw new IllegalStateException( "No activation found for event: " + event.toString() );
            }

            activation.activate( this, event );
        }
    }
}
