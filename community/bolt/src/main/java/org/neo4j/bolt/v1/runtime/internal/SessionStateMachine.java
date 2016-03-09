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
package org.neo4j.bolt.v1.runtime.internal;

import java.util.Map;
import java.util.UUID;

import org.neo4j.bolt.security.auth.Authentication;
import org.neo4j.bolt.security.auth.AuthenticationException;
import org.neo4j.bolt.v1.runtime.Session;
import org.neo4j.bolt.v1.runtime.StatementMetadata;
import org.neo4j.bolt.v1.runtime.spi.RecordStream;
import org.neo4j.bolt.v1.runtime.spi.StatementRunner;
import org.neo4j.kernel.GraphDatabaseQueryService;
import org.neo4j.kernel.api.AccessMode;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.api.exceptions.Status;
import org.neo4j.kernel.impl.core.ThreadToStatementContextBridge;
import org.neo4j.kernel.impl.coreapi.InternalTransaction;
import org.neo4j.kernel.impl.coreapi.PropertyContainerLocker;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.kernel.impl.logging.LogService;
import org.neo4j.kernel.impl.query.Neo4jTransactionalContext;
import org.neo4j.kernel.impl.query.QuerySession;
import org.neo4j.udc.UsageData;

import static org.neo4j.kernel.api.AccessMode.FULL;
import static org.neo4j.kernel.api.KernelTransaction.Type.explicit;
import static org.neo4j.kernel.api.KernelTransaction.Type.implicit;

/**
 * State-machine based implementation of {@link Session}. With this approach,
 * the discrete states a session can be in are explicit. Each state describes which actions from the context
 * interface are legal given that particular state, and how those actions behave given the current state.
 */
public class SessionStateMachine implements Session, SessionState
{
    /**
     * The session state machine, this is the heart of how a session operates. This enumerates the various discrete
     * states a session can be in, and describes how it behaves in those states.
     */
    enum State
    {
        /**
         * Before the session has been initialized.
         */
        UNINITIALIZED
                {
                    @Override
                    public State init( SessionStateMachine ctx, String clientName, Map<String,Object> authToken )
                    {
                        try
                        {
                            ctx.spi.authenticate( authToken );
                            ctx.spi.udcRegisterClient( clientName );
                            return IDLE;
                        }
                        catch ( AuthenticationException e )
                        {
                            return error( ctx, new Neo4jError( e.status(), e.getMessage(), e ) );
                        }
                    }

                    @Override
                    protected State onNoImplementation( SessionStateMachine ctx, String command )
                    {
                        ctx.error( new Neo4jError( Status.Request.Invalid, "No operations allowed until you send an " +
                                                                           "INIT message." ) );
                        return halt( ctx );
                    }
                },

        /**
         * No open transaction, no open result.
         */
        IDLE
                {
                    @Override
                    public State beginTransaction( SessionStateMachine ctx )
                    {
                        assert ctx.currentTransaction == null;
                        ctx.currentTransaction = ctx.spi.beginTransaction( explicit, FULL );
                        return IN_TRANSACTION;
                    }

                    @Override
                    public State runStatement( SessionStateMachine ctx, String statement, Map<String,Object> params )
                    {
                        try
                        {
                            ctx.currentResult = ctx.spi.run( ctx, statement, params );
                            ctx.result( ctx.currentStatementMetadata );
                            //if the call to run failed we must remain in state ERROR
                            if ( ctx.state == ERROR )
                            {
                                return ERROR;
                            }
                            else
                            {
                                return STREAM_OPEN;
                            }
                        }
                        catch ( Throwable e )
                        {
                            return error( ctx, e );
                        }
                    }

                    @Override
                    public State beginImplicitTransaction( SessionStateMachine ctx )
                    {
                        assert ctx.currentTransaction == null;
                        ctx.currentTransaction = ctx.spi.beginTransaction( implicit, FULL );
                        return IN_TRANSACTION;
                    }

                    @Override
                    public State reset( SessionStateMachine ctx )
                    {
                        return IDLE;
                    }

                    @Override
                    public State rollbackTransaction( SessionStateMachine ctx )
                    {
                        return error( ctx, new Neo4jError( Status.Request.Invalid,
                                "rollback cannot be done when there is no open transaction in the session." ) );
                    }
                },

