package org.neo4j.approx.primitive.hyloglog;

import org.junit.Test;

import org.neo4j.approx.primitive.hyloglog.HyLogLogCounter;
import org.neo4j.approx.primitive.hyloglog.HyLogLogRegisters;

import static org.junit.Assert.*;

import static org.neo4j.approx.primitive.hyloglog.HyLogLogRegisters.getHiNibble;
import static org.neo4j.approx.primitive.hyloglog.HyLogLogRegisters.getLoNibble;
import static org.neo4j.approx.primitive.hyloglog.HyLogLogRegisters.setHiNibble;
import static org.neo4j.approx.primitive.hyloglog.HyLogLogRegisters.setLoNibble;

public class HyLogLogRegistersTest
{
    @Test
    public void shouldGetAndSetEachValInEachRegister() {
        HyLogLogRegisters registers = new HyLogLogRegisters();

        for (int register = 0; register < HyLogLogCounter.M; register++)
        {
            for (byte value = 0; value < HyLogLogCounter.P; value++) {

                byte leftBefore = registers.get( left( register ) );
                byte rightBefore = registers.get( right( register ) );
                registers.set( register, value );
                byte leftAfter = registers.get( left( register ) );
                byte rightAfter = registers.get( right( register ) );

                assertEquals( value, registers.get( register ) );
                assertEquals( leftBefore, leftAfter );
                assertEquals( rightBefore, rightAfter );
            }
        }
    }

    private int left( int register )
    {
        return Math.abs( register - 1 ) % HyLogLogCounter.M;
    }

    private int right( int register )
    {
        return Math.abs(register + 1) % HyLogLogCounter.M;
    }

    @Test
    public void shouldGetLoNibble() {
        for (int value = 0; value < 256; value++)
        {
            assertEquals( value % 16, getLoNibble( (byte) value ) );
        }
    }

    @Test
    public void shouldSetLoNibble() {
        for (int value = 0; value < 256; value++)
        {
            for ( int newValue = 0; newValue < 16; newValue++ )
            {
                byte setValue = setLoNibble( (byte) value, (byte) newValue );
                assertEquals( newValue, getLoNibble( setValue ) );
                assertEquals( getHiNibble( (byte) value ), getHiNibble( setValue ) );
            }
        }
    }

    @Test
    public void shouldGetHiNibble() {
        for (int value = 0; value < 256; value++)
        {
            assertEquals( value / 16, getHiNibble( (byte) value ) );
        }
    }

    @Test
    public void shouldSetHiNibble() {
        for (int value = 16; value < 256; value++)
        {
            for ( int newValue = 0; newValue < 16; newValue++ )
            {
                byte setValue = setHiNibble( (byte) value, (byte) newValue );
                assertEquals( newValue, getHiNibble( setValue ) );
                assertEquals( getLoNibble( (byte) value ), getLoNibble( setValue ) );
            }
        }
    }
}
