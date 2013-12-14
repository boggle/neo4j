package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.sched

import org.scalatest.{Matchers, BeforeAndAfter, FunSuite}
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor
import org.mockito.Mockito
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.{Router, Activation, Event}

class DefaultSchedulerTest extends FunSuite with Matchers with BeforeAndAfter {

  test("execute simple activation") {
    // given
    val cursor1 = Mockito.mock(classOf[TestCursor])
    val cursor2 = Mockito.mock(classOf[TestCursor])
    val scheduler = new DefaultScheduler[TestCursor]()

    val activation = new Activation[TestCursor]() {
      def activate(router: Router[TestCursor], event: Event[TestCursor]) {
        router.submit(Event.outputFrom("A", cursor2))
      }
    }

    // when
    scheduler.register("A", activation)
    scheduler.submit(Event.inputTo("A", cursor1))
    val result = scheduler.execute()

    // then
    result.cursor() should be(cursor2)
  }

  abstract class TestCursor extends Cursor[TestCursor]
}

