package com.dillo.pathfinding.stevebot.core.pathfinding.actions.playeractions;

import com.dillo.pathfinding.stevebot.core.data.blockpos.BaseBlockPos;
import com.dillo.pathfinding.stevebot.core.data.blockpos.FastBlockPos;
import com.dillo.pathfinding.stevebot.core.data.modification.Modification;
import com.dillo.pathfinding.stevebot.core.misc.Direction;
import com.dillo.pathfinding.stevebot.core.misc.ProcState;
import com.dillo.pathfinding.stevebot.core.misc.StateMachine;
import com.dillo.pathfinding.stevebot.core.pathfinding.actions.ActionCosts;
import com.dillo.pathfinding.stevebot.core.pathfinding.actions.ActionFactory;
import com.dillo.pathfinding.stevebot.core.pathfinding.actions.ActionObserver;
import com.dillo.pathfinding.stevebot.core.pathfinding.actions.ActionUtils;
import com.dillo.pathfinding.stevebot.core.pathfinding.nodes.Node;
import com.dillo.pathfinding.stevebot.core.pathfinding.nodes.NodeCache;
import com.dillo.pathfinding.stevebot.core.player.PlayerUtils;

public class ActionPillarUp extends Action {

  private enum State {
    SLOWING_DOWN,
    JUMPING,
    LANDING,
  }

  private enum Transition {
    SLOW_ENOUGH,
    PLACED_BLOCK,
  }

  private final StateMachine<State, Transition> stateMachine = new StateMachine<>();
  private final Modification[] modifications;

  private ActionPillarUp(Node from, Node to, double cost, Modification[] modifications) {
    super(from, to, cost);
    this.modifications = modifications;
    stateMachine.defineTransition(State.SLOWING_DOWN, Transition.SLOW_ENOUGH, State.JUMPING);
    stateMachine.defineTransition(State.JUMPING, Transition.PLACED_BLOCK, State.LANDING);
    stateMachine.addListener(Transition.SLOW_ENOUGH, ((previous, next, transition) -> onSlowEnough()));
  }

  @Override
  public void resetAction() {
    stateMachine.setState(State.SLOWING_DOWN);
  }

  @Override
  public ProcState tick(boolean firstTick) {
    ActionObserver.tickAction(this.getActionName());
    switch (stateMachine.getState()) {
      case SLOWING_DOWN:
        {
          return tickSlowDown();
        }
      case JUMPING:
        {
          return tickJump();
        }
      case LANDING:
        {
          return tickLand();
        }
      default:
        {
          return ProcState.FAILED;
        }
    }
  }

  /**
   * Prepare by slowing down enough.
   */
  private ProcState tickSlowDown() {
    boolean slowEnough = PlayerUtils.getMovement().slowDown(0.075);
    if (slowEnough) {
      stateMachine.fireTransition(Transition.SLOW_ENOUGH);
    } else {
      PlayerUtils
        .getCamera()
        .setLookAt(getTo().getPos().getX(), getTo().getPos().getY(), getTo().getPos().getZ(), true);
    }
    return ProcState.EXECUTING;
  }

  private void onSlowEnough() {
    PlayerUtils.getCamera().enableForceCamera();
  }

  /**
   * Pillar up by jumping and placing a block below.
   */
  private ProcState tickJump() {
    PlayerUtils.getMovement().moveTowards(getTo().getPos(), true);
    if (PlayerUtils.getPlayerBlockPos().equals(getFrom().getPos())) {
      PlayerUtils.getInput().setJump();
    }
    if (PlayerUtils.getPlayerBlockPos().equals(getTo().getPos())) {
      if (!PlayerUtils.getInventory().selectThrowawayBlock(true)) {
        return ProcState.FAILED;
      }
      ActionUtils.placeBlockAgainst(getFrom().getPosCopy().add(Direction.DOWN), Direction.UP);
      stateMachine.fireTransition(Transition.PLACED_BLOCK);
    }
    return ProcState.EXECUTING;
  }

  /**
   * Land on the new block.
   */
  private ProcState tickLand() {
    if (PlayerUtils.isOnGround() && PlayerUtils.getPlayerBlockPos().equals(getTo().getPos())) {
      PlayerUtils.getCamera().disableForceCamera(true);
      return ProcState.DONE;
    } else {
      return ProcState.EXECUTING;
    }
  }

  @Override
  public boolean isOnPath(BaseBlockPos position) {
    return position.equals(getFrom().getPos()) || position.equals(getTo().getPos());
  }

  @Override
  public boolean hasModifications() {
    return true;
  }

  @Override
  public Modification[] getModifications() {
    return this.modifications;
  }

  @Override
  public String getActionName() {
    return "pillar-up";
  }

  public static class PillarUpFactory implements ActionFactory {

    @Override
    public Action createAction(Node node, Result result) {
      return new ActionPillarUp(node, result.to, result.estimatedCost, result.modifications);
    }

    @Override
    public Result check(Node node) {
      // check inventory
      if (!PlayerUtils.getActiveSnapshot().hasThrowawayBlockInHotbar(true)) {
        return Result.invalid();
      }

      // check to-position
      final FastBlockPos to = node.getPosCopy().add(Direction.UP);
      if (!ActionUtils.canMoveThrough(to)) {
        return Result.invalid();
      }

      // check from-position
      if (!ActionUtils.canStandAt(node.getPos())) {
        return Result.invalid();
      }

      // build valid result
      final int indexThrowaway = PlayerUtils.getActiveSnapshot().findThrowawayBlock(true);
      final Modification[] modifications = new Modification[] {
        Modification.placeBlock(node.getPos(), PlayerUtils.getActiveSnapshot().getAsBlock(indexThrowaway)),
      };
      return Result.valid(Direction.UP, NodeCache.get(to), ActionCosts.get().PILLAR_UP, modifications);
    }

    @Override
    public Direction getDirection() {
      return Direction.UP;
    }

    @Override
    public Class<ActionPillarUp> producesAction() {
      return ActionPillarUp.class;
    }
  }
}
