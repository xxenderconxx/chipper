release
=======

features
--------

* systemc integration -- jim -- convert to scala and doc
* multiple clock domain changes -- jim
* bigger stdlib -- new package and new directory structure -- jb and jim 
* parameters -- adam -- merge and doc
* tagged unions -- jb -- finish up and doc
* typed enums -- jb -- finish up and doc
* complex numbers -- stephen and jb doc
* mem init -- jb and doc
* reg partial init -- jb and doc
* advanced tester for decoupled interfaces -- stephen -- should merge into tester?
* assignment to bits -- doc
* new new tester -- done and doc?
* better float simulation -- optional

speed
-----

* faster c++ compilation using better c++ code emission -- finish up island finder
* improved fpga backend -- deprecate fpga backend just placeholder

bug fixes
---------

* log2Up 1 = 0 -- noisily fail -- invent new operators that work correctly -- names? -- log2Ceil and log2Floor
* zero width handling for powerful generators with less code -- jim fix for ref chip
* correct tester behavior -- done
* correct mem doc -- done

docs
----

* better getting started -- jb
* more advanced tutorial docs -- jb

