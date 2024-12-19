

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
                .setScaleFactor(3)
                .build();

        PacmanController pacMan = //new aaaimx.gen2021.fakeInjuryPacman.MsPacMan();
        		//new aaaimx.gen2023.team.MsPacMan();
        		new HumanController(new KeyBoardInput());
        		//new pacman.controllers.examples.PacManRandom();
        		//new es.ucm.fdi.ici.c2021.practica1.grupo01.MsPacMan();
        		//new es.ucm.fdi.ici.c2021.practica2.grupo09.MsPacMan();
        		//new es.ucm.fdi.ici.c2021.practica1.grupo03.MsPacMan();
        		//new aaaimx.gen2021.fakeInjuryPacman.MsPacMan();
        GhostController ghostsAAAIMX = new aaaimx.gen2021.reactiveGhosts.Ghosts();
        GhostController ghostsUCM = new IBC.UCM.Ghosts();
        //GhostController ghostsAgre = new IBC.agressive.AggressiveGhosts();
        GhostController ghostsAgre = new pacman.controllers.examples.GhostsRandom();
        //while(true) {
       // System.out.println("Partida 1");
       int match1= executor.runGame(pacMan, ghostsAgre, 100);
       //System.out.println("Partida 2");
       // int match2= executor.runGame(pacMan, ghostsUCM, 30);
        //System.out.println("Partida 3");
        //int match3= executor.runGame(pacMan, ghostsAAAIMX, 30);
        //System.out.println("Promedio");
        //System.out.println((match1+match2+match3)/3);
        //}
    }
}
