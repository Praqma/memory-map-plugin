/*
 * The MIT License
 *
 * Copyright 2015 Mads.
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
package net.praqma.jenkins.unit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import net.praqma.jenkins.usecases.MemoryMapResultContainer;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Mads
 */
public class MemoryMapResultContainerSerialization {
    
    @Test
    @Ignore
    public void testDeserialize() throws Exception {
        
        HashMap<Integer,HashMap<String,String>> results = new HashMap<>();
        HashMap<String,String> build1 = new HashMap<>();
        build1.put(".bss", "0x00013AF");
        build1.put(".data", "0x0022323");
        build1.put(".edata", "0x0405965");
        
        HashMap<String,String> build2 = new HashMap<>();
        build2.put(".bss", "0x00D13AF");
        build2.put(".data", "0x0D22323");
        build2.put(".edata", "0xD405965");
        
        results.put(1, build1);
        results.put(2, build2);
        
        HashMap<Integer,HashMap<String,String>> resultsGraph = new HashMap<>();
        HashMap<String,String> buildGraph1 = new HashMap<>();
        buildGraph1.put("flash", "1131");
        buildGraph1.put("ram", "1203");
        
        HashMap<String,String> buildGraph2 = new HashMap<>();
        buildGraph2.put("flash", "1200");
        buildGraph2.put("ram", "1560");
        
        resultsGraph.put(1, buildGraph1);
        resultsGraph.put(2, buildGraph2);
        
        
        //MemoryMapResultContainer container = new MemoryMapResultContainer(results);
        MemoryMapResultContainer container = null;
        container.setGraphResults(resultsGraph);
        Gson gson = new GsonBuilder().create();
        String result = gson.toJson(container);
        System.out.println(result);
    }
}
