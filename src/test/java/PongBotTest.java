import com.frozerain.sdbc.EventDispatcherWrapper;
import com.frozerain.sdbc.config.ConfigProvider;
import com.frozerain.sdbc.event.EventType;
import com.frozerain.sdbc.event.filter.ProcessorEventFilter;
import com.frozerain.sdbc.util.DefaultConstants;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class PongBotTest {

    @Test
    public void createDiscordBot() {

        ConfigProvider.init("configs");
        GatewayDiscordClient client = DiscordClientBuilder.create(ConfigProvider.getGeneral().getBotToken())
                .build()
                .gateway()
                .setEnabledIntents(IntentSet.all())
                .login()
                .block();
        System.out.println("Load gateway");

        EventDispatcherWrapper dispatcherWrapper = new EventDispatcherWrapper(client.getEventDispatcher());
        dispatcherWrapper.adapter()
                .initDefaultFilters()
                .registerFilter(EventType.MESSAGE_CREATE_EVENT, (ProcessorEventFilter<MessageCreateEvent>) (event, processor) -> {
                    System.out.println("Content: " + event.getMessage().getContent());
                    return event.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false) && event.getMessage().getContent().startsWith("!")
                            && processor.getProcessorId().contains(event.getMessage().getContent().split(" ")[0].replace("!", ""));
                        });
        dispatcherWrapper.enable();
        System.out.println("Enable dispatcher");
        
        client.onDisconnect().block();
    }

    @Test
    public void utilTest() {
        ConfigProvider.init("configs");
    }
}
