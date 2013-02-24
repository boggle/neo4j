/**
 * Copyright (c) 2002-2013 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.skip;

import java.util.Random;

public final class RandomHeightGenerator
{
    private final Random rand;

    private final int h_max;
    private final int p_bits;
    private final int rand_bits;

    public RandomHeightGenerator( Random rand, int h_max, int p_bits ) {
        this.rand = rand;

        assert h_max > 0;
        assert p_bits > 0;

        this.h_max = h_max;
        this.p_bits = p_bits;

        this.rand_bits =(h_max - 1) * p_bits;

        assert rand_bits <= 64;
    }


    public RandomHeightGenerator( int h_max, int p_bits ) {
        this( new Random(), h_max, p_bits );
    }

    public int getRandomHeight() {
        long randomBits = makeRandomBits( rand );

        int i = 1;
        do {
            if ((randomBits & p_bits) == 0)
            {
                /* coin flip success */
                randomBits = (randomBits >> p_bits);
                i++;
            }
            else
            {
                /* coin flip fail */
                break;
            }
        } while (i < h_max);
        return i;
    }

    private long makeRandomBits(Random rand) {
        /* Generate only as many random bits as we need */
        return (rand_bits <= 32) ? (long) rand.nextInt() : rand.nextLong();
    }
}
