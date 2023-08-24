

import pacman.Executor;
import pacman.controllers.GhostController;
import pacman.controllers.PacmanController;
import pacman.controllers.HumanController;
import pacman.game.internal.POType;
import pacman.controllers.KeyBoardInput;


public class TestRobot {

    public static void main(String[] args) {
        Executor executor = new Executor.Builder()
                .setTickLimit(4000)
                .setGhostPO(false)
                .setPacmanPO(false)
                .setPacmanPOvisual(false) 
                .setVisual(true)
                .setPOType(POType.RADIUS)
                .setScaleFactor(3)
                .setConnectRobot(true)
                .build();

        PacmanController pacMan = new aaaimx.gen2021.fakeInjuryPacman.MsPacMan();

        GhostController ghostsAgre = new IBC.agressive.AggressiveGhosts();
        int match1= executor.runGame(pacMan, ghostsAgre, 30);

        
    }
}
