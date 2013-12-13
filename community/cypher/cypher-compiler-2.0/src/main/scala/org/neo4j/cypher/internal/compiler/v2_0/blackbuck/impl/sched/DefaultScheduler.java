package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.Scheduler;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

public abstract class DefaultScheduler<C extends Cursor<C>> implements Scheduler<C>
{
}
