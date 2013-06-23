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
package org.neo4j.test;

import org.junit.Ignore;

import static java.lang.Math.abs;
import static java.lang.System.currentTimeMillis;

@Ignore( "Not a test, merely a utility" )
public class PerformanceMeasurement
{
    public static long measure( Runnable performanceTest )
    {
        return measure( performanceTest, SYSOUT );
    }
    
    public static long measure( Runnable performanceTest, Events events )
    {
        long time = -1;
        while ( true )
        {
            long start = currentTimeMillis();
            performanceTest.run();
            long timeForThisRun = currentTimeMillis()-start;
            if ( time != -1 )
            {
                double factor = (double) timeForThisRun / time;
                if ( abs( 1-factor ) < 0.05D )
                {
                    events.stableResult( timeForThisRun );
                    return timeForThisRun;
                }
                else
                {
                    events.unstableResult( time, timeForThisRun );
                }
            }
            else
            {
                events.firstResult( timeForThisRun );
            }
            time = timeForThisRun;
        }
    }
    
    public static interface Events
    {
        void firstResult( long time );
        
        void unstableResult( long previousTime, long time );
        
        void stableResult( long time );
    }
    
    public static final Events SYSOUT = new Events()
    {
        @Override
        public void firstResult( long time )
        {
            System.out.println( "First result " + time );
        }
        
        @Override
        public void unstableResult( long previousTime, long time )
        {
            System.out.println( "Still warming up " + time + " compared to previous " + previousTime );
        }
        
        @Override
        public void stableResult( long time )
        {
            System.out.println( "Stable result " + time );
        }
    };
}
