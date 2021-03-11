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
    
}
