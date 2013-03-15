/**
 * Copyright (c) 2002-2013 "Neo Technology,"
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
package org.neo4j.kernel.impl.nioneo.store;

import static org.neo4j.helpers.Exceptions.launderedException;

import java.nio.ByteBuffer;

import org.neo4j.helpers.Converter;
import org.neo4j.kernel.impl.api.index.LabelRule;

public interface SchemaRule extends RecordSerializable
{
    /**
     * The persistence id for this rule.
     */
    long getId();
    
    /**
     * @return id of label to which this schema rule has been attached
     */
    long getLabel();

    /**
     * @return the kind of this schema rule
     */
    Kind getKind();

    public static enum Kind
    {
        INDEX_RULE( 1, IndexRule.class )
        {
            @Override
            protected IndexRule newRule( long id, long labelId, ByteBuffer buffer )
            {
                return new IndexRule( id, labelId, buffer );
            }
        },

        LABEL_RULE( 2, LabelRule.class )
        {
            @Override
            protected LabelRule newRule( long id, long labelId, ByteBuffer buffer )
            {
                return new LabelRule( id, labelId, buffer );
            }
        };

        private final byte id;
        private final Class<? extends SchemaRule> ruleClass;

        private Kind( int id, Class<? extends SchemaRule> ruleClass )
        {
            assert id > 0 : "Kind id 0 is reserved";
            this.id = (byte) id;
            this.ruleClass = ruleClass;
        }
        
        public Class<? extends SchemaRule> getRuleClass()
        {
            return this.ruleClass;
        }
        
        public byte id()
        {
            return this.id;
        }

        @SuppressWarnings("unchecked")
        public static <T extends SchemaRule> Converter<SchemaRule, T> getConverter(Class<T> clazz)
        {
            if (clazz.isAssignableFrom( IndexRule.class ))
                return (Converter<SchemaRule, T>) new KindConverter<IndexRule>( INDEX_RULE );
            if (clazz.isAssignableFrom( LabelRule.class ))
                return (Converter<SchemaRule, T>) new KindConverter<IndexRule>( LABEL_RULE );

            throw new IllegalArgumentException( "Unsupported kind converter requested" );
        }

        protected abstract SchemaRule newRule( long id, long labelId, ByteBuffer buffer );

        public static SchemaRule deserialize( long id, ByteBuffer buffer )
        {
            long labelId = buffer.getInt();
            Kind kind = kindForId( buffer.get() );
            try
            {
                return kind.newRule( id, labelId, buffer );
            }
            catch ( Exception e )
            {
                throw launderedException( e );
            }
        }
        
        public static Kind kindForId( byte id )
        {
            switch ( id )
            {
            case 1: return Kind.INDEX_RULE;
            case 2: return Kind.LABEL_RULE;
            default:
                throw new IllegalArgumentException( "Unknown kind id " + id );
            }
        }
    }

    public static class KindConverter<T extends SchemaRule> implements Converter<SchemaRule, T>
    {
        private final Kind kind;

        public KindConverter( Kind kind )
        {
            if (kind == null)
                throw new IllegalArgumentException( "Kind must not be null" );
            this.kind = kind;
        }

        @Override
        public boolean accept( SchemaRule item )
        {
            return kind.equals( item.getKind() );
        }

        @SuppressWarnings("unchecked")
        @Override
        public T apply( SchemaRule schemaRule )
        {
            return (T) kind.getRuleClass().cast( schemaRule );
        }
    }
}
