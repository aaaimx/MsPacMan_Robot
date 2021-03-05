package aaaimx.gen2021.fakeInjuryPacman;

import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Constants.GHOST;
import pacman.game.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

/*
 * Class MsPacMan that implements "Fake Injury" behavior
 */
public final class MsPacMan extends PacmanController {
	//Game info for easy access
	private int pcLocation;
	private MOVE pcLastMove;
	private int [] remPPills;
	private int pcPowerTime;
	private MOVE[] pcMoves;
	
	private int[] route;
	private int routeIndex;
	//Engine stuff
	private int tickCount = 0;
    private MOVE[] allMoves = MOVE.values();
    
    @Override
    public MOVE getMove(Game game, long timeDue) {
    	tickCount += 1;
    	//Update game info
    	remPPills = game.getActivePowerPillsIndices();
    	pcLocation = game.getPacmanCurrentNodeIndex();
    	pcLastMove = game.getPacmanLastMoveMade();
    	pcPowerTime = game.getGhostEdibleTime(GHOST.BLINKY);
    	pcMoves = game.getPossibleMoves(pcLocation, pcLastMove);
    	
    	MOVE nextMove;
    	if(pcPowerTime > 0) {
    		nextMove = getMovePursuit(game);
    	}else {
    		nextMove = getMovePPill(game);
    	}
    	tickCount += 1;
    	return nextMove;	
    }
    
    private MOVE getMovePPill(Game game) {
    	if(tickCount % 5 == 0) System.out.print("Go to Pill\n");
    	
    	int nearestPPillIndex;
    	//Get nearest PPill route every 5 ticks
    	if(tickCount % 5 == 0 || route==null) {
    		ArrayList<Integer> ppill_distances = new ArrayList<Integer>();
    		for (int i=0; i<remPPills.length; i++) {
    			ppill_distances.add(game.getShortestPathDistance(pcLocation, remPPills[i], pcLastMove));
    		}
    		nearestPPillIndex = Collections.max(ppill_distances);
    		route = game.getShortestPath(pcLocation, nearestPPillIndex, pcLastMove);
    		routeIndex = 0;
    	}
    	MOVE move = game.getMoveToMakeToReachDirectNeighbour(pcLocation, route[routeIndex]);
    	routeIndex += 1;  	
    	if(Arrays.asList(pcMoves).contains(move)) return move;
    	else return pcMoves[0];
    }
    
    private MOVE getMovePursuit (Game game){
    	if(tickCount % 5 == 0) System.out.print("Pursuit\n");
    	return MOVE.UP;
    }
}
