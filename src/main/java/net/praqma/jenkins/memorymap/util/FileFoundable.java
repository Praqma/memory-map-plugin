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
import java.io.File;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.types.FileSet;

/**
 *
 * Small class that wraps the file callable interface, to wrap functionality to find a file given a pattern on a remote machine.
 * 
 * @author Praqma
 */
public abstract class FileFoundable<T> implements FilePath.FileCallable<T>  {
    
    public File findFile(File file, String pattern) throws IOException {
        if(StringUtils.isBlank(pattern)) {
            throw new MemoryMapFileNotFoundError(String.format("Empty file pattern provided, this is not legal. Workspace was %s", file.getAbsolutePath()), file);            
        }
        
        FileSet fileSet = new FileSet();
        org.apache.tools.ant.Project project = new org.apache.tools.ant.Project();
        fileSet.setProject(project);
        fileSet.setDir(file);
        fileSet.setIncludes(pattern);
        
        int numberOfFoundFiles = fileSet.getDirectoryScanner(project).getIncludedFiles().length;
        if(numberOfFoundFiles == 0) {
            throw new MemoryMapFileNotFoundError(String.format("Filematcher found no files using pattern %s in folder %s", pattern, file.getAbsolutePath()), file);
        } 
        
        File f = new File(file.getAbsoluteFile() + System.getProperty("file.separator") + fileSet.getDirectoryScanner(project).getIncludedFiles()[0]);

        if(!f.exists()) {
            throw new MemoryMapFileNotFoundError(String.format("File %s not found workspace was %s scanner found %s files.", f.getAbsolutePath(),file.getAbsolutePath(),numberOfFoundFiles), file);            
        }
        return f;
    } 
}
