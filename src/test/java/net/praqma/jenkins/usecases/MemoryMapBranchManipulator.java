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

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.MutableObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevCommitList;

/**
 *
 * @author Mads
 */
public class MemoryMapBranchManipulator {
    
    private MemoryMapCommitListForUseCase useCase;
    private File workDir;
    
    //Create the repository for this use case. We create a bare repo and commit push each commit as required. 
    public File initRepositoryForUseCase() throws IOException, GitAPIException {
        
        File dir = new File(FileUtils.getTempDirectory(),"/"+useCase.getBranchName()+".git");
        if(dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        dir.mkdir();
        
        Git.cloneRepository().setURI(useCase.getBareGitRepo().getDirectory().getAbsolutePath()).
                setBranch("master").
                setBare(true).
                setDirectory(dir).call().close();
        
        
        return dir;
    }
    
    public File createWorkRepo(File bareRepo) throws IOException, GitAPIException {
        File dir = new File(FileUtils.getTempDirectory(),"/"+useCase.getBranchName());
        if(dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        dir.mkdir();
        
        Git.cloneRepository().setURI(bareRepo.getAbsolutePath()).
        setBranch("master").
        setBare(false).
        setDirectory(dir).call().close();
        
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
}
