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
package org.neo4j.kernel.impl.api.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.neo4j.helpers.collection.IteratorUtil.asSet;

import org.junit.Test;

public class LabelRuleRepositoryTest
{
    @Test
    public void shouldIncludeLabelRuleAfterItsBeenAdded() {
        // Given
        LabelRuleRepository repo = new LabelRuleRepository();

        // When
        repo.add( new LabelRule( 1, 10, new long[] { 100l } ) );

        // Then
        assertThat( repo.getDirectlyImpliedLabels( new long[] { 10l } ), equalTo(asSet( 100l )));

    }

    @Test
    public void shouldInferImpliedLabels() {
        // Given
        LabelRuleRepository repo = new LabelRuleRepository();

        // When
        repo.add( new LabelRule( 1, 10, new long[] { 100l } ) );
        repo.add( new LabelRule( 1, 100, new long[] { 200l } ) );
        repo.add( new LabelRule( 1, 200, new long[] { 300l, 400l } ) );

        // Then
        assertThat(
                repo.getTransitivelyImpliedLabels( new long[] { 10l } ),
                equalTo( asSet( 100l, 200l, 300l, 400l ) ) );

    }
}
