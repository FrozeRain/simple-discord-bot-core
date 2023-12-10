package com.frozerain.sdbc.event.handler.impl;

import com.frozerain.sdbc.event.processor.EventProcessor;
import com.frozerain.sdbc.event.filter.ProcessorEventFilter;
import discord4j.core.event.domain.Event;

import java.util.ArrayList;
import java.util.List;

class ProcessorContext<T extends Event> {

    private List<EventProcessor<T>> processors;
    private ProcessorEventFilter<T> processorFilter;
    private T event;
    private EventProcessor<T> currentProcessor;

    ProcessorContext(ProcessorEventFilter<T> processorFilter) {
        this.processorFilter = processorFilter;
        this.processors = new ArrayList<>();
    }

    List<EventProcessor<T>> processors() {
        return this.processors;
    }

    void addProcessor(EventProcessor<T> processor) {
        this.processors.add(processor);
    }

    void setEvent(T event) {
        this.event = event;
    }

    void setProcessor(EventProcessor<T> processor) {
        this.currentProcessor = processor;
    }

    String getCurrentProcessorName() {
        return this.currentProcessor.getClass().getName();
    }

    void setFilter(ProcessorEventFilter<T> filter) {
        this.processorFilter = filter;
    }

    boolean isEventApplicable(EventProcessor<T> processor) {
        return this.processorFilter == null || this.processorFilter.isApplicable(this.event, processor);
    }
}
