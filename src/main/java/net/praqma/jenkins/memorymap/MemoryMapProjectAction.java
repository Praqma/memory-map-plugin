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
package net.praqma.jenkins.memorymap;

import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.Job;
import hudson.model.Run;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Praqma
 */
public class MemoryMapProjectAction extends Actionable implements Action {

    public static final String ICON_NAME = "/plugin/memory-map/images/64x64/memory.png";
    private final Job<?, ?> project;

    public MemoryMapProjectAction(Job<?, ?> project) {
        this.project = project;
    }

    @Override
    public String getDisplayName() {
        return "Memory Map Publisher";
    }

    @Override
    public String getSearchUrl() {
        return "Memory map";
    }

    @Override
    public String getIconFileName() {
        return ICON_NAME;
    }

    @Override
    public String getUrlName() {
        return "memory-map";
    }

    public Job<?, ?> getProject() {
        return project;
    }

    public MemoryMapBuildAction getLatestActionInProject() {
        if (getProject().getLastCompletedBuild() != null) {
            return getProject().getLastCompletedBuild().getAction(MemoryMapBuildAction.class);
        }
        return null;
    }

    //Gets the last 'applicable' build action from project. That is a bui
    public MemoryMapBuildAction getLastApplicableMemoryMapResult() {
        Run<?, ?> build = getProject().getLastCompletedBuild();
        while (build != null) {
            MemoryMapBuildAction buildAction = build.getAction(MemoryMapBuildAction.class);
            if (buildAction != null && buildAction.isValidConfigurationWithData()) {
                return buildAction;
            }
            build = build.getPreviousBuild();
        }
        return null;
    }

    public void doDrawMemoryMapUsageGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (getLastApplicableMemoryMapResult() != null) {
            getLastApplicableMemoryMapResult().doDrawMemoryMapUsageGraph(req, rsp);
        }
    }

    public List<AbstractMemoryMapParser> parsersChosen() {
        return getLatestActionInProject().getChosenParsers();
    }

    public List<String> getGraphTitles(String parserId) {
        List<String> graphTitles = new ArrayList<>();
        List<AbstractMemoryMapParser> parsers = parsersChosen();
        for (AbstractMemoryMapParser parser : parsers) {
            if (parser.getParserUniqueName().equals(parserId)) {
                for (MemoryMapGraphConfiguration conf : parser.getGraphConfiguration()) {
                    graphTitles.add(conf.getGraphCaption());
                }
            }
        }
        return graphTitles;
    }

    public String getAssociatedMemoryAreas(String graphTitle, String id) {
        String result = null;
        List<AbstractMemoryMapParser> parsers = parsersChosen();
        for (AbstractMemoryMapParser parser : parsers) {
            if (parser.getParserUniqueName().equals(id)) {
                for (MemoryMapGraphConfiguration conf : parser.getGraphConfiguration()) {
                    if (conf.getGraphCaption().equals(graphTitle)) {
                        result = conf.getGraphDataList();
                    }
                }
            }

        }
        return result;
    }

    public List<String> getGraphTitles() {
        ArrayList<String> titles = new ArrayList<>();
        List<AbstractMemoryMapParser> parsers = parsersChosen();
        for (AbstractMemoryMapParser parser : parsers) {
            List<MemoryMapGraphConfiguration> graphConfigurations = parser.getGraphConfiguration();
            for (MemoryMapGraphConfiguration gc : graphConfigurations) {
                titles.add(gc.getGraphCaption());
            }
        }
        return titles;
    }
}
