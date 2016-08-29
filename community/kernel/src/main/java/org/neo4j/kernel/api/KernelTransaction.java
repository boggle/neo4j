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
package org.neo4j.kernel.api;

import java.util.Set;
import java.util.stream.Stream;

import org.neo4j.kernel.api.exceptions.Status;
import org.neo4j.kernel.api.exceptions.TransactionFailureException;
import org.neo4j.kernel.api.security.AccessMode;
import org.neo4j.kernel.impl.api.Kernel;

/**
 * Represents a transaction of changes to the underlying graph.
 * Actual changes are made in the {@linkplain #acquireStatement() statements} acquired from this transaction.
 * Changes made within a transaction are visible to all operations within it.
 *
 * The reason for the separation between transactions and statements is isolation levels. While Neo4j is read-committed
 * isolation, a read can potentially involve multiple operations (think of a cypher statement). Within that read, or
 * statement if you will, the isolation level should be repeatable read, not read committed.
 *
 * Clearly separating between the concept of a transaction and the concept of a statement allows us to cater to this
 * type of isolation requirements.
 *
 * This class has a 1-1 relationship with{@link org.neo4j.kernel.impl.transaction.state.TransactionRecordState}, please see its'
 * javadoc for details.
 *
 * Typical usage:
 * <pre>
 * try ( KernelTransaction transaction = kernel.newTransaction() )
 * {
 *      try ( Statement statement = transaction.acquireStatement() )
 *      {
 *          ...
 *      }
 *      ...
 *      transaction.success();
 * }
 * catch ( SomeException e )
 * {
 *      ...
 * }
 * catch ( SomeOtherExceptionException e )
 * {
 *      ...
 * }
 * </pre>
 *
 * Typical usage of failure if failure isn't controlled with exceptions:
 * <pre>
 * try ( KernelTransaction transaction = kernel.newTransaction() )
 * {
 *      ...
 *      if ( ... some condition )
 *      {
 *          transaction.failure();
 *      }
 *
 *      transaction.success();
 * }
 * </pre>
 */
public interface KernelTransaction extends AutoCloseable
{
    enum Type {
        implicit,
        explicit
    }

    interface CloseListener
    {
        /**
         * @param txId On success, the actual id of the current transaction if writes have been performed, 0 otherwise.
         * On rollback, always -1.
         */
        void notify( long txId );
    }

    long ROLLBACK = -1;
    long READ_ONLY = 0;

    /**
     * Acquires a new {@link Statement} for this transaction which allows for reading and writing data from and
     * to the underlying database. After the group of reads and writes have been performed the statement
     * must be {@link Statement#close() released}.
     * @return a {@link Statement} with access to underlying database.
     */
    Statement acquireStatement();

    /**
     * Marks this transaction as successful. When this transaction later gets {@link #close() closed}
     * its changes, if any, will be committed. If this method hasn't been called or if {@link #failure()}
     * has been called then any changes in this transaction will be rolled back as part of {@link #close() closing}.
     */
    void success();

    /**
     * Marks this transaction as failed. No amount of calls to {@link #success()} will clear this flag.
     * When {@link #close() closing} this transaction any changes will be rolled back.
     */
    void failure();

    /**
     * Closes this transaction, committing its changes if {@link #success()} has been called and neither
     * {@link #failure()} nor {@link #markForTermination(Status)} has been called.
     * Otherwise its changes will be rolled back.
     *
     * @return id of the committed transaction or {@link #ROLLBACK} if transaction was rolled back or
     * {@link #READ_ONLY} if transaction was read-only.
     */
    long closeTransaction() throws TransactionFailureException;

    /**
     * Closes this transaction, committing its changes if {@link #success()} has been called and neither
     * {@link #failure()} nor {@link #markForTermination(Status)} has been called.
     * Otherwise its changes will be rolled back.
     */
    @Override
    default void close() throws TransactionFailureException
    {
        closeTransaction();
    }

    /**
     * @return {@code true} if the transaction is still open, i.e. if {@link #close()} hasn't been called yet.
     */
    boolean isOpen();

    /**
     * @return the mode this transaction is currently executing in.
     */
    AccessMode mode();

    /**
     * @return {@code true} if {@link #markForTermination(Status)} has been invoked, otherwise {@code false}.
     */
    Status getReasonIfTerminated();

    /**
     * Marks this transaction for termination, such that it cannot commit successfully and will try to be
     * terminated by having other methods throw a specific termination exception, as to sooner reach the assumed
     * point where {@link #close()} will be invoked.
     */
    void markForTermination( Status reason );

    /**
     * @return The timestamp of the last transaction that was committed to the store when this transaction started.
     */
    long lastTransactionTimestampWhenStarted();

    /**
     * @return The id of the last transaction that was committed to the store when this transaction started.
     */
    long lastTransactionIdWhenStarted();

    /**
     * @return start time of this transaction, i.e. basically {@link System#currentTimeMillis()} when user called
     * {@link Kernel#newTransaction(Type, AccessMode)}.
     */
    long localStartTime();

    /**
     * Register a {@link CloseListener} to be invoked after commit, but before transaction events "after" hooks
     * are invoked.
     * @param listener {@link CloseListener} to get these notifications.
     */
    void registerCloseListener( CloseListener listener );

    /**
     * Kernel transaction type
     *
     * Implicit if created internally in the database
     * Explicit if created by the end user
     *
     * @return the transaction type: implicit or explicit
     */
    Type transactionType();

    /**
     * Return transaction id that assigned during transaction commit process.
     * @see org.neo4j.kernel.impl.api.TransactionCommitProcess
     * @return transaction id.
     * @throws IllegalStateException if transaction id is not assigned yet
     */
    long getTransactionId();

    /**
     * Return transaction commit time (in millis) that assigned during transaction commit process.
     * @see org.neo4j.kernel.impl.api.TransactionCommitProcess
     * @return transaction commit time
     * @throws IllegalStateException if commit time is not assigned yet
     */
    long getCommitTime();

    Revertable restrict( AccessMode mode );

    interface Revertable extends AutoCloseable
    {
        @Override
        void close();
    }
}
