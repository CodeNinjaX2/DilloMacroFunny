package com.dillo.main.scan.Utils.objects;

import com.google.gson.annotations.Expose;
import java.awt.*;
import net.minecraft.block.state.IBlockState;

public class ColorBlockState {

  @Expose
  public IBlockState blockState;

  @Expose
  public Color color;

  public ColorBlockState(IBlockState blockState, Color color) {
    this.blockState = blockState;
    this.color = color;
  }
}
