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

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.neo4j.kernel.DefaultIdGeneratorFactory;
import org.neo4j.kernel.DefaultTxHook;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.test.EphemeralFileSystemRule;

import static org.junit.Assert.assertEquals;

import static org.neo4j.helpers.collection.MapUtil.stringMap;
import static org.neo4j.kernel.impl.util.StringLogger.DEV_NULL;

public class LabelStatisticsStoreTest
{
    private Config config;
    private LabelStatisticsStore store;

    @Rule
    public EphemeralFileSystemRule fs = new EphemeralFileSystemRule();

    private StoreFactory storeFactory;

    @Test
    public void shouldStoreRecords() throws Exception
    {
        store.setHighId( 100 );
        store.updateRecord( new LabelStatisticsRecord( 1, 100 ) );
    }

    @Test
    public void shouldStoreAndLoadRecord() throws Exception
    {
        store.setHighId( 100 );
        store.updateRecord( new LabelStatisticsRecord( 1, 100 ) );
        assertEquals( 100, store.forceGetRecord( 1 ).getCount() );
    }

    @Test
    public void shouldLoadEmptyRecord() throws Exception
    {
        assertEquals( 0, store.forceGetRecord( 100 ).getCount() );
    }

    @Before
    public void before() throws Exception
    {
        config = new Config( stringMap() );
        DefaultIdGeneratorFactory idGeneratorFactory = new DefaultIdGeneratorFactory();
        DefaultWindowPoolFactory windowPoolFactory = new DefaultWindowPoolFactory();
        storeFactory = new StoreFactory( config, idGeneratorFactory, windowPoolFactory, fs.get(), DEV_NULL,
                new DefaultTxHook() );
        File file = new File( "label-statistics-store" );

        storeFactory.createLabelStatisticsStore( file );
        store = storeFactory.newLabelStatisticsStore( file );
    }

    @After
    public void after() throws Exception
    {
        store.close();
    }
}
