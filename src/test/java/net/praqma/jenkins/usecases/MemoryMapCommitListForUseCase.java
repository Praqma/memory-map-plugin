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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 *
 * @author Mads
 */
public class MemoryMapCommitListForUseCase implements Iterable<ObjectId>{

    public final static String PREFIX = "[Memory Map Use Cases]";
    private final String branchName;
    private final List<ObjectId> commits;
    private final File bareRepo;
    
    public MemoryMapCommitListForUseCase(String branchName, File bareRepo) throws IOException, GitAPIException {
        this.branchName = branchName;
        this.commits = new ArrayList<>();
        this.bareRepo = bareRepo;
        init();
    }
      
    //Determine order for commit for the use case
    private void init() throws IOException, GitAPIException {
        Repository repo = getBareGitRepo();
            
        List<Ref> list = Git.open(bareRepo).tagList().call();
        Ref first = null;
        Ref last = null;
        for(Ref r : list) {
            if(r.getName().equals("refs/tags/first_"+branchName)) {
                first = r;
            }
            if(r.getName().equals("refs/tags/last_"+branchName)) {
                last = r;
            }
        }
       
        assert first != null;
        assert last != null;
        
        System.out.printf("%sFirst ref is %s(%s)%n", PREFIX, first.getName(), first.getObjectId());
        System.out.printf("%sLast ref is %s(%s)%n", PREFIX, last.getName(), last.getObjectId());
        
        RevWalk walker = new RevWalk(repo);
       
        walker.markStart(walker.parseCommit(last.getObjectId()));
        walker.markUninteresting(walker.parseCommit(first.getObjectId()));
        
        
        Iterator<RevCommit> itcommit = walker.iterator();
        
        while(itcommit.hasNext()) {
            ObjectId it_c = itcommit.next().getId();
            commits.add(it_c);
        }
        
        commits.add(walker.parseCommit(first.getObjectId()));        
        repo.close();        
    }

    /**
     * @return the commits
     */
    public List<ObjectId> getCommits() {
        return commits;
    }

    @Override
    public Iterator<ObjectId> iterator() {
        
        final Stack<ObjectId> ids = new Stack<>();
        final List<ObjectId> mutableIds = new ArrayList<>(commits);        
        ids.addAll(mutableIds);
        
        return new Iterator<ObjectId>() {
            
            private ObjectId current;
            
            @Override
            public boolean hasNext() {
                return !ids.isEmpty();
            }

            @Override
            public ObjectId next() {
                current = ids.pop();
                return current;
            }

            @Override
            public void remove() {
                commits.remove(current);
            }
        };
    }

    /**
     * @return the bareRepo
     */
    public File getBareRepo() {
        return bareRepo;
    }
    
    public String getFileRemoteName() {
        return "file://"+bareRepo.getAbsolutePath();
    }
    
    public String getDeliverBranchName() {
        return "deliver_"+branchName;
    }
    
    public Repository getBareGitRepo() throws IOException {
       Repository repo = Git.open(bareRepo).getRepository();
       return repo;
    }

    /**
     * @return the branchName
     */
    public String getBranchName() {
        return branchName;
    }
}
