package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.operator;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Activation;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;

public abstract
class AbstractActivation<P extends Pos<P>, C extends Cursor<P, C>, E, O extends AbstractOperator<P, C, E>>
    implements Activation<P, C, E>
{
    private final O operator;

    public AbstractActivation( O operator )
    {
        this.operator = operator;
    }

    protected O operator()
    {
        return operator;
    }
}
