package org.neo4j.kernel.impl.skip.base;

import java.util.Random;

import org.neo4j.kernel.impl.skip.LevelGenerator;

/**
 * {@link org.neo4j.kernel.impl.skip.LevelGenerator} based on {@link java.util.Random}
 *
 * Unnecessary calls to the random generator are avoided by using all random bits returned from calls to
 * {@link java.util.Random#nextInt()} and {@link java.util.Random#nextLong()}
 */
public class RandomLevelGenerator implements LevelGenerator
{
    private final Random rand;

    private final int h_max;
    private final int p_bits;
    private final int rand_bits;

    public RandomLevelGenerator( Random rand, int h_max, int p_bits ) {
        this.rand = rand;

        assert h_max > 0;
        assert p_bits > 0;

        this.h_max = h_max;
        this.p_bits = p_bits;

        this.rand_bits = ( h_max - 1 ) * p_bits;

        assert rand_bits <= 64;
    }

    public RandomLevelGenerator( int h_max, int p_bits ) {
        this( new Random(), h_max, p_bits );
    }

    @Override
    public int getMaxHeight() {
        return h_max;
    }

    @Override
    public int newLevel() {
        long randomBits = makeRandomBits( rand );

        for ( int i = 0; i < h_max; i++ )
        {
            if ( (randomBits & p_bits) == 0 )
            {
                /* coin flip success */
                randomBits = ( randomBits >> p_bits );
                continue;
            }
            else
            {
                /* coin flip fail */
                return i;
            }
        }
        return h_max - 1;
    }

    private long makeRandomBits(Random rand) {
        /* Generate only as many random bits as we need */
        return (rand_bits <= 32) ? (long) rand.nextInt() : rand.nextLong();
    }
}
