package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.operator;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Operator;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

public abstract class FusingOperator<C extends Cursor<C>> extends ChainingOperator<C> {
    private final Operator<C> lhs;
    private final Operator<C> rhs;

    public FusingOperator( Object key, Operator<C> destination, Operator<C> lhs, Operator<C> rhs )
    {
        super( key, destination );
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Operator<C> lhs() {
        return lhs;
    }

    public Operator<C> rhs() {
        return rhs;
    }
}
