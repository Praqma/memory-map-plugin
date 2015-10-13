package net.praqma.jenkins.memorymap;

import javaposse.jobdsl.dsl.Context;

public class MemoryMapGraphDslContext implements Context{
    String graphData;

    public void graphData(String value){
        graphData = value;
    }

    String graphCaption;

    public void graphCaption(String value){
        graphCaption = value;
    }
}