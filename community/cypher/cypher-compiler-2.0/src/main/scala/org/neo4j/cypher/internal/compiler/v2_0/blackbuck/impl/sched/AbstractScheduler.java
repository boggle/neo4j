package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Activation;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Scheduler;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractScheduler<C extends Cursor<C>> implements Scheduler<C>
{
    private final Map<Object, Activation<C>> registry = new HashMap<Object, Activation<C>>();

    public void register( Object key, Activation<C> activation )
    {
        if ( registry.containsKey( key ) )
        {
            throw new IllegalStateException("There already is an activation for: " + key );
        }

        registry.put( key, activation );
    }

    public void replace( Object key, Activation<C> oldActivation, Activation<C> newActivation )
    {
        Activation<C> currentActivation = registry.get( key );
        if ( currentActivation != oldActivation )
        {
            throw new IllegalStateException( "Cannot replace different activation for: " + key );
        }
        registry.put( key, newActivation );
    }

    public void unRegister( Object key ) {
        if ( ! registry.containsKey( key ) )
        {
            throw new IllegalStateException( "There is no activation for: " + key );
        }

        registry.remove( key );
    }

    @Override
    public boolean isRegistered( Object key ) {
        return null != lookup( key );
    }

    protected Activation<C> lookup( Object key )
    {
        return registry.get( key );
    }
}
