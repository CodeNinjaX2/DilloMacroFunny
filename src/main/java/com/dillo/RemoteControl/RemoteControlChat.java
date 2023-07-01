package com.dillo.RemoteControl;

import com.dillo.data.config;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class RemoteControlChat {

  public static List<String> chatArray = new ArrayList<>();

  @SubscribeEvent
  public void onChatReceived(ClientChatReceivedEvent event) {
    if (config.remoteControl) {
      chatArray.add(event.message.getUnformattedText());
    }
  }
}
