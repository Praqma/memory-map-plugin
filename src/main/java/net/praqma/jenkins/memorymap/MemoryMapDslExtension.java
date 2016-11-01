package net.praqma.jenkins.memorymap;

import hudson.Extension;
import javaposse.jobdsl.dsl.RequiresPlugin;
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;

/*
```
job {
    publishers {
        memoryMap {
            wordSize (Integer wordSize) // Defaults to 8
            showBytesOnGraphs (boolean showBytesOnGraph = true) // Defaults to false
            scale (String scale)
            parser(String parserType, String parserUniqueName, String commandFile, String mapFile) {
                parserTitle (String parserTitle)
              	graph {
                  	graphCaption (String graphCaption)
                  	graphData    (String graphData)
              	}
            }
        }
    }
}
```

Valid values for `parserType` are `GCC` and `TI`. Valid values for `scale` are `DEFAULT`, `KILO`, `MEGA` and `GIGA`.

```
job ('mmap_GEN'){
    publishers {
        memoryMap {
            wordSize 16
            showBytesOnGraphs true
            scale 'KILO'
            parser('GCC', 'gcc-5391', 'cmd.ld', 'mem.map') {
                parserTitle 'gcc graphs'
              	graph {
                  	graphCaption 'RAM'
                  	graphData    'ram08+ram09'
              	}
              	graph {
                  	graphCaption 'ETC'
                  	graphData    'etc.'
              	}
            }
        }
    }
}
```
*/

@Extension(optional = true)
public class MemoryMapDslExtension extends ContextExtensionPoint {
    
    @RequiresPlugin(id = "memory-map", minimumVersion = "2.1.0")
    @DslExtensionMethod(context = PublisherContext.class)
    public Object memoryMap(Runnable closure){
        MemoryMapJobDslContext context = new MemoryMapJobDslContext();
        executeInContext(closure, context);

        MemoryMapRecorder mmr = new MemoryMapRecorder(context.parsers);
        mmr.setScale(context.scale);
        mmr.setWordSize(context.wordSize);
        mmr.setShowBytesOnGraph(context.showBytesOnGraphs);
        return mmr;
    }
}