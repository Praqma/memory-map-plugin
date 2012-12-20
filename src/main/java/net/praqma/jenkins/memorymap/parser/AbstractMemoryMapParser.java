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
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jenkins.model.Jenkins;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import org.apache.commons.collections.ListUtils;

/**
 *
 * @author Praqma
 */
public abstract class AbstractMemoryMapParser implements Describable<AbstractMemoryMapParser>, ExtensionPoint, MemoryMapParsable, Serializable {

    /* FULLY FUNCTIONAL FIND ALL */
    private static final Pattern CONFIG_FILE_PATTERN =  Pattern.compile("^(\\s+)(\\S+)(.*origin\\s=\\s)(0x\\S+)(,.*)(0x\\S+)(.*)$",Pattern.MULTILINE);   
    private static final String UTF_8_CHARSET = "UTF8";
    private static final String ISO_8859_1 = "8859_1";
    protected static final Logger logger = Logger.getLogger(AbstractMemoryMapParser.class.toString());
    
    protected List<Pattern> patterns;
    protected String mapFile;
    private String configurationFile;
    private Integer wordSize;
    private Boolean bytesOnGraph;

    
    public AbstractMemoryMapParser () {  
        this.patterns = ListUtils.EMPTY_LIST;
    }
    
    public AbstractMemoryMapParser(String mapFile, String configurationFile, Integer wordSize, Boolean bytesOnGraph, Pattern... pattern) {
        this.patterns = Arrays.asList(pattern);
        this.mapFile = mapFile;
        this.configurationFile = configurationFile;
        this.wordSize = wordSize;
        this.bytesOnGraph = bytesOnGraph;
    }
     
    protected CharSequence createCharSequenceFromFile(File f) throws IOException {
        return createCharSequenceFromFile(UTF_8_CHARSET, f);
    }    
     
    protected CharSequence createCharSequenceFromFile(String charset, File f) throws IOException {
        String chosenCharset = charset;
        
        CharBuffer cbuf = null;
        FileInputStream fis = null;
        try 
        {
            fis = new FileInputStream(f.getAbsolutePath());
            FileChannel fc = fis.getChannel();
            ByteBuffer bbuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int)fc.size());
            
            if(!Charset.isSupported(chosenCharset)) {
                logger.warning(String.format("The charset %s is not supported", charset));
                cbuf = Charset.defaultCharset().newDecoder().decode(bbuf);
            } else {
                cbuf = Charset.forName(charset).newDecoder().decode(bbuf);
            }
            
        } catch (IOException ex) {
            throw ex;
        } finally {
            if(fis != null) {
                fis.close();
            }
        }
        return cbuf;
    }    
    
    @Override
    public MemoryMapConfigMemory parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) throws IOException {
        MemoryMapConfigMemory config =  new MemoryMapConfigMemory();
        CharSequence sequence = createCharSequenceFromFile(f);
        System.out.println("Number of items found; "+config.size());
        System.out.println("Graphconfig size; "+graphConfig.size());
        //Matcher m = CONFIG_FILE_PATTERN.matcher(sequence);
        for(MemoryMapGraphConfiguration graph : graphConfig) {
            String[] split = graph.getGraphDataList().split(",");
            for(String s : split) {
                //throw new IOException(" 0 = "+split[0]+ "" + " 1 = "+split[1] + " "+MemoryMapConfigFileParserDelegate.getPatternForMemoryLayout(s).toString());
                
                Matcher m = MemoryMapConfigFileParserDelegate.getPatternForMemoryLayout(s).matcher(sequence);                
                while(m.find()) {
                    MemoryMapConfigMemoryItem item = new MemoryMapConfigMemoryItem(m.group(1), m.group(3), m.group(5));
                    config.add(item);
                    System.out.println(item);
                }
                
            }
        }
       
        
        //TODO:Remove before release
        System.out.println("Number of items found; "+config.size());
        return config;
    }
    
    @Override
    public MemoryMapConfigMemory parseMapFile(File f, MemoryMapConfigMemory configuration) throws IOException {
        CharSequence sequence = createCharSequenceFromFile(f);
        //throw new IOException("MATCH FOUND");
        
        for(MemoryMapConfigMemoryItem item : configuration) {
            //// (1)   (2)FLASH (3)                  (4)003e8000 (5)    (6)0000ff80 (7)   (8)0000f1a6 (9)   (10)00000dda (11)  
            
            Matcher matcher = MemoryMapMapParserDelegate.getPatternForMemorySection(item.getName()).matcher(sequence);
            while(matcher.find()) {
                //throw new IOException("MATCH FOUND");
                item.setUsed(matcher.group(8));
                item.setUnused(matcher.group(10));
            }      
            
            //throw new IOException("Pattern: "+MemoryMapMapParserDelegate.getPatternForMemorySection(item.getName()).toString());
        }
        //return null;
        return configuration;
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

    /**
     * @return the wordSize
     */
    public Integer getWordSize() {
        return wordSize;
    }

    /**
     * @param wordSize the wordSize to set
     */
    public void setWordSize(Integer wordSize) {
        this.wordSize = wordSize;
    }

    /**
     * @return the bytesOnGraph
     */
    public Boolean getBytesOnGraph() {
        return bytesOnGraph;
    }

    /**
     * @param bytesOnGraph the bytesOnGraph to set
     */
    public void setBytesOnGraph(Boolean bytesOnGraph) {
        this.bytesOnGraph = bytesOnGraph;
    }

    /**
     * @return the configurationFile
     */
    public String getConfigurationFile() {
        return configurationFile;
    }

    /**
     * @param configurationFile the configurationFile to set
     */
    public void setConfigurationFile(String configurationFile) {
        this.configurationFile = configurationFile;
    }
}
