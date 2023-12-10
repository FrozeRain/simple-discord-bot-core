package com.frozerain.sdbc.event.processor;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

import java.util.List;

public interface EventProcessor<T extends Event> {

    Mono<Void> processEvent(T event);

    Mono<Void> processEventException(T event, Throwable error);

    List<String> getProcessorId();
}
