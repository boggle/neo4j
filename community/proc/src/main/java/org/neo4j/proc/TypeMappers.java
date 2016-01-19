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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.neo4j.kernel.api.exceptions.ProcedureException;
import org.neo4j.kernel.api.exceptions.Status;
import org.neo4j.proc.Neo4jTypes.AnyType;

import static org.neo4j.proc.Neo4jTypes.NTAny;
import static org.neo4j.proc.Neo4jTypes.NTBoolean;
import static org.neo4j.proc.Neo4jTypes.NTFloat;
import static org.neo4j.proc.Neo4jTypes.NTInteger;
import static org.neo4j.proc.Neo4jTypes.NTList;
import static org.neo4j.proc.Neo4jTypes.NTMap;
import static org.neo4j.proc.Neo4jTypes.NTNumber;
import static org.neo4j.proc.Neo4jTypes.NTString;

public class TypeMappers
{
    /**
     * Converts a java object to the specified {@link #type() neo4j type}. In practice, this is often the same java object - but this gives a guarantee
     * that only java objects Neo4j can digest are outputted.
     */
    interface ToNeoValue
    {
        AnyType type();

        Object apply( Object javaValue ) throws ProcedureException;
    }

    /**
     * Converts from the internal representation back to the types, defined by the procedure. In most cases this is the
     * same object but in some cases the value must be casted to fit the signature of the procedure. For example if
     * the signature declares an argument using int, float (or the corresponding reference types or lists thereof)
     * the value
     * coming from Neo4j must be casted to fit the signature.
     */
    interface ToProcedureValue
    {
        Object apply( Object neoValue ) throws ProcedureException;
    }

    private final Map<Class<?>, ToNeoValue> javaToNeo = new HashMap<>();

    public TypeMappers()
    {
        registerScalarsAndCollections();
    }

    /**
     * We don't have Node, Relationship, Property available down here - and don't strictly want to, we want the procedures to be independent of which
     * Graph API is being used (and we don't want them to get tangled up with kernel code). So, we only register the "core" type system here, scalars and
     * collection types. Node, Relationship, Path and any other future graph types should be registered from the outside in the same place APIs to work
     * with those types is registered.
     */
    private void registerScalarsAndCollections()
    {
        registerType( String.class, TO_STRING );
        registerType( long.class, TO_INTEGER );
        registerType( Long.class, TO_INTEGER );
        registerType( double.class, TO_FLOAT );
        registerType( Double.class, TO_FLOAT );
        registerType( Number.class, TO_NUMBER );
        registerType( boolean.class, TO_BOOLEAN );
        registerType( Boolean.class, TO_BOOLEAN );
        registerType( Map.class, TO_MAP );
        registerType( List.class, TO_LIST );
        registerType( Object.class, TO_ANY );
    }

    public ToNeoValue javaToNeo( Class<?> javaType ) throws ProcedureException
    {
        ToNeoValue converter = javaToNeo.get( javaType );
        if( converter != null )
        {
            return converter;
        }
        throw javaToNeoMappingError( javaType, Neo4jTypes.NTAny );
    }

    public void registerType( Class<?> javaClass, ToNeoValue toNeo )
    {
        javaToNeo.put( javaClass, toNeo );
    }

    public static class ToNeoTypeMapper implements ToNeoValue
    {
        private final AnyType type;
        private final Function<Object,Object> mapper;

        public ToNeoTypeMapper( AnyType type, Function<Object,Object> mapper )
        {
            this.type = type;
            this.mapper = mapper;
        }

        @Override
        public AnyType type()
        {
            return type;
        }

        @Override
        public Object apply( Object javaValue ) throws ProcedureException
        {
            if( javaValue == null )
            {
                return null;
            }

            Object out = mapper.apply( javaValue );
            if( out != null )
            {
                return out;
            }
            throw javaToNeoMappingError( javaValue.getClass(), type() );
        }
    }

    private final ToNeoValue TO_STRING = new ToNeoTypeMapper( NTString, ( v ) -> v instanceof String ? v : null );
    private final ToNeoValue TO_INTEGER =
            new ToNeoTypeMapper( NTInteger, ( v ) -> v instanceof Number ? ((Number) v).longValue() : null );
    private final ToNeoValue TO_FLOAT =
            new ToNeoTypeMapper( NTFloat, ( v ) -> v instanceof Number ? ((Number) v).doubleValue() : null );
    private final ToNeoValue TO_NUMBER = new ToNeoTypeMapper( NTNumber, ( v ) -> v instanceof Number ? v : null );
    private final ToNeoValue TO_BOOLEAN = new ToNeoTypeMapper( NTBoolean, ( v ) -> v instanceof Boolean ? v : null );
    private final ToNeoValue TO_MAP = new ToNeoTypeMapper( NTMap, ( v ) -> v instanceof Map ? v : null );
    private final ToNeoValue TO_LIST = new ToNeoTypeMapper( NTList( NTAny ), ( v ) -> v instanceof List ? v : null );
    private final ToNeoValue TO_ANY = new ToNeoTypeMapper( NTAny, ( v ) -> v );

    private static ProcedureException javaToNeoMappingError( Class<?> cls, AnyType neoType )
    {
        return new ProcedureException( Status.Statement.InvalidType, "Don't know how to map `%s` to `%s`", cls, neoType );
    }
}
