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

import java.util.ArrayList;
import java.util.Arrays;
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
    public void memoryMapBuildAction_trivial_accessor_mutator_tests() {
        MemoryMapBuildAction memoryMapBuildAction = new MemoryMapBuildAction(null, null);
        assertNull(memoryMapBuildAction.getIconFileName());
        assertNull(memoryMapBuildAction.getUrlName());
        assertEquals("Memory map", memoryMapBuildAction.getDisplayName());
    }
    
    @Test
    public void memoryMapBuildAction_intialization_compare() {
        
        String ebss = ".ebss";
        String bss = ".bss";
        MemoryMapBuildAction mmba = new MemoryMapBuildAction(null, null);
        mmba.setResults(new LinkedList<MemoryMapParsingResult>());        
        
        int sum = 0;
        
        assertEquals(sum, mmba.sumOfValues("dummy","non-existant"));
        assertNotNull(mmba.getResults());
        assertEquals(0, mmba.getResults().size());
        
        MemoryMapParsingResult mmpa = new MemoryMapParsingResult();
        mmpa.setName(".ebss");
        mmpa.setValue(1000);
        
        //Add a result with .ebss
        mmba.getResults().add(mmpa);
        
        assertEquals(1000, mmba.sumOfValues(".ebss"));
        
        MemoryMapParsingResult mmpa2 = new MemoryMapParsingResult();
        mmpa2.setName(".bss");
        mmpa2.setValue(1000);
        
        //Add a result with .bss
        mmba.getResults().add(mmpa2);
        assertEquals(1000, mmba.sumOfValues(".ebss"));
        assertEquals(2000, mmba.sumOfValues(".ebss",".bss"));
        
        List<String> list = Arrays.asList(ebss,bss);
        List<String> list2 = Arrays.asList(ebss);
        
        assertEquals(1000, mmba.sumOfValues(list2));
        assertEquals(2000, mmba.sumOfValues(list));
        
        assertTrue(mmba.validateThreshold(1000, list2));
        assertFalse(mmba.validateThreshold(500, list2));
        assertTrue(mmba.validateThreshold(1001, list2));
        
        assertTrue(mmba.validateThreshold(1000, ebss));
        assertFalse(mmba.validateThreshold(500, ebss));
        assertTrue(mmba.validateThreshold(1001, ebss));
        
    }
            
}
