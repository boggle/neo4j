/*
 * Copyright (c) 2002-2015 "Neo Technology,"
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
package org.neo4j.kernel.impl.api.integrationtest;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.helpers.collection.Visitor;
import org.neo4j.kernel.api.ReadOperations;
import org.neo4j.kernel.api.SchemaWriteOperations;
import org.neo4j.kernel.api.procedure.ProcedureException;
import org.neo4j.kernel.api.procedure.ProcedureSignature;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.neo4j.helpers.collection.IteratorUtil.asCollection;
import static org.neo4j.kernel.api.procedure.ProcedureSignature.procedureSignature;
import static org.neo4j.kernel.impl.store.Neo4jTypes.NTText;

public class ProceduresKernelIT extends KernelIntegrationTest
{
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final ProcedureSignature signature = procedureSignature( "example", "exampleProc" )
            .in( "name", NTText )
            .out( "name", NTText ).build();

    @Test
    public void shouldCreateProcedure() throws Throwable
    {
        // Given
        SchemaWriteOperations ops = schemaWriteOperationsInNewTransaction();

        // When
        ops.procedureCreate( signature, "javascript", "emit(1);" );

        // Then
        assertThat( asCollection( ops.proceduresGetAll() ),
                Matchers.<Collection<ProcedureSignature>>equalTo( asList( signature ) ) );

        // And when
        commit();

        // Then
        assertThat( asCollection( readOperationsInNewTransaction().proceduresGetAll() ),
                Matchers.<Collection<ProcedureSignature>>equalTo( asList( signature ) ) );
    }

    @Test
    public void shouldGetProcedureByName() throws Throwable
    {
        // Given
        shouldCreateProcedure();

        // When
        ProcedureSignature found = readOperationsInNewTransaction().procedureGetSignature( new String[]{"example"}, "exampleProc" );

        // Then
        assertThat( found, equalTo( found ) );
    }

    @Test
    public void nonexistantProcedureShouldThrow() throws Throwable
    {
        // Expect
        exception.expect( ProcedureException.class );

        // When
        readOperationsInNewTransaction().procedureGetSignature( new String[]{"example"}, "exampleProc" );
    }

    @Test
    public void shouldBeAbleToInvokeSimpleProcedure() throws Throwable
    {
        // Given
        {
            SchemaWriteOperations ops = schemaWriteOperationsInNewTransaction();

            ops.procedureCreate( signature, "javascript", "emit(name);\n" );
            commit();
        }

        ReadOperations ops = readOperationsInNewTransaction();

        // When
        final List<List<Object>> records = new LinkedList<>();
        ops.procedureCall( signature.name(), new Object[]{"hello"}, new Visitor<Object[],ProcedureException>()
        {
            @Override
            public boolean visit( Object[] record ) throws ProcedureException
            {
                records.add( Arrays.asList( record ) );
                return true;
            }
        });

        // Then
        assertEquals( asList( asList( "hello" ) ), records );
    }
}
