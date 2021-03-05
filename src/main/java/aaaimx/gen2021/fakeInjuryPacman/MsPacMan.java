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
	//Pacman info
	private int pcLocation;
	private MOVE pcLastMove;
	private int pcPowerTime;
	//Enemy info
	private GHOST[] edibleGhosts;
	private GHOST[] nonEdibleGhosts;
	//Map info
	private int [] remPPills;
	//Navigation info
	private MOVE[] pcMoves;
	private int[] route;
	private int routeIndex;
	//Engine stuff
	private int tickCount = 0;
    private MOVE[] allMoves = MOVE.values();
    private GHOST[] allGhosts = GHOST.values();
    
    private GHOST[] getEdibleGhosts(Game game) {
    	ArrayList<GHOST> ghosts = new ArrayList<GHOST>();
    	for(GHOST ghost : this.allGhosts) if (game.isGhostEdible(ghost)) ghosts.add(ghost);
    	return (GHOST[]) ghosts.toArray();
    }
    
    private ArrayList<GHOST> getNonEdibleGhosts(Game game) {
    	ArrayList<GHOST> ghosts = new ArrayList<GHOST>();
    	for(GHOST ghost : this.allGhosts) if (!game.isGhostEdible(ghost)) ghosts.add(ghost);
    	return ghosts;
    } 
    
    private void updateGameInfo(Game game) {
    	this.tickCount += 1;

    	this.pcLocation = game.getPacmanCurrentNodeIndex();
    	this.pcLastMove = game.getPacmanLastMoveMade();
    	
    	this.pcPowerTime = game.getGhostEdibleTime(GHOST.BLINKY); //TODO: Change ghost when BLINKY is eaten
    	remPPills = game.getActivePowerPillsIndices();
    	
    	pcMoves = game.getPossibleMoves(pcLocation, pcLastMove);
    }
    
    @Override
    public MOVE getMove(Game game, long timeDue) {
    	long start = System.nanoTime();
    	
    	
    	
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
    	//Compute distances to pills
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
    	//Navigate calculated route
    	MOVE move = game.getMoveToMakeToReachDirectNeighbour(pcLocation, route[routeIndex]);
    	return move;
    }
    
    private MOVE pursueGhosts(Game game) {
    	if(tickCount % 5 == 0) System.out.print("Pursue - ");
    	//Calculate route to nearest ghost
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
    	//Navigate calculated route
    	MOVE move = game.getMoveToMakeToReachDirectNeighbour(pcLocation, this.route[this.routeIndex]);
    	return move;
    }
    
    private void setAndConfigRoute(int[] route) {
    	this.route = route;
    	this.routeIndex = route.length - 1;
    }
}
