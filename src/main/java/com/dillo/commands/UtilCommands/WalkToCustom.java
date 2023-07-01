package com.dillo.commands.UtilCommands;

import com.dillo.utils.previous.SendChat;
import com.dillo.utils.previous.random.ids;
import com.dillo.utils.renderUtils.renderModules.RenderMultipleLines;
import gg.essential.api.commands.Command;
import gg.essential.api.commands.DefaultHandler;
import net.minecraft.util.BlockPos;

import java.util.List;

import static com.dillo.utils.ScoreboardUtils.GetCurArea.getArea;

public class WalkToCustom extends Command {

  public static boolean startRender = false;

  public WalkToCustom() {
    super("walkToCustom");
  }

  @DefaultHandler
  public void handle() {
    //SendChat.chat(String.valueOf(ids.mc.thePlayer.isRiding()));
    // NewSpinDrive.putAllTogether();
    // ArmadilloStates.offlineState = "online";
    // ArmadilloStates.currentState = "spinDrive";

    getArea();

    //new Thread(GetCurGemPrice::getCurrGemPrice).start();

    //GetSBItems.getSack();

    // usePathfinder(new BlockPos(x, y, z));

    // StartNuker.startNuker();

    // SendChat.chat(String.valueOf(detectBlocks()));
    //SendChat.chat(String.valueOf(getYawBlock(new BlockPos(29, 86, 261))));
    //SendChat.chat(String.valueOf(getYawBlock(new BlockPos(28, 86, 262))));

    /*RenderMultipleLines.renderMultipleLines(null, null, false);

        List<BlockPos> foundRoute = FindPathToBlock.pathfinderTest(new BlockPos(x, y, z));

        if (foundRoute != null) {
            for (int i = 0; i < foundRoute.size(); i++) {
                if (i != foundRoute.size() - 1) {
                    RenderMultipleLines.renderMultipleLines(foundRoute.get(i), foundRoute.get(i + 1), true);
                }
            }

            foundRoute.remove(0);

            ArmadilloStates.offlineState = "online";
            SendChat.chat(String.valueOf(foundRoute.size()));
            WalkOnPath.walkOnPath(foundRoute);
        }*/

    //TeleportToBlock.teleportToBlock(new BlockPos(x, y, z), 500, 500, null);
  }

  private static float getYawBlock(BlockPos block) {
    double dX = block.getX() + 0.5 - ids.mc.thePlayer.posX;
    double dZ = block.getZ() + 0.5 - ids.mc.thePlayer.posZ;

    double angle = Math.atan2(dZ, dX);
    float rotationYaw = (float) Math.toDegrees(angle) - 90.0f;

    if (rotationYaw < 0.0f) {
      rotationYaw += 360.0f;
    }

    float playerYaw = ids.mc.thePlayer.rotationYaw;

    if (ids.mc.thePlayer.rotationYaw > 360) {
      playerYaw = (float) (ids.mc.thePlayer.rotationYaw - (Math.floor(ids.mc.thePlayer.rotationYaw / 360)) * 360);
    }

   // SendChat.chat(" !!! " + playerYaw + " !!! ");

    if (playerYaw > 180) {
      playerYaw -= 360;
    }

    rotationYaw = rotationYaw - playerYaw;

    return Math.abs(rotationYaw);
  }
}
