package aaaimx.gen2021.fakeInjuryPacman;

import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Constants.GHOST;
import pacman.game.Constants;
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
	private int pcPowerTime;
	private int [] remPPills;
	//Navigation info
	private MOVE[] pcMoves;
	private int[] route;
	private int routeIndex;
	//Engine stuff
	private int tickCount = 0;
    private MOVE[] allMoves = MOVE.values();
    private GHOST[] ghosts = GHOST.values();
    
    @Override
    public MOVE getMove(Game game, long timeDue) {
    	long start = System.nanoTime();
    	
    	tickCount += 1;
    	//Update game info
    	remPPills = game.getActivePowerPillsIndices();
    	pcLocation = game.getPacmanCurrentNodeIndex();
    	pcLastMove = game.getPacmanLastMoveMade();
    	//TODO: Change ghost when BLINKY is eaten
    	pcPowerTime = game.getGhostEdibleTime(GHOST.BLINKY);
    	pcMoves = game.getPossibleMoves(pcLocation, pcLastMove);
    	
    	//if (tickCount % 5 == 0) System.out.println();
    		
    	MOVE nextMove;
    	if(pcPowerTime > 0) {
    		nextMove = pursueGhosts(game);
    	} else {
    		if (remPPills.length > 0) {
    			nextMove = goToPPill(game);
    		} else {
    			nextMove = this.pcMoves[0];
    		}
    	}
    	//TODO: Detect collisions with ghosts
    	
    	//Update engine info
    	tickCount += 1;
    	routeIndex -= 1;
    	
    	long time = System.nanoTime() - start;
    	System.out.println("TIME:" + time/1000000 + " ms");
    	
    	return nextMove;	
    }
    
    private MOVE goToPPill(Game game) {
    	if(tickCount % 5 == 0) System.out.print("Go to Pill - ");
    	//Calculate route to nearest power pill (every 5 ticks, to salvage computing time)
    	if(tickCount % 2 == 0 || route==null) {
    		ArrayList<Integer> ppill_distances = new ArrayList<Integer>();
    		//TODO: Translate to for each
    		for (int i=0; i<remPPills.length; i++) {
    			ppill_distances.add(game.getShortestPathDistance(pcLocation, remPPills[i], pcLastMove));
    		}
    		int nearestPPillIndex = 0;
    		for (int i=1; i<ppill_distances.size(); i++) {
    			if (ppill_distances.get(i) < ppill_distances.get(nearestPPillIndex)) nearestPPillIndex = i;
    		}
    		this.setAndConfigRoute(game.getShortestPath(pcLocation, nearestPPillIndex, pcLastMove));
    	}
    	//Navigate calculated route
    	MOVE move = game.getMoveToMakeToReachDirectNeighbour(pcLocation, route[routeIndex]);
    	return move;
    	//if(Arrays.asList(pcMoves).contains(move)) return move;
    	//else return pcMoves[0];
    }
    
    private MOVE pursueGhosts(Game game) {
    	if(tickCount % 5 == 0) System.out.print("Pursue - ");
    	//Calculate route to nearest ghost (every 5 ticks, to salvage computing time)
    	if(tickCount % 2 == 0 || route==null) {
    		//Get edible ghosts
    		ArrayList<GHOST> edibleGhosts = new ArrayList<GHOST>();
    		for(GHOST ghost: ghosts) if (game.isGhostEdible(ghost)) edibleGhosts.add(ghost);
    		//Get nearest edible ghost location
    		//TODO: Handle situation where no ghost is edible
    		int nearestGhostLocation = game.getGhostCurrentNodeIndex(edibleGhosts.get(0));
    		int nearestGhostDistance = game.getShortestPathDistance(pcLocation, nearestGhostLocation);
    		for(int i=1; i<edibleGhosts.size(); i++) {
    			int ghostLocation = game.getGhostCurrentNodeIndex(edibleGhosts.get(i));
    			int ghostDistance = game.getShortestPathDistance(pcLocation, ghostLocation, pcLastMove);
    			if (ghostDistance < nearestGhostDistance) {
    				nearestGhostLocation = ghostLocation;
    				nearestGhostDistance = ghostDistance;
    			}
    		}
    		//Calculate route to nearest ghost
    		int[] routeToGhost = game.getShortestPath(pcLocation, nearestGhostLocation, pcLastMove);
    		this.setAndConfigRoute(routeToGhost);
    	}
    	//Navigate calculated route
    	MOVE move = game.getMoveToMakeToReachDirectNeighbour(pcLocation, this.route[this.routeIndex]);
    	if(Arrays.asList(this.pcMoves).contains(move)) return move;
    	else return this.pcMoves[0];
    }
    
    private void setAndConfigRoute(int[] route) {
    	this.route = route;
    	this.routeIndex = route.length - 1;
    }
}
