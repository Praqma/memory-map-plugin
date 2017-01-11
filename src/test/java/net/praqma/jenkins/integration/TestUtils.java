/*
 * The MIT License
 *
 * Copyright 2015 Praqma.
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
package net.praqma.jenkins.integration;

import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.SubmoduleConfig;
import hudson.plugins.git.UserRemoteConfig;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.praqma.jenkins.memorymap.MemoryMapRecorder;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import net.praqma.jenkins.memorymap.util.HexUtils;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.jvnet.hudson.test.JenkinsRule;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Class containing a bunch of static methods to make testing easier.
 *
 * TODO: Maybe add a state driven testing sequence? TODO: Move the XML parsing
 * stuff to an XML helper class.
 *
 * @author thi
 */
public class TestUtils {

    private static final GsonXml gsonXml = createGson();

    /**
     * Fully prebuilt test that runs a build with the given configuration and
     * asserts all the usage values given.
     *
     * @param jenkins a JenkinsRule instance
     * @param parser a fully configured MemoryMapParser
     * @param zipName name of the zip file containing required link/map/...
     * files
     * @param expectedValues map of the values you want to check. key = name,
     * value = expected usage value
     */
    public static void testUsageValues(JenkinsRule jenkins, AbstractMemoryMapParser parser, String zipName, Map<String, String> expectedValues) throws Exception {
        FreeStyleProject project = createProject(jenkins);
        TestUtils.prepareProjectWorkspace(project, zipName);

        TestUtils.setMemoryMapConfiguration(project, parser);

        doBuildAssert(project, jenkins, expectedValues);
    }

    /**
     * Creates a new project to test with.
     *
     * @param jenkins a JenkinsRule instance used to create the project.
     * @return a new project
     */
    public static FreeStyleProject createProject(JenkinsRule jenkins) throws Exception {
        return createProject(jenkins, true);
    }

