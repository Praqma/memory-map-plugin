memory-map-plugin
=================

A repository for the memory-map-plugin

- Invalid builds are not considered when drawing a graph, so builds with configuration errors will not be included
- Max value markers are drawn on project basis, and are not stored as part of build info
- The plugin needs 2 files available to function, the first one is the linker command file, the second one is the actual .map file. The linker command file takes precedence
- The plugin reports values in either kb or kWords. Word size is configurable.
