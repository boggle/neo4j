package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

public abstract class AbstractActivation<C extends Cursor<C>> implements Activation<C>
{
    public boolean readyToOperate( Router<C> router, Object key )
    {
        return router.isRegistered( key );
    }

    protected boolean operateOrShutdown( Router<C> router, Object key )
    {
        if ( readyToOperate( router, key ) )
        {
            return true;
        }

        if ( router.isRegistered( key ) )
        {
            shutdown( router, key );
        }

        return false;
    }

    protected void shutdown( Router<C> router, Object key )
    {
        router.unRegister( key );
    }
}
