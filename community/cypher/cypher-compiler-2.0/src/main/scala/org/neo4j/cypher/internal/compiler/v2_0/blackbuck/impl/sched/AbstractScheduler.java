package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.RowPool;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Activation;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Operator;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Scheduler;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractScheduler<P extends Pos<P>, C extends Cursor<P, C>, E> implements Scheduler<P, C, E>
{
    private final Map<Object, Activation<P, C, E>> registry = new HashMap<Object, Activation<P, C, E>>();

    @Override
    public void install( Operator<P, C, E> operator )
    {
        register( operator.key(), operator.newActivation( pool(), this ) );
    }

    public void register( Object key, Activation<P, C, E> activation )
    {
        if ( registry.containsKey( key ) )
        {
            throw new IllegalStateException("There already is an activation for: " + key );
        }

        registry.put( key, activation );
    }

    public void replace( Object key, Activation<P, C, E> oldActivation, Activation<P, C, E> newActivation )
    {
        Activation<P, C, E> currentActivation = registry.get( key );
        if ( currentActivation != oldActivation )
        {
            throw new IllegalStateException( "Cannot replace different activation for: " + key );
        }
        registry.put( key, newActivation );
    }

    public void unRegister( Object key, Activation<P, C, E> activation ) {
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

    protected Activation<P, C, E> lookup( Object key )
    {
        return registry.get( key );
    }

    protected abstract RowPool<P, C> pool();
}
