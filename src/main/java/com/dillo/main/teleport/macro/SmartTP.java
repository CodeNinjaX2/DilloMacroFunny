package com.dillo.main.teleport.macro;

import static com.dillo.calls.CurrentState.ARMADILLO;
import static com.dillo.calls.CurrentState.SMARTTP;
import static com.dillo.config.config.smartTpDepth;
import static com.dillo.config.config.smartTpRange;
import static com.dillo.main.blockutils.colisions.GetUnobstructedPos.getUnobstructedPos;
import static com.dillo.main.teleport.macro.TeleportToNextBlock.attemptedToSmartTP;
import static com.dillo.main.teleport.macro.TeleportToNextBlock.isThrowRod;
import static com.dillo.utils.BlockUtils.makeNewBlock;
import static com.dillo.utils.RayTracingUtils.adjustLook;

import com.dillo.calls.ArmadilloStates;
import com.dillo.calls.KillSwitch;
import com.dillo.config.config;
import com.dillo.main.teleport.utils.TeleportToBlock;
import com.dillo.utils.previous.SendChat;
import com.dillo.utils.previous.random.ids;
import com.dillo.utils.previous.random.prefix;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class SmartTP {

  private static BlockPos nextBlock = null;
  private static JsonObject reRoutes = new JsonObject();
  private static boolean triedAPull = false;
  private static boolean overide = false;
  public static boolean prevFail = false;
  public static HashSet<BlockPos> smartTpBlocks = new HashSet<>();

  public static void smartTP(BlockPos finalBlock, boolean reTpOnFail) {
    smartTpBlocks.add(finalBlock);

    new Thread(() -> {
      JsonElement cord = reRoutes.get(String.valueOf(finalBlock));
      BlockPos block = ids.mc.thePlayer.getPosition();
      List<BlockPos> blocks = new ArrayList<>();
      SmartTp smartPositions = new SmartTp(null, null);

      if (cord == null || overide) {
        for (int j = -smartTpDepth; j <= smartTpDepth; j++) {
          for (int i = -smartTpRange; i <= smartTpRange; i++) {
            for (int k = -smartTpRange; k <= smartTpRange; k++) {
              BlockPos newBlock = makeNewBlock(i, j, k, block);

              Block blockUnderOne = ids.mc.theWorld.getBlockState(makeNewBlock(0, -1, 0, newBlock)).getBlock();
              Block blockUnderTwo = ids.mc.theWorld.getBlockState(makeNewBlock(0, -2, 0, newBlock)).getBlock();
              Block blockUnderThree = ids.mc.theWorld.getBlockState(makeNewBlock(0, -3, 0, newBlock)).getBlock();

              Block blockAbove1 = ids.mc.theWorld.getBlockState(makeNewBlock(0, 1, 0, newBlock)).getBlock();
              Block blockAbove2 = ids.mc.theWorld.getBlockState(makeNewBlock(0, 2, 0, newBlock)).getBlock();

              if (ids.mc.theWorld.getBlockState(newBlock).getBlock() == Blocks.cobblestone) {
                if (
                  blockAbove1 == Blocks.air &&
                  blockAbove2 == Blocks.air &&
                  blockUnderOne == Blocks.cobblestone &&
                  blockUnderTwo == Blocks.cobblestone &&
                  blockUnderThree == Blocks.cobblestone
                ) {
                  blocks.add(newBlock);
                }
              }
            }
          }
        }

        for (BlockPos blockPos : blocks) {
          Vec3 blockTp = adjustLook(
            ids.mc.thePlayer.getPositionVector(),
            blockPos,
            new net.minecraft.block.Block[] { Blocks.air },
            false
          );

          if (blockTp != null) {
            Vec3 finalTp = adjustLook(
              new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5),
              finalBlock,
              new net.minecraft.block.Block[] { Blocks.air },
              false
            );

            if (finalTp != null) {
              smartPositions.block1 = blockPos;
              smartPositions.block2 = finalBlock;
            }
          } else if (getUnobstructedPos(blockPos) != null) {
            Vec3 finalTp = adjustLook(
              new Vec3(blockPos.getX(), blockPos.getY() - 1, blockPos.getZ()),
              finalBlock,
              new net.minecraft.block.Block[] { Blocks.air },
              false
            );

            if (finalTp != null) {
              smartPositions.block1 = blockPos;
              smartPositions.block2 = finalBlock;
            } else if (getUnobstructedPos(finalBlock) != null) {
              smartPositions.block1 = blockPos;
              smartPositions.block2 = finalBlock;
            }
          }
        }
      } else {
        triedAPull = true;

        JsonObject jsonElement = cord.getAsJsonObject();

        BlockPos pos = new BlockPos(
          jsonElement.get("x").getAsInt(),
          jsonElement.get("y").getAsInt(),
          jsonElement.get("z").getAsInt()
        );

        Vec3 blockTp = adjustLook(
          ids.mc.thePlayer.getPositionVector(),
          pos,
          new net.minecraft.block.Block[] { Blocks.air },
          false
        );

        if (blockTp != null) {
          Vec3 finalTp = adjustLook(
            new Vec3(pos.getX(), pos.getY() - 1, pos.getZ()),
            finalBlock,
            new net.minecraft.block.Block[] { Blocks.air },
            false
          );

          if (finalTp != null) {
            smartPositions.block1 = pos;
            smartPositions.block2 = finalBlock;
          }
        } else if (getUnobstructedPos(pos) != null) {
          Vec3 finalTp = adjustLook(
            new Vec3(pos.getX(), pos.getY() - 1, pos.getZ()),
            finalBlock,
            new net.minecraft.block.Block[] { Blocks.air },
            false
          );

          if (finalTp != null) {
            smartPositions.block1 = pos;
            smartPositions.block2 = finalBlock;
          } else if (getUnobstructedPos(finalBlock) != null) {
            smartPositions.block1 = pos;
            smartPositions.block2 = finalBlock;
          }
        }
      }

      if (smartPositions.block1 != null && smartPositions.block2 != null) {
        nextBlock = smartPositions.block2;

        if (!reRoutes.has(String.valueOf(smartPositions.block1))) {
          JsonObject loc = new JsonObject();
          loc.add("x", new JsonPrimitive(smartPositions.block1.getX()));
          loc.add("y", new JsonPrimitive(smartPositions.block1.getY()));
          loc.add("z", new JsonPrimitive(smartPositions.block1.getZ()));

          reRoutes.add(String.valueOf(finalBlock), loc);
        }

        overide = false;

        TeleportToBlock.teleportToBlock(smartPositions.block1, config.tpHeadMoveSpeed, 0, SMARTTP);
      } else {
        if (triedAPull && !overide) {
          overide = true;
          smartTP(finalBlock, reTpOnFail);
        } else {
          if (reTpOnFail) {
            TeleportToNextBlock.teleportToNextBlock();
          } else {
            triedAPull = false;
            overide = false;
            SendChat.chat(prefix.prefix + "Found no teleport locations using smart tp!");
            attemptedToSmartTP = true;
            TeleportToNextBlock.teleportToNextBlock();
          }
        }
      }
    })
      .start();
  }

  public static void TPToNext() {
    TeleportToBlock.teleportToBlock(nextBlock, config.tpHeadMoveSpeed, 0, ARMADILLO);
  }

  @Getter
  @AllArgsConstructor
  public static class SmartTp {

    public BlockPos block1;
    public BlockPos block2;
  }
}
