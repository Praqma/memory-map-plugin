package net.praqma.jenkins.memorymap;

import java.util.ArrayList;
import java.util.List;
import javaposse.jobdsl.dsl.Context;
import static javaposse.jobdsl.plugin.ContextExtensionPoint.executeInContext;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;

public class MemoryMapParserDslContext implements Context {

    boolean showBytesOnGraph;
    List<MemoryMapGraphConfiguration> graphConfigurations = new ArrayList<>();
    String parserTitle;

    public MemoryMapParserDslContext(boolean showBytesOnGraph){
        this.showBytesOnGraph = showBytesOnGraph;
    }

    public void parserTitle(String value){
        parserTitle = value;
    }

    public void graph(Runnable closure){
        MemoryMapGraphDslContext context = new MemoryMapGraphDslContext();
        executeInContext(closure, context);

        graphConfigurations.add(new MemoryMapGraphConfiguration(context.graphData, context.graphCaption, showBytesOnGraph));
    }
}