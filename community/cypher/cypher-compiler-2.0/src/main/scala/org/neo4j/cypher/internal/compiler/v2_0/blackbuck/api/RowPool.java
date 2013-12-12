package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api;

public interface RowPool {
    int create();

    void acquire(int row);
    void release(int row);
}
