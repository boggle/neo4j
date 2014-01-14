package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;

public interface Router<P extends Pos<P>, C extends Cursor<P, C>, E>
{
    E environment();

    boolean isRegistered( Object key );
    
    void register( Object key, Activation<P, C, E> activation );
    void replace( Object key, Activation<P, C, E> oldActivation, Activation<P, C, E> newActivation );
    void unRegister( Object key, Activation<P, C, E> activation );

    void submit( Event<?> event );
}
