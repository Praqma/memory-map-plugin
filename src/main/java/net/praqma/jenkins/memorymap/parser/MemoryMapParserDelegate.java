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
package net.praqma.jenkins.memorymap.parser;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import net.praqma.jenkins.memorymap.result.MemoryMapParsingResult;
import org.apache.tools.ant.types.FileSet;

/**
 * Class to wrap the FileCallable method. Serves as a proxy to the parser method. 
 * @author Praqma
 */
public class MemoryMapParserDelegate implements FilePath.FileCallable<List<MemoryMapParsingResult>> 
{
    private AbstractMemoryMapParser parser;
    //Empty constructor. For serialization purposes.
    public MemoryMapParserDelegate() { }

    public MemoryMapParserDelegate(AbstractMemoryMapParser parser) {
        this.parser = parser;
    }

    @Override
    public List<MemoryMapParsingResult> invoke(File file, VirtualChannel vc) throws IOException, InterruptedException {        
        FileSet fileSet = new FileSet();
        org.apache.tools.ant.Project project = new org.apache.tools.ant.Project();
        fileSet.setProject(project);
        fileSet.setDir(file);
        fileSet.setIncludes(parser.getMapFile());
        
        File f = new File(file.getAbsoluteFile() + System.getProperty("file.separator") + fileSet.getDirectoryScanner(project).getIncludedFiles()[0]);
        int lenghth = fileSet.getDirectoryScanner(project).getIncludedFiles().length;
        
        if(!f.exists()) {
            throw new FileNotFoundException(String.format("File %s not found workspace was %s scanner found %s files", f.getAbsolutePath(),file.getAbsolutePath(),lenghth));
        } 
        return getParser().parse(f);
    }

    /**
     * @return the parser
     */
    public AbstractMemoryMapParser getParser() {
        return parser;
    }

    /**
     * @param parser the parser to set
     */
    public void setParser(AbstractMemoryMapParser parser) {
        this.parser = parser;
    }

}
