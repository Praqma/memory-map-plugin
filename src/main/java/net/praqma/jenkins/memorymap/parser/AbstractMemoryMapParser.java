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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jenkins.model.Jenkins;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfigurationDescriptor;
import net.praqma.jenkins.memorymap.parser.gcc.GccMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.ti.TexasInstrumentsMemoryMapParser;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import org.apache.commons.collections.ListUtils;
import org.kohsuke.stapler.DataBoundSetter;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
    @Type(value = TexasInstrumentsMemoryMapParser.class, name = "TexasInstrumentsMemoryMapParser")
    ,
    @Type(value = GccMemoryMapParser.class, name = "GccMemoryMapParser")})

public abstract class AbstractMemoryMapParser implements Describable<AbstractMemoryMapParser>, ExtensionPoint, Serializable {

    private static final String UTF_8_CHARSET = "UTF8";

    protected static final Logger logger = Logger.getLogger(AbstractMemoryMapParser.class.toString());

    protected List<Pattern> patterns;
    private List<MemoryMapGraphConfiguration> graphConfiguration;
    private String parserUniqueName;
    protected String mapFile;
    private String configurationFile;
    private Integer wordSize;
    private Boolean bytesOnGraph;
    private String parserTitle;

    /**
     *
     * @return The default word size. If the map files contains usages in
     * decimal value of bytes (say 1 200 bytes used). Use a word size of 8. else
     * use what your compiler prefers.
     */
    public abstract int getDefaultWordSize();

    public AbstractMemoryMapParser() {
        this.patterns = ListUtils.EMPTY_LIST;
        this.graphConfiguration = new ArrayList<>();
        this.parserUniqueName = "Default";
    }

    public AbstractMemoryMapParser(String parserUniqueName, String mapFile, String configurationFile, Integer wordSize, Boolean bytesOnGraph, List<MemoryMapGraphConfiguration> graphConfiguration, Pattern... pattern) {
        this.patterns = Arrays.asList(pattern);
        this.mapFile = mapFile;
        this.configurationFile = configurationFile;
        this.wordSize = wordSize;
        this.bytesOnGraph = bytesOnGraph;
        this.graphConfiguration = graphConfiguration;
        this.parserUniqueName = parserUniqueName;
    }

    public Object readResolve() {
        if (graphConfiguration == null) {
            graphConfiguration = new ArrayList<>();
        }
        if (getParserUniqueName() == null) {
            logger.log(Level.FINE, "Entering 1.x compatibility block, assigning name: Default");
            setParserUniqueName("Default");
        }
        return this;
    }

    /**
     * Implemented in order to get a unique name for the chosen parser
     *
     * @return The parsers unique name
     */
    public String getUniqueName() {
        return String.format("%s_%s_%s", this.getClass().getSimpleName().replace(".class", ""), mapFile, configurationFile);
    }

    protected CharSequence createCharSequenceFromFile(File f) throws IOException {
        return createCharSequenceFromFile(UTF_8_CHARSET, f);
    }

    protected CharSequence createCharSequenceFromFile(String charset, File f) throws IOException {
        CharBuffer cBuffer = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f.getAbsolutePath());
            logger.log(java.util.logging.Level.FINE, String.format("Parser %s created input stream for file.", getParserUniqueName()));
            FileChannel fc = fis.getChannel();
            ByteBuffer bBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int) fc.size());

            if (!Charset.isSupported(charset)) {
                logger.warning(String.format("The charset %s is not supported", charset));
                cBuffer = Charset.defaultCharset().newDecoder().decode(bBuffer);
            } else {
                cBuffer = Charset.forName(charset).newDecoder().decode(bBuffer);
            }
        } catch (FileNotFoundException ex) {
            logger.log(java.util.logging.Level.FINE, String.format("Parser %s reported exception of type FileNotFoundException.", getParserUniqueName()));
            throw ex;
        } catch (IOException ex) {
            logger.log(java.util.logging.Level.FINE, String.format("Parser %s reported exception of type IOException.", getParserUniqueName()));
            throw ex;
        } finally {
            if (fis != null) {
                fis.close();
                logger.log(java.util.logging.Level.FINE, String.format("Parser %s closed input stream for file.", getParserUniqueName()));
            }
        }
        return cBuffer;
    }

    public abstract MemoryMapConfigMemory parseConfigFile(File f) throws IOException;

    public abstract MemoryMapConfigMemory parseMapFile(File f, MemoryMapConfigMemory configuration) throws IOException;

    /**
     * @return the includeFilePattern
     */
    public String getMapFile() {
        return mapFile;
    }

    /**
     * @param mapFile the mapFile to set
     */
    public void setMapFile(String mapFile) {
        this.mapFile = mapFile;
    }

    @Override
    public Descriptor<AbstractMemoryMapParser> getDescriptor() {
        return (Descriptor<AbstractMemoryMapParser>) Jenkins.getActiveInstance().getDescriptorOrDie(getClass());
    }

    /**
     * @return All registered {@link AbstractMemoryMapParser}s.
     */
    public static DescriptorExtensionList<AbstractMemoryMapParser, MemoryMapParserDescriptor<AbstractMemoryMapParser>> all() {
        return Jenkins.getActiveInstance().<AbstractMemoryMapParser, MemoryMapParserDescriptor<AbstractMemoryMapParser>>getDescriptorList(AbstractMemoryMapParser.class);
    }

    public static List<MemoryMapParserDescriptor<?>> getDescriptors() {
        return all().stream().collect(Collectors.toList());
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

    public List<MemoryMapGraphConfigurationDescriptor<?>> getGraphOptions() {
        return MemoryMapGraphConfiguration.getDescriptors();
    }

    @Override
    public String toString() {
        return getUniqueName();
    }

    /**
     * @return the parserUniqueName
     */
    public String getParserUniqueName() {
        return parserUniqueName;
    }

    /**
     * @param parserUniqueName the parserUniqueName to set
     */
    public void setParserUniqueName(String parserUniqueName) {
        this.parserUniqueName = parserUniqueName;
    }

    /**
     * @return the parserTitle
     */
    public String getParserTitle() {
        return parserTitle;
    }

    /**
     * @param parserTitle the parserTitle to set
     */
    @DataBoundSetter
    public void setParserTitle(String parserTitle) {
        this.parserTitle = parserTitle;
    }

    /**
     * @return the graphConfiguration
     */
    public List<MemoryMapGraphConfiguration> getGraphConfiguration() {
        return graphConfiguration;
    }

    /**
     * @param graphConfiguration the graphConfiguration to set
     */
    public void setGraphConfiguration(List<MemoryMapGraphConfiguration> graphConfiguration) {
        this.graphConfiguration = graphConfiguration;
    }
}
