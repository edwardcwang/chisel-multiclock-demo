// Tester for the clock divider demo.

package multiclock_demo

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest.simulator.WriteVcdAnnotation

class ClockDividerDemoUnitTester(c: ClockDividerDemoWrapper) {
  // Test for a certain number of cycles.
  val num_cycles = 100
  var cycle = 0

  // Get initial values
  var prev_div4 = c.io.clock_divBy4.peek().litValue
  var div4_last_change_cycle: Option[Int] = None
  var prev_div6 = c.io.clock_divBy6.peek().litValue
  var div6_last_change_cycle: Option[Int] = None

  while (cycle < num_cycles) {
    c.clock.step()
    cycle += 1

    val div4 = c.io.clock_divBy4.peek().litValue
    val div6 = c.io.clock_divBy6.peek().litValue

    // If the divided-by-4 clock changed, check that at least 2 cycles have passed.
    if (div4 != prev_div4) {
      div4_last_change_cycle match {
        // divBy4 should change every 2 cycles
        case Some(last) => assert(cycle - last == 2)
        case None =>
      }
      div4_last_change_cycle = Some(cycle)
    }

    // If the divided-by-6 clock changed, check that at least 3 cycles have passed.
    if (div6 != prev_div6) {
      div6_last_change_cycle match {
        // divBy6 should change every 3 cycles
        case Some(last) =>assert(cycle - last == 3)
        case None =>
      }
      div6_last_change_cycle = Some(cycle)
    }

    prev_div4 = div4
    prev_div6 = div6
  }
}

class ClockDividerDemoTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "ClockDividerDemo"
  it should "divide correctly" in {
    test(new ClockDividerDemoWrapper).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      new ClockDividerDemoUnitTester(dut)
    }
  }
}