        /**
         * Open transaction, no open stream
         * <p>
         * This is when the client has explicitly requested a transaction to be opened.
         */
        IN_TRANSACTION
                {
                    @Override
                    public State runStatement( SessionStateMachine ctx, String statement, Map<String,Object> params )
                    {
                        return IDLE.runStatement( ctx, statement, params );
                    }

                    @Override
                    public State commitTransaction( SessionStateMachine ctx )
                    {
                        try
                        {
                            ctx.currentTransaction.success();
                            ctx.currentTransaction.close();
                        }
                        catch ( Throwable e )
                        {
                            return error( ctx, e );
                        }
                        finally
                        {
                            ctx.currentTransaction = null;
                        }
                        return IDLE;
                    }

                    @Override
                    public State rollbackTransaction( SessionStateMachine ctx )
                    {
                        try
                        {
                            KernelTransaction tx = ctx.currentTransaction;
                            ctx.currentTransaction = null;

                            tx.failure();
                            tx.close();
                            return IDLE;
                        }
                        catch ( Throwable e )
                        {
                            return error( ctx, e );
                        }
                    }

                    @Override
                    public State reset( SessionStateMachine ctx )
                    {
                        return rollbackTransaction( ctx );
                    }

                },

        /**
         * A result stream is ready for consumption, there may or may not be an open transaction.
         */
        STREAM_OPEN
                {
                    @Override
                    public State pullAll( SessionStateMachine ctx )
                    {
                        try
                        {
                            ctx.result( ctx.currentResult );
                            return discardAll( ctx );
                        }
                        catch ( Throwable e )
                        {
                            return error( ctx, e );
                        }
                    }

                    @Override
                    public State discardAll( SessionStateMachine ctx )
                    {
                        try
                        {
                            ctx.currentResult.close();

                            if ( !ctx.hasTransaction() )
                            {
                                return IDLE;
                            }
                            else if ( ctx.currentTransaction.transactionType() == implicit )
                            {
                                return IN_TRANSACTION.commitTransaction( ctx );
                            }
                            else
                            {
                                return IN_TRANSACTION;
                            }
                        }
                        catch ( Throwable e )
                        {
                            return error( ctx, e );
                        }
                        finally
                        {
                            ctx.currentResult = null;
                        }
                    }

                    @Override
                    public State reset( SessionStateMachine ctx )
                    {
                        // Do an extra reset, since discardAll may put us
                        // in the IN_TRANSACTION state
                        return discardAll( ctx ).reset( ctx );
                    }

                },

        /** An error has occurred, client must acknowledge it before anything else is allowed. */
        ERROR
                {
                    @Override
                    public State reset( SessionStateMachine ctx )
                    {
                        return IDLE;
                    }

                    @Override
                    protected State onNoImplementation( SessionStateMachine ctx, String command )
                    {
                        ctx.ignored();
                        return ERROR;
                    }
                },

        /**
         * A recoverable error has occurred within an explicitly opened transaction. After the client acknowledges
         * it, we will move back to {@link #IN_TRANSACTION}.
         */
        RECOVERABLE_ERROR
                {
                    @Override
                    public State reset( SessionStateMachine ctx )
                    {
                        return IN_TRANSACTION;
                    }

                    @Override
                    protected State onNoImplementation( SessionStateMachine ctx, String command )
                    {
                        ctx.ignored();
                        return RECOVERABLE_ERROR;
                    }
                },


        /** The state machine is permanently stopped. */
        STOPPED
                {
                    @Override
                    public State halt( SessionStateMachine ctx )
                    {
                        return STOPPED;
                    }

                    @Override
                    protected State onNoImplementation( SessionStateMachine ctx, String command )
                    {
                        ctx.ignored();
                        return STOPPED;
                    }
                };

        // Operations that a session can perform. Individual states override these if they want to support them.

        public State init( SessionStateMachine ctx, String clientName, Map<String,Object> authToken )
        {
            return onNoImplementation( ctx, "initializing the session" );
        }

        public State runStatement( SessionStateMachine ctx, String statement, Map<String,Object> params )
        {
            return onNoImplementation( ctx, "running a statement" );
        }

        public State pullAll( SessionStateMachine ctx )
        {
            return onNoImplementation( ctx, "pulling full stream" );
        }

        public State discardAll( SessionStateMachine ctx )
        {
            return onNoImplementation( ctx, "discarding remainder of stream" );
        }

        public State commitTransaction( SessionStateMachine ctx )
        {
            return onNoImplementation( ctx, "committing transaction" );
        }

        public State rollbackTransaction( SessionStateMachine ctx )
        {
            return onNoImplementation( ctx, "rolling back transaction" );
        }

