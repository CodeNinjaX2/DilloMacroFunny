package com.dillo.commands;

import com.dillo.dilloUtils.BlockUtils.fileUtils.AddBlockToRoute;
import com.dillo.dilloUtils.BlockUtils.fileUtils.localizedData.currentRoute;
import com.dillo.utils.previous.chatUtils.SendChat;
import com.dillo.utils.previous.random.ids;
import com.dillo.utils.previous.random.prefix;
import gg.essential.api.commands.Command;
import gg.essential.api.commands.DefaultHandler;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

public class AddBlockRouteCommand extends Command {

  public AddBlockRouteCommand() {
    super("block");
  }

  @DefaultHandler
  public void handle() {
    BlockPos pos = new BlockPos(ids.mc.thePlayer.posX, ids.mc.thePlayer.posY - 1, ids.mc.thePlayer.posZ);
    //RenderPoints.renderPoint(null, 0, false);

    if (ids.mc.theWorld.getBlockState(pos).getBlock() == Blocks.cobblestone) {
      if (currentRoute.currentRouteFile != null) {
        BlockPos block = new BlockPos(ids.mc.thePlayer.posX, ids.mc.thePlayer.posY - 1, ids.mc.thePlayer.posZ);

        if (!currentRoute.currentRoute.contains(block)) {
          AddBlockToRoute.addBlockToRoute(block, currentRoute.currentRouteFile);
          SendChat.chat(prefix.prefix + "Block has been added!");
        } else {
          SendChat.chat(prefix.prefix + "You have already added this block to your route! Please do not add it more.");
        }
      } else {
        SendChat.chat(prefix.prefix + "You have not selected a route! Please select one or run /helpMIT!");
      }
    } else {
      SendChat.chat(prefix.prefix + "Cannot add block, The block MUST be a cobblestone!");
    }
  }
}
