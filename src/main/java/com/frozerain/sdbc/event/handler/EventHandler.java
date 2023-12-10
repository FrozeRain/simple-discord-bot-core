package com.frozerain.sdbc.event.handler;

import com.frozerain.sdbc.event.filter.ProcessorEventFilter;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventHandler<T extends Event> {

    EventHandler<T> applyFilter(ProcessorEventFilter<T> filter);

    Flux<Void> handle(Event event);

    Flux<Void> handleEvent(T event);

    Mono<Void> handleException(Throwable throwable);
}
