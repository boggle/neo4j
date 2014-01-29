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
package org.neo4j.cypher.internal.compiler.v2_1.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HashJoinOp implements Operator {
    private final StatementContext ctx;
    private final EntityRegister joinRegister;
    private final Registers lhsTail;
    private final Operator lhs;
    private final Operator rhs;
    private final Map<Long, ArrayList<RegisterSnapshot>> bucket = new HashMap<>();
    private int bucketPos = 0;
    private ArrayList<RegisterSnapshot> currentBucketEntry = null;

    public HashJoinOp(StatementContext ctx,
                      EntityRegister joinNode,
                      Registers lhsTail,
                      Operator lhs,
                      Operator rhs)
    {
        this.ctx = ctx;
        this.joinRegister = joinNode;
        this.lhsTail = lhsTail;

        this.lhs = lhs;
        this.rhs = rhs;

        fillHashBucket();
    }

    @Override
    public void open() {
        lhs.open();
        rhs.open();
    }

    @Override
    public boolean next() {
        while (currentBucketEntry == null || bucketPos >= currentBucketEntry.size())
        {
            // If we've emptied our rhs, we're done here
            if (!rhs.next()) {
                return false;
            }

            // let's see if we find a match
            produceMatchIfPossible();
        }

        // We've found a match! Let's copy the data over.
        restoreFromTailEntry();

        return true;
    }

    private void produceMatchIfPossible() {
        long key = joinRegister.getEntity();
        currentBucketEntry = bucket.get(key);
        bucketPos = 0;
    }

    @Override
    public void close() {
        rhs.close();
        lhs.close();
    }

    private void fillHashBucket()
    {
        while (lhs.next())
        {
            long key = joinRegister.getEntity();
            ArrayList<RegisterSnapshot> objects = getTailEntriesForId(key);
            RegisterSnapshot tailEntry = copyToTailEntry();
            objects.add(tailEntry);
        }
    }

    private ArrayList<RegisterSnapshot> getTailEntriesForId(long key)
    {
        ArrayList<RegisterSnapshot> objects = bucket.get(key);
        if (objects == null) {
            objects = new ArrayList<>();
            bucket.put(key, objects);
        }
        return objects;
    }

    private void restoreFromTailEntry() {
        int idx = bucketPos++;
        RegisterSnapshot from = currentBucketEntry.get(idx);
        from.restore( lhsTail );
    }

    private RegisterSnapshot copyToTailEntry()
    {
        return new RegisterSnapshot( lhsTail );
    }
}
