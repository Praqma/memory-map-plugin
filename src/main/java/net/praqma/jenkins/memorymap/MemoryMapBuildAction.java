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
import hudson.util.StackedAreaRenderer2;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import net.praqma.jenkins.memorymap.result.MemoryMapParsingResult;
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
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 *
 * @author Praqma
 */
public class MemoryMapBuildAction implements Action {

    private MemoryMapConfigMemory memoryMapConfig;
    private AbstractBuild<?,?> build;
    private MemoryMapRecorder recorder;
    
    public MemoryMapBuildAction(AbstractBuild<?,?> build, MemoryMapConfigMemory memoryMapConfig) {
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
    public MemoryMapBuildAction getPreviousAction(AbstractBuild<?,?> base) {
        MemoryMapBuildAction action = null;
        AbstractBuild<?,?> start = base;
        while(true) {
            start = start.getPreviousCompletedBuild();
            if(start == null) {
                return null;
            }
            action = start.getAction(MemoryMapBuildAction.class);            
            if(action != null) {
                return action;
            }
        }
    }
    
    public MemoryMapBuildAction getPreviousAction() {
        MemoryMapBuildAction action = null;
        AbstractBuild<?,?> start = build;
        while(true) {
            start = start.getPreviousCompletedBuild();
            if(start == null) {
                return null;
            }
            action = start.getAction(MemoryMapBuildAction.class);            
            
            if(action != null && (action.isValidConfigurationWithData())) {
                return action;
            }
        }
    }


    public void doDrawMemoryMapUsageGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataset = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
        
        String members = req.getParameter("categories");
        String graphTitle = req.getParameter("title");
        
        List<String> memberList = Arrays.asList(members.split(","));
        
        List<ValueMarker> markers = new ArrayList<ValueMarker>();

        int max = Integer.MIN_VALUE;
        boolean markersMarked = false;
        
        
        for(MemoryMapBuildAction membuild = this; membuild != null; membuild = membuild.getPreviousAction()) {            
            ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(membuild.build);
            MemoryMapConfigMemory result = membuild.getMemoryMapConfig();
            
            
            for(MemoryMapConfigMemoryItem res : result) {
                if(memberList.contains(res.getName())) {                    
                    int value = HexUtils.bitCount(res.getUsed(), 16);
                    int maxx = HexUtils.bitCount(res.getLength(), 16);
                    dataset.add(value, res.getName(), label);
                    
                    if(!markersMarked) {
                        markers.add(new ValueMarker((double)maxx, Color.BLACK, new BasicStroke(
                                    2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
                                    1.0f, new float[] {6.0f, 6.0f}, 0.0f
                                    )));
                    }
                    
                    if(maxx > max) {
                        max = maxx;
                    }
                
                }
            }
            markersMarked = true;
        }

        JFreeChart chart = createPairedBarCharts(graphTitle, "Words", (int)((double)max*1.10), 0, dataset.build(), markers);
         
        chart.setBackgroundPaint(Color.WHITE);
        chart.getLegend().setPosition( RectangleEdge.BOTTOM );
        ChartUtil.generateGraph( req, rsp, chart, 400, 300 );     
    }    
    
    /**
     * The simplest way to represent the threshold boundary. Use the same plot type as the parent plots. 
     * @param title
     * @param yaxis
     * @param max
     * @param min
     * @param dataset
     * @return 
     */
    protected JFreeChart createStackedCharts(String title, String yaxis, int max, int min, CategoryDataset dataset, List<ValueMarker> markers) {
        final CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        final NumberAxis rangeAxis = new NumberAxis("Words");
        rangeAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
        rangeAxis.setUpperBound( max );
        rangeAxis.setLowerBound( min );
        
        StackedAreaRenderer2 renderer = new StackedAreaRenderer2();

        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        plot.setDomainAxis( domainAxis );
        domainAxis.setCategoryLabelPositions( CategoryLabelPositions.UP_90 );
        domainAxis.setLowerMargin( 0.0 );
        domainAxis.setUpperMargin( 0.0 );
        domainAxis.setCategoryMargin( 0.0 );

        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setBackgroundPaint( Color.WHITE );
        plot.setOutlinePaint( null );
        plot.setRangeGridlinesVisible( true );
        plot.setRangeGridlinePaint( Color.black );
        plot.setInsets( new RectangleInsets( 5.0, 0, 0, 5.0 ) );

        for(ValueMarker mkr : markers) {
            plot.addRangeMarker(mkr);
        }       

        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(title);
        return chart;
    }
    
    protected JFreeChart createPairedBarCharts(String title, String yaxis, int max, int min, CategoryDataset dataset, List<ValueMarker> markers) {
        final CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        final NumberAxis rangeAxis = new NumberAxis("Words");
        rangeAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
        rangeAxis.setUpperBound( max );
        rangeAxis.setLowerBound( min );

        //StackedAreaRenderer2 renderer = new StackedAreaRenderer2();
        BarRenderer renderer = new BarRenderer();

        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        plot.setDomainAxis( domainAxis );
        domainAxis.setCategoryLabelPositions( CategoryLabelPositions.UP_90 );
        domainAxis.setLowerMargin( 0.0 );
        domainAxis.setUpperMargin( 0.0 );
        domainAxis.setCategoryMargin( 0.0 );

        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setBackgroundPaint( Color.WHITE );
        plot.setOutlinePaint( null );
        plot.setRangeGridlinesVisible( true );
        plot.setRangeGridlinePaint( Color.black );
        plot.setInsets( new RectangleInsets( 5.0, 0, 0, 5.0 ) );

        for(ValueMarker mkr : markers) {
            plot.addRangeMarker(mkr);
        }       

        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(title);
        return chart;
    }
    
    
    protected JFreeChart createChart( CategoryDataset dataset, String title, String yaxis, int max, int min ) {
        final JFreeChart chart = ChartFactory.createStackedAreaChart( title, // chart                                                                                                                                       // title
                        null, // unused
                        yaxis, // range axis label
                        dataset, // data
                        PlotOrientation.VERTICAL, // orientation
                        true, // include legend
                        true, // tooltips
                        false // urls
        );

        final LegendTitle legend = chart.getLegend();

        legend.setPosition( RectangleEdge.BOTTOM );

        chart.setBackgroundPaint( Color.white );

        final CategoryPlot plot = chart.getCategoryPlot();

        plot.setBackgroundPaint( Color.WHITE );
        plot.setOutlinePaint( null );
        plot.setRangeGridlinesVisible( true );
        plot.setRangeGridlinePaint( Color.black );

        CategoryAxis domainAxis = new ShiftedCategoryAxis( null );
        plot.setDomainAxis( domainAxis );
        domainAxis.setCategoryLabelPositions( CategoryLabelPositions.UP_90 );
        domainAxis.setLowerMargin( 0.0 );
        domainAxis.setUpperMargin( 0.0 );
        domainAxis.setCategoryMargin( 0.0 );

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
        rangeAxis.setUpperBound( max );
        rangeAxis.setLowerBound( min );
        
        final StackedAreaRenderer renderer = (StackedAreaRenderer) plot.getRenderer();
        renderer.setBaseStroke( new BasicStroke( 2.0f ) );
        plot.setInsets( new RectangleInsets( 5.0, 0, 0, 5.0 ) );
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
