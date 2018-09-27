Chisel Multiclock Demos
=======================

This repo has some examples of multi-clock support in Chisel 3.

* [Clock divider demo](src/main/scala/multiclock_demo/ClockDividerDemo.scala) ([tester](src/test/scala/multiclock_demo/AsyncCrossingDemoTester.scala))
* [Asynchronous crossing demo](src/main/scala/multiclock_demo/AsyncCrossingDemo.scala) ([tester](src/test/scala/multiclock_demo/ClockDividerDemoTester.scala))

Run these demos with `sbt test`. You can browse the generated waveforms in VCD format in `test_run_dir/`.

(As a side effect, this repo also has a small example of subprojects in sbt in [build.sbt](build.sbt).)
