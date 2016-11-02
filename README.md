Memory Map plugin
=================
This readme file mostly contains developer oriented documentation. 
For user oriented documentation, please see the [plugin's Jenkins wiki page](https://wiki.jenkins-ci.org/display/JENKINS/Memory+Map+Plugin)

##Introduction
The Memory Map plugin provides assistance for monitoring the memory map created by a linker. It is especially useful when development is being conducted in conditions where memory is limited, e.g. when developing for an embedded system. 
It allows for displaying memory map values throughout builds in a graph, making it easier to follow up on memory usage.

##References
###Plugin repositories
* [Jenkins CI's repository on GitHub](https://github.com/jenkinsci/memory-map-plugin)
* [Praqma's repository on GitHub](https://github.com/Praqma/memory-map-plugin)
 
###Automated builds
* [Praqma's Memory Map plugin build view](http://code.praqma.net/ci/view/Open%20Source/view/Memory%20Map%20Plugin/)
* [Maven project Memory Map Plugin](https://jenkins.ci.cloudbees.com/job/plugins/job/memory-map-plugin/)

###Roadmap
* [Memory Map plugin's Trello board](https://trello.com/b/eOsTMooO/memory-map-plugin-for-jenkins-ci)

###Wiki and issue tracker
User oriented documentation can be found on the wiki:
* [Memory Map plugin's Jenkins wiki page](https://wiki.jenkins-ci.org/display/JENKINS/Memory+Map+Plugin)

Issues are tracked using the Jenkins JIRA issue tracker:
* [Memory Map on JIRA](https://issues.jenkins-ci.org/browse/JENKINS-29122?jql=project%20%3D%20JENKINS%20AND%20status%20in%20%28Open%2C%20%22In%20Progress%22%2C%20Reopened%29%20AND%20component%20%3D%20%27memory-map-plugin%27)

##Contributing

We happily accept pull requests on [Praqma's Memory Map GitHub repository](https://github.com/Praqma/memory-map-plugin), where we also release from. **Do not make pull requests on the [Jenkins CI GitHub Repository](https://github.com/jenkinsci/pretested-integration-plugin)** - it is only used as final archive for released versions.

* Please reference a JIRA issue in your pull request.
* Please either include tests for your code changes or make sure your changes are covered by existing tests.
* Unless you are contributing a simple bug fix or feature implementation, please consult the  [Memory Map plugin's Trello board](https://trello.com/b/eOsTMooO/memory-map-plugin-for-jenkins-ci) to discuss implementation ideas.
