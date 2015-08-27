package net.praqma.jenkins.memorymap;

import hudson.Extension;
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;

@Extension(optional = true)
public class MemoryMapDslExtension  extends ContextExtensionPoint {
    @DslExtensionMethod(context = PublisherContext.class)
    public Object memoryMap(Runnable closure){
        MemoryMapDslContext context = new MemoryMapDslContext();
        executeInContext(closure, context);

        return new MemoryMapRecorder(context.parsers, context.showBytesOnGraphs, String.valueOf(context.wordSize), context.scale, null);
    }
}