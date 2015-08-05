/*
 * The MIT License
 *
 * Copyright 2015 Mads.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.praqma.jenkins.usecases;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.tasks.BatchFile;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.praqma.jenkins.integration.TestUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.jvnet.hudson.test.JenkinsRule;

/**
 *
 * @author Mads
 */
public class MemoryMapUseCase {
    
    public static final String PUBLIC_EXAMPLE_URL = "https://github.com/Praqma/memory-map-examples";
    
    @Rule 
    public TestName nameRule = new TestName();
    
    @Rule
    public MemoryMapUseCaseRule useCaseRule = new MemoryMapUseCaseRule(PUBLIC_EXAMPLE_URL, this.getClass());
    
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    
    @Test
    @MemoryMapUseCaseAnnotation(useCases = {"arm-none-eabi-gcc_4.8.4_hello_world"})
    public void testIterator() throws Exception {
        List<MemoryMapCommitListForUseCase> useCases = useCaseRule.getCommitListForTest(nameRule.getMethodName());
        int commitNumber = 1;
        for(MemoryMapCommitListForUseCase useCase : useCases) {
            //For each use case. We create a new repository. The repository must be bare since we'll be using jenkins to 
            //check out this repository
            //We need to sequentially cherry-pick and commit to this repository for our tests.
            //We use master as the target branch but pick commits from the use case branch
            MemoryMapBranchManipulator manipulator = new MemoryMapBranchManipulator(useCase);
            
            FreeStyleProject project = TestUtils.createProject(jenkinsRule);
            project = TestUtils.configureGit(project, manipulator.getUseCase().getDeliverBranchName(), manipulator.getUseCase().getFileRemoteName());
            project.getBuildersList().add(new BatchFile("git branch -a"));
            project.getBuildersList().add(new BatchFile("git log -n1"));
            project.save();
            
            
            
            
                    
            //Create the local bare repository (Which the jenkins job will use)
            File localBare = manipulator.initRepositoryForUseCase();            
                       
            //Create the working repository. Where we will cherry-pick commits one at a time for our tests
            File workdir = manipulator.createWorkRepo(localBare);
            System.out.printf("Created work repository here: %s%n", workdir.getAbsolutePath());
            
            //Cherry pick a sha and transplant a commmit
            ObjectId current;
            
            //Extract json from file
            File expectedResults = new File(MemoryMapUseCase.class.getClassLoader().getResource("result_expect.json").getFile());
            String json = FileUtils.readFileToString(expectedResults);
            MemoryMapBuildResultValidator validator = new MemoryMapBuildResultValidator();
            validator.expectResults(json);
            
            
            while((current = manipulator.cherryPickNextForUseCase()) != null) {
                System.out.printf("%sCherry picker #%s %s%n",MemoryMapCommitListForUseCase.PREFIX, commitNumber, current.getName());
                AbstractBuild<?,?> build = project.scheduleBuild2(0, new Cause.UserIdCause()).get(60, TimeUnit.SECONDS);
                System.out.printf("%sBuild %s finished with status %s using sha %s%n", MemoryMapCommitListForUseCase.PREFIX, build.number, build.getResult(), current.getName());                
                validator.validateBuild(build).validate();                
                commitNumber++;
            }
        }
    }
}
