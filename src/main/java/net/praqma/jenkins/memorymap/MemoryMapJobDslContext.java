package net.praqma.jenkins.memorymap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javaposse.jobdsl.dsl.Context;
import static javaposse.jobdsl.dsl.Preconditions.checkArgument;
import static javaposse.jobdsl.plugin.ContextExtensionPoint.executeInContext;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.gcc.GccMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.ti.TexasInstrumentsMemoryMapParser;

public class MemoryMapJobDslContext implements Context {

    int wordSize = 8;

    public void wordSize(int value) {
        wordSize = value;
    }

    boolean showBytesOnGraphs = false;

    public void showBytesOnGraphs() {
        showBytesOnGraphs = true;
    }

    public void showBytesOnGraphs(boolean value) {
        showBytesOnGraphs = value;
    }

    String scale = "default";
    Map<String, String> scales = new HashMap<String, String>() {
        {
            put("DEFAULT", "default");
            put("KILO", "kilo");
            put("MEGA", "Mega");
            put("GIGA", "Giga");
        }
    };

    public void scale(String value) {
        checkArgument(scales.containsKey(value), "Scale must be one of " + scales);
        scale = scales.get(value);
    }

    List<AbstractMemoryMapParser> parsers = new ArrayList<>();
    List<String> parserTypes = Arrays.asList("GCC", "TI");

    public void parser(String parserType, String parserUniqueName, String commandFile, String mapFile, Runnable closure) {
        checkArgument(parserTypes.contains(parserType), "Parser type must be one of " + parserTypes);
        MemoryMapParserDslContext context = new MemoryMapParserDslContext();
        executeInContext(closure, context);

        AbstractMemoryMapParser parser = null;
        switch (parserType) {
            case "GCC":
                parser = new GccMemoryMapParser(parserUniqueName, mapFile, commandFile, wordSize, showBytesOnGraphs, context.graphConfigurations);
                break;
            case "TI":
                parser = new TexasInstrumentsMemoryMapParser(parserUniqueName, mapFile, commandFile, wordSize, context.graphConfigurations, showBytesOnGraphs);
                break;
            default:
                System.out.println("Unable to allocate parser: " + parserType);
                break;
        }

        if (parser != null) {
            parser.setParserTitle(context.parserTitle);
            parsers.add(parser);
        }
    }
}
