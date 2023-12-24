package processor;

import com.frozerain.sdbc.annotation.Processor;
import com.frozerain.sdbc.event.EventType;
import com.frozerain.sdbc.event.processor.impl.SimpleEventProcessor;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Processor(type = EventType.MESSAGE_CREATE_EVENT)
public class PongProcessor extends SimpleEventProcessor<MessageCreateEvent> {

    @Override
    public Mono<Void> processEvent(MessageCreateEvent event) {
        System.out.println("Processing event with content: " + event.getMessage().getContent());
        return event.getMessage().getChannel().flatMap(channel -> channel.createMessage("pong!")).then();
    }

    @Override
    public List<String> getProcessorId() {
        return Collections.singletonList("ping");
    }
}
