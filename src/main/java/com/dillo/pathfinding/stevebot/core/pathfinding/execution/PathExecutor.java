package com.dillo.pathfinding.stevebot.core.pathfinding.execution;

import com.dillo.events.DonePathEvent;
import com.dillo.events.PlayerLocChangeEvent;
import com.dillo.events.utilevents.RouteClearDoneWalking;
import com.dillo.pathfinding.stevebot.core.data.blockpos.BaseBlockPos;
import com.dillo.pathfinding.stevebot.core.minecraft.MinecraftAdapter;
import com.dillo.pathfinding.stevebot.core.misc.Config;
import com.dillo.pathfinding.stevebot.core.misc.ProcState;
import com.dillo.pathfinding.stevebot.core.misc.StevebotLog;
import com.dillo.pathfinding.stevebot.core.misc.TransitionListener;
import com.dillo.pathfinding.stevebot.core.pathfinding.actions.playeractions.Action;
import com.dillo.pathfinding.stevebot.core.pathfinding.goal.Goal;
import com.dillo.pathfinding.stevebot.core.pathfinding.path.EmptyPath;
import com.dillo.pathfinding.stevebot.core.pathfinding.path.Path;
import com.dillo.pathfinding.stevebot.core.pathfinding.path.PathFactory;
import com.dillo.pathfinding.stevebot.core.player.PlayerCamera;
import com.dillo.pathfinding.stevebot.core.player.PlayerUtils;
import com.dillo.pathfinding.stevebot.core.rendering.Color;
import com.dillo.pathfinding.stevebot.core.rendering.Renderer;
import com.dillo.pathfinding.stevebot.core.rendering.renderables.DynPointCollectionRenderObject;
import com.dillo.utils.previous.SendChat;
import com.dillo.utils.previous.random.ids;
import net.minecraftforge.common.MinecraftForge;

