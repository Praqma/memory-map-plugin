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
package net.praqma.jenkins.unit;

import java.util.LinkedList;
import java.util.List;
import net.praqma.jenkins.memorymap.MemoryMapBuildAction;
import net.praqma.jenkins.memorymap.result.MemoryMapParsingResult;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Praqma
 */


public class MemoryMapBuildActionTestSimple {
    
    @Test
    public void memoryMapBuildAction_intialization_compare() {
        MemoryMapBuildAction mmba = new MemoryMapBuildAction(null, null);
        mmba.setResults(new LinkedList<MemoryMapParsingResult>());        
        
        int sum = 0;
        
        assertEquals(sum, mmba.sumOfValues("dummy","non-existant"));
        assertEquals(0, mmba.getResults().size());
        
        MemoryMapParsingResult mmpa = new MemoryMapParsingResult();
        mmpa.setName(".ebss");
        mmpa.setValue(1000);
        
        //Add a result with .ebss
        mmba.getResults().add(mmpa);
        
        assertEquals(1000, mmba.sumOfValues(".ebss"));
        
    }
            
}
