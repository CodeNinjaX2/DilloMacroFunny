package com.dillo.commands.UtilCommands;

import com.dillo.dilloUtils.BlockUtils.fileUtils.localizedData.currentRoute;
import com.dillo.utils.previous.SendChat;
import com.dillo.utils.previous.random.ids;
import gg.essential.api.commands.Command;
import gg.essential.api.commands.DefaultHandler;

import static com.dillo.ArmadilloMain.ArmadilloMain.test;

public class DetectEntityUnderCommand extends Command {

  public boolean startRender = false;

  public DetectEntityUnderCommand() {
    super("testE");
  }

  @DefaultHandler
  public void handle() {
    SendChat.chat("Testing");
    currentRoute.curPlayerPos = ids.mc.thePlayer.getPosition();
    test = !test;
    /*if (Objects.equals(arg, "true")) {
      RenderMultipleBlocksMod.renderMultipleBlocks(null, false);
      RenderMultipleLines.renderMultipleLines(null, null, false);

      BlockPos playerPos = new BlockPos(ids.mc.thePlayer.posX, ids.mc.thePlayer.posY, ids.mc.thePlayer.posZ);
      BlockPos destBlockPos = new BlockPos(205, 81, 218);

      List<BlockPos> foundPath = PathFinderV2.pathFinder(
        new BlockNode(destBlockPos, DistanceFromTo.distanceFromTo(playerPos, destBlockPos), 0.0, null),
        new BlockNode(playerPos, 0.0, DistanceFromTo.distanceFromTo(playerPos, destBlockPos), null)
      );

      if (foundPath != null) {
        for (int i = 0; i < foundPath.size(); i++) {
          if (i != foundPath.size() - 1) {
            RenderMultipleLines.renderMultipleLines(foundPath.get(i), foundPath.get(i + 1), true);
          }
        }

        foundPath.remove(0);

        ArmadilloStates.offlineState = "online";
        SendChat.chat(String.valueOf(foundPath.size()));
        WalkOnPath.walkOnPath(foundPath);
      } else {
        SendChat.chat("Path has not been found.");
      }
      /*
            HashSet<BlockPos> testSet = new HashSet<BlockPos>();
            testSet.add(new BlockPos(1, 2, 54));

            SendChat.chat(String.valueOf(testSet.contains(new BlockPos(1, 2, 54))));
    } else {
      WalkOnPath.stopWalking();
    }*/
  }
}
