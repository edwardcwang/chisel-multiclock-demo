// Demo of an asynchronous FIFO in Chisel (using the rocket-chip AsyncQueue).
package multiclock_demo

import chisel3._
import chisel3.experimental.withClockAndReset
import chisel3.experimental.RawModule
import chisel3.util.{Counter, Decoupled, MuxLookup}
import freechips.asyncqueue.{AsyncQueue, AsyncQueueParams}

object AsyncCrossingDemoUtil {
  /**
    * Values to send across the AsyncQueue.
    *
    * @return 1,3,5,...,15,0,2,4,...,14
    */
  def values: Seq[Int] =
    ((0 to 7) map ((v: Int) => (v * 2) + 1)) ++ ((0 to 7) map (_ * 2))
}

// Transmitting module
class TransmitModule extends Module {
  val io = IO(new Bundle {
    val data_out = Decoupled(UInt(4.W))
  })

  io.data_out.valid := false.B
  io.data_out.bits := DontCare

  // Transmit each value until we are out of values, then stop
  // transmitting values.
  val counter = Counter(AsyncCrossingDemoUtil.values.length + 1)
  when (counter.value < AsyncCrossingDemoUtil.values.length.U) {
    // Enqueue the current value
    io.data_out.enq(
      MuxLookup(counter.value, 0.U,
        AsyncCrossingDemoUtil.values.zipWithIndex.map {
          case (value: Int, index: Int) => (index.U, value.U)
    }))

    // Advance to the next item when data is being transferred on this cycle.
    when (io.data_out.fire()) {
      counter.inc()
    }
  }


}

// Receiving module
class ReceiveModule extends Module {
  val io = IO(new Bundle {
    val data_in = Flipped(Decoupled(UInt(4.W)))
    val value_out = Output(UInt(4.W))
  })
  // RX is always ready
  io.data_in.ready := true.B
  // When data is transmitted, update output.
  val output = RegInit(0.U)
  io.value_out := output
  when (io.data_in.fire()) {
    output := io.data_in.bits
  }
}

class AsyncCrossingDemo extends Module {
  val io = IO(new Bundle {
    // Output values received
    val value_out = Output(UInt(4.W))
  })

  // 10000MHz simulation clock
  // Divide by 58 -> 172MHz TX clock
  // Divide by 302 -> 33MHz RX clock
  val tx_clock = ClockDivider(clock, 58)
  val rx_clock = ClockDivider(clock, 302)

  // An alternative way to use withClockAndReset
  val tx = withClockAndReset(clock = tx_clock, reset = reset) { Module(new TransmitModule) }
  val rx = withClockAndReset(clock = rx_clock, reset = reset) { Module(new ReceiveModule) }

  // Pull the output.
  io.value_out := rx.io.value_out

  val async_crossing = Module(new AsyncQueue(UInt(4.W), AsyncQueueParams.singleton()))
  async_crossing.io.enq_clock := tx_clock
  async_crossing.io.enq <> tx.io.data_out
  async_crossing.io.deq_clock := rx_clock
  async_crossing.io.deq <> rx.io.data_in
  // We can get away without the resets apparently
  async_crossing.io.enq_reset := DontCare
  async_crossing.io.deq_reset := DontCare
}
