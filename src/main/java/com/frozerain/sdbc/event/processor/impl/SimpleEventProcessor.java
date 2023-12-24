package com.frozerain.sdbc.event.processor.impl;

import com.frozerain.sdbc.event.processor.EventProcessor;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.List;
import java.util.concurrent.Callable;

public class SimpleEventProcessor<T extends Event> implements EventProcessor<T> {

    private static final Logger log = Loggers.getLogger(SimpleEventProcessor.class);

    private boolean defer = false;

    @Override
    public Mono<Void> processEvent(T event) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> processEventException(T event, Throwable error) {
//        if (event instanceof DeferrableInteractionEvent) {
//            if (error instanceof ValidationException && ((ValidationException) error).isSendError()) {
//                return ((DeferrableInteractionEvent) event).reply(error.getMessage()).withEphemeral(true);
//            }
//            MVPHelpDesk.LOGGER.severe("[ERROR] {event=" + event.getClass().getSimpleName()
//                    + ", error=" + error.getMessage() + "}\n");
//            error.printStackTrace();
//            return ((DeferrableInteractionEvent) event)
//                    .reply(String.format(CommandConstants.MARKDOWN.FIX, CommandConstants.ERROR.SORRY))
//                    .withEphemeral(true);
//        }
//        MVPHelpDesk.LOGGER.severe("[ERROR] {event=" + event.getClass().getSimpleName()
//                + ", error=" + error.getMessage() + "}\n");
        log.error(String.format("Event process error {event=%s, processor=%s}", event.getClass().getSimpleName(), this.getClass().getSimpleName()), error);
        return Mono.empty();
    }

    @Override
    public List<String> getProcessorId() {
        return null;
    }

    public void resetDefer() {
        this.defer = false;
    }

    public void setDefer() {
        this.defer = true;
    }

    public boolean isDefer() {
        return this.defer;
    }

    public Mono<Void> deferrable(T event, Callable<Mono<Void>> callable) {
        if (event instanceof DeferrableInteractionEvent) {
            setDefer();
            return ((DeferrableInteractionEvent) event).deferReply().then(Mono.defer(() -> {
                try {
                    return callable.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
