package net.praqma.jenkins.memorymap;

import javaposse.jobdsl.dsl.Context;

public class MemoryMapGraphDslContext implements Context{
    String graphData;
    String graphCaption;

    public void graphData(String value){
        graphData = value;
    }

    public void graphCaption(String value){
        graphCaption = value;
    }
}