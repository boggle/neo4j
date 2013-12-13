package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Activation;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Event;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Scheduler;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public abstract class DefaultScheduler<C extends Cursor<C>> implements Scheduler<C>
{
    private final Map<Object, Activation<C>> registry = new HashMap<Object, Activation<C>>();
    private final Deque<Event<C>> events;

    public DefaultScheduler()
    {
        events = new ArrayDeque<>();
    }

    public DefaultScheduler( int eventQueueSizeHint )
    {
        events = new ArrayDeque<>( eventQueueSizeHint );
    }

    @Override
    public void register( Object key, Activation<C> activation )
    {
        if ( registry.containsKey( key ) )
        {
            throw new IllegalStateException("There already is an activation for: " + key );
        }

        registry.put( key, activation );
    }

    @Override
    public void replace( Object key, Activation<C> oldActivation, Activation<C> newActivation )
    {
        Activation<C> currentActivation = registry.get( key );
        if ( currentActivation != oldActivation )
        {
            throw new IllegalStateException( "Cannot replace different activation for: " + key );
        }
        registry.put( key, newActivation );
    }

    @Override
    public void unRegister( Object key ) {
        if ( ! registry.containsKey( key ) )
        {
            throw new IllegalStateException( "There is no activation for: " + key );
        }

        registry.remove( key );
    }

    @Override
    public void emit( Event<C> event )
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
    public Event<C> execute() {
        for ( ;; )
        {
            Event<C> event = events.poll();
            if ( null == event || event.isOutput() )
            {
                return event;
            }

            Activation<C> activation = registry.get( event.destination() );
            if ( null == activation )
            {
                throw new IllegalStateException( "No activation found for event: " + event.toString() );
            }

            activation.activate( this, event );
        }
    }
}
