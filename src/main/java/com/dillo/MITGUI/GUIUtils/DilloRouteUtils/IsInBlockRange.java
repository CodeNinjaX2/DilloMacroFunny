package com.dillo.MITGUI.GUIUtils.DilloRouteUtils;

import static com.dillo.MITGUI.GUIUtils.MatchServer.IsChecked.isChecked;

import com.dillo.dilloUtils.BlockUtils.fileUtils.localizedData.currentRoute;
import com.dillo.utils.DistanceFromTo;
import com.dillo.utils.previous.random.ids;

public class IsInBlockRange {

  public static boolean isInCheckRange() {
    if (
      currentRoute.currentRoute.size() < 1 ||
      DistanceFromTo.distanceFromTo(ids.mc.thePlayer.playerLocation, currentRoute.currentRoute.get(0)) > 120 ||
      isChecked()
    ) {
      return false;
    }

    return true;
  }
}
