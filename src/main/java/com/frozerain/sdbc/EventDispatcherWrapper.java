package com.frozerain.sdbc;

import com.frozerain.sdbc.config.ConfigProvider;
import com.frozerain.sdbc.config.PropertyKey;
import com.frozerain.sdbc.event.EventType;
import com.frozerain.sdbc.event.filter.FilterProvider;
import com.frozerain.sdbc.event.filter.ProcessorEventFilter;
import com.frozerain.sdbc.event.handler.EventHandler;
import com.frozerain.sdbc.event.handler.impl.SimpleEventHandler;
import com.frozerain.sdbc.util.DefaultConstants;
import com.frozerain.sdbc.util.SilentCallable;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.interaction.*;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import org.reactivestreams.Publisher;
import org.reflections.Configuration;
import org.reflections.util.ConfigurationBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.frozerain.sdbc.event.EventType.*;

public class EventDispatcherWrapper {

    private EventDispatcher eventDispatcher;
    private SimpleEventAdapter adapter;

    public EventDispatcherWrapper(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
        List<String> processorsRoot = ConfigProvider.getGeneral().getProperty(PropertyKey.PROCESSOR_ROOT_PACKAGE);
        this.adapter = new SimpleEventAdapter(processorsRoot);
    }

    public SimpleEventAdapter adapter() {
        return this.adapter;
    }

    public void enable() {
        this.eventDispatcher.on(this.adapter).subscribe();
    }

    public static class SimpleEventAdapter extends ReactiveEventAdapter {

        private Map<EventType, EventHandler> eventHandlers = new HashMap<>();
        private Map<EventType, ProcessorEventFilter> eventFilters = new HashMap<>();
        private Configuration processorsPackageConfig;

        SimpleEventAdapter(List<String> processorsRootPackage) {
            if (processorsRootPackage == null || processorsRootPackage.isEmpty()) {
                throw new RuntimeException("Processors root package cannot be empty!");
            }
            this.processorsPackageConfig = new ConfigurationBuilder().forPackages(processorsRootPackage.toArray(new String[]{}));
        }

        public SimpleEventAdapter initDefaultFilters() {
            this.eventFilters.put(CHAT_INPUT_INTERACTION_EVENT, FilterProvider.slashCommandFilter());
            this.eventFilters.put(USER_INTERACTION_EVENT, FilterProvider.userCommandFilter());
            this.eventFilters.put(MESSAGE_INTERACTION_EVENT, FilterProvider.messageCommandFilter());
            this.eventFilters.put(BUTTON_INTERACTION_EVENT, FilterProvider.buttonCommandFilter());
            this.eventFilters.put(MODAL_SUBMIT_INTERACTION_EVENT, FilterProvider.modalSubmitFilter());
            this.eventFilters.put(SELECT_MENU_INTERACTION_EVENT, FilterProvider.selectMenuFilter());
            return this;
        }

        public SimpleEventAdapter registerHandler(EventType type, EventHandler handler) {
            this.eventHandlers.put(type, handler);
            return this;
        }

        public SimpleEventAdapter registerFilter(EventType type, ProcessorEventFilter filter) {
            this.eventFilters.put(type, filter);
            return this;
        }

        @Override
        public Publisher<?> onMessageCreate(MessageCreateEvent event) {
            System.out.println("Catch MessageCreateEvent...");
            return provideHandler(MESSAGE_CREATE_EVENT, () -> new SimpleEventHandler<MessageCreateEvent>(
                    MESSAGE_CREATE_EVENT, processorsPackageConfig)).handle(event);
        }

        @Override
        public Publisher<?> onMessageDelete(MessageDeleteEvent event) {
            return provideHandler(MESSAGE_DELETE_EVENT, () -> new SimpleEventHandler<MessageDeleteEvent>(
                    MESSAGE_DELETE_EVENT, processorsPackageConfig)).handle(event);
        }

        @Override
        public Publisher<?> onChatInputInteraction(ChatInputInteractionEvent event) {
            return provideHandler(CHAT_INPUT_INTERACTION_EVENT, () -> new SimpleEventHandler<ChatInputInteractionEvent>(
                    CHAT_INPUT_INTERACTION_EVENT, processorsPackageConfig)).handle(event);
        }

        @Override
        public Publisher<?> onUserInteraction(UserInteractionEvent event) {
            return provideHandler(USER_INTERACTION_EVENT, () -> new SimpleEventHandler<UserInteractionEvent>(
                    USER_INTERACTION_EVENT, processorsPackageConfig)).handle(event);
        }

        @Override
        public Publisher<?> onMessageInteraction(MessageInteractionEvent event) {
            return provideHandler(MESSAGE_INTERACTION_EVENT, () -> new SimpleEventHandler<MessageInteractionEvent>(
                    MESSAGE_INTERACTION_EVENT, processorsPackageConfig)).handle(event);
        }

        @Override
        public Publisher<?> onVoiceStateUpdate(VoiceStateUpdateEvent event) {
            return provideHandler(VOICE_STATE_UPDATE_EVENT, () -> new SimpleEventHandler<VoiceStateUpdateEvent>(
                    VOICE_STATE_UPDATE_EVENT, processorsPackageConfig)).handle(event);
        }

        @Override
        public Publisher<?> onMemberLeave(MemberLeaveEvent event) {
            return provideHandler(MEMBER_LEAVE_EVENT, () -> new SimpleEventHandler<MemberLeaveEvent>(
                    MEMBER_LEAVE_EVENT, processorsPackageConfig)).handle(event);
        }

        @Override
        public Publisher<?> onButtonInteraction(ButtonInteractionEvent event) {
            return provideHandler(BUTTON_INTERACTION_EVENT, () -> new SimpleEventHandler<ButtonInteractionEvent>(
                    BUTTON_INTERACTION_EVENT, processorsPackageConfig)).handle(event);
        }

        @Override
        public Publisher<?> onModalSubmitInteraction(ModalSubmitInteractionEvent event) {
            return provideHandler(MODAL_SUBMIT_INTERACTION_EVENT, () -> new SimpleEventHandler<ModalSubmitInteractionEvent>(
                    MODAL_SUBMIT_INTERACTION_EVENT, processorsPackageConfig)).handle(event);
        }

        @Override
        public Publisher<?> onSelectMenuInteraction(SelectMenuInteractionEvent event) {
            return provideHandler(SELECT_MENU_INTERACTION_EVENT, () -> new SimpleEventHandler<SelectMenuInteractionEvent>(
                    SELECT_MENU_INTERACTION_EVENT, processorsPackageConfig)).handle(event);
        }

        @Override
        public Publisher<?> onReactionAdd(ReactionAddEvent event) {
            return provideHandler(REACTION_ADD_EVENT, () -> new SimpleEventHandler<ReactionAddEvent>(
                    REACTION_ADD_EVENT, processorsPackageConfig)).handle(event);
        }

        @Override
        public Publisher<?> onReactionRemove(ReactionRemoveEvent event) {
            return provideHandler(REACTION_REMOVE_EVENT, () -> new SimpleEventHandler<ReactionRemoveEvent>(
                    REACTION_REMOVE_EVENT, processorsPackageConfig)).handle(event);
        }

        @SuppressWarnings("unchecked")
        private EventHandler provideHandler(EventType type, SilentCallable<EventHandler> callable) {
            return eventHandlers.computeIfAbsent(type, type1 -> callable.call()).applyFilter(eventFilters.get(type));
        }
    }
}
