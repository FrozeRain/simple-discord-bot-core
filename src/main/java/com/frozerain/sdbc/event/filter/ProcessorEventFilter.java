package com.frozerain.sdbc.event.filter;

import com.frozerain.sdbc.event.processor.EventProcessor;
import discord4j.core.event.domain.Event;

@FunctionalInterface
public interface ProcessorEventFilter<T extends Event> {

    boolean isApplicable(T event, EventProcessor<T> processor);
}
