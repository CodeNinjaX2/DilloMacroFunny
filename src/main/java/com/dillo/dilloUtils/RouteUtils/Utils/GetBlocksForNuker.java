package com.dillo.dilloUtils.RouteUtils.Utils;

import static com.dillo.dilloUtils.RouteUtils.LegitRouteClear.LegitRouteClear.startLegit;
import static com.dillo.dilloUtils.RouteUtils.Nuker.NukerMain.nukerStart;

import com.dillo.Events.DoneNukerBlocks;
import com.dillo.Events.DonePathEvent;
import com.dillo.utils.BlockUtils;
import com.dillo.utils.DistanceFromTo;
import com.dillo.utils.previous.SendChat;
import com.dillo.utils.previous.random.ids;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;

public class GetBlocksForNuker {

  public static List<BlockPos> Blockss = new ArrayList<>();
  public static int currBlocksOfAir = 0;
  private static String call = null;

  public static void getBlocks(List<BlockPos> blocksOnRoute, String callNext) {
    call = callNext;

    new Thread(() -> {
      currBlocksOfAir = 0;
      List<BlockPos> blocks = new ArrayList<>();

      for (int i = 0; i < blocksOnRoute.size(); i++) {
        int second = i + 1;

        if (i == blocksOnRoute.size() - 1 && blocksOnRoute.size() > 2) {
          second = 0;
        }

        if (second < blocksOnRoute.size()) {
          BlockPos block = blocksOnRoute.get(i);
          blocks.addAll(
            findBlocks(
              new Vec3(block.getX(), block.getY() + 1.64, block.getZ()),
              BlockUtils.fromBlockPosToVec3(blocksOnRoute.get(second)),
              1
            )
          );
        }
      }

      Blockss = blocks;

      doneGettingBlocks();
    })
      .start();
  }

  public static void doneGettingBlocks() {
    if (call == "nuker") {
      nukerStart();
    } else if (call == "legit") {
      startLegit();
    }
  }

  public static List<BlockPos> findBlocks(Vec3 pos1, Vec3 pos2, double cylRad) {
    List<BlockPos> finalBlockPos = new ArrayList<>();

    double dx = pos2.xCoord - pos1.xCoord;
    double dy = pos2.yCoord - pos1.yCoord;
    double dz = pos2.zCoord - pos1.zCoord;
    double blockOAir = 0;

    finalBlockPos.add(new BlockPos(pos1.xCoord, pos1.yCoord, pos1.zCoord));
    finalBlockPos.add(new BlockPos(pos1.xCoord, pos1.yCoord + 1, pos1.zCoord));
    finalBlockPos.add(new BlockPos(pos1.xCoord, pos1.yCoord + 2, pos1.zCoord));

    if (Math.abs(dz) < 0.000001) {
      dz = 0.000001;
    }

    double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
    double stepX = dx / length;
    double stepY = dy / length;
    double stepZ = dz / length;

    double planeCoef = pos1.xCoord * dx + pos1.yCoord * dy + pos1.zCoord * dz;

    double xN = 0;
    double yN = 0;
    double zN = planeCoef / dz;
    double dxN = xN - pos1.xCoord;
    double dyN = yN - pos1.yCoord;
    double dzN = zN - pos1.zCoord;
    double lenN = Math.sqrt(dxN * dxN + dyN * dyN + dzN * dzN);
    dxN = dxN / lenN;
    dyN = dyN / lenN;
    dzN = dzN / lenN;

    double dxM = dy * dzN - dz * dyN;
    double dyM = dz * dxN - dx * dzN;
    double dzM = dx * dyN - dy * dxN;
    double cLen = Math.sqrt(dxM * dxM + dyM * dyM + dzM * dzM);
    dxM = dxM / cLen;
    dyM = dyM / cLen;
    dzM = dzM / cLen;

    for (int i = 0; i < length; i++) {
      for (int degree = 0; degree < 360; degree += 10) {
        double angle = degree * (Math.PI / 180);
        double dxP = dxN * Math.cos(angle) + dxM * Math.sin(angle);
        double dyP = dyN * Math.cos(angle) + dyM * Math.sin(angle);
        double dzP = dzN * Math.cos(angle) + dzM * Math.sin(angle);

        double newX = stepX * i + pos1.xCoord + dxP * cylRad;
        double newY = stepY * i + pos1.yCoord + dyP * cylRad;
        double newZ = stepZ * i + pos1.zCoord + dzP * cylRad;

        BlockPos block = new BlockPos(newX, newY, newZ);

        boolean found = false;

        for (int j = 0; j < finalBlockPos.size() - 1; j++) {
          if (
            finalBlockPos.get(j).getX() == block.getX() &&
            finalBlockPos.get(j).getY() == block.getY() &&
            finalBlockPos.get(j).getZ() == block.getZ()
          ) {
            found = true;
          }
        }

        if (!found) {
          if (ids.mc.theWorld.getBlockState(block).getBlock() == Blocks.air) {
            blockOAir++;
          } else {
            finalBlockPos.add(block);
          }
        }
      }
    }

    finalBlockPos.add(new BlockPos(pos2.xCoord, pos2.yCoord, pos2.zCoord));
    finalBlockPos.add(new BlockPos(pos2.xCoord, pos2.yCoord + 1, pos2.zCoord));
    finalBlockPos.add(new BlockPos(pos2.xCoord, pos2.yCoord + 2, pos2.zCoord));

    currBlocksOfAir = (int) blockOAir;

    if (cylRad > 0.6) {
      List<BlockPos> midList = findBlocks(pos1, pos2, 0.5);

      for (BlockPos block : midList) {
        if (!finalBlockPos.contains(block)) {
          finalBlockPos.add(block);
        }
      }
    }

    finalBlockPos.sort((a, b) -> {
      if (
        DistanceFromTo.distanceFromTo(a, BlockUtils.fromVec3ToBlockPos(pos1)) <
        DistanceFromTo.distanceFromTo(b, BlockUtils.fromVec3ToBlockPos(pos1))
      ) {
        return -1;
      } else if (
        DistanceFromTo.distanceFromTo(a, BlockUtils.fromVec3ToBlockPos(pos1)) ==
        DistanceFromTo.distanceFromTo(b, BlockUtils.fromVec3ToBlockPos(pos1))
      ) {
        return 0;
      } else {
        return 1;
      }
    });

    return finalBlockPos;
  }

  public static List<BlockPos> middle(List<BlockPos> alrAdded, Vec3 block1, Vec3 block2) {
    List<BlockPos> finalBlockPos = new ArrayList<>();

    double dx = block2.xCoord - block1.xCoord;
    double dy = block2.yCoord - block1.yCoord;
    double dz = block2.zCoord - block1.zCoord;

    if (Math.abs(dz) < 0.000001) {
      dz = 0.000001;
    }

    double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
    double stepX = dx / length;
    double stepY = dy / length;
    double stepZ = dz / length;

    for (int i = 0; i < length; i++) {
      BlockPos newBlock = new BlockPos(stepX * i + block1.xCoord, stepY * i + block1.yCoord, stepZ * i + block1.zCoord);

      if (ids.mc.theWorld.getBlockState(newBlock).getBlock() != Blocks.air && !alrAdded.contains(newBlock)) {
        finalBlockPos.add(newBlock);
      }
    }

    return finalBlockPos;
  }
}
