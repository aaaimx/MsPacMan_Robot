package aaaimx.gen2021.reactivePacman;

import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

import java.util.Random;

/*
 * The Class RandomPacMan.
 */
public final class MsPacMan extends PacmanController {
    private Random rnd = new Random();
    private MOVE[] allMoves = MOVE.values();

    @Override
    public MOVE getMove(Game game, long timeDue) {
        return allMoves[rnd.nextInt(allMoves.length)];
    }
}
