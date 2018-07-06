multibranchPipelineJob("Memory Map Jenkins Plugin") {
    branchSources {
        git {
            credentialsId("github")
            remote("https://github.com/Praqma/memory-map-plugin.git")
        }

        triggers {
            periodic(20)
        }
    }

    configure {
        it / 'sources' / 'data' / 'jenkins.branch.BranchSource' / 'source' / 'traits' << 'jenkins.plugins.git.traits.CloneOptionTrait' {
            extension(class: 'hudson.plugins.git.extensions.impl.CloneOption') {
                shallow(false)
                noTag(false)
                reference()
                depth(0)
                honorRefspec(false)
            }
        }
    }
}