package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.RowPool;

public interface Pos<P extends Pos<P>> {
    boolean isStart();

    boolean isFirst();
    boolean isLast();

    boolean isRow();

    RowPool<P, ?> pool();
}