        public State beginImplicitTransaction( SessionStateMachine ctx )
        {
            return onNoImplementation( ctx, "beginning implicit transaction" );
        }

        public State beginTransaction( SessionStateMachine ctx )
        {
            return onNoImplementation( ctx, "beginning implicit transaction" );
        }

        public State reset( SessionStateMachine ctx )
        {
            return onNoImplementation( ctx, "resetting the current session" );
        }

        protected State onNoImplementation( SessionStateMachine ctx, String command )
        {
            String msg = "'" + command + "' cannot be done when a session is in the '" + ctx.state.name() + "' state.";
            return error( ctx, new Neo4jError( Status.Request.Invalid, msg ) );
        }

        public State halt( SessionStateMachine ctx )
        {
            if ( ctx.currentTransaction != null )
            {
                try
                {
                    ctx.currentTransaction.close();
                }
                catch ( Throwable e )
                {
                    ctx.error( Neo4jError.from( e ) );
                }
            }
            return STOPPED;
        }

        State error( SessionStateMachine ctx, Throwable err )
        {
            return error( ctx, Neo4jError.from( err ) );
        }

        State error( SessionStateMachine ctx, Neo4jError err )
        {
            ctx.spi.reportError( err );
            State outcome = ERROR;
            if ( ctx.hasTransaction() )
            {
                // Is this error bad enough that we should roll back, or did the failure occur in an implicit
                // transaction?
                if(  err.status().code().classification().rollbackTransaction() ||
                     ctx.currentTransaction.transactionType() == implicit )
                {
                    try
                    {
                        ctx.currentTransaction.failure();
                        ctx.currentTransaction.close();
                    }
                    catch ( Throwable t )
                    {
                        ctx.spi.reportError( "While handling '" + err.status() + "', a second failure occurred when " +
                                             "rolling back transaction: " + t.getMessage(), t );
                    }
                    finally
                    {
                        ctx.currentTransaction = null;
                    }
                }
                else
                {
                    // A non-fatal error occurred inside an explicit transaction, such as a syntax error.
                    // These are recoverable, so we leave the transaction open for the user.
                    // This is mainly to cover cases of direct user-driven work, where someone might have
                    // manually built up a large transaction, and we'd rather not have it all be thrown out
                    // because of a spelling mistake.
                    outcome = RECOVERABLE_ERROR;
                }
            }
            ctx.error( err );
            return outcome;
        }
    }

    private final String id = UUID.randomUUID().toString();

    /** A re-usable statement metadata instance that always represents the currently running statement */
    private final StatementMetadata currentStatementMetadata = new StatementMetadata()
    {
        @Override
        public String[] fieldNames()
        {
            return currentResult.fieldNames();
        }
    };

    /** The current session state */
    private State state = State.UNINITIALIZED;

    /** The current pending result, if present */
    private RecordStream currentResult;

    /** The current transaction, if present */
    private KernelTransaction currentTransaction;

    /** Callback poised to receive the next response */
    private Callback currentCallback;

    /** Callback attachment */
    private Object currentAttachment;

    /** These are the "external" actions the state machine can take */
    private final SPI spi;

    /**
     * This SPI encapsulates the "external" actions the state machine can take.
     * It exists for three reasons:
     * 1) It makes it very clear what side-effects the SSM can have
     * 2) It decouples the SSM from the actual components performing these operations
     * 3) It makes it *much* easier to test the SSM without having to re-implement
     *    the whole database as mocks.
     *
     * If you are adding new functionality to the SSM where the new function needs
     * to reach out to some component outside the SSM, please add it here. And when
     * you do, please consider the law of demeter - if you are simply adding
     * "getQueryEngine" to the SPI, you're doing it wrong, then we might as well
     * have the full components as fields.
     */
    interface SPI
    {
        void reportError( Neo4jError err );
        void reportError( String message, Throwable cause );
        KernelTransaction beginTransaction( KernelTransaction.Type type, AccessMode mode );
        void bindTransactionToCurrentThread( KernelTransaction tx );
        void unbindTransactionFromCurrentThread();
        RecordStream run( SessionStateMachine ctx, String statement, Map<String, Object> params )
                throws KernelException;
        void authenticate( Map<String, Object> authToken ) throws AuthenticationException;
        void udcRegisterClient( String clientName );
    }

    public SessionStateMachine( UsageData usageData, GraphDatabaseFacade db, ThreadToStatementContextBridge txBridge,
            StatementRunner engine, LogService logging, Authentication authentication )
    {
        this( new StandardStateMachineSPI( usageData, db, engine, logging, authentication, txBridge ));
    }

