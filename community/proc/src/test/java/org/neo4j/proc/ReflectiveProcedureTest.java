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

import java.util.List;
import java.util.stream.Stream;

import org.neo4j.collection.RawIterator;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.api.exceptions.ProcedureException;
import org.neo4j.proc.Procedure.BasicContext;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.neo4j.helpers.collection.IteratorUtil.asList;
import static org.neo4j.proc.ProcedureSignature.procedureSignature;

public class ReflectiveProcedureTest
{
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCompileProcedure() throws Throwable
    {
        // When
        List<Procedure> procedures = compile( SingleReadOnlyProcedure.class );

        // Then
        assertEquals( 1, procedures.size() );
        assertThat( procedures.get( 0 ).signature(), equalTo(
                procedureSignature( "org", "neo4j", "proc", "listCoolPeople" )
                        .out( "name", Neo4jTypes.NTString )
                        .build() ) );
    }


    @Test
    public void shouldRunSimpleReadOnlyProcedure() throws Throwable
    {
        // Given
        Procedure proc = compile( SingleReadOnlyProcedure.class ).get( 0 );

        // When
        RawIterator<Object[],ProcedureException> out = proc.apply( new BasicContext(), new Object[0] );

        // Then
        assertThat( asList( out ), contains(
                new Object[]{"Bonnie"},
                new Object[]{"Clyde"}
        ) );
    }

    @Test
    public void shouldIgnoreClassesWithNoProcedures() throws Throwable
    {
        // When
        List<Procedure> procedures = compile( PrivateConstructorButNoProcedures.class );

        // Then
        assertEquals( 0, procedures.size() );
    }

    @Test
    public void shouldRunClassWithMultipleProceduresDeclared() throws Throwable
    {
        // Given
        List<Procedure> compiled = compile( MultiProcedureProcedure.class );
        Procedure bananaPeople = compiled.get( 0 );
        Procedure coolPeople = compiled.get( 1 );

        // When
        RawIterator<Object[],ProcedureException> coolOut = coolPeople.apply( new BasicContext(), new Object[0] );
        RawIterator<Object[],ProcedureException> bananaOut = bananaPeople.apply( new BasicContext(), new Object[0] );

        // Then
        assertThat( asList( coolOut ), contains(
                new Object[]{"Bonnie"},
                new Object[]{"Clyde"}
        ) );

        assertThat( asList( bananaOut ), contains(
                new Object[]{"Jake", 18L},
                new Object[]{"Pontus", 2L}
        ) );
    }

    @Test
    public void shouldGiveHelpfulErrorOnConstructorThatRequiresArgument() throws Throwable
    {
        // Expect
        exception.expect( ProcedureException.class );
        exception.expectMessage( "Unable to find a usable public no-argument constructor " +
                                 "in the class `WierdConstructorProcedure`. Please add a " +
                                 "valid, public constructor, recompile the class and try again." );

        // When
        compile( WierdConstructorProcedure.class );
    }

    @Test
    public void shouldGiveHelpfulErrorOnNoPublicConstructor() throws Throwable
    {
        // Expect
        exception.expect( ProcedureException.class );
        exception.expectMessage( "Unable to find a usable public no-argument constructor " +
                                 "in the class `PrivateConstructorProcedure`. Please add " +
                                 "a valid, public constructor, recompile the class and try again." );

        // When
        compile( PrivateConstructorProcedure.class );
    }

    public static class MyOutputRecord
    {
        public String name;

        public MyOutputRecord( String name )
        {
            this.name = name;
        }
    }


    public static class SomeOtherOutputRecord
    {
        public String name;
        public long bananas;

        public SomeOtherOutputRecord( String name, long bananas )
        {
            this.name = name;
            this.bananas = bananas;
        }
    }

    public static class SingleReadOnlyProcedure
    {
        @ReadOnlyProcedure
        public Stream<MyOutputRecord> listCoolPeople()
        {
            return Stream.of(
                    new MyOutputRecord( "Bonnie" ),
                    new MyOutputRecord( "Clyde" ) );
        }
    }

    public static class MultiProcedureProcedure
    {
        @ReadOnlyProcedure
        public Stream<MyOutputRecord> listCoolPeople()
        {
            return Stream.of(
                    new MyOutputRecord( "Bonnie" ),
                    new MyOutputRecord( "Clyde" ) );
        }

        @ReadOnlyProcedure
        public Stream<SomeOtherOutputRecord> listBananaOwningPeople()
        {
            return Stream.of(
                    new SomeOtherOutputRecord( "Jake", 18 ),
                    new SomeOtherOutputRecord( "Pontus", 2 ) );
        }
    }

    public static class WierdConstructorProcedure
    {
        public WierdConstructorProcedure( WierdConstructorProcedure wat )
        {

        }

        @ReadOnlyProcedure
        public Stream<MyOutputRecord> listCoolPeople()
        {
            return Stream.of(
                    new MyOutputRecord( "Bonnie" ),
                    new MyOutputRecord( "Clyde" ) );
        }
    }

    public static class PrivateConstructorProcedure
    {
        private PrivateConstructorProcedure()
        {

        }

        @ReadOnlyProcedure
        public Stream<MyOutputRecord> listCoolPeople()
        {
            return Stream.of(
                    new MyOutputRecord( "Bonnie" ),
                    new MyOutputRecord( "Clyde" ) );
        }
    }

    public static class PrivateConstructorButNoProcedures
    {
        private PrivateConstructorButNoProcedures()
        {

        }

        public Stream<MyOutputRecord> thisIsNotAProcedure()
        {
            return null;
        }
    }

    private List<Procedure> compile( Class<?> clazz ) throws KernelException
    {
        return new ReflectiveProcedureCompiler( new TypeMappers() ).compile( clazz );
    }
}
