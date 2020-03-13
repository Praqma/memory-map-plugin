# Architecture

This is a very short write-up of the current and future architecture.

## Current

The drawing shows the implementation is currently wrapped in a Jenkins plugin, and there are Jenkins specific implementation details, though the core functionality is basically separated into logical areas within the usual Jenkins plugin architecture.
![Current Jenkins plugin architecture](drawings/MemoryMapPlugin-architecture.png)

Consider the illustration as an idea on there are some common generic parts in there that should be pulled out as these are not Jenkins specific and could very well be re-used in other projects.

## Future architecture


![Example of separation in a core memory map utility library](drawings/MemoryMapUtility-architecture.png)

In a future architecture we image first to separate functionality out of Jenkins, but even more splitting the MemoryMap plugin into a Memory Map utility with a core and some modules for outputting analysis result, as well as implementing the actual parsing of memory map output files from specific linker/compilers.

* If the core implements a lexer, that can be automatically created based on a grammar it is easy to support new compilers by just adjusting or implementing new grammars.
* There should be a graphing tool, so when used without Jenkins it can still show visual results.
* Output should be separated so it can easy be extended to support new output formats.
* There should exist a generic memory map data model, that is intermediate representation of all data from memory maps.
* There should be interfaces to interact with the memory map utility from programming languages and command line to make it usable in a Jenkins plugin, as well as maybe in an IDE as a plugin there.

Also read the [road-map](roadmap.md) about future plan.
