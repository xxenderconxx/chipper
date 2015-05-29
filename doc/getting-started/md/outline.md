installation
============

* development tool installation
* setting up the tutorial

basics
======

* the chisel directory structure
* running your first chisel build
* combinational logic
* registers

basic types and operations
==========================

* chisel assignments and reassignments
* the chisel uint class
* the chisel bool class
* casting between types

modules
=======

* module instantiation
* the vec class
* parameterization
* built in primitives

writing scala testbenches
=========================

* the scala testbench
* an example
* debug output
* general testbench
* limitations of the testbench

creating your own project
=========================

* directory structure
* chisel main
* build.sbt template
* compiling chisel source
* running the chisel tests
* compiling verilog
* putting it all together

conditional assignments and memories
====================================

* conditional register updates
* combinational conditional assignments
* read-only memories
* read-write memories

debugging
=========

* strings and printing
    + constructing strings
    + sprintf, printf
* assert
    + simple form
    + message
* visualization
    + producing with dot backend
    + viewing with vizgraph
* vcd dumps 
    + producing with chisel tester
    + viewing with waveform viewer
* debug api
    + peek, poke, step
    + snapshot

scripting hardware generation
=============================

* using the for loop
* using if, else if, else
* using def

GETTING-GOING-TUTORIAL
======================

advanced modules -- from tutorial
================

* bulk connections
    + basic connections
    + partial interfaces
* blackboxes
    + changing names
    + adding clocks

advanced mems
=============

* mems vs vecs -- vecs can be used for wires
    + mem init -- implement this
    + vec init -- arbitrary elt by elt init
* sequential mems -- from tutorial
    + reg
    + isSeq
* structural mems -- figure this out from various options
    + num ports
    + accessing ports

advanced combinational 
======================

* math 
    + log2up / log2down / isPow2
* bits 
    + PopCount
    + Reverse
    + FillInterleaved
* random 
    + LFSR16 -- parameterized
    + big random
* arithmetic 
    + multiplier / divider
    + cordic ???
* selection
    + PriorityMux
    + one hot encodings -- PriorityEncoderOH, Mux1H
    + decode -- sparse lookup
* examples
    + processor

advanced sequential
===================

* conditional update rules
    + order of updates
* state machines
    + state variables
* reg forms
    + RegEnable, RegNext
    + RegReset
    + partial resets
* counters 
    + simple counter
    + wide counter
* delaying
    + shift register -- 
    + Latch -- RegEnable
    + Delays -- add this
* examples
    + FIR filter

advanced scripting
==================

* getWidth or none
    + rules 
* functional 
    + map, zip 
    + foldLeft, foldRight
* data structures
    + arrayBuffers
    + maps and sets
* examples
    + FIR filter parameterized

vec uses 
========

* creation 
    + fill 
    + tabulate
* functional 
    + map 
    + reduce 
    + forall, exists, contains, count, indexWhere, lastIndexWhere, onlyIndexWhere
* bitvec 
    + andR, orR
    + assignments
* examples
    + cams
    + btb
    + boom examples

advanced types
==============

* conversion
    + fill 
    + toBits, fromBits
* typed enums
    + defining
* tagged unions
    + defining
    + debugging
* str
    + defining
* type parameterization
    + parameterized functions
    + parameterized classes
* defining your own types 
    + subclassing bits
    + num trait
    + subclassing bundle -- complex

backends
========

c++ backend
-----------

* c-run-time
    + clock_lo
    + clock_hi
* chiselMain/Test options

verilog backend
---------------

* c-run-time
    + clock_lo
    + clock_hi
* chiselMain/Test options

dsp uses
========

* sint
    + operations
    + width inference
    + conversion
* sfix/ufix 
    + operations
    + width inference
* flo/dbl 
    + operations
    + simulation -- backends
* complex
    + operations
* examples
    + FIR 
    + CORDIC

decoupled
=========

* pipe
    + pipeio
    + pipe
* how to do rv signals 
    + no combinational loops 
    + fire
* queue
    + simple
    + advanced
* arbiters 
    + simple
    + locking 
    + rr
* yunsup example
* decoupled gcd
* split and join
* crossbar example

testing
=======

* types
    + signed numbers
    + vecs
    + tagged unions
* decoupled
    + wait -- advanced tester
    + takestep(s), until, eventually, do_until, assert
    + connecting to/from scala2 queues
* advanced
    + fuzzer
    + generative
* debug api 
    + direct calls
* examples
    + decouple testing

parameters
==========

* declaring
* threading through modules
* constraints
* providing values
* producing design space

SUPER ADVANCED TUTORIAL
=======================

jackhammer
==========

* sampling
* results
* reports
* cluster version

rocc accelerators
=================

* risc-v architecture
* accelerators
* spike
* testing with adv tester

asic flow
=========

* srams with cacti etc
* how to connect to cad tools
* backannotation

fpga flow
=========

* block rams
* how to connect to cad tools
* clocking
* zynq specifics such as axi

configuration
=============

* project file
* submodule

multiple clock domains
======================

* creating clock domains
* cross clock domains
* backend specifics
* realistic examples

intermediate representation
===========================

* api 
* file format

more material
=============

* have chisel generated c++ be spike compatible

scala
=====

* what it is
* bindings
* collections
* maps and sets
* iteration
* functions
* functional
* object orientation
* singleton objects

what is chisel?
===============

* library to language
