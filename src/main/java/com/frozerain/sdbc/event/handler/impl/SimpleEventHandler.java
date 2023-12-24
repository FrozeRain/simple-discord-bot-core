package com.frozerain.sdbc.event.handler.impl;

import com.frozerain.sdbc.annotation.Processor;
import com.frozerain.sdbc.event.EventType;
import com.frozerain.sdbc.event.handler.EventHandler;
import com.frozerain.sdbc.event.processor.EventProcessor;
import com.frozerain.sdbc.event.filter.ProcessorEventFilter;
import discord4j.core.event.domain.Event;
import org.reflections.Configuration;
import org.reflections.Reflections;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Set;

public class SimpleEventHandler<T extends Event> implements EventHandler<T> {

    private static final Logger log = Loggers.getLogger(SimpleEventHandler.class);

    private ProcessorContext<T> context;
    private EventType type;

    public SimpleEventHandler(EventType eventType, Configuration processorPackage) {
        System.out.println("Create handler for: " + eventType);
        this.init(eventType, processorPackage, null);
    }

    public SimpleEventHandler(EventType eventType, Configuration processorPackage, ProcessorEventFilter<T> filter) {
        this.init(eventType, processorPackage, filter);
    }

    @Override
    public EventHandler<T> applyFilter(ProcessorEventFilter<T> filter) {
        this.context.setFilter(filter);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Flux<Void> handle(Event event) {
        return handleEvent((T) event);
    }

    @Override
    public Flux<Void> handleEvent(T event) {
        this.context.setEvent(event);
        System.out.println("Handle event: " + event.getClass().getSimpleName() + ", processors amount: " + this.context.processors().size());
        return this.context.processors().isEmpty()
                ? Flux.empty()
                : Flux.fromStream(this.context.processors().stream())
                .filter(processor -> this.context.isEventApplicable(processor))
                .flatMap(processor -> process(processor, event))
                .onErrorResume(this::handleException);
    }

    @Override
    public Mono<Void> handleException(Throwable throwable) {
        log.error(String.format("Error occurred while process event. {type=%s, processor=%s}",
                this.type, this.context.getCurrentProcessorName()), throwable);
        return Mono.empty();
    }

    Mono<Void> process(EventProcessor<T> processor, T event) {
        //MVPHelpDesk.LOGGER.info("[EVENT] process event: " + event.getClass().getSimpleName());
//        boolean contextRequired = processor.getClass().getAnnotation(Processor.class).contextRequired();
//        if (contextRequired) {
//            EventActionExecutor.MANGER.initContext(false);
//        }
        this.context.setProcessor(processor);
        return processor.processEvent(event)
                .onErrorResume(e -> processor.processEventException(event, e));
//        try {
//
//            //.then(Mono.fromRunnable(EventActionExecutor.MANGER::release));
//        } catch (Exception e) {
//            return processor.processEventException(event, e);
//            //.then(Mono.fromRunnable(EventActionExecutor.MANGER::release));
//        }
    }

    private void init(EventType type, Configuration processorPackage, ProcessorEventFilter<T> filter) {
        this.context = new ProcessorContext<>(null);
        this.type = type;
        loadProcessors(type, processorPackage);
    }

    @SuppressWarnings("unchecked")
    private void loadProcessors(EventType type, Configuration processorPackage) {
        Reflections reflections = new Reflections(processorPackage);
        Set<Class<?>> types = reflections.getTypesAnnotatedWith(Processor.class);

        for (Class<?> clazz : types) {
            if (type.equals(clazz.getAnnotation(Processor.class).type())) {
                Object newInstance;
                try {
                    newInstance = clazz.newInstance();
                } catch (IllegalAccessException | InstantiationException e) {
//                    MVPHelpDesk.LOGGER.severe("[ERROR] Cannot initialize processor {clazz=" + clazz.getName()
//                            + ", error=" + e.getMessage() + "}");
                    e.printStackTrace();
                    continue;
                }
                if (newInstance instanceof EventProcessor<?>) {
                    System.out.println(String.format("Load processor: %s", newInstance.getClass().getSimpleName()));
                    this.context.addProcessor((EventProcessor<T>) newInstance);
                }
            }
        }
    }
}
