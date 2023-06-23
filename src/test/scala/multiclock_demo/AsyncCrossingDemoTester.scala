// Tester for the clock divider demo.

package multiclock_demo

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest.simulator.WriteVcdAnnotation

class AsyncCrossingDemoUnitTester(c: AsyncCrossingDemo) {
  // Recall: 10000MHz simulation clock, 33MHz RX clock, 172MHz TX clock
  // 303 simulation cycles per RX clock
  // AsyncCrossingDemoUtil.values.length*33 cycles
  c.clock.setTimeout((AsyncCrossingDemoUtil.values.length + 1) * 303 * 10)

  // Basically step through until the RX has read out all the values.
  var next_index: Int = 0
  private var prev_value = c.io.value_out.peek()
  while (next_index < (AsyncCrossingDemoUtil.values.length)) {
    c.clock.step()
    val value = c.io.value_out.peek()
    if (value.litValue != prev_value.litValue) {
      assert(value.litValue == AsyncCrossingDemoUtil.values(next_index))
      next_index += 1
    }
    prev_value = value
  }
}

class AsyncCrossingDemoTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "AsyncCrossingDemo"
  it should "divide correctly" in {
    test (new AsyncCrossingDemo).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      new AsyncCrossingDemoUnitTester(dut)
    }
  }
}
