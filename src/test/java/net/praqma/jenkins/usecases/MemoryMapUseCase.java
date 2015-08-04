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

import java.io.File;
import java.util.List;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 *
 * @author Mads
 */
public class MemoryMapUseCase {
    
    public static final String PUBLIC_EXAMPLE_URL = "https://github.com/MadsNielsen/memory-map-examples";
    
    @Rule 
    public TestName rule = new TestName();
    
    @Rule
    public MemoryMapUseCaseRule rule2 = new MemoryMapUseCaseRule(PUBLIC_EXAMPLE_URL, this.getClass());
    
    @Test
    @MemoryMapUseCaseAnnotation(useCases = {"arm-linux-gnueabi-gcc-4.7.4"})
    public void testIterator() throws Exception {
        List<MemoryMapCommitListForUseCase> useCases = rule2.getCommitListForTest(rule.getMethodName());
        int commitNumber = 1;
        for(MemoryMapCommitListForUseCase useCase : useCases) {
            //For each use case. We create a new repository. The repository must be bare since we'll be using jenkins to 
            //check out this repository
            //We need to sequentially cherry-pick and commit to this repository for our tests.
            //We use master as the target branch but pick commits from the use case branch
            MemoryMapBranchManipulator manipulator = new MemoryMapBranchManipulator(useCase);
            
            //Create the local bare repository (Which the jenkins job will use)
            File localBare = manipulator.initRepositoryForUseCase();            
                       
            //Create the working repository. Where we will cherry-pick commits one at a time for our tests
            File workdir = manipulator.createWorkRepo(localBare);
            System.out.printf("Created work repository here: %s%n", workdir.getAbsolutePath());
            
            //Cherry pick a sha and transplant a commmit
            ObjectId current;
            while((current = manipulator.cherryPickNextForUseCase()) != null) {
                System.out.printf("Cherry picker #%s %s%n", commitNumber, current.getName());
                
                /*
                    INSERT TESTS HERE: 
                        1. You can get the branch under test using manipulator.getUseCase().getBranchName()
                        2. The repo remote is here: manipulator.getUseCase().getFileRemoteName()
                        3. Profit
                */
                
                commitNumber++;
            }
        }
    }
    
    @Test
    @MemoryMapUseCaseAnnotation(useCases = {"arm-linux-gnueabi-gcc-4.7.4-2222"})
    public void testIterator2() {
        Assert.assertTrue(true);
    }
}
