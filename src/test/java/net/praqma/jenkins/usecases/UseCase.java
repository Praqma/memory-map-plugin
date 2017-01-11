package net.praqma.jenkins.usecases;

import com.google.gson.JsonSyntaxException;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.Shell;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import net.praqma.jenkins.integration.TestUtils;
import net.praqma.jenkins.memorymap.MemoryMapRecorder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.lib.ObjectId;
import static org.hamcrest.CoreMatchers.is;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jvnet.hudson.test.JenkinsRule;

@RunWith(Parameterized.class)
public class UseCase {

    static final String PUBLIC_EXAMPLE_URL = "https://github.com/Praqma/memory-map-examples";
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
        FreeStyleProject project = TestUtils.createProject(jenkinsRule, false);
        project = TestUtils.configureGit(project, manipulator.getUseCase().getDeliverBranch(), manipulator.getUseCase().getFileRemoteName());
        project.getPublishersList().add(getMemoryMapRecorder());
        project.getBuildersList().add(new Shell("env BN=" + useCase + " sh run.sh"));
        project.save();

        //Configuration of the validator
        File expectedResults = new File(useCaseRule.getUseCaseDir(useCase), "expectedResult.json");
        String resultsJson = FileUtils.readFileToString(expectedResults);
        BuildResultValidator validator = new BuildResultValidator();
        validator.expect(resultsJson);

        //Cherry pick a sha and transplant a commit
        ObjectId current;
        int commitNumber = 1;
        while ((current = manipulator.nextCommit()) != null) {
            System.out.printf("%sCherry picked #%s %s%n", UseCaseCommits.PREFIX, commitNumber, current.getName());
            AbstractBuild<?, ?> build = project.scheduleBuild2(0, new Cause.UserIdCause()).get();
            Assert.assertThat(build.getResult(), is(Result.SUCCESS));
            System.out.printf("%sBuild %s finished with status %s using sha %s%n", UseCaseCommits.PREFIX, build.number, build.getResult(), current.getName());
            validator.forBuild(build).validate();
            commitNumber++;
        }
    }

    @Test
    public void testUseCase_pipelines() throws Exception {
        System.out.printf("%sUse case: %s%n", UseCaseCommits.PREFIX, useCase);

        UseCaseCommits commits = new UseCaseCommits(useCase, useCaseRule.getRepository());
        BranchManipulator manipulator = new BranchManipulator(commits);

        InputStream fis = UseCase.class.getResourceAsStream("pipeScript.txt");
        String s = IOUtils.toString(fis).replace("$deliverBranch", manipulator.getUseCase().getDeliverBranch()).replace("$deliverUrl",manipulator.getUseCase().getFileRemoteName());
                
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "p");
        CpsFlowDefinition flowDef = new CpsFlowDefinition(s, true);
        job.setDefinition(flowDef);
       
        System.out.println("----------------flow def script ---------------");
        System.out.println(flowDef.getScript());
        System.out.println("-------------------------------------------------");
        
        //Configure the validator
        File expectedResults = new File(useCaseRule.getUseCaseDir(useCase), "expectedResult.json");
        String resultsJson = FileUtils.readFileToString(expectedResults);
        BuildResultValidator validator = new BuildResultValidator();
        validator.expect(resultsJson);
        
        ObjectId current;
        int commitNumber = 1;
        while ((current = manipulator.nextCommit()) != null) {
            System.out.printf("%sCherry picked #%s %s%n", UseCaseCommits.PREFIX, commitNumber, current.getName());
            WorkflowRun b = job.scheduleBuild2(0).get();
            System.out.println("++++++++++ LOG +++++++++++++++++");
            System.out.println(b.getLog());
            System.out.println("++++++++++++++++++++++++++++++++");
            Assert.assertThat(b.getResult(), is(Result.SUCCESS));
            System.out.printf("%sBuild %s finished with status %s using sha %s%n", UseCaseCommits.PREFIX, b.number, b.getResult(), current.getName());
            validator.forBuild(b).validate();
            commitNumber++;
        }
    }


    private MemoryMapRecorder getMemoryMapRecorder() throws JsonSyntaxException, IOException {
        File recorderConfigFile = new File(useCaseRule.getUseCaseDir(useCase), "graphConfiguration.json");
        return JsonParser.jackson.readValue(recorderConfigFile, MemoryMapRecorder.class);
    }
}
