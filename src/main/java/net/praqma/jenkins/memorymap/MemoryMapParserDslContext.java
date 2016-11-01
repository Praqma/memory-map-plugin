package net.praqma.jenkins.memorymap;

import java.util.ArrayList;
import java.util.List;
import javaposse.jobdsl.dsl.Context;
import static javaposse.jobdsl.plugin.ContextExtensionPoint.executeInContext;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;

public class MemoryMapParserDslContext implements Context {

    String parserTitle;

    public void parserTitle(String value){
        parserTitle = value;
    }

    List<MemoryMapGraphConfiguration> graphConfigurations = new ArrayList<>();

    public void graph(Runnable closure){
        MemoryMapGraphDslContext context = new MemoryMapGraphDslContext();
        executeInContext(closure, context);

        graphConfigurations.add(new MemoryMapGraphConfiguration(context.graphData, context.graphCaption));
    }
}