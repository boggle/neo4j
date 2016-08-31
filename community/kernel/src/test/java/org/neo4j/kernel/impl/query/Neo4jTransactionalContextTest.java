package org.neo4j.kernel.impl.query;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Collections;

import org.neo4j.kernel.GraphDatabaseQueryService;
import org.neo4j.kernel.api.ExecutingQuery;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.api.MetaDataOperations;
import org.neo4j.kernel.api.Statement;
import org.neo4j.kernel.api.dbms.DbmsOperations;
import org.neo4j.kernel.api.security.AccessMode;
import org.neo4j.kernel.impl.core.ThreadToStatementContextBridge;
import org.neo4j.kernel.impl.coreapi.InternalTransaction;
import org.neo4j.kernel.impl.coreapi.PropertyContainerLocker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Neo4jTransactionalContextTest
{
    @Test
    public void neverStopsExecutingQueryDuringCommitAndRestartTx()
    {
        // Given
        GraphDatabaseQueryService graph = mock( GraphDatabaseQueryService.class );
        InternalTransaction initialTransaction = mock( InternalTransaction.class );
        KernelTransaction initialKTX = mock( KernelTransaction.class );
        Statement initialStatement = mock( Statement.class );
        MetaDataOperations initialMeta = mock( MetaDataOperations.class );
        ExecutingQuery executingQuery = mock( ExecutingQuery.class );
        PropertyContainerLocker locker = null;
        ThreadToStatementContextBridge txBridge = mock( ThreadToStatementContextBridge.class );
        DbmsOperations.Factory dbmsOperationsFactory = null;

        KernelTransaction secondKTX = mock( KernelTransaction.class );
        InternalTransaction secondTransaction = mock( InternalTransaction.class );
        Statement secondStatement = mock( Statement.class );
        MetaDataOperations secondMeta = mock( MetaDataOperations.class );

        when( executingQuery.queryText() ).thenReturn( "X" );
        when( executingQuery.queryParameters() ).thenReturn( Collections.emptyMap() );
        when( initialStatement.metaOperations() ).thenReturn( initialMeta );
        when( graph.beginTransaction( null, null ) ).thenReturn( secondTransaction );
        when( txBridge.getKernelTransactionBoundToThisThread( true ) ).thenReturn( initialKTX, secondKTX );
        when( txBridge.get() ).thenReturn( secondStatement );
        when( secondStatement.metaOperations() ).thenReturn( secondMeta );

        Neo4jTransactionalContext context = new Neo4jTransactionalContext(
                graph, initialTransaction, initialStatement, executingQuery,
                locker, txBridge, dbmsOperationsFactory
        );

        // When
        context.commitAndRestartTx();

        // Then
        InOrder order = Mockito.inOrder( txBridge, initialTransaction, initialMeta, initialKTX, secondMeta, secondKTX );

        // (1) Unbind old
        order.verify( txBridge ).getKernelTransactionBoundToThisThread( true );
        order.verify( txBridge ).unbindTransactionFromCurrentThread();

        // (2) Register and unbind new
        order.verify( txBridge ).get();
        order.verify( secondMeta ).registerExecutingQuery( executingQuery );
        order.verify( txBridge ).getKernelTransactionBoundToThisThread( true );
        order.verify( txBridge ).unbindTransactionFromCurrentThread();

        // (3) Rebind, unregister, and close old
        order.verify( txBridge ).bindTransactionToCurrentThread( initialKTX );
        order.verify( initialMeta ).unregisterExecutingQuery( executingQuery );
        order.verify( initialTransaction ).success();
        order.verify( initialTransaction ).close();

        // (4) Rebind new
        order.verify( txBridge ).bindTransactionToCurrentThread( secondKTX );
    }
}
