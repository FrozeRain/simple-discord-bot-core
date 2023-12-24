package com.frozerain.sdbc.event.filter;

import discord4j.core.event.domain.interaction.*;

public class FilterProvider {

    public static ProcessorEventFilter<ChatInputInteractionEvent> slashCommandFilter() {
        return (event, processor) -> processor.getProcessorId().contains(event.getCommandName())
                && !event.getInteraction().getUser().isBot();
    }

    public static ProcessorEventFilter<UserInteractionEvent> userCommandFilter() {
        return (event, processor) -> processor.getProcessorId().contains(event.getCommandName())
                && !event.getInteraction().getUser().isBot();
    }

    public static ProcessorEventFilter<MessageInteractionEvent> messageCommandFilter() {
        return (event, processor) -> processor.getProcessorId().contains(event.getCommandName())
                && !event.getInteraction().getUser().isBot();
    }

    public static ProcessorEventFilter<ButtonInteractionEvent> buttonCommandFilter() {
        return (event, processor) -> processor.getProcessorId().contains(event.getCustomId())
                && !event.getInteraction().getUser().isBot();
    }

    public static ProcessorEventFilter<ModalSubmitInteractionEvent> modalSubmitFilter() {
        return (event, processor) -> processor.getProcessorId().contains(event.getCustomId())
                && !event.getInteraction().getUser().isBot();
    }

    public static ProcessorEventFilter<SelectMenuInteractionEvent> selectMenuFilter() {
        return (event, processor) -> processor.getProcessorId().contains(event.getCustomId())
                && !event.getInteraction().getUser().isBot();
    }
}