    public SessionStateMachine( SPI spi )
    {
        this.spi = spi;
    }

    @Override
    public String key()
    {
        return id;
    }

    @Override
    public <A> void init( String clientName, Map<String,Object> authToken, A attachment, Callback<Void,A> callback )
    {
        before( attachment, callback );
        try
        {
            state = state.init( this, clientName, authToken );
        }
        finally { after(); }
    }

    @Override
    public <A> void run( String statement, Map<String,Object> params, A attachment,
            Callback<StatementMetadata,A> callback )
    {
        before( attachment, callback );
        try
        {
            state = state.runStatement( this, statement, params );
        }
        finally { after(); }
    }

    @Override
    public <A> void pullAll( A attachment, Callback<RecordStream,A> callback )
    {
        before( attachment, callback );
        try
        {
            state = state.pullAll( this );
        }
        finally { after(); }
    }

    @Override
    public <A> void discardAll( A attachment, Callback<Void,A> callback )
    {
        before( attachment, callback );
        try
        {
            state = state.discardAll( this );
        }
        finally { after(); }
    }

    @Override
    public <A> void reset( A attachment, Callback<Void,A> callback )
    {
        before( attachment, callback );
        try
        {
            state = state.reset( this );
        }
        finally { after(); }
    }

    @Override
    public void close()
    {
        before( null, null );
        try
        {
            state = state.halt( this );
        }
        finally { after(); }
    }

    // Below are methods used from within the state machine, to alter state while its executing an action

    @Override
    public void beginImplicitTransaction()
    {
        state = state.beginImplicitTransaction( this );
    }

    @Override
    public void beginTransaction()
    {
        state = state.beginTransaction( this );
    }

    @Override
    public void commitTransaction()
    {
        state = state.commitTransaction( this );
    }

    @Override
    public void rollbackTransaction()
    {
        state = state.rollbackTransaction( this );
    }

    @Override
    public boolean hasTransaction()
    {
        return currentTransaction != null;
    }

    @Override
    public QuerySession createSession( GraphDatabaseQueryService service, PropertyContainerLocker locker )
    {
        InternalTransaction transaction =
                service.beginTransaction( currentTransaction.transactionType(), currentTransaction.mode() );
        Neo4jTransactionalContext transactionalContext =
                new Neo4jTransactionalContext( service, transaction, currentTransaction.acquireStatement(), locker );

        return new QuerySession( transactionalContext )
        {

            @Override
            public String toString()
            {
                return "bolt";
            }
        };
    }

    public State state()
    {
        return state;
    }

    @Override
    public String toString()
    {
        return "Session[" + id + "," + state.name() + "]";
    }

    /**
     * Set the callback to receive the next response. This will receive one completion or one failure, and then be
     * detached again. This exists both to ensure that each callback only gets called once, as well as to avoid
     * repeating the callback and attachments in every method signature in the state machine.
     */
    private void before( Object attachment, Callback cb )
    {
        if ( cb != null )
        {
            cb.started( attachment );
        }

        if ( hasTransaction() )
        {
            spi.bindTransactionToCurrentThread( currentTransaction );
        }
        assert this.currentCallback == null;
        assert this.currentAttachment == null;
        this.currentCallback = cb;
        this.currentAttachment = attachment;
    }

    /** Signal to the currently attached client callback that the request has been processed */
    private void after()
    {
        try
        {
            if ( currentCallback != null )
            {
                try
                {
                    currentCallback.completed( currentAttachment );
                }
                finally
                {
                    currentCallback = null;
                    currentAttachment = null;
                }
            }
        }
        finally
        {
            if ( hasTransaction() )
            {
                spi.unbindTransactionFromCurrentThread();
            }
        }
    }

    /** Forward an error to the currently attached callback */
    private void error( Neo4jError err )
    {
        if ( err.status().code().classification() == Status.Classification.DatabaseError )
        {
            spi.reportError( err );
        }

        if ( currentCallback != null )
        {
            currentCallback.failure( err, currentAttachment );
        }
    }

    /** Forward a result to the currently attached callback */
    private void result( Object result ) throws Exception
    {
        if ( currentCallback != null )
        {
            currentCallback.result( result, currentAttachment );
        }
    }

    /**
     * A message is being ignored, because the state machine is waiting for an error to be acknowledged before it
     * resumes processing.
     */
    private void ignored()
    {
        if ( currentCallback != null )
        {
            currentCallback.ignored( currentAttachment );
        }
    }
}
