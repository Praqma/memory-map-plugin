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

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import jenkins.model.Jenkins;
import org.apache.commons.collections.ListUtils;

/**
 *
 * @author Praqma
 */
public abstract class AbstractMemoryMapParser implements Describable<AbstractMemoryMapParser>, ExtensionPoint, MemoryMapParsable, Serializable {
    
    protected List<Pattern> patterns;
    protected String mapFile;
    
    public AbstractMemoryMapParser () {  
        this.patterns = ListUtils.EMPTY_LIST;
    }
    
    public AbstractMemoryMapParser(String mapFile, Pattern... pattern) {
        this.patterns = Arrays.asList(pattern);
        this.mapFile = mapFile;
    }
     
    protected CharSequence createCharSequenceFromFile(File f) throws IOException {
        return createCharSequenceFromFile("8859_1", f);
    }
     
    protected CharSequence createCharSequenceFromFile(String charset, File f) throws IOException {
        CharBuffer cbuf = null;
        FileInputStream fis = null;
        try 
        {
            fis = new FileInputStream(f.getAbsolutePath());
            FileChannel fc = fis.getChannel();

            ByteBuffer bbuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int)fc.size());
            cbuf = Charset.forName(charset).newDecoder().decode(bbuf);
        } catch (IOException ex) {
            throw ex;
        } finally {
            if(fis != null) {
                fis.close();
            }
        }
        return cbuf;
    } 

    /**
     * @return the includeFilePattern
     */
    public String getMapFile() {
        return mapFile;
    }

    /**
     * @param includeFilePattern the includeFilePattern to set
     */
    public void setMapFile(String mapFile) {
        this.mapFile = mapFile;
    }
    
    @Override
	public Descriptor<AbstractMemoryMapParser> getDescriptor() {
		return (Descriptor<AbstractMemoryMapParser>) Jenkins.getInstance().getDescriptorOrDie( getClass() );
	}
    
    	/**
	 * All registered {@link AbstractConfigurationRotatorSCM}s.
	 */
	public static DescriptorExtensionList<AbstractMemoryMapParser, MemoryMapParserDescriptor<AbstractMemoryMapParser>> all() {
		return Jenkins.getInstance().<AbstractMemoryMapParser, MemoryMapParserDescriptor<AbstractMemoryMapParser>> getDescriptorList( AbstractMemoryMapParser.class );
	}
    
    public static List<MemoryMapParserDescriptor<?>> getDescriptors() {
		List<MemoryMapParserDescriptor<?>> list = new ArrayList<MemoryMapParserDescriptor<?>>();
		for( MemoryMapParserDescriptor<?> d : all() ) {
			list.add( d );
		}
		return list;
	}
}
