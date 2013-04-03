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
package org.neo4j.kernel.impl.skip;

/**
 * Storage operations used by implementers of {@link SkipListOperations}
 *
 */
public interface SkipListCabinet<R, K, V>
{
    void acquire();
    void release();

    boolean isOpen();

    void assertOpen();

    int getMaxHeight();

    int getMaxLevel( R record );

    R[] newVisitationArray();

    R nil();
    R getHead();

    boolean isNil(R r);
    boolean isHead(R r);

    int newRandomLevel();

    R createRecordWithHeight( int height, K key, V data );

    void removeRecord( R record );

    boolean areSameRecord( R first, R second );

    K getRecordKey(R record);

    V getRecordValue( R record );

    int getHeight(R record);

    R getNext( R record, int i);
    void setNext( R record, int i, R newNext );

    public void close();

    public void delete();
}
