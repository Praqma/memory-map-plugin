package net.praqma.jenkins.usecases;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 * @author bue
 * http://stackoverflow.com/questions/358802/junit-test-with-dynamic-number-of-tests
 */
@RunWith(Parameterized.class)
public class MemoryMapExamplesTester {

    private File file;
    private String teststring = "no used";
    
    // RepoUrl = https://github.com/Praqma/memory-map-examples.git //hardcoded, we don't need to change that
    // branchPattern = "examples/" //all examples to run tests for are on branches matching pattern: examples/*
    
    // Directory for the bare clone of memory-map-examples. A bare clone is used
    // to point the jenkins job to
    // repoCloneDir = 
    
    // We need a local working clone of the repository as well to get things
    // from master and examples branches prior tests and prior the configuration
    // of the Jenkins test job
    // repoWorkDir =
    
    // current branch under test
    // exampleBranch =
    
    
    // Assumptions:
    // * each example branch have run.sh script that can be used to build
    // * each example branch have an expected_results.json that for every build
    //      couting from 1 contain the expected result that the Memory Map Plugin
    //      should find in graphs and data
    // * each example branch have a graph_configuration.json that contain the
    //      relevant information needed to configure the Memory Map Plugin
    //      graphs
    // * all example branches that are relevant to test matches the branch 
    //      pattern 'examples/*'
    
    
    // this method just need the branch name
    public MemoryMapExamplesTester(File file) {
        this.file = file;
    }
    
    @Before
    public void setUp() throws Exception {
        // checking setup is called before?
        teststring = "setup method called alright" + file.getName();
        
        // clone repoUrl to repoCloneDir
        // clone repoCloneDir to repoWorkDir
        
        // note that some things that could normally be done as part of setUp
        // is postponed to the test methods to actually test if a branch 
        // follows our conventions (contain json files etc.) and to give the 
        // user a good error message if not
    }
    
    @Test
    public void memoryMapExamplesTest() throws Exception {  
        
        System.out.println(String.format("file name: %s", file.getName()));
        System.out.println(String.format("teststring: %s", teststring));
        
        // assert that first_%exampleBranch can be checked out in repoWorkdDir
        // by creating deliver_%exampleBranch (that branch will contain the same 
        // content as %examplBranch have in the commit tagged first_%exampleBranch
        // which is our starting point, it will also be the first commit that the
        // jenkins test job below builds
        
        // assert that last_%exampleBranch tag exists (but don't do anything with it
        
        // assert that expectedResults.json exists
        // parse and get json file into java object
        
        // assert that graphConfiguration.json exists
        // parse and get json file into java object
        
        // assert that run.sh exists
        
        // count number of commits between first_%exampleBranch and last_%exampleBranch tags
        // on the origin (repoCloneDir), including both tagged commits
        // commitCount = int
        
        // create a jenkins job as we usually do in our tests with:
        // * branch pattern deliver_%exampleBranch
        // * repo url poiting to the repoCloneDir
        // * Memory Map Plugin configuration matching the graph_configuration.json data
        // * a build step that is execute shell and just contain ./run.sh
        
        
        // currentCommit = is actually the current commit in repoWorkDir which
        // equals both first_%exampleBranch tag, and HEAD of deliver_%exampleBranch
        
        
        // start verifying the example commit by commit:
        
        // counter = 1
        // while (counter <= commitCount)
                // run the created jenkins job (in first iteration it will run on the first_%exampleBranch commit on the deliver%examplbranch
                // assert build result is success
                // print console like I (Bue) do in PIP newest tests
                
                // assert each of the expected results
                        // checkResult(counter, json result data)   // method should probably reside inside this test class... it should use asserts from junit
                        // method throws asserts on:
                                // - if counter entry does not exists in json
                                // - if results doesn't match
        
                // prepare next run
                // currentCommit = first_%exampleBranch tag + counter commits after (going towards the last tag)
        
                // checkout deliver_%exampleBranch in repoWorkDirk
                // cherry_pick currentCommit (it will get new commit sha on deliver_%exampleBranch during this)
                // push to deliver_%exampleBranch the just cherry-picked commit
                // counter + 1
        
        // end while
        
        // ... testing done tearDown method will clean up
        
        
        
        
    }

    // parameters should find all branches matching example pattern
    @Parameters
    public static Collection<Object[]> data() {
        // load the files as you want
        Object[] fileArg1 = new Object[] { new File("/home/bue/github-repos/praqma/memory-map-plugin/README.md") };
        Object[] fileArg2 = new Object[] { new File("/home/bue/github-repos/praqma/memory-map-plugin/pom.xml") };

        Collection<Object[]> data = new ArrayList<Object[]>();
        data.add(fileArg1);
        data.add(fileArg2);
        return data;
    }
    
    @After
    public void tearDown() throws Exception {
         System.out.println(String.format("cleaned up", file.getName()));
    }
}