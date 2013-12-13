package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

public interface Router<C extends Cursor<C>>
{
    void emit( Object destination, C cursor );

    void subscribe( Object source );
    // void unSubscribe( Object source );
}
