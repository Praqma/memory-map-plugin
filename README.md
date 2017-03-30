---
Maintainer:
- buep
---

# Memory Map plugin

This readme file mostly contains developer oriented documentation.

For user oriented documentation, please see the [plugin's Jenkins wiki page](https://wiki.jenkins-ci.org/display/JENKINS/Memory+Map+Plugin)

## Introduction
The Memory Map plugin provides assistance for monitoring the memory map created by a linker. It is especially useful when development is being conducted in conditions where memory is limited, e.g. when developing for an embedded system.
It allows for displaying memory map values throughout builds in a graph, making it easier to follow up on memory usage.

## References

### Plugin repositories
* [Jenkins CI's repository on GitHub](https://github.com/jenkinsci/memory-map-plugin)
* [Praqma's repository on GitHub](https://github.com/Praqma/memory-map-plugin)

### Automated builds
* [Praqma's Memory Map plugin build view](http://code.praqma.net/ci/view/Open%20Source/view/Memory%20Map%20Plugin/)
* [Maven project Memory Map Plugin](https://jenkins.ci.cloudbees.com/job/plugins/job/memory-map-plugin/)

### Roadmap

See [docs/roadmap.md](docs/roadmap.md)-file.

### Wiki and issue tracker

User oriented documentation can be found on the wiki:
* [Memory Map plugin's Jenkins wiki page](https://wiki.jenkins-ci.org/display/JENKINS/Memory+Map+Plugin)

We are not using the Jenkins CI community issue tracker any more. All issues are tracked using github issues.

## Contributing

We happily accept pull requests on [Praqma's Memory Map GitHub repository](https://github.com/Praqma/memory-map-plugin), where we also release from. **Do not make pull requests on the [Jenkins CI GitHub Repository](https://github.com/jenkinsci/pretested-integration-plugin)** - it is only used as final archive for released versions.

* Please reference a JIRA issue in your pull request.
* Please either include tests for your code changes or make sure your changes are covered by existing tests.
* Unless you are contributing a simple bug fix or feature implementation, please consult the  [Memory Map plugin's Trello board](https://trello.com/b/eOsTMooO/memory-map-plugin-for-jenkins-ci) to discuss implementation ideas.

### Developer details

See some of our initial research on compiler and linkers, and their memory map output, and get a grasp about the idea behind it all. Read the [research notes in the docs folder](docs/ResearchNotes.md).

See also the brief architecture illustration in [docs/architecture.md](docs/architecture.md).
