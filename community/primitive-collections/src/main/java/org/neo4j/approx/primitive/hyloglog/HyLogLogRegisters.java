package org.neo4j.approx.primitive.hyloglog;

class HyLogLogRegisters
{
    private final static int SHIFT = 4;
    private final static int LOW_MASK = (1 << SHIFT) - 1;
    private final static int HIGH_MASK = ~ LOW_MASK;

    private final byte[] registers = new byte[HyLogLogCounter.M >> 1];

    byte get( int register )
    {
        int idx = register >> 1;
        byte data = registers[idx];
        return ((register & 1) == 0) ? getLoNibble( data ) : getHiNibble( data );
    }

    void set( int register, byte value )
    {
        int idx = register >> 1;
        byte data = registers[idx];
        registers[idx] = ((register & 1) == 0) ? setLoNibble( data, value ) : setHiNibble( data, value );
    }

    void clearAll()
    {
        for (int idx = 0; idx < registers.length; idx++)
        {
            registers[idx] = 0;
        }
    }

    int numberOfClearedRegisters()
    {
        int numRegisters = 0;
        for (int idx = 0; idx < registers.length; idx++) {
            byte data = registers[idx];
            if (registers[idx] == 0)
            {
                numRegisters += 2;
            }
            else
            {
                if (getLoNibble( data ) != 0 )
                {
                    numRegisters += 1;
                }
                if (getHiNibble( data ) != 0 )
                {
                    numRegisters += 1;
                }
            }
        }
        return numRegisters;
    }

    static byte getLoNibble(byte b)
    {
        return (byte) (b & LOW_MASK);
    }

    static byte setLoNibble(byte b, byte newValue)
    {
        return (byte) ((b & HIGH_MASK) | (newValue & LOW_MASK));
    }

    static byte getHiNibble(byte b)
    {
        return (byte) (b >> SHIFT & LOW_MASK);
    }

    static byte setHiNibble(byte b, byte newValue)
    {
        return (byte) (((newValue & LOW_MASK) << SHIFT) | (b & LOW_MASK));
    }
}
