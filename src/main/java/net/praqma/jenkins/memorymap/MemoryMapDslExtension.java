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
            wordSize (Integer wordSize)
            showBytesOnGraphs (boolean showBytesOnGraph = true) //defaults to false
            scale (String scale)
            parser(String parserType, String parserUniqueName, String commandFile, String mapFile) {
                parserTitle (String parserTitle)
              	graph{
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
job {
    publishers {
        memoryMap {
            wordSize 16
            showBytesOnGraphs true
            scale 'KILO'
            parser('GCC', 'gcc-5391', 'cmd.ld', 'mem.map') {
                parserTitle 'gcc graphs'
              	graph{
                  	graphCaption 'RAM'
                  	graphData    'ram08+ram09'
              	}
              	graph{
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
public class MemoryMapDslExtension  extends ContextExtensionPoint {
    
    @RequiresPlugin(id = "memory-map", minimumVersion = "2.1.0")
    @DslExtensionMethod(context = PublisherContext.class)
    public Object memoryMap(Runnable closure){
        MemoryMapDslContext context = new MemoryMapDslContext();
        executeInContext(closure, context);

        return new MemoryMapRecorder(context.parsers, context.showBytesOnGraphs, String.valueOf(context.wordSize), context.scale, null);
    }
}