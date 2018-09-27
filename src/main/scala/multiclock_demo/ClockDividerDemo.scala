// Demo of a clock divider in Chisel.

package multiclock_demo

import chisel3._
import chisel3.experimental.withClockAndReset
import chisel3.experimental.RawModule
import chisel3.util.log2Ceil

object ClockDivider {
  /**
    * Create a clock divider.
    * @param clock_in Clock signal to divide.
    * @param divide_by Factor to divide by (e.g. divide by 2). Must be even.
    * @param reset Optional reset signal.
    * @return Divided clock.
    */
  def apply(clock_in: Clock, divide_by: Int, reset: Option[Bool]): Clock = {
    require(divide_by % 2 == 0, "Must divide by an even factor")

    // Declare some wires for use in this function.
    val output_clock = Wire(Clock())
    val resetWire = Wire(Bool())
    resetWire := reset.getOrElse(false.B)

    withClockAndReset(clock=clock_in, reset=resetWire) {
      // Divide down by n means that every n/2 cycles, we should toggle
      // the new clock.
      val max: Int = divide_by / 2
      // log2Ceil(n) = the # of bits needed to represent n unique things
      val counter = RegInit(0.U(log2Ceil(max).W))
      counter := counter + 1.U // The counter always increments.

      // Every second cycle, toggle the new divided down clock.
      val dividedDownClock = RegInit(false.B)
      when (counter === (max - 1).U) {
        dividedDownClock := ~dividedDownClock
        counter := 0.U
      }

      // Connect the register for the divided down clock to the output IO.
      output_clock := dividedDownClock.asClock
    }
    output_clock
  }
}

// This module does not have an implicit clock and reset since it is a
// RawModule as opposed to a normal Chisel Module.
class ClockDividerDemo extends RawModule {
  val io = IO(new Bundle {
    // Input clock
    val clock = Input(Clock())
    // Reset for the above clock (optional)
    val reset = Input(Bool())
    // Clock divided by 4
    val clock_divBy4 = Output(Clock())
    // Clock divided by 6
    val clock_divBy6 = Output(Clock())
  })

  // Use our ClockDivider function above.
  io.clock_divBy4 := ClockDivider(io.clock, 4, reset = Some(io.reset))
  io.clock_divBy6 := ClockDivider(io.clock, 6, reset = Some(io.reset))
}

// This wrapper module is a regular Chisel module with an implicit
// clock which is driven by the testers.
class ClockDividerDemoWrapper extends Module {
  val io = IO(new Bundle{
    // Clock divided by 4
    val clock_divBy4 = Output(Bool())
    // Clock divided by 6
    val clock_divBy6 = Output(Bool())
  })

  val clock_divider = Module(new ClockDividerDemo)
  clock_divider.io.clock := clock
  clock_divider.io.reset := reset
  // Convert Clocks to Bools for testing.
  io.clock_divBy4 := clock_divider.io.clock_divBy4.asUInt.toBool
  io.clock_divBy6 := clock_divider.io.clock_divBy6.asUInt.toBool
}
