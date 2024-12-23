package pacman.controllers.examples;

import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

import java.util.Random;

/*
 * The Class RandomPacMan.
 */
public final class PacManRandom extends PacmanController {
    private Random rnd = new Random();
    private MOVE[] allMoves = MOVE.values();

    @Override
    public MOVE getMove(Game game, long timeDue) {
    	int node = game.getPacmanCurrentNodeIndex();
    	System.out.println("nodo : "+node);
        return allMoves[rnd.nextInt(allMoves.length)];
    }
}