public class PathExecutor
  implements
    TransitionListener<PathExecutionStateMachine.ExecutionState, PathExecutionStateMachine.ExecutionTransition> {

  public static long pathTicks = 0;

  private final PathFactory pathFactory;
  public final PathExecutionStateMachine stateMachine = new PathExecutionStateMachine();
  private final PathCrawler crawler = new PathCrawler();

  private boolean isExecuting = false;
  private boolean follow = false;
  private boolean firstTick = true;

  private static final int OFF_PATH_THRESHOLD = 64;
  private int offPathCounter = 0;

  //private final Renderer renderer;
  //private Renderable pathRenderable;
  //private final Renderable startRenderable;
  //private final Renderable goalRenderable;
  private final DynPointCollectionRenderObject pathTraceRenderable = new DynPointCollectionRenderObject(3);

  private PathExecutionListener pathListener;

  public PathExecutor(MinecraftAdapter minecraftAdapter, BaseBlockPos posStart, Goal goal, Renderer renderer) {
    this.pathFactory = new PathFactory(minecraftAdapter, posStart, goal);
    this.stateMachine.addListener(this);
    //this.goalRenderable = goal.createRenderable();
    //this.startRenderable = new BoxRenderObject(posStart, 3, Color.YELLOW);
    /*this.renderer = renderer;
    renderer.addRenderable(goalRenderable);
    renderer.addRenderable(startRenderable);
    renderer.addRenderable(pathTraceRenderable);*/
  }

  /**
   * @param listener the {@link PathExecutionListener} listening to this {@link PathExecutor}.
   */
  public void setPathListener(PathExecutionListener listener) {
    this.pathListener = listener;
  }

  /**
   * Starts execution the specified path.
   */
  public void start() {
    StevebotLog.log(
      "Starting Path from " +
      pathFactory.getPosStart().getX() +
      " " +
      pathFactory.getPosStart().getY() +
      " " +
      pathFactory.getPosStart().getZ() +
      " to " +
      pathFactory.getGoal().goalString()
    );
    isExecuting = true;
  }

  /**
   * Stops the execution the specified path. It can not be restarted.
   */
  public void stop() {
    PlayerUtils.getInput().stopAll();
    isExecuting = false;
    if (!Config.isKeepPathRenderable()) {
      /*renderer.removeRenderable(goalRenderable);
      renderer.removeRenderable(startRenderable);
      renderer.removeRenderable(pathRenderable);
      renderer.removeRenderable(pathTraceRenderable);*/
    }
    onFinished();
  }

  /**
   * Start following the path.
   *
   * @param enableFreelook if freelook should be enabled automatically
   */
  public void startFollowing(boolean enableFreelook) {
    if (enableFreelook) {
      PlayerUtils.getCamera().setState(PlayerCamera.CameraState.FREELOOK);
    }
    follow = true;
  }

  /**
   * Called when the path is completed, failed or stopped
   */
  public void onFinished() {
    if (pathListener != null) {
      pathListener.onFinished();
    }
  }

  public void onClientTick() {
    if (!isExecuting) {
      System.out.println("STOP");
      return;
    }

    switch (stateMachine.getState()) {
      case PREPARE_EXECUTION:
        {
          pathFactory.prepareNextPath();
          stateMachine.fireTransition(PathExecutionStateMachine.ExecutionTransition.START);
          onClientTick();
          break;
        }
      case WAITING_TO_START:
        {
          if (follow) {
            stateMachine.fireTransition(PathExecutionStateMachine.ExecutionTransition.START_FOLLOW);
            onClientTick();
          }
          break;
        }
      case WAITING_FOR_SEGMENT:
        {
          pathTicks = 0;
          if (pathFactory.hasPath()) {
            if (
              pathFactory.getCurrentPath() instanceof EmptyPath || pathFactory.getCurrentPath().getNodes().size() <= 1
            ) {
              stateMachine.fireTransition(PathExecutionStateMachine.ExecutionTransition.REACHED_END_OF_PATH);
            } else {
              crawler.startPath(pathFactory.getCurrentPath());
              stateMachine.fireTransition(PathExecutionStateMachine.ExecutionTransition.SEGMENT_CALCULATED);
            }
            onClientTick();
          } else {
            PlayerUtils.getInput().stopAll();
          }
          break;
        }
      case FOLLOWING:
        {
          final ProcState state = tick();

          if (state == ProcState.FAILED) {
            stateMachine.fireError();
            onClientTick();
          }

          if (state == ProcState.DONE) {
            StevebotLog.log("Done following segment  (" + pathTicks + ")");
            MinecraftForge.EVENT_BUS.post(new RouteClearDoneWalking());
            Path path = pathFactory.getCurrentPath();
            if (path.reachedGoal() || path instanceof EmptyPath) {
              pathFactory.removeCurrentPath();
              stateMachine.fireTransition(PathExecutionStateMachine.ExecutionTransition.REACHED_END_OF_PATH);
            } else {
              pathFactory.prepareNextPath();
              pathFactory.removeCurrentPath();
              stateMachine.fireTransition(PathExecutionStateMachine.ExecutionTransition.REACHED_END_OF_SEGMENT);
            }
            onClientTick();
          }
          break;
        }
      case DONE:
        {
          StevebotLog.log("Done following path.");
          stop();
          break;
        }
      case ERROR:
        {
          StevebotLog.log("Failed to follow path.");
          stop();
          break;
        }
    }
  }

  @Override
  public void onTransition(
    PathExecutionStateMachine.ExecutionState previous,
    PathExecutionStateMachine.ExecutionState next,
    PathExecutionStateMachine.ExecutionTransition transition
  ) {
    if (transition == PathExecutionStateMachine.ExecutionTransition.SEGMENT_CALCULATED) {
      //renderer.removeRenderable(pathRenderable);
      Path path = pathFactory.getLastPath();
      //pathRenderable = Path.toRenderable(path);
      //renderer.addRenderable(pathRenderable);
    }
  }

  /**
   * Update the current action. If the action is completed the {@link PathCrawler} will step to the next action.
   *
   * @return the resulting {@link ProcState} of this tick
   */
  private ProcState tick() {
    if (checkPathFailed()) {
      return ProcState.FAILED;
    }

    PlayerUtils.getInput().stopAll();

    final Action currentAction = crawler.getCurrentNodeTo().getAction();
    if (firstTick) {
      currentAction.resetAction();
    }
    ProcState actionState = currentAction.tick(firstTick);
    pathTicks++;
    firstTick = false;
    pathTraceRenderable.addPoint(PlayerUtils.getPlayerPosition(), Color.MAGENTA);

    if (actionState == ProcState.FAILED) {
      currentAction.onActionFinished(ProcState.FAILED);
      return ProcState.FAILED;
    }
    if (actionState == ProcState.DONE) {
      currentAction.onActionFinished(ProcState.DONE);
      firstTick = true;
      boolean hasNext = crawler.nextAction();
      if (!hasNext) {
        return ProcState.DONE;
      }
    }
    return ProcState.EXECUTING;
  }

  /**
   * Checks whether the player is off the given path for too long.
   *
   * @return true, if the path failed
   */
  private boolean checkPathFailed() {
    final boolean onPath = crawler.getCurrentNodeTo().getAction().isOnPath(PlayerUtils.getPlayerBlockPos());
    if (onPath) {
      offPathCounter = 0;
    } else {
      offPathCounter++;
    }
    return offPathCounter > OFF_PATH_THRESHOLD;
  }
}