    /**
     * Creates a new project to test with.
     *
     * @param jenkins a JenkinsRule instance used to create the project.
     * @return a new project
     */
    public static FreeStyleProject createProject(JenkinsRule jenkins, boolean useSlave) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject(UUID.randomUUID().toString());
        if(useSlave) {
            project.setAssignedNode(jenkins.createOnlineSlave());
        }
        return project;
    }

    public static FreeStyleProject configureGit(FreeStyleProject project, String branchName, String repository) throws IOException {
        List<UserRemoteConfig> repos = Collections.singletonList(new UserRemoteConfig(repository, null, null, null));
        GitSCM gitSCM = new GitSCM(repos,
                Collections.singletonList(new BranchSpec(branchName)),
                false, Collections.<SubmoduleConfig>emptyList(),
                null, null, Collections.EMPTY_LIST);
        project.setScm(gitSCM);

        return project;
    }

    /**
     * Runs a build and asserts all the given usage values.
     *
     * @param project the project to build and test
     * @param jenkins a JenkinsRule instance
     * @param expectedValues map of the values you want to check. key = name,
     * value = expected usage value
     */
    public static void doBuildAssert(FreeStyleProject project, JenkinsRule jenkins, Map<String, String> expectedValues) throws Exception {
        FreeStyleBuild build = TestUtils.runNewBuild(project);
        TestUtils.printBuildConsoleLog(build, jenkins);
        jenkins.assertBuildStatus(Result.SUCCESS, build);

        //Assert the values are correct
        HashMap<String, MemoryMapConfigMemoryItem> usage = TestUtils.getMemoryItems(build);
        for (String key : expectedValues.keySet()) {
            assertTrue(String.format("Key '%s' not found.", key), usage.containsKey(key));
            String expectedValue = new HexUtils.HexifiableString(expectedValues.get(key)).toFormattedHexString().rawString;
            String actualValue = new HexUtils.HexifiableString(usage.get(key).getUsed()).toFormattedHexString().rawString;
            assertEquals(String.format("Value for key '%s' did not match expectations.", key), expectedValue, actualValue);
        }
    }

    /**
     * Sets up the project workspace and extracts the given archive into the
     * workspace.
     *
     * @param project the project for which to do the setup
     * @param archiveName the archive you want to extract to the workspace
     */
    public static void prepareProjectWorkspace(FreeStyleProject project, String archiveName) throws Exception {
        TestUtils.runNewBuild(project);
        if (archiveName != null) {
            TestUtils.unzipArchiveToProjectWorkspace(project, archiveName);
        }
    }

    /**
     * Configures the Memory Map Plugin for the project, using the given parser.
     *
     * @param project the project to configure the plugin for
     * @param parser the parser whose configuration must be added
     */
    public static void setMemoryMapConfiguration(FreeStyleProject project, AbstractMemoryMapParser parser) {
        MemoryMapRecorder recorder = new MemoryMapRecorder(Collections.singletonList(parser));
        project.getPublishersList().clear(); //remove any old recorders
        project.getPublishersList().add(recorder);
    }

    /**
     * Schedules a new build for the project that is run immediately.
     *
     * @param project the project to run the build for
     * @return the build
     * @throws Exception
     */
    public static FreeStyleBuild runNewBuild(FreeStyleProject project) throws Exception {
        return project.scheduleBuild2(0).get();
    }

    /**
     * Prints the build console of given build.
     *
     * @param build the build of which to print the console log
     * @param jenkins an jenkinsRule instance
     */
    public static void printBuildConsoleLog(FreeStyleBuild build, JenkinsRule jenkins) throws Exception {
        System.out.println(jenkins.createWebClient().getPage(build, "console").asText());
    }

    /**
     * Extracts given archive into the project's workspace. Note: archive file is
     * retrieved from TestUtils's resources.
     *
     * @param archiveName Archive to extract.
     * @param project The project in whose workspace the archive will be
     * extracted.
     * @throws Exception
     */
    public static void unzipArchiveToProjectWorkspace(FreeStyleProject project, String archiveName) throws Exception {
        FilePath workspace = project.getWorkspace();
        File zipfile = new File(TestUtils.class.getResource(archiveName).getFile());
        FileUtils.copyFileToDirectory(zipfile, new File(workspace.absolutize().getRemote()), true);
        FilePath zipInWorkspace = new FilePath(workspace, archiveName);
        zipInWorkspace.unzip(workspace);
    }

    public static void copyFilesToProjectWorkspace(FreeStyleProject project, String... files) throws Exception {
        FilePath workspace = project.getWorkspace();
        for (String file : Arrays.asList(files)) {
            workspace.copyFrom(TestUtils.class.getResource(file));
        }
    }

    /**
     * Returns a map of all the MemoryMapConfigMemoryItems with their name as
     * key.
     *
     * TODO: there's probably a better way to deserialize all the items..
     *
     * @param build the build of which to get the MemoryMapConfigMemoryItems
     * @return the MemoryMapConfigMemoryItems
     * @throws Exception
     */
    private static HashMap<String, MemoryMapConfigMemoryItem> getMemoryItems(FreeStyleBuild build) throws Exception {
        File buildFile = new File(build.getLogFile().getParent() + "/build.xml");
        Document document = parseXml(buildFile);

        HashMap<String, MemoryMapConfigMemoryItem> usageMap = new HashMap<>();

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

    /**
     * Returns given node in its raw xml form.
     *
     * @param node the node to return as raw xml.
     * @return the node as raw xml
     */
    private static String getRawXml(Node node) throws Exception {
        Document nodeDocument = node.getOwnerDocument();
        DOMImplementationLS domImplLS = (DOMImplementationLS) nodeDocument.getImplementation();
        LSSerializer serializer = domImplLS.createLSSerializer();
        return serializer.writeToString(node);
    }

    /**
     * Parses given xml file, returning it as a Document.
     *
     * @param xml the xml file to parse
     * @return the xml file as a Document
     */
    private static Document parseXml(File xml) throws Exception {
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
        XmlParserCreator parserCreator = () -> {
            try {
                return XmlPullParserFactory.newInstance().newPullParser();
            } catch (XmlPullParserException e) {
                throw new RuntimeException(e);
            }
        };
        return new GsonXmlBuilder().setXmlParserCreator(parserCreator).create();
    }

    public static boolean printAndReturnConsoleOfBuild(Run<?,?> build, JenkinsRule jenkinsRule) throws IOException, SAXException {
        // this outputs loft of HTML garbage... so pretty printing after:
        String console = jenkinsRule.createWebClient().getPage(build, "console").asXml();
        System.out.println("************************************************************************");
        System.out.println("* Relevant part of Jenkins build console (captured with regexp)");
        System.out.println(String.format("Consle out for build #%s", build.number));

        // the pattern we want to search for
        Pattern p = Pattern.compile("<link rel=\"stylesheet\" type=\"text/css\" href=\"/jenkins/descriptor/hudson.console.ExpandableDetailsNote/style.css\"/>"
                + ".*<pre.*>(.*)</pre>", Pattern.DOTALL);
        Matcher m = p.matcher(console);
        // if we find a match, get the group
        if (m.find()) {
            // get the matching group
            String capturedText = m.group(1);

            // print the group
            System.out.format("%s%n", capturedText);
            return true;
        } else {
            System.out.format("Didn't match any relevant part of the console%n");
            System.out.format("Writing full log to trace%n");
            System.out.format("************************************************************************%n");
            System.out.format(console+"%n");
            System.out.format("************************************************************************%n");
            return false;
        }
    }
}
