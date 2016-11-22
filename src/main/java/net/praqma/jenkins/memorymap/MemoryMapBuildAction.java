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
import hudson.model.Run;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import jenkins.tasks.SimpleBuildStep;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import net.praqma.jenkins.memorymap.util.HexUtils;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryAxis3D;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 *
 * @author Praqma
 */
public class MemoryMapBuildAction implements Action, SimpleBuildStep.LastBuildAction {

    private static final Logger logger = Logger.getLogger(MemoryMapBuildAction.class.getName());

    private static final double labelOffset = 1.2d;
    private HashMap<String, MemoryMapConfigMemory> memoryMapConfigs;
    private Run<?, ?> build;
    private MemoryMapRecorder recorder;
    private List<AbstractMemoryMapParser> parsers;

    public MemoryMapBuildAction(Run<?, ?> build, HashMap<String, MemoryMapConfigMemory> memoryMapConfig) {
        this.build = build;
        this.memoryMapConfigs = memoryMapConfig;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Memory map";
    }

    @Override
    public String getUrlName() {
        return null;
    }

    /**
     * Fetches the previous MemoryMap build. Takes all successful, but failed
     * builds.
     *
     * Goes to the end of list.
     *
     * @param base the Run to use as base
     * @return The previous MemoryMap build.
     */
    public MemoryMapBuildAction getPreviousAction(Run<?, ?> base) {
        logger.log(Level.FINER, "Entering getPreviousAction(base).");
        MemoryMapBuildAction action;
        Run<?, ?> start = base;
        while (true) {
            start = start.getPreviousCompletedBuild();
            if (start == null) {
                logger.log(Level.FINER, "getPreviousCompletedBuild null, returning null.");
                return null;
            }
            action = start.getAction(MemoryMapBuildAction.class);
            if (action != null) {
                logger.log(Level.FINER, "Returning action for build #{0}", action.build.number);
                return action;
            }
        }
    }

    public MemoryMapBuildAction getPreviousAction() {
        logger.log(Level.FINER, "Entering getPreviousAction.");
        MemoryMapBuildAction action;
        Run<?, ?> start = build;
        while (true) {
            start = start.getPreviousCompletedBuild();
            if (start == null) {
                logger.log(Level.FINER, "getPreviousCompletedBuild null, returning null.");
                return null;
            }
            action = start.getAction(MemoryMapBuildAction.class);

            if (action != null && (action.isValidConfigurationWithData())) {
                logger.log(Level.FINER, "Returning action for build #{0}", action.build.number);
                return action;
            }
        }
    }

    /**
     * We need to filter out markers with the same max value.
     * Simply concatenate the marker labels if they share the same max value.
     */
    private void filterMarkers(HashMap<String, ValueMarker> markers) {
        HashMap<Long, String> labels = new HashMap<>();

        for (Map.Entry<String, ValueMarker> marker : markers.entrySet()) {
            long markerValue = (long) marker.getValue().getValue();
            if (labels.containsKey(markerValue)) {
                // Append marker label
                String existingLabel = labels.get(markerValue);
                labels.put(markerValue, existingLabel + " " + marker.getKey());
            } else {
                // New entry
                labels.put(markerValue, marker.getKey());
            }
        }

        markers.clear();
        for (Map.Entry<Long, String> label : labels.entrySet()) {
            makeMarker(label.getValue(), (double) label.getKey(), markers);
        }

    }

    public void doDrawMemoryMapUsageGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSet = new DataSetBuilder<>();

        String members = req.getParameter("categories");
        String graphTitle = req.getParameter("title");
        String uniqueDataSet = req.getParameter("dataset");

        int w = Integer.parseInt(req.getParameter("width"));
        int h = Integer.parseInt(req.getParameter("height"));

        List<String> memberList = Arrays.asList(members.split(","));

        HashMap<String, ValueMarker> markers = new HashMap<>();

        String scale = getRecorder().getScale();

        double max = buildDataSet(memberList, uniqueDataSet, dataSet, markers);
        filterMarkers(markers);

        String s = "";
        if (scale.equalsIgnoreCase("kilo")) {
            s = "k";
        } else if (scale.equalsIgnoreCase("mega")) {
            s = "M";
        } else if (scale.equalsIgnoreCase("giga")) {
            s = "G";
        }

        String byteLegend = s + "Bytes";
        String wordLegend = s + "Words";

        String legend = getRecorder().getShowBytesOnGraph() ? byteLegend : wordLegend;

        JFreeChart chart = createPairedBarCharts(graphTitle, legend, max * 1.1d, 0d, dataSet.build(), markers.values());

