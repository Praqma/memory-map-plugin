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
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.StoredConfig;

/**
 *
 * @author Mads
 */
public class MemoryMapBranchManipulator {
    
    private MemoryMapCommitListForUseCase useCase;
    private File workDir;
    
    //Create the repository for this use case. We create a bare repo and commit push each commit as required. 
    //This is the bare repository that the jenkins project will poll on.
    public File initRepositoryForUseCase() throws IOException, GitAPIException {
        
        File dir = new File(FileUtils.getTempDirectory(),"/"+useCase.getBranchName()+".git");
        if(dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        dir.mkdir();
        
        Git.init().setBare(true).setDirectory(dir).call().close();
        return dir;
    }
    
    //Create the repository where we can work and cherry pick our commits
    //When calling useCase.getBareRepo() we have the local master copy of the projects directory
    public File createWorkRepo(File bareRepo) throws IOException, GitAPIException {
        File dir = new File(FileUtils.getTempDirectory(),"/"+useCase.getBranchName());
        if(dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        dir.mkdirs();

        Git.init().setDirectory(dir).call().close();
        
        StoredConfig config = Git.open(dir).getRepository().getConfig();
        
        String fileRemoteName = "file://"+useCase.getBareRepo().getAbsolutePath();
        
        config.setString("remote", "origin", "url", fileRemoteName);
        config.setString("remote", "origin" , "fetch", "+refs/heads/*:refs/remotes/origin/*");
        config.save();

        System.out.printf("Remote set to: %s%n", fileRemoteName);
        
        System.out.printf("Initialized empty repository (Working directory for use case) in %s%n", dir.getAbsolutePath());
        
        Git.open(dir).fetch().setRemote("origin").call();
        
        String deliverBranchName = "deliver_"+useCase.getBranchName();        
        Git.open(dir).branchCreate().setStartPoint("refs/tags/first_"+useCase.getBranchName()).setName(deliverBranchName).call();               
        Git.open(dir).checkout().setName(deliverBranchName).call();
        Git.open(dir).push().setRemote("origin").add(deliverBranchName).call();       
        this.workDir = dir;
        
        return dir;
    }
    
    //Cherry pick and transplant next commit
    public ObjectId cherryPickNextForUseCase() throws IOException, GitAPIException {
        Iterator<ObjectId> ids = useCase.iterator();
        ObjectId id = null;
        if(ids.hasNext()) {
            id = ids.next();
            ids.remove();
        }
        
        assert workDir != null;
        
        //If the id was found. Push the changes to the remote used for testing        
        if(id != null) {
            Git.open(workDir).cherryPick().include((AnyObjectId)id).call();
            Git.open(workDir).push().call();
        }
        
        return id;
    }

    public MemoryMapBranchManipulator(MemoryMapCommitListForUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * @return the workDir
     */
    public File getWorkDir() {
        return workDir;
    }

    /**
     * @param workDir the workDir to set
     */
    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    /**
     * @return the useCase
     */
    public MemoryMapCommitListForUseCase getUseCase() {
        return useCase;
    }

    /**
     * @param useCase the useCase to set
     */
    public void setUseCase(MemoryMapCommitListForUseCase useCase) {
        this.useCase = useCase;
    }
}
