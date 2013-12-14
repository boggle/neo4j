package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

public interface Router<C extends Cursor<C>>
{    
    boolean isRegistered( Object key );
    
    void register( Object key, Activation<C> activation );
    void replace( Object key, Activation<C> oldActivation, Activation<C> newActivation );
    void unRegister( Object key );

    void submit( Event<C> event );
}
