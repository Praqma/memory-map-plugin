/*
 * The MIT License
 *
 * Copyright 2012 Praqma.
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
package net.praqma.jenkins.memorymap.util;

import hudson.FilePath;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;

/**
 * Small class that wraps the file callable interface, to wrap functionality to
 * find a file given a pattern on a remote machine.
 */
public abstract class FileFinder<T> implements FilePath.FileCallable<T> {

    public File findFile(File dir, String pattern) throws IOException {
        if (StringUtils.isBlank(pattern)) {
            throw new MemoryMapFileNotFoundError(String.format("Failed to find files as an empty file pattern was provided. Workspace was '%s'", dir.getAbsolutePath()));
        }

        FileSet fileSet = new FileSet();
        org.apache.tools.ant.Project project = new org.apache.tools.ant.Project();
        fileSet.setProject(project);
        fileSet.setDir(dir);
        fileSet.setIncludes(pattern);

        int numberOfFoundFiles = fileSet.getDirectoryScanner(project).getIncludedFiles().length;
        if (numberOfFoundFiles == 0) {
            throw new MemoryMapFileNotFoundError(String.format("Found no files matching '%s' in directory '%s'", pattern, dir.getAbsolutePath()));
        }

        File file = new File(dir.getAbsoluteFile() + System.getProperty("file.separator") + fileSet.getDirectoryScanner(project).getIncludedFiles()[0]);

        if (!file.exists()) {
            throw new MemoryMapFileNotFoundError(String.format("Couldn't find file '%s' in workspace '%s'. Scanner matched '%s' files.", file.getAbsolutePath(), dir.getAbsolutePath(), numberOfFoundFiles));
        }
        return file;
    }
}
