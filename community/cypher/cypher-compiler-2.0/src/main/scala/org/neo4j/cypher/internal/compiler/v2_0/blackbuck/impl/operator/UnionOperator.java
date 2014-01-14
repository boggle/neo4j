//package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.operator;
//
//import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.RowPool;
//import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.*;
//import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;
//
//public class UnionOperator<C extends Cursor<C>> extends FusingOperator<C>
//{
//    public UnionOperator( Object key, Operator<C> destination, Operator<C> lhs, Operator<C> rhs )
//    {
//        super( key, destination, lhs, rhs );
//    }
//
//    @Override
//    public void install( final RowPool<C> pool, Scheduler<C> scheduler ) {
//        final Object lhsKey = lhs().key();
//        final Object rhsKey = rhs().key();
//
//        Activation<C> activation = new Activation<C>() {
//            private final C output = pool.newCursor();
//
//            private boolean lhs = true;
//
//            @Override
//            public void activate( Router<C> router, Event<C> event )
//            {
//                if ( event.isFrom( lhsKey ) )
//                {
//                    if ( ! lhs )
//                    {
//                        throw new IllegalStateException( "Union received lhs chunk out of order" );
//                    }
//
//                    emit( router, event.cursor() );
//
//                    if ( event.isLast() )
//                    {
//                        lhs = false;
//
//                        output.rewind();
//                        while ( output.hasNext() )
//                        {
//                            emit( router, output.nextChunk() );
//                        }
//                    }
//                }
//                else if ( event.isFrom( rhsKey ) )
//                {
//                    if ( lhs )
//                    {
//                        event.cursor().appendTo( output );
//                    }
//                    else
//                    {
//                        emit( router, event.cursor() );
//                    }
//                }
//                else
//                {
//                    throw new IllegalStateException( "Unknown event" );
//                }
//            }
//        };
//
//        scheduler.register( lhsKey, activation );
//        scheduler.register( rhsKey, activation );
//    }
//}
