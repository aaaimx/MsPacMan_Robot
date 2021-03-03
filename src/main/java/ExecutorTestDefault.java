

import pacman.Executor;
import pacman.controllers.GhostController;
import pacman.controllers.PacmanController;
import pacman.controllers.HumanController;
import pacman.game.internal.POType;
import pacman.controllers.KeyBoardInput;


public class ExecutorTestDefault {

    public static void main(String[] args) {
        Executor executor = new Executor.Builder()
                .setTickLimit(4000)
                .setGhostPO(false)
                .setPacmanPO(false)
                .setPacmanPOvisual(false) 
                .setVisual(true)
                .setPOType(POType.RADIUS)
                .setScaleFactor(1.5)
                .build();

        PacmanController pacMan = new HumanController(new KeyBoardInput());
        		//new pacman.controllers.examples.PacManRandom();
        GhostController ghosts = new pacman.controllers.examples.GhostsRandom();
        
        System.out.println( 
        		executor.runGame(pacMan, ghosts, 40)
        );
        
    }
}
