/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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
package org.neo4j.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class DoubleLatch
{
    private static final int FIVE_MINUTES = 5;
    private final CountDownLatch startSignal;
    private final CountDownLatch finishSignal;

    public DoubleLatch()
    {
        this( 1 );
    }

    public DoubleLatch( int numberOfContestants )
    {
        this.startSignal = new CountDownLatch( numberOfContestants );
        this.finishSignal = new CountDownLatch( numberOfContestants );
    }

    public void waitForAllToStart()
    {
        awaitLatch( startSignal );
    }

    public void startAndWaitForAllToStart()
    {
        startSignal.countDown();
        awaitLatch( startSignal );
    }

    public void startAndWaitForAllToStartAndFinish()
    {
        startAndWaitForAllToStart();
        awaitLatch( finishSignal );
    }

    public void finish()
    {
        finishSignal.countDown();
    }

    public void finishAndWaitForAllToFinish()
    {
        finish();
        waitForAllToFinish();
    }

    public void waitForAllToFinish()
    {
        awaitLatch( finishSignal );
    }

    public static void awaitLatch( CountDownLatch latch )
    {
        try
        {
            assertTrue( "Latch specified waiting time elapsed.", latch.await( FIVE_MINUTES, TimeUnit.MINUTES ) );
        }
        catch ( InterruptedException e )
        {
            Thread.interrupted();
            throw new RuntimeException( "Thread interrupted while waiting on latch", e );
        }
    }

    @Override
    public String toString()
    {
        return super.toString() + "[Start[" + startSignal.getCount() + "], Finish[" + finishSignal.getCount() + "]]";
    }
}
