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

import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import net.praqma.jenkins.memorymap.result.MemoryMapParsingResult;

/**
 *
 * @author Praqma
 */
public abstract class AbstractMemoryMapParser implements FilePath.FileCallable<List<MemoryMapParsingResult>> {
    
    protected List<Pattern> patterns;
    protected String includeFilePattern;
    
    public AbstractMemoryMapParser() {}
    
    public AbstractMemoryMapParser(String includeFilePattern, Pattern... pattern) {
        this.patterns = Arrays.asList(pattern);
        this.includeFilePattern = includeFilePattern;
    }
    
    public abstract List<MemoryMapParsingResult> parse(File f) throws IOException;
    public abstract String getParserName();

    @Override
    public List<MemoryMapParsingResult> invoke(File file, VirtualChannel vc) throws IOException, InterruptedException {
        return parse(file);
    }
    
    protected CharSequence createCharSequenceFromFile() throws IOException {
        FileInputStream fis = new FileInputStream(includeFilePattern);
        FileChannel fc = fis.getChannel();

        ByteBuffer bbuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int)fc.size());
        CharBuffer cbuf = Charset.forName("8859_1").newDecoder().decode(bbuf);
        return cbuf;
    }    

    /**
     * @return the includeFilePattern
     */
    public String getIncludeFilePattern() {
        return includeFilePattern;
    }

    /**
     * @param includeFilePattern the includeFilePattern to set
     */
    public void setIncludeFilePattern(String includeFilePattern) {
        this.includeFilePattern = includeFilePattern;
    }
}
