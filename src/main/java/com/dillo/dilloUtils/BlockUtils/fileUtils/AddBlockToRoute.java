package com.dillo.dilloUtils.BlockUtils.fileUtils;

import java.io.File;
import net.minecraft.util.BlockPos;

public class AddBlockToRoute {

  public static void addBlockToRoute(BlockPos block, File file) {
    WriteFile.writeFile(file, block);
  }
}
