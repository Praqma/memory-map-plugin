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

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;
import java.awt.BasicStroke;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import net.praqma.jenkins.memorymap.util.HexUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.title.LegendTitle;
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
public class MemoryMapBuildAction implements Action {

    private static final double labelOffset = 1.2d;
    private MemoryMapConfigMemory memoryMapConfig;
    private AbstractBuild<?, ?> build;
    private MemoryMapRecorder recorder;

    public MemoryMapBuildAction(AbstractBuild<?, ?> build, MemoryMapConfigMemory memoryMapConfig) {
        this.build = build;
        this.memoryMapConfig = memoryMapConfig;

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
     * Returns an indication wheather as to the requirements are met. You do one check per set of values you wish to compare. 
     * 
     * @param threshold
     * @param valuenames
     * @return 
     */
    public boolean validateThreshold(int threshold, String... valuenames) {
        return sumOfValues(valuenames) <= threshold;
    }

    public boolean validateThreshold(int threshold, List<String> valuenames) {
        return sumOfValues(valuenames) <= threshold;
    }

    public int sumOfValues(String... valuenames) {
        int sum = 0;
        /*
        for(MemoryMapParsingResult res : getResults()) {
        for(String s : valuenames) {
        if(res.getName().equals(s)) {
        sum+=res.getValue();
        }
        }
        }
         */
        return sum;
    }

    public int sumOfValues(List<String> values) {
        int sum = 0;
        /*
        for(MemoryMapParsingResult res : getResults()) {
        for(String s : values) {
        if(res.getName().equals(s)) {
        sum+=res.getValue();
        }
        }
        }
         */
        return sum;
    }

    /**
     * Fetches the previous MemoryMap build. Takes all succesful, but failed builds. 
     * 
     * Goes to the end of list.
     */
    public MemoryMapBuildAction getPreviousAction(AbstractBuild<?, ?> base) {
        MemoryMapBuildAction action = null;
        AbstractBuild<?, ?> start = base;
        while (true) {
            start = start.getPreviousCompletedBuild();
            if (start == null) {
                return null;
            }
            action = start.getAction(MemoryMapBuildAction.class);
            if (action != null) {
                return action;
            }
        }
    }

    public MemoryMapBuildAction getPreviousAction() {
        MemoryMapBuildAction action = null;
        AbstractBuild<?, ?> start = build;
        while (true) {
            start = start.getPreviousCompletedBuild();
            if (start == null) {
                return null;
            }
            action = start.getAction(MemoryMapBuildAction.class);

            if (action != null && (action.isValidConfigurationWithData())) {
                return action;
            }
        }
    }

    public void doDrawMemoryMapUsageGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataset = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

        String members = req.getParameter("categories");
        String graphTitle = req.getParameter("title");

        int w = Integer.parseInt(req.getParameter("width"));
        int h = Integer.parseInt(req.getParameter("height"));

        List<String> memberList = Arrays.asList(members.split(","));
        List<List<String>> memberLists = new ArrayList<List<String>>();

        for (String s : memberList) {
            memberLists.add(Arrays.asList(s.split(" ")));
        }

        List<ValueMarker> markers = new ArrayList<ValueMarker>();

        double max = Double.MIN_VALUE;
        Set<String> drawnMarker = new HashSet<String>();

        String scale = getRecorder().scale;

        for (MemoryMapBuildAction membuild = this; membuild != null; membuild = membuild.getPreviousAction()) {
            ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(membuild.build);
            MemoryMapConfigMemory result = membuild.getMemoryMapConfig();
            MemoryMapConfigMemory resultBlacklist = new MemoryMapConfigMemory();
            for (List<String> list : memberLists) {
                double value = 0.0d;
                double maxx = 0.0d;
                String labelName = "";
                for (MemoryMapConfigMemoryItem res : result) {
                    if (list.contains(res.getName()) && !resultBlacklist.contains(res)) {
                        resultBlacklist.add(res);
                        if (labelName.equals("")) {
                            labelName = res.getName();
                        } else {
                            labelName = String.format("%s+%s", labelName, res.getName());
                        }

                        if (getRecorder().getShowBytesOnGraph()) {
                            maxx = maxx + HexUtils.byteCount(res.getLength(), getRecorder().getWordSize(), scale);
                            value = value + HexUtils.byteCount(res.getUsed(), getRecorder().getWordSize(), scale);
                        } else {
                            maxx = maxx + HexUtils.wordCount(res.getLength(), getRecorder().getWordSize(), scale);
                            value = value + HexUtils.wordCount(res.getUsed(), getRecorder().getWordSize(), scale);
                        }
                    } else {
                    }

                    if (maxx > max) {
                        max = maxx;
                    }
                }
                if (!labelName.equals("")) {
                    dataset.add(value, labelName, label);
                }

                boolean makeMarker = true;
                for (ValueMarker vm : markers) {
                    if (maxx == vm.getValue() && !vm.getLabel().contains(labelName) && !labelName.equals("")) {
                        drawnMarker.add(vm.getLabel().replace("(MAX) - ", "") + " - " + labelName);
                        String s = vm.getLabel().replace("(MAX) - ", "");

                        vm.setLabel(String.format("%s - %s", vm.getLabel(), labelName));
                        //this is the size of chars used for setting the offset right
                        double i = vm.getLabel().length() * labelOffset + 40;
                        vm.setLabelOffset(new RectangleInsets(5, i, -20, 5));

                        makeMarker = false;
                    }
                }

                if ((!labelName.equals("")) && (drawnMarker.add(labelName))) {
                    if (makeMarker) {
                        ValueMarker vm = new ValueMarker((double) maxx, Color.BLACK, new BasicStroke(
                                1.2f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
                                1.0f, new float[]{6.0f, 6.0f}, 0.0f));

                        vm.setLabel(String.format("(MAX) - %s", labelName));

                        double i = vm.getLabel().length() * labelOffset + 40;
                        vm.setLabelOffset(new RectangleInsets(5, i, -20, 5));
                        vm.setLabelAnchor(RectangleAnchor.TOP_LEFT);
                        vm.setPaint(Color.BLACK);
                        vm.setOutlinePaint(Color.BLACK);
                        vm.setAlpha(1.0f);
                        markers.add(vm);
                    }
                }
            }
        }

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

        JFreeChart chart = createPairedBarCharts(graphTitle, legend, max * 1.1d, 0d, dataset.build(), markers);
        

        chart.setBackgroundPaint(Color.WHITE);
        chart.getLegend().setPosition(RectangleEdge.BOTTOM);
        ChartUtil.generateGraph(req, rsp, chart, w, h);
    }

