package net.praqma.jenkins.usecases;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.TagOpt;

public class BranchManipulator {

    private UseCaseCommits useCase;
    private File workDir;

    public BranchManipulator(UseCaseCommits useCase) throws IOException, GitAPIException {
        this.useCase = useCase;
        initRepo();
    }

    /*
     * Creates the repository where we can work and cherry-pick our commits.
     */
    private File initRepo() throws IOException, GitAPIException {
        File dir = new File(FileUtils.getTempDirectory(), "/" + useCase.getBranch());
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        dir.mkdirs();
        Git.init().setDirectory(dir).call().close();

        StoredConfig config = Git.open(dir).getRepository().getConfig();
        String fileRemoteName = "file://" + useCase.getRepository().getAbsolutePath();
        config.setString("remote", "origin", "url", fileRemoteName);
        config.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
        config.save();

        System.out.printf("Remote set to: %s%n", fileRemoteName);
        System.out.printf("Initialized empty repository (Working directory for use case) in %s%n", dir.getAbsolutePath());

        Git.open(dir).fetch().setRemote("origin").setTagOpt(TagOpt.FETCH_TAGS).call();
        String deliverBranchName = "deliver_" + useCase.getBranch();
        Git.open(dir).branchCreate().setStartPoint("refs/tags/first_" + useCase.getBranch()).setName(deliverBranchName).call();
        Git.open(dir).checkout().setName(deliverBranchName).call();
        Git.open(dir).push().setRemote("origin").add(deliverBranchName).call();
        this.workDir = dir;
        return dir;
    }

    /*
    * Cherry-pick and transplant the next commit
    * @return the commit ObjectId
    */
    public ObjectId nextCommit() throws IOException, GitAPIException {
        Iterator<ObjectId> ids = useCase.iterator();
        ObjectId id = null;
        if (ids.hasNext()) {
            id = ids.next();
            ids.remove();
        }

        assert workDir != null;

        // Push the changes to the remote used for testing
        if (id != null) {
            Git.open(workDir).cherryPick().include(id).call();
            Git.open(workDir).push().call();
        }

        return id;
    }

    /**
     * @return the workDir
     */
    public File getWorkDir() {
        return workDir;
    }

    /**
     * @return the useCase
     */
    public UseCaseCommits getUseCase() {
        return useCase;
    }

    /**
     * @param useCase the useCase to set
     */
    public void setUseCase(UseCaseCommits useCase) {
        this.useCase = useCase;
    }
}
