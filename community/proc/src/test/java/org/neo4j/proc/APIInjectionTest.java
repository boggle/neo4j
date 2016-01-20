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
package org.neo4j.proc;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.api.exceptions.ProcedureException;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class APIInjectionTest
{
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCompileAndRunProcedure() throws Throwable
    {
        // Given
        Procedure proc = compile( ProcedureWithInjectedAPI.class ).get( 0 );

        // Then
        List<Object[]> out = IteratorUtil.asList( proc.apply( new Procedure.BasicContext(), new Object[0] ) );

        // Then
        assertThat( out.get( 0 ), equalTo( (new Object[]{"Bonnie"}) ) );
        assertThat( out.get( 1 ), equalTo( (new Object[]{"Clyde"}) ) );
    }

    @Test
    public void shouldFailNicelyWhenUnknownAPI() throws Throwable
    {
        //Expect
        exception.expect( ProcedureException.class );
        exception.expectMessage( "Field `api` has type `class org.neo4j.proc.APIInjectionTest$UnknownAPI` " +
                                 "which is not assignable from `class org.neo4j.proc.APIInjectionTest$MyAwesomeAPI`." );

        // When
        compile( procedureWithUnknownAPI.class ).get( 0 );
    }

    public static class MyOutputRecord
    {
        public String name;

        public MyOutputRecord( String name )
        {
            this.name = name;
        }
    }

    public static class MyAwesomeAPI
    {

        List<String> listCoolPeople()
        {
            return asList( "Bonnie", "Clyde" );
        }
    }

    public static class UnknownAPI
    {

        List<String> listCoolPeople()
        {
            return asList( "booh!" );
        }
    }

    public static class ProcedureWithInjectedAPI
    {
        @API
        public MyAwesomeAPI api;

        @ReadOnlyProcedure
        public Stream<MyOutputRecord> listCoolPeople()
        {
            return api.listCoolPeople().stream().map( MyOutputRecord::new );
        }
    }

    public static class procedureWithUnknownAPI
    {
        @API
        public UnknownAPI api;

        @ReadOnlyProcedure
        public Stream<MyOutputRecord> listCoolPeople()
        {
            return api.listCoolPeople().stream().map( MyOutputRecord::new );
        }
    }


    private List<Procedure> compile( Class<?> clazz ) throws KernelException
    {
        TypeMappers typeMappers = new TypeMappers();
        typeMappers.registerComponent( API.class, MyAwesomeAPI::new );
        return new ReflectiveProcedureCompiler( typeMappers ).compile( clazz );
    }
}