        chart.setBackgroundPaint(Color.WHITE);
        chart.getLegend().setPosition(RectangleEdge.BOTTOM);
        ChartUtil.generateGraph(req, rsp, chart, w, h);
    }

    protected JFreeChart createPairedBarCharts(String title, String yAxis, double max, double min, CategoryDataset dataSet, Collection<ValueMarker> markers) {
        final CategoryAxis domainAxis = new CategoryAxis3D();
        final NumberAxis rangeAxis = new NumberAxis(yAxis);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperBound(max);
        rangeAxis.setLowerBound(min);
        BarRenderer renderer = new BarRenderer();

        CategoryPlot plot = new CategoryPlot(dataSet, domainAxis, rangeAxis, renderer);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);

        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        for (ValueMarker mkr : markers) {
            plot.addRangeMarker(mkr);
        }

        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(title);
        return chart;
    }

    /**
     * @return the recorder
     */
    public MemoryMapRecorder getRecorder() {
        return recorder;
    }

    /**
     * @param recorder the recorder to set
     */
    public void setRecorder(MemoryMapRecorder recorder) {
        this.recorder = recorder;
    }

    /**
     * @return the chosen parsers
     */
    public List<AbstractMemoryMapParser> getChosenParsers() {
        return parsers;
    }

    /**
     * @param parsers the parsers to set
     */
    public void setChosenParsers(List<AbstractMemoryMapParser> parsers) {
        this.parsers = parsers;
    }

    /**
     * @return the memoryMapConfig
     */
    public HashMap<String, MemoryMapConfigMemory> getMemoryMapConfigs() {
        return memoryMapConfigs;
    }

    /**
     * @param memoryMapConfigs the memoryMapConfigs to set
     */
    public void setMemoryMapConfigs(HashMap<String, MemoryMapConfigMemory> memoryMapConfigs) {
        this.memoryMapConfigs = memoryMapConfigs;
    }

    public boolean isValidConfigurationWithData() {
        return memoryMapConfigs != null && memoryMapConfigs.size() >= 1;
    }

    private String constructMaxLabel(String... parts) {
        StringBuilder builder = new StringBuilder();
        builder.append("(MAX)");
        for (String part : parts) {
            builder.append(" ").append(part);
        }

        return builder.toString();
    }

    private String constructCategoryLabel(String... parts) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (parts.length > 1) {
                builder.append(part).append("+");
            } else {
                builder.append(part);
            }
        }

        if (parts.length > 1) {
            int plusIndex = builder.lastIndexOf("+");
            return builder.substring(0, plusIndex);
        }

        return builder.toString();
    }

    /**
     * Extracts the value from a given memory map item. If multiple objects are
     * passed in, values get added.
     *
     * @param items the MemoryMapConfigMemoryItems to get the value of
     * @return the value of the MemoryMapConfigMemoryItems
     */
    private double extractValue(MemoryMapConfigMemoryItem... items) {
        double value = 0d;
        String scale = getRecorder().getScale();
        for (MemoryMapConfigMemoryItem item : items) {
            if (!StringUtils.isBlank(item.getUsed())) {
                if (getRecorder().getShowBytesOnGraph()) {
                    value = value + HexUtils.byteCount(item.getUsed(), getRecorder().getWordSize(), scale);
                } else {
                    value = value + HexUtils.wordCount(item.getUsed(), getRecorder().getWordSize(), scale);
                }
            }
        }
        return value;
    }

    private double extractMaxNonZeroValue(MemoryMapConfigMemoryItem... item) {
        double value = 0d;
        String scale = getRecorder().getScale();
        for (MemoryMapConfigMemoryItem it : item) {

            if (getRecorder().getShowBytesOnGraph()) {
                if (it.getTopLevelMemoryMax() != null) {
                    value = value + HexUtils.byteCount(it.getTopLevelMemoryMax(), getRecorder().getWordSize(), scale);
                    if (value > 0d) {
                        return value;
                    }
                }
            } else {
                if (it.getTopLevelMemoryMax() != null) {
                    value = value + HexUtils.byteCount(it.getTopLevelMemoryMax(), getRecorder().getWordSize(), scale);
                    if (value > 0d) {
                        return value;
                    }
                }
            }
        }
        return value;
    }

    private double extractMaxValue(MemoryMapConfigMemoryItem... item) {
        double value = 0d;
        String scale = getRecorder().getScale();
        for (MemoryMapConfigMemoryItem it : item) {

            if (getRecorder().getShowBytesOnGraph()) {
                if (it.getTopLevelMemoryMax() != null) {
                    value = value + HexUtils.byteCount(it.getTopLevelMemoryMax(), getRecorder().getWordSize(), scale);
                }
            } else {
                if (it.getTopLevelMemoryMax() != null) {
                    value = value + HexUtils.wordCount(it.getTopLevelMemoryMax(), getRecorder().getWordSize(), scale);
                }
            }
        }
        return value;
    }

    public void makeMarker(String labelName, double value, HashMap<String, ValueMarker> markers) {
        if (!markers.containsKey(labelName)) {
            ValueMarker vm = new ValueMarker(value, Color.BLACK, new BasicStroke(
                    1.2f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
                    1.0f, new float[]{6.0f, 6.0f}, 0.0f));
            vm.setLabel(labelName);

            double i = vm.getLabel().length() * labelOffset + 40;
            vm.setLabelOffset(new RectangleInsets(5, i, -20, 5));
            vm.setLabelAnchor(RectangleAnchor.TOP_LEFT);
            vm.setPaint(Color.BLACK);
            vm.setOutlinePaint(Color.BLACK);
            vm.setAlpha(1.0f);
            markers.put(labelName, vm);
        }
    }

    /**
     * Builds the data set. Returns the maximum value.
     *
     * @param graphData GraphData
     * @param dataSet the data set
     * @param graphDataSet the graph data set
     * @param markers markers to add (max values)
     * @return a double with the value to be drawn on graph
     */
    public double buildDataSet(List<String> graphData, String dataSet, DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> graphDataSet, HashMap<String, ValueMarker> markers) {
        logger.log(Level.FINE, "Entering buildDataSet with data set {0}", dataSet);
        double max = 0d;
        for (String s : graphData) {
            if (s.contains(" ")) {
                String[] parts = s.split(" ");

                String maxLabel = constructMaxLabel(parts);
                String categoryLabel = constructCategoryLabel(parts);

                for (MemoryMapBuildAction memBuild = this; memBuild != null; memBuild = memBuild.getPreviousAction()) {
                    logger.log(Level.FINE, "Building graph data set for build #{0}", memBuild.build.number);
                    MemoryMapConfigMemory result = memBuild.getMemoryMapConfigs().get(dataSet);
                    logger.log(Level.FINE, "Building MemoryMapConfig: {0}", result);
                    if (result == null) {
                        logger.log(Level.FINEST, "Data set {0} not found", dataSet);
                        for (String key : memBuild.getMemoryMapConfigs().keySet()) {
                            logger.log(Level.FINEST, "found {0}", key);
                        }
                    }
                    ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(memBuild.build);
                    if (result != null) {
                        List<MemoryMapConfigMemoryItem> ourItems = result.getItemByNames(parts);
                        MemoryMapConfigMemoryItem[] ourItemsArray = ourItems.toArray(new MemoryMapConfigMemoryItem[ourItems.size()]);
                        double value = extractValue(ourItemsArray);

                        boolean allBelongSameParent = MemoryMapConfigMemoryItem.allBelongSameParent(ourItemsArray);

                        if (allBelongSameParent) {
                            max = extractMaxNonZeroValue(ourItemsArray);
                        } else {
                            max = extractMaxValue(ourItemsArray);
                        }

                        graphDataSet.add(value, categoryLabel, label);
                        makeMarker(maxLabel, max, markers);
                    }
                }

            } else {
                for (MemoryMapBuildAction memBuild = this; memBuild != null; memBuild = memBuild.getPreviousAction()) {
                    logger.log(Level.FINE, "Building graph data set for build #{0}", memBuild.build.number);
                    MemoryMapConfigMemory result = memBuild.getMemoryMapConfigs().get(dataSet);
                    logger.log(Level.FINE, "Building MemoryMapConfig: {0}", result);
                    if (result == null) {
                        logger.log(Level.FINEST, "Data set {0} not found", dataSet);
                        for (String key : memBuild.getMemoryMapConfigs().keySet()) {
                            logger.log(Level.FINEST, "found {0}", key);
                        }
                    }
                    ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(memBuild.build);
                    if (result != null) {
                        //Do something we have a result

                        for (MemoryMapConfigMemoryItem item : result) {
                            //The name of the item matches the configured graph item

                            if (item.getName().equals(s)) {
                                String maxLabel = constructMaxLabel(item.getName());
                                double newMax = extractMaxNonZeroValue(item);
                                double value = extractValue(item);
                                String categoryLabel = constructCategoryLabel(item.getName());
                                graphDataSet.add(value, categoryLabel, label);

                                if (newMax >= max) {
                                    max = newMax;
                                }

                                if (newMax > 0d) {
                                    makeMarker(maxLabel, newMax, markers);
                                }

                            }

                        }

                    }
                }
            }
        }
        return max;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        List<MemoryMapProjectAction> projectActions = new ArrayList<>();
        projectActions.add(new MemoryMapProjectAction(build.getParent()));
        return projectActions;
    }
}
