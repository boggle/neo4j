package org.neo4j.kernel.impl.skip;

import java.util.Random;

/**
 * Generates levels for newly inserted skip list records
 *
 * It is expected that the generated levels follow a poisson distribution as needed by skip lists
 */
public interface LevelGenerator
{
    int getMaxHeight();

    int newLevel();

}
