package net.praqma.jenkins.usecases;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

public class UseCaseCommits implements Iterable<ObjectId> {

    static final String PREFIX = "[Memory Map Use Cases]";
    private final List<ObjectId> commits = new ArrayList<>();
    private final String branch;
    private final File repository;

    public UseCaseCommits(String branch, File repository) throws IOException, GitAPIException {
        this.branch = branch;
        this.repository = repository;
        initCommits();
    }

    private void initCommits() throws IOException, GitAPIException {
        Repository repo = getGitRepository();
        List<Ref> list = Git.open(repository).tagList().call();
        Ref first = null;
        Ref last = null;
        for (Ref r : list) {
            if (r.getName().equals("refs/tags/first_" + branch)) {
                first = r;
                if (last != null) {
                    break;
                }
            }
            if (r.getName().equals("refs/tags/last_" + branch)) {
                last = r;
                if (first != null) {
                    break;
                }
            }
        }
        assert first != null;
        assert last != null;
        System.out.printf("%sFirst ref is %s(%s)%n", PREFIX, first.getName(), first.getObjectId());
        System.out.printf("%sLast ref is %s(%s)%n", PREFIX, last.getName(), last.getObjectId());

        RevWalk walker = new RevWalk(repo);
        walker.markStart(walker.parseCommit(last.getObjectId()));
        walker.markUninteresting(walker.parseCommit(first.getObjectId()));
        for (RevCommit commit : walker) {
            commits.add(commit.getId());
        }
        commits.add(walker.parseCommit(first.getObjectId()));
        repo.close();
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

    public List<ObjectId> getCommits() {
        return commits;
    }

    public File getRepository() {
        return repository;
    }

    public String getFileRemoteName() {
        return "file://" + repository.getAbsolutePath();
    }

    public Repository getGitRepository() throws IOException {
        return Git.open(repository).getRepository();
    }

    public String getBranch() {
        return branch;
    }

    public String getDeliverBranch() {
        return "deliver_" + branch;
    }
}
