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
package net.praqma.jenkins.usecases;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;
import hudson.model.FreeStyleBuild;
import hudson.model.Run;
import java.io.File;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 *
 * @author Mads
 */
public class MemoryMapBuildResultValidator {
    
    private Run<?,?> build; 
    private MemoryMapResultContainer expectedResults;
    private static final Gson gson = new GsonBuilder().create();
    private static final GsonXml gsonXml = createGson();
    private boolean validateGraphs = true;
    private boolean validateValues = true;
    
    public MemoryMapBuildResultValidator() {} 
    
    public MemoryMapBuildResultValidator expectResults(MemoryMapResultContainer expectedResults) {
        this.expectedResults = expectedResults;
        return this;
    }
    
    public MemoryMapBuildResultValidator validateBuild(Run<?,?> build) {
        this.build = build;
        return this;
    }
    
    public MemoryMapBuildResultValidator expectResults(String json) {
        this.expectedResults = gson.fromJson(json, MemoryMapResultContainer.class);
        return this;
    }
    
    public MemoryMapBuildResultValidator validateGraphs(boolean validateGraphs) {
        this.validateGraphs = validateGraphs;
        return this;
    }
    
    public MemoryMapBuildResultValidator validateValues(boolean validateValues) {
        this.validateValues = validateValues;
        return this;
    }
    
    public void validate() throws Exception {
        HashMap<String,String> expecteds = expectedResults.getBuildResults().get(build.number);
        HashMap<String,MemoryMapConfigMemoryItem> actuals =  getMemoryItems(build);
        if(validateValues) {
            for(String key : expecteds.keySet()) {
                assertNotNull(String.format( "Expected value for key '%s' not found in build result for build #%s", key, build.number), actuals.get(key));                
                String expectedValue = expecteds.get(key);
                
                if(actuals.get(key) != null) {
                    String actualsValue = actuals.get(key).getUsed();
                    assertEquals(String.format("Expected %s, was %s for key '%s' for build #%s", expectedValue, actualsValue, key, build.number), expectedValue, actualsValue);
                }
            }
        } 
        
        if(validateGraphs) {
            //TODO: Implement me
        }
    }
    
    private HashMap<String, MemoryMapConfigMemoryItem> getMemoryItems(Run<?,?> build) throws Exception {
        File buildFile = new File(build.getLogFile().getParent() + "/build.xml");
        Document document = parseXml(buildFile);

        HashMap<String, MemoryMapConfigMemoryItem> usageMap = new HashMap<String, MemoryMapConfigMemoryItem>();

        NodeList allNodes = document.getElementsByTagName("*");
        for (int i = 0; i < allNodes.getLength(); i++) {
            if (allNodes.item(i).getNodeName().equals("net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem")) {
                String nodeRaw = getRawXml(allNodes.item(i));
                MemoryMapConfigMemoryItem item = gsonXml.fromXml(nodeRaw, MemoryMapConfigMemoryItem.class);
                usageMap.put(item.getName(), item);
            }
        }
        return usageMap;
    }
    
    private static String getRawXml(Node node) throws Exception {
        Document nodeDocument = node.getOwnerDocument();
        DOMImplementationLS domImplLS = (DOMImplementationLS) nodeDocument.getImplementation();
        LSSerializer serializer = domImplLS.createLSSerializer();
        return serializer.writeToString(node);
    }
    
    private Document parseXml(File xml) throws Exception {
        Document document;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(xml);
        return document;
    }
    
    /**
     * Creates an instance of GsonXml, used to help with deserializing objects
     * from the build.xml
     *
     * @return an instance of GsonXml
     */
    private static GsonXml createGson() {
        XmlParserCreator parserCreator = new XmlParserCreator() {
            @Override
            public XmlPullParser createParser() {
                try {
                    return XmlPullParserFactory.newInstance().newPullParser();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        return new GsonXmlBuilder().setXmlParserCreator(parserCreator).create();
    }
}
