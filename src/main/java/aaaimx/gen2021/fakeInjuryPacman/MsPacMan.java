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
	//SHORTCUTS TO VITAL INFO ABOUT THE GAME
	//Engine stuff
	private int tickCount = 1;
	private long pacmanStartTime = System.nanoTime();
	private long pacmanPrevStartTime = System.nanoTime();
	private GHOST[] allGhosts = GHOST.values();
	//Pacman info
	private int pcLocation;
	private MOVE pcLastMove;
	private int pcPowerTime;
	//Enemy info
	private ArrayList<GHOST> edibleGhosts;
	private ArrayList<Integer> edibleGhostsLocations;
	private ArrayList<Integer> edibleGhostsDistances;
	private ArrayList<GHOST> nonEdibleGhosts;
	private ArrayList<Integer> nonEdibleGhostsLocations;
	private ArrayList<Integer> nonEdibleGhostsDistances;
	//Map info
	private int [] remPPillsLocations;
	private ArrayList<Integer> remPPillsDistances;
	//Navigation info
	private MOVE[] pcMoves;
	
    
    private void getEdibleGhostsInfo(Game game) {
    	ArrayList<GHOST> ghosts = new ArrayList<GHOST>();
    	ArrayList<Integer> locations = new ArrayList<Integer>();
    	ArrayList<Integer> distances = new ArrayList<Integer>();
    	for(GHOST ghost : this.allGhosts) {
    		if (game.isGhostEdible(ghost)) {
    			int location = game.getGhostCurrentNodeIndex(ghost);
    			int distance = game.getShortestPathDistance(location, this.pcLocation, game.getGhostLastMoveMade(ghost));
    			ghosts.add(ghost);
    			locations.add(location);
    			distances.add(distance);
    		}
    	}
    	this.edibleGhosts = ghosts;
    	this.edibleGhostsLocations = locations;
    	this.edibleGhostsDistances = distances;
    }
    
    private void getNonEdibleGhostsInfo(Game game) {
    	ArrayList<GHOST> ghosts = new ArrayList<GHOST>();
    	ArrayList<Integer> locations = new ArrayList<Integer>();
    	ArrayList<Integer> distances = new ArrayList<Integer>();
    	for(GHOST ghost : this.allGhosts) {
    		if (!game.isGhostEdible(ghost)) {
    			int location = game.getGhostCurrentNodeIndex(ghost);
    			int distance = game.getShortestPathDistance(location, this.pcLocation, game.getGhostLastMoveMade(ghost));
    			ghosts.add(ghost);
    			locations.add(location);
    			distances.add(distance);
    		}
    	}
    	this.nonEdibleGhosts = ghosts;
    	this.nonEdibleGhostsLocations = locations;
    } 
    
    private void updateGameInfo(Game game) {
    	this.tickCount += 1;
    	this.pacmanPrevStartTime = this.pacmanStartTime;
    	this.pacmanStartTime = System.nanoTime();

    	this.pcLocation = game.getPacmanCurrentNodeIndex();
    	this.pcLastMove = game.getPacmanLastMoveMade();
    	
    	this.getEdibleGhostsInfo(game);
    	this.getNonEdibleGhostsInfo(game);
    	
    	if (this.edibleGhosts.size() > 0) this.pcPowerTime = game.getGhostEdibleTime(edibleGhosts.get(0));
    	else this.pcPowerTime = 0;
    	
    	this.remPPillsLocations = game.getActivePowerPillsIndices();
    	ArrayList<Integer> distances = new ArrayList<Integer>();
    	for (int pPillLocation: this.remPPillsLocations) distances.add(game.getShortestPathDistance(this.pcLocation, pPillLocation, this.pcLastMove));
    	this.remPPillsDistances = distances;
    	
    	this.pcMoves = game.getPossibleMoves(pcLocation, pcLastMove);
    }
    
    private void printTickInfo() {
    	long pacmanTime = System.nanoTime() - this.pacmanStartTime;
    	long tickTime = this.pacmanStartTime - this.pacmanPrevStartTime;
    	System.out.println("Tick: " + this.tickCount + " -  Pacman Decision Time: " + pacmanTime/1000 + " us -  TickTime: " + tickTime/1000000 + " ms" );
    }
    
    @Override
    public MOVE getMove(Game game, long timeDue) {    	
    	//Update game info
    	this.updateGameInfo(game);
    	
    	//PACMAN MAIN LOGIC
    	//Select destination based on behavior
    	int destination = -1;
    	if(pcPowerTime > 0) destination = getNearestEdibleGhostLocation(game); //Pursue ghosts
    	else if (remPPillsLocations.length > 0) destination = getNearestPPillLocation(game); //Get to power pill
    	else destination = -1; //Eat remaining pills
    	//Navigate to destination
    	//TODO: Detect collisions with ghosts
    	MOVE move;
    	if(destination==-1) move = MOVE.UP;
    	else {
    		move = game.getNextMoveTowardsTarget(this.pcLocation, destination, Constants.DM.EUCLID);
    	}
    	
    	//Print tick info
    	this.printTickInfo();
    	
    	return move;
    }
    
    private int getNearestPPillLocation(Game game) {
    	if(this.tickCount % 5 == 0) System.out.print("GO TO PILL - ");
    	//Get nearest power pill location
		int nearestPPillLocation = -1;
		int nearestPPillDistance = 10000;
		for(int pPillLocation: this.remPPillsLocations) {
			int pPillDistance = game.getShortestPathDistance(this.pcLocation, pPillLocation, this.pcLastMove);
			if(pPillDistance < nearestPPillDistance) {
				nearestPPillLocation = pPillLocation;
				nearestPPillDistance = pPillDistance;
			}
		}
		return nearestPPillLocation;
    }
    
    private int getNearestEdibleGhostLocation(Game game) {
    	if(tickCount % 5 == 0) System.out.print("PURSUE GHOSTS - ");
		//Get nearest edible ghost location
		int nearestGhostLocation = -1;
		int nearestGhostDistance = 10000;
		for(Integer integerGhostLocation: this.edibleGhostsLocations) {
			int ghostLocation = integerGhostLocation.intValue();
			int ghostDistance = game.getShortestPathDistance(this.pcLocation, ghostLocation, this.pcLastMove);
			if (ghostDistance < nearestGhostDistance) {
				nearestGhostLocation = ghostLocation;
				nearestGhostDistance = ghostDistance;
			}
		}
		return nearestGhostLocation;
    }
    
    private MOVE getMoveToPursueGhosts(Game game) {
    	MOVE nextMove;
    	int destination = getNearestEdibleGhostLocation(game);
    	if(game.isJunction(this.pcLocation)) {
    		nextMove = getMoveToEvadeGhost(game, destination);
    	} else {
    		nextMove = game.getNextMoveTowardsTarget(this.pcLocation, destination, this.pcLastMove, Constants.DM.EUCLID);
    	}
    	return nextMove;
    }
    
    private MOVE getMoveToGoToPowerPill(Game game) {
    	MOVE nextMove;
    	int destination = getNearestPPillLocation(game);
    	if(game.isJunction(this.pcLocation)) {
    		nextMove = getMoveToEvadeGhost(game, destination);
    	} else {
    		nextMove = game.getNextMoveTowardsTarget(this.pcLocation, destination, this.pcLastMove, Constants.DM.EUCLID);
    	}
    	return nextMove;
    }
    
    private MOVE getMoveToEvadeGhost(Game game, int destination) {
    	//For each move MsPacman can make, calculate metrics about its path
    	ArrayList<ArrayList> allMetrics = new ArrayList<ArrayList>();
    	for(MOVE move: this.pcMoves) {
    		int origin = 0; //game.getReachableNodeIfMoveIsMade(move)
    		int[] metrics = calculatePathMetrics(game, origin, destination, move);
    		allMetrics.add(metrics);
    	}
    	//Select best move to make given alternate paths metrics
    	//Ranking criteria: No collision, highest score, longer collision distance  
    	MOVE bestMove;
    	ArrayList<ArrayList> nonCollisionMetrics = new ArrayList<ArrayList>();
    	ArrayList<MOVE> nonCollisionMoves = new ArrayList<MOVE>();
    	for(int i=0; i<allMetrics.size(); i++) {
    		if (allMetrics.get(i)[0] == -1) {
    			nonCollisionMetrics.add(metrics);
    			nonCollisionMoves.add(move);
    		}
    	}
    	//First ranking criteria. Evaluate scores for non collision paths or all paths
    	if(nonCollisionMetrics.size()>0) {
    		int highestScoreMove;
    		int highestScore = -1;
    		for(int i=0; i<nonCollisionMetrics.size(); i++) {
    			currentScore = nonCollisionMetrics.get(i)[1];
    			if (currentScore > highestScore) {
    				highestScore = currentScore;
    				highestScoreMove = nonCollisionMoves.get(i);
    			}
    		}
    		return highestScoreMove;
    	} else {
    		int highestScoreMove;
    		int highestScore = -1;
    		for(int i=0; i<allMetrics.size(); i++) {
    			currentScore = allMetrics.get(i)[1];
    			if (currentScore > highestScore) {
    				highestScore = currentScore;
    				highestScoreMove = this.pcMoves[i];
    			}
    		}
    		return highestScoreMove;
    	}
    }
    
    private int[] calculatePathMetrics(Game game, int location, int destination, MOVE lastMoveMade) {
    	//PARAMS
    	//Prevention range modulates area in which MsPacman will try to detect collisions
    	//k_steps regulates granularity of collision detection
    	int prevRangeStart = 5; int prevRangeEnd = 10; int kSteps = 5;
    	
    	//LOGIC
    	int[] path = game.getShortestPath(location, destination, lastMoveMade);
    	//distPO is distance from Pacman to objective, distGO is distance from ghost to objective
    	//For each partial objective...
    	for(int distPO=prevRangeStart - 1; distPO < path.length && distPO < prevRangeEnd; distPO += kSteps) {
    		ArrayList<Integer> collisionDistances = new ArrayList<Integer>();
    		//...for each ghost, detect future collitions
    		for(GHOST ghost: this.edibleGhosts) {
    			int ghostLocation = game.getGhostCurrentNodeIndex(ghost);
    			MOVE ghostLastMove = game.getGhostLastMoveMade(ghost);
    			int distGO = game.getShortestPathDistance(ghostLocation, path[distPO], ghostLastMove);
    			if (distGO < distPO) {
    				int collisionDistance = distGO + (((distPO - distGO) / 2) - 1);//-1 for more precise collision detection
    				if (collisionDistance >= 0) collisionDistances.add(collisionDistance); //TODO: Can a collision distance be negative?
    			}
    		}
    		//If collisions happened, calculate metrics for path (take closest collision in account)
    		if (collisionDistances.size() > 0) {
    			int score = 0;
    			int minCollitionDistance = Collections.min(collisionDistances);
    			for(int i = 0; i < minCollitionDistance; i++) if(game.getPillIndex(path[i]) != -1) score += 10;
    			int[] metrics = {1, minCollitionDistance, score};
    			return metrics;
    		}
    	}
    	//If code got here, there were no collisions, but metrics are still calculated
    	int score = 0;
		for(int i = 0; i < prevRangeEnd; i++) if(game.getPillIndex(path[i]) != -1) score += 10;
		int[] metrics = {-1, prevRangeEnd, score};
		return metrics;
    }
    
    //The following methods are heuristics. Will be added in the next sprint.
    private int getMostIdealPowerPillLocation(Game game) {
    	return 0;
    }
    
    private int getMostIdealEdibleGhostLocation(Game game) {
    	return 0;
    }
    
}
