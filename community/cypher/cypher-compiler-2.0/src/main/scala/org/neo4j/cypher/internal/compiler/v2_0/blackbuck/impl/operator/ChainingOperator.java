package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.operator;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Event;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Operator;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Router;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

public abstract class ChainingOperator<C extends Cursor<C>> extends AbstractOperator<C>
{
    private final Operator<C> destination;

    public ChainingOperator( Object key, Operator<C> destination )
    {
        super( key );
        this.destination = destination;
    }

    public Operator<C> destination()
    {
        return destination;
    }

    protected void emit( Router<C> router, C cursor )
    {
        router.submit(Event.message( key(), destination.key(), cursor ) );
    }

    @Override
    public boolean readyToOperate(Router<C> router) {
        return super.readyToOperate(router) && router.isRegistered( destination );
    }
}
