/**
 * Copyright (c) 2002-2014 "Neo Technology,"
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
package org.neo4j.kernel.impl.store.impl;

import java.io.IOException;

import org.neo4j.io.fs.StoreChannel;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.io.pagecache.PagedFile;
import org.neo4j.kernel.impl.store.Store;

/**
 * Defines the format of headers and records in a {@link org.neo4j.kernel.impl.store.impl.StandardStore}
 * @param <RECORD> the record type in this store
 * @param <CURSOR> the cursor type for this store, see {@link org.neo4j.kernel.impl.store.Store.RecordCursor}
 */
public interface StoreFormat<RECORD, CURSOR extends Store.RecordCursor>
{
    CURSOR createCursor( PagedFile file, StoreToolkit toolkit );

    /** Access the format for reading individual records. */
    RecordFormat<RECORD> recordFormat();

    /** The format is allowed a fixed-size header for metadata, and must specify the size of that header here. */
    int headerSize();

    /** Determine the record size, given access to the raw file on disk. */
    int recordSize( StoreChannel channel ) throws IOException;

    /** Given a completely empty file, do any setup work needed to create the new store. */
    void createStore( StoreChannel channel ) throws IOException;

    interface RecordFormat<RECORD>
    {
        /** A simple name for the record type, such as "NodeRecord" */
        String recordName();

        /** Get the id from a record. */
        long id( RECORD record );

        /** Serialize a full record at the specified offset. */
        void serialize( PageCursor cursor, int offset, RECORD record );

        /** Deserialize a full record at the specified offset. */
        RECORD deserialize( PageCursor cursor, int offset, long id );

        /** Determine if a record at the specified offset is in use. */
        boolean inUse( PageCursor pageCursor, int offset );
    }
}
