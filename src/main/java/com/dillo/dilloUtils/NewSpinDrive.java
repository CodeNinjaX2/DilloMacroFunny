package com.dillo.dilloUtils;

import static com.dillo.dilloUtils.DilloDriveBlockDetection.detectBlocks;
import static com.dillo.dilloUtils.DilloDriveBlockDetection.getBlocksLayer;

import com.dillo.ArmadilloMain.ArmadilloStates;
import com.dillo.data.config;
import com.dillo.dilloUtils.BlockUtils.BlockUtils;
import com.dillo.dilloUtils.BlockUtils.FromBlockToHP;
import com.dillo.dilloUtils.BlockUtils.fileUtils.localizedData.currentRoute;
import com.dillo.dilloUtils.Teleport.TeleportToNextBlock;
import com.dillo.dilloUtils.Utils.LookYaw;
import com.dillo.utils.GetAngleToBlock;
import com.dillo.utils.degreeToRad;
import com.dillo.utils.previous.chatUtils.SendChat;
import com.dillo.utils.previous.random.ids;
import com.dillo.utils.previous.random.prefix;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class NewSpinDrive {

  public static float angleTudaSuda = 0;
  public static float angle = 0;
  public static java.util.Random random = new java.util.Random();
  private static final KeyBinding jump = Minecraft.getMinecraft().gameSettings.keyBindJump;
  public static List<DilloDriveBlockDetection.BlockAngle> returnBlocks = new ArrayList<>();
  public static float yPosition = 0;
  public static float lastBlockAngle = 0;
  private static int blockTime = 0;

  public static void newSpinDrive() {
    if (angle < lastBlockAngle + 50) {
      angle += config.headMovement * 7 + random.nextFloat() * 10;

      KeyBinding.setKeyBindState(jump.getKeyCode(), true);
      BlockPos block = returnBlocks.get((int) Math.floor(returnBlocks.size() / 2)).blockPos;
      BlockUtils.alreadyBroken.add(block);

      float y = block.getY() + yPosition;

      float anglePlayerToBlock = GetAnglePlayerToBlock.getAnglePlayerToBlock(block);
      angleTudaSuda = angle;

      angleTudaSuda += anglePlayerToBlock - 90;

      float radians = (float) degreeToRad.degreeToRad(angleTudaSuda);
      float dx = (float) (Math.cos(radians) * 5);
      float dz = (float) (Math.sin(radians) * 5);

      float x = (float) (block.getX() + dx + 0.5);
      float z = (float) (block.getZ() + dz + 0.5);

      int time = (int) (config.headMovement * 10);

      //IBlockState blockUnder = ids.mc.theWorld.getBlockState(new BlockPos(ids.mc.thePlayer.posX, ids.mc.thePlayer.posY - 2, ids.mc.thePlayer.posZ));

      LookAt.smoothLook(LookAt.getRotation(new Vec3(x, y, z)), time);
    } else if (
      (
        getBlocksLayer(
          new BlockPos(
            currentRoute.curPlayerPos.getX(),
            currentRoute.curPlayerPos.getY() + 2,
            currentRoute.curPlayerPos.getZ()
          )
        )
          .size() >
        0 ||
        getBlocksLayer(
          new BlockPos(
            currentRoute.curPlayerPos.getX(),
            currentRoute.curPlayerPos.getY() + 1,
            currentRoute.curPlayerPos.getZ()
          )
        )
          .size() >
        0 ||
        getBlocksLayer(
          new BlockPos(
            currentRoute.curPlayerPos.getX(),
            currentRoute.curPlayerPos.getY(),
            currentRoute.curPlayerPos.getZ()
          )
        )
          .size() >
        0
      ) &&
      blockTime < 1000
    ) {
      List<BlockPos> blocks1 = getBlocksLayer(
        new BlockPos(
          currentRoute.curPlayerPos.getX(),
          currentRoute.curPlayerPos.getY() + 2,
          currentRoute.curPlayerPos.getZ()
        )
      );

      List<BlockPos> blocks2 = getBlocksLayer(
        new BlockPos(
          currentRoute.curPlayerPos.getX(),
          currentRoute.curPlayerPos.getY() + 1,
          currentRoute.curPlayerPos.getZ()
        )
      );

      List<BlockPos> blocks3 = getBlocksLayer(
        new BlockPos(
          currentRoute.curPlayerPos.getX(),
          currentRoute.curPlayerPos.getY(),
          currentRoute.curPlayerPos.getZ()
        )
      );

      List<BlockPos> combined = new ArrayList<>();
      combined.addAll(blocks1);
      combined.addAll(blocks2);
      combined.addAll(blocks3);

      List<DilloDriveBlockDetection.BlockAngle> angles = new ArrayList<>();

      for (BlockPos block : combined) {
        float angle = GetAngleToBlock.calcAngle(block);

        DilloDriveBlockDetection.BlockAngle blockAngle = new DilloDriveBlockDetection.BlockAngle(angle, block);
        angles.add(blockAngle);
      }

      angles.sort((a, b) -> {
        return a.angle < b.angle ? -1 : 1;
      });

      if (angles.size() > 0) {
        float angle = GetAngleToBlock.calcAngle(angles.get(angles.size() - 1).blockPos);

        if (angle - 360 > 0) {
          LookYaw.lookToYaw(config.headMovement * 10L, config.headMovement * 7 + random.nextFloat() * 10);
        } else {
          LookYaw.lookToYaw(config.headMovement * 10L, -config.headMovement * 7 + random.nextFloat() * 10);
        }

        blockTime++;
      }
    } else {
      blockTime = 0;
      KeyBinding.setKeyBindState(jump.getKeyCode(), false);
      angle = 0;
      lastBlockAngle = 0;
      ArmadilloStates.currentState = null;
      SendChat.chat(prefix.prefix + "Done breaking! Moving to next vein!");
      TeleportToNextBlock.teleportToNextBlock();
    }
  }

  public static List<BlockPos> getBlocksAround(double yPos) {
    BlockPos referencePoint = new BlockPos(ids.mc.thePlayer.posX, ids.mc.thePlayer.posY + yPos, ids.mc.thePlayer.posZ);
    List<BlockPos> blocks = new ArrayList<>();

    for (int i = -1; i <= 1; i++) {
      for (int j = -1; j <= 1; j++) {
        BlockPos newBlock = new BlockPos(referencePoint.getX() + i, referencePoint.getY(), referencePoint.getZ() + j);

        if (!newBlock.equals(referencePoint)) {
          if (canAdd(newBlock)) {
            blocks.add(newBlock);
          }
        }
      }
    }

    //SendChat.chat(String.valueOf(blocks.size()));

    return blocks;
  }

  public static void putAllTogether() {
    returnBlocks = detectBlocks();

    if (returnBlocks != null) {
      if (returnBlocks.size() < 1) {
        ArmadilloStates.currentState = null;
        ArmadilloStates.offlineState = "offline";
        SendChat.chat("NO BLOCKS FOUND....");
      } else {
        DilloDriveBlockDetection.BlockAngle lastPos = returnBlocks.get(returnBlocks.size() - 1);
        lastBlockAngle = lastPos.angle;
        currentRoute.curPlayerPos = ids.mc.thePlayer.getPosition();
      }
    } else {
      ArmadilloStates.currentState = null;
      ArmadilloStates.offlineState = "offline";
      SendChat.chat("NO BLOCKS FOUND....");
    }
  }

  private static BlockPos getBestBlock(List<BlockPos> blocks, BlockPos block) {
    BlockPos currentBestBlock = blocks.get(0);
    float currentBestAngle = GetAnglePlayerToBlock.getAngleFromOneBlockToAnother(currentBestBlock, block);

    for (BlockPos bl : blocks) {
      float currAngle = GetAnglePlayerToBlock.getAngleFromOneBlockToAnother(currentBestBlock, bl);

      if (currentBestAngle > currAngle) {
        currentBestBlock = bl;
        currentBestAngle = currAngle;
      }
    }

    return currentBestBlock;
  }

  public static boolean canAddIgnorePanes(BlockPos block) {
    Block blockType = ids.mc.theWorld.getBlockState(block).getBlock();

    return blockType == Blocks.stained_glass;
  }

  public static boolean canAdd(BlockPos block) {
    Block blockType = ids.mc.theWorld.getBlockState(block).getBlock();

    return blockType == Blocks.stained_glass || blockType == Blocks.stained_glass_pane;
  }

  private static void maxAngle(List<BlockPos> blocks) {
    BlockPos block = blocks.get(0);
    lastBlockAngle = FromBlockToHP.getYaw(block.getX(), block.getY(), block.getZ());

    for (BlockPos blockPos : blocks) {
      float blockAngle = FromBlockToHP.getYaw(blockPos.getX(), blockPos.getY(), blockPos.getZ());

      if (blockAngle > lastBlockAngle) {
        lastBlockAngle = blockAngle;
      }
    }
  }
}
