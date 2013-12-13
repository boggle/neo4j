package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

public interface Router<C extends Cursor<C>>
{
    void register( Object source, Activation<C> activation );
    void replace( Object source, Activation<C> oldActivation, Activation<C> newActivation );
    void unRegister( Object source );

    void emit( Event<C> event );
}
