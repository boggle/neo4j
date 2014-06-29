package org.neo4j.approx.primitive.hyloglog;

import org.neo4j.approx.primitive.base.AbstractHashBasedApproxDistinctCounter;

/**
 * This implements the improved hyper log log algorithm as described in
 *
 * S. Heule, M. Nunkesser, and A. Hall.
 * HyperLogLog in Practice: Algorithmic Engineering of a State of The Art Cardinality Estimation Algorithm.
 *
 * This is a practical re-descricption of the original algorithm which may be found in
 *
 * P. Flajolet, Éric Fusy, O. Gandouet, and F. Meunier.
 * Hyperloglog: The analysis of a near-optimal cardinality estimation algorithm.
 *
 */
public final class HyLogLogCounter extends AbstractHashBasedApproxDistinctCounter
{
    // These values are from the paper; We use 32 bit hashes since Java provides those

    public final static int P = 16;
    public final static int M = 1 << P;
    public final static double EST_FACTOR = /* ALPHA_M */ 0.7212881245439701 * /* M * M */( ((double) M) * ((double) M) );
    public final static double LOW_THRESHOLD = (((double) M) * 5) / 2;
    public final static double POW_2_32 = Math.pow(2, 32);
    public final static double HIGH_THRESHOLD = POW_2_32 / 30;


    private final static int LOW_MASK = (1 << P) - 1;
    private final static int HIGH_MASK = ~ LOW_MASK;

    private long samples = 0;
    private HyLogLogRegisters registers = new HyLogLogRegisters();

    @Override
    protected void addHash( int value )
    {
        int loWord = value & LOW_MASK;
        int hiWord = value >> P & LOW_MASK;

        byte sigma = (byte) (Integer.numberOfLeadingZeros( loWord ) + 1 - P);
        byte current = registers.get( hiWord );

        if ( sigma > current ) {
            registers.set( hiWord, sigma );
        }

        samples++;
    }

    @Override
    public void reset()
    {
        samples = 0;
        registers.clearAll();
    }

    @Override
    public long size()
    {
        return samples;
    }

    @Override
    public double estimate()
    {
        double est = basicEstimate();
//
//        if (est <= LOW_THRESHOLD)
//        {
//            int numberOfClearedRegisters = registers.numberOfClearedRegisters();
//            return numberOfClearedRegisters == 0  ? est : linearScaling( numberOfClearedRegisters );
//        }
//
//        if (est > HIGH_THRESHOLD)
//        {
//            return adjustedEstimate( est );
//        }

        return est;
    }

    private double basicEstimate()
    {
        // ALPHA_M * M^2 * SUM( j = 0, .., M-1 : 2^-reg(j) )
        double est = 0.0;
        for (int j = 0; j < M - 1; j++)
        {
             est += 1 / (double) (1 << registers.get( j ) );
        }
//        est *= EST_FACTOR;
        return est;
    }

    private double linearScaling( int numberOfClearedRegisters )
    {
        return M * Math.log( M / numberOfClearedRegisters );
    }

    private double adjustedEstimate( double est )
    {
        return - POW_2_32 * Math.log( 1 - ( est / POW_2_32 ) );
    }
}
