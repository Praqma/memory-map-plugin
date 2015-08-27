package net.praqma.jenkins.memorymap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javaposse.jobdsl.dsl.Context;
import static javaposse.jobdsl.dsl.Preconditions.checkArgument;
import static javaposse.jobdsl.plugin.ContextExtensionPoint.executeInContext;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.gcc.GccMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.ti.TexasInstrumentsMemoryMapParser;

public class MemoryMapDslContext implements Context {

    int wordSize;
    boolean showBytesOnGraphs;
    String scale;
    List<MemoryMapGraphConfiguration> graphs = new ArrayList<>();
    List<AbstractMemoryMapParser> parsers = new ArrayList<>();
    List<String> scales = Arrays.asList("default", "kilo", "Mega", "Giga");
    List<String> parserTypes = Arrays.asList("gcc", "ti");

    public void wordSize(int value) {
        wordSize = value;
    }

    public void showBytesOnGraphs(boolean value) {
        showBytesOnGraphs = value;
    }

    public void scale(String value) {
        checkArgument(scales.contains(value), "Scale must be one of " + scales);
        scale = value;
    }

    public void parser(String parserType, Runnable closure) {
        checkArgument(parserTypes.contains(parserType), "Parser type must be one of " + parserTypes);
        MemoryMapParserDslContext context = new MemoryMapParserDslContext(showBytesOnGraphs);
        executeInContext(closure, context);

        AbstractMemoryMapParser parser = null;
        switch (parserType) {
            case "gcc":
                parser = new GccMemoryMapParser(context.parserUniqueName, context.mapFile, context.commandFile, wordSize, showBytesOnGraphs, context.graphConfigurations);
                break;
            case "ti":
                parser = new TexasInstrumentsMemoryMapParser(context.parserUniqueName, context.mapFile, context.commandFile, wordSize, context.graphConfigurations, showBytesOnGraphs);
                break;
        }

        if(parser != null){
            parser.setParserTitle(context.parserTitle);
            parsers.add(parser);
        }
    }
}