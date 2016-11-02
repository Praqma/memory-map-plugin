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

import net.praqma.jenkins.memorymap.MemoryMapProjectAction;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Praqma
 */
public class MemoryMapProjectActionSimpleTest {
    
    @Test
    public void memoryMapProjectAction_trivial_accessor_mutator_test()  {
        MemoryMapProjectAction projectAction = new MemoryMapProjectAction(null);
        assertNotNull(projectAction.getIconFileName());
        assertEquals("memory-map",projectAction.getUrlName());
        assertEquals("Memory map",projectAction.getSearchUrl());
        assertEquals("Memory Map Publisher", projectAction.getDisplayName());
    }
    
    @Test(expected=NullPointerException.class)
    public void memoryMapProjectAction_null_pointer_is_thrown_test() {
        MemoryMapProjectAction projectAction = new MemoryMapProjectAction(null);
        projectAction.getLastApplicableMemoryMapResult();
    }
    
    @Test(expected=NullPointerException.class)
    public void memoryMapProjectAction_null_pointer_is_thrown_get_latest_action_in_project_test() {
        MemoryMapProjectAction projectAction = new MemoryMapProjectAction(null);
        projectAction.getLatestActionInProject();
    }
}
