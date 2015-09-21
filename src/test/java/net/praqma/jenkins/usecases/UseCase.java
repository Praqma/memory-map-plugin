package net.praqma.jenkins.usecases;

import com.google.gson.JsonSyntaxException;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.Shell;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import net.praqma.jenkins.integration.TestUtils;
import net.praqma.jenkins.memorymap.MemoryMapRecorder;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jvnet.hudson.test.JenkinsRule;

@RunWith(Parameterized.class)
public class UseCase {

    static final String PUBLIC_EXAMPLE_URL = "https://github.com/praqma/memory-map-examples";
    final String useCase;

    @Rule
    public UseCaseRule useCaseRule = new UseCaseRule(PUBLIC_EXAMPLE_URL);

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"arm-none-eabi-gcc_4.8.4_hello_world"}
        });
    }

    public UseCase(String useCase) {
        this.useCase = useCase;
    }

    @Test
    public void testUseCase() throws Exception {
        System.out.printf("%sUse case: %s%n", UseCaseCommits.PREFIX, useCase);

        UseCaseCommits commits = new UseCaseCommits(useCase, useCaseRule.getRepository());
        BranchManipulator manipulator = new BranchManipulator(commits);

        //Configuration of the jenkins job
        FreeStyleProject project = TestUtils.createProject(jenkinsRule);
        project = TestUtils.configureGit(project, manipulator.getUseCase().getDeliverBranch(), manipulator.getUseCase().getFileRemoteName());
        project.getPublishersList().add(getMemoryMapRecorder());
        project.getBuildersList().add(new Shell("env BN=" + useCase + " sh run.sh"));
        project.save();

        //Configuration of the validator
        File expectedResults = new File(useCaseRule.getUseCaseDir(useCase), "expectedResult.json");
        String resultsJson = FileUtils.readFileToString(expectedResults);
        BuildResultValidator validator = new BuildResultValidator();
        validator.expect(resultsJson);

        //Cherry pick a sha and transplant a commmit
        ObjectId current;
        int commitNumber = 1;
        while ((current = manipulator.nextCommit()) != null) {
            System.out.printf("%sCherry picked #%s %s%n", UseCaseCommits.PREFIX, commitNumber, current.getName());
            AbstractBuild<?, ?> build = project.scheduleBuild2(0, new Cause.UserIdCause()).get(240, TimeUnit.SECONDS);
            assert build.getResult() == Result.SUCCESS;
            System.out.printf("%sBuild %s finished with status %s using sha %s%n", UseCaseCommits.PREFIX, build.number, build.getResult(), current.getName());
            validator.forBuild(build).validate();
            commitNumber++;
        }
    }

    private MemoryMapRecorder getMemoryMapRecorder() throws JsonSyntaxException, IOException {
        File recorderConfigFile = new File(useCaseRule.getUseCaseDir(useCase), "graphConfiguration.json");
        MemoryMapRecorder recorder = JsonParser.jackson.readValue(recorderConfigFile, MemoryMapRecorder.class);
        return recorder;
    }
}
