package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.operator;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Operator;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

public abstract class AbstractOperator<C extends Cursor<C>> implements Operator<C>
{
    private final Object key;

    public AbstractOperator( Object key ) {
        this.key = key;
    }

    @Override
    public Object key() {
        return key;
    }
}
