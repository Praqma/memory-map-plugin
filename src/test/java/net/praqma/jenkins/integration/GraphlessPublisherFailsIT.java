package net.praqma.jenkins.integration;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import net.praqma.jenkins.memorymap.parser.gcc.GccMemoryMapParser;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.UUID;

public class GraphlessPublisherFailsIT {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void failsWithoutGraphs() throws Exception {
        GccMemoryMapParser parser = createParser();
        parser.setMapFile("gcc482.map");
        parser.setConfigurationFile("prom482.ld");

        FreeStyleProject project = TestUtils.createProject(jenkins);
        TestUtils.prepareProjectWorkspace(project, "gcc482.zip");
        TestUtils.setMemoryMapConfiguration(project, parser);

        FreeStyleBuild build = TestUtils.runNewBuild(project);
        TestUtils.printBuildConsoleLog(build, jenkins);
        jenkins.assertBuildStatus(Result.FAILURE, build);
    }

    private GccMemoryMapParser createParser() {
        return new GccMemoryMapParser(UUID.randomUUID().toString(), null, null, 8, true, null, null);
    }
}
