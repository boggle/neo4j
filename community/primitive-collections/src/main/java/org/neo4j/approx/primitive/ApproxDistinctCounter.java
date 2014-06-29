package org.neo4j.approx.primitive;

import org.neo4j.collection.primitive.PrimitiveIntVisitor;
import org.neo4j.collection.primitive.PrimitiveLongVisitor;

public interface ApproxDistinctCounter
{
    void reset();

    PrimitiveIntVisitor addIntVisitor();
    PrimitiveLongVisitor addLongVisitor();

    void addAll(Iterable<?> iterable);
    void add(Object obj);

    long size();
    double estimate();
}
