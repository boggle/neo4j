package org.neo4j.approx.primitive.hyloglog;

import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.*;

public class HyLogLogCounterTest
{
    @Test
    public void shouldAcceptHashesForAnyHiWord() {
        HyLogLogCounter counter = new HyLogLogCounter();

        for (int i = 0; i < 65536; i++) {
            int h = (i << HyLogLogCounter.P) | 23;

            counter.addHash( h );
        }

        counter.addHash( Integer.MIN_VALUE );
        counter.addHash( Integer.MAX_VALUE );
    }

    @Test
    public void shouldCountEstimateForLowThreshold() {
        Random rand = new Random( 23L );
        HyLogLogCounter counter = new HyLogLogCounter();

        for (int i = 0; i < HyLogLogCounter.LOW_THRESHOLD / 2; i++)
        {
            long value = rand.nextLong();
            counter.add( value );
            counter.add( value * 17 );
            counter.add( value * 23 );
        }

        System.out.println(counter.size());
        System.out.println( Math.round( counter.estimate() ) );
    }
}
