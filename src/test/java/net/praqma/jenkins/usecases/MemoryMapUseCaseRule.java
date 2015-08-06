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
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestName;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 *
 * @author Mads
 */
public class MemoryMapUseCaseRule extends ExternalResource {

    private String url;
    private static File bareRepo;
    
    public MemoryMapUseCaseRule(String url) {
        this.url = url;
    }
 
    public File getBareRepo() {
        return MemoryMapUseCaseRule.bareRepo;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        try {
            File f = new File(FileUtils.getTempDirectory(),"/"+System.currentTimeMillis()+".git");
            if(bareRepo != null && MemoryMapUseCaseRule.bareRepo.exists()) {
                //If exits. Update
                Git.open(bareRepo).fetch().call();
                System.out.println("Repo for tests exists. Reuse but update: "+bareRepo.getAbsolutePath());
            } else {
                f.mkdir();
                bareRepo = f;
                Git.cloneRepository().setURI(url).setBare(true).setCloneAllBranches(true).setDirectory(bareRepo).call().close();
                System.out.println("Created repo in: "+f.getAbsolutePath());
            }
            return super.apply(base, description); //To change body of generated methods, choose Tools | Templates.
        } catch (Exception ex) {
            throw new RuntimeException("Failed to clone", ex);
        } 
    }

}
