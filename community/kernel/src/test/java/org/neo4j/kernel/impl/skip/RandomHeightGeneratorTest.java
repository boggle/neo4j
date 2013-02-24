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

import static junit.framework.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class RandomHeightGeneratorTest
{
    @Test
    public void testHeightGenerator() {
        // GIVEN
        int h_max = 24;  /* keep low enough for this test to run fast */
        int p_bits = 1;

        long[] counts = new long[h_max];
        int i = 0;
        long rounds = 0;
        long sum = 0;
        RandomHeightGenerator heightGenerator = new RandomHeightGenerator( h_max, p_bits );

        // WHEN
        while (i < h_max) {
            int j = heightGenerator.getRandomHeight();
            assertTrue( j > 0 );
            assertTrue( j <= h_max );
            if (counts[j-1] == 0) {
               System.out.print( j );
               System.out.print( ' ' );
               i++;
            }
            counts[j-1]++;
            rounds++;
            sum += j;
        }

        // THEN
        assertTrue( sum >= counts.length );

        // TELL (1)
        System.out.println( );
        System.out.println( Arrays.toString( counts ) );
        System.out.println( "Inserts until all heights reached: " + rounds);
        System.out.println( "Average height: " + ((double)sum)/((double)rounds) );

        // TELL (2)
        long goal = (long) (0.85 * rounds);
        for(int k = 0; k < counts.length; k++) {
            if (goal > counts[k])
                goal -= counts[k];
            else
            {
                System.out.println( "85 % have no more links than: " + (k + 1) );
                break;
            }
        }
    }
}
