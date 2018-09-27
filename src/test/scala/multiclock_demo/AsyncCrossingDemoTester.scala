// Tester for the clock divider demo.

package multiclock_demo

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, PeekPokeTester}

class AsyncCrossingDemoUnitTester(c: AsyncCrossingDemo) extends PeekPokeTester(c) {
  // Recall: 10000MHz simulation clock, 33MHz RX clock, 172MHz TX clock
  // 303 simulation cycles per RX clock
  // AsyncCrossingDemoUtil.values.length*33 cycles
  //step((AsyncCrossingDemoUtil.values.length + 1) * 303 * 10)

  // Basically step through until the RX has read out all the values.
  var next_index: Int = 0
  private var prev_value = peek(c.io.value_out)
  while (next_index < AsyncCrossingDemoUtil.values.length) {
    step(1)
    val value = peek(c.io.value_out)
    if (value != prev_value) {
      assert(value == AsyncCrossingDemoUtil.values(next_index))
      next_index += 1
    }
    prev_value = value
  }
}

class AsyncCrossingDemoTester extends ChiselFlatSpec {
  private val backendNames = Array("verilator")
  for ( backendName <- backendNames ) {
    "AsyncCrossingDemo" should s"divide correctly (with $backendName)" in {
      iotesters.Driver.execute(Array("--fint-write-vcd", "--backend-name", backendName), () => new AsyncCrossingDemo) {
        c => new AsyncCrossingDemoUnitTester(c)
      } should be(true)
    }
  }
}
