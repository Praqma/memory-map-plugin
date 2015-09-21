package net.praqma.jenkins.usecases;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class UseCaseRule extends ExternalResource {

    private final String url;
    private static File workDir;

    public UseCaseRule(String url) {
        this.url = url;
    }

    public File getWorkDir() {
        return workDir;
    }

    public File getRepository() {
        return new File(workDir, "/.git");
    }

    public File getUseCaseDir(String useCase) {
        return new File(workDir, "/examples/" + useCase);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        try {
            File dir = new File(FileUtils.getTempDirectory(), "/rule" + System.currentTimeMillis());
            if (workDir != null && workDir.exists()) {
                Git.open(workDir).fetch().call();
                System.out.println("Repo for tests already exists. Reusing: " + workDir.getAbsolutePath());
            } else {
                dir.mkdir();
                workDir = dir;
                Git.cloneRepository().setURI(url).setCloneAllBranches(true).setDirectory(workDir).call().close();
                System.out.println("Repo for test doesn't exist. Creating: " + workDir.getAbsolutePath());
            }
            return super.apply(base, description);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create test repo", ex);
        }
    }
}