    protected JFreeChart createPairedBarCharts(String title, String yaxis, double max, double min, CategoryDataset dataset, List<ValueMarker> markers) {
        final CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        final NumberAxis rangeAxis = new NumberAxis(yaxis);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperBound(max);
        rangeAxis.setLowerBound(min);
        /*TODO : wrong scale choosen - Jes
         * if the user selects Mega or Giga as the scale, but there only are 
         * a couple of Kilo in the graph it would have no ticks on the axis.
         * this can be solved by. redefining the ticks,
         * We have not done this because it's a bit tricky to figure out the rigth 
         * factor to devid with
         * 
         * but the method wuld be 
         * double factor = 10
         * rangeAxis.setStandardTickUnits(new StandardTickUnitSource(max / factor));
         */

        //StackedAreaRenderer2 renderer = new StackedAreaRenderer2();
        BarRenderer renderer = new BarRenderer();
        

        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);        
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
        chart.setPadding(new RectangleInsets(30, 15, 15, 15));
        chart.setTitle(title);
        return chart;
    }

    protected JFreeChart createChart(CategoryDataset dataset, String title, String yaxis, int max, int min) {
        final JFreeChart chart = ChartFactory.createStackedAreaChart(title, // chart                                                                                                                                       // title
                null, // unused
                yaxis, // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips
                false // urls
                );

        final LegendTitle legend = chart.getLegend();

        legend.setPosition(RectangleEdge.BOTTOM);

        chart.setBackgroundPaint(Color.white);

        final CategoryPlot plot = chart.getCategoryPlot();

        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperBound(max);
        rangeAxis.setLowerBound(min);

        final StackedAreaRenderer renderer = (StackedAreaRenderer) plot.getRenderer();
        renderer.setBaseStroke(new BasicStroke(2.0f));
        plot.setInsets(new RectangleInsets(5.0, 0, 0, 5.0));
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
     * @return the memoryMapConfig
     */
    public MemoryMapConfigMemory getMemoryMapConfig() {
        return memoryMapConfig;
    }

    /**
     * @param memoryMapConfig the memoryMapConfig to set
     */
    public void setMemoryMapConfig(MemoryMapConfigMemory memoryMapConfig) {
        this.memoryMapConfig = memoryMapConfig;
    }

    public boolean isValidConfigurationWithData() {
        return memoryMapConfig != null && memoryMapConfig.size() >= 1;
    }
}
