package IBC.UCM;

import java.awt.Color;
import java.util.EnumMap;
//import java.util.Random;

import pacman.controllers.GhostController;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class Ghosts  extends GhostController{

	
	private EnumMap<GHOST, MOVE> moves = new EnumMap<GHOST, MOVE>(GHOST.class);

	private int limit = 40; //40
	private int ghostsNearLimit = 19; //30(3038), 20 (2826)
	private int ghostNearLimitLeave = 19;
	
	private Constants.DM distanceUnit;
	
	public Ghosts(){
		distanceUnit = Constants.DM.PATH;
	}
	
	//Coordenadas de un fantasma
	private class Coords{
		public int x,y;
	}
	/**
	 * Obtener las coordenadas de un fantasma (x,y) de su nodo
	 * @param nodeIndex
	 * @param game
	 * @return
	 */
	private Coords getCoords(int nodeIndex, Game game) {
		Coords c = new Coords();
		c.x = game.getNodeXCood(nodeIndex);
		c.y = game.getNodeYCood(nodeIndex);
		return c;
	}
	
	@Override
	public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
		
		moves.clear();
		int pacman = game.getPacmanCurrentNodeIndex();
		int [] powerpills = game.getActivePowerPillsIndices();

		//Para cada fantasma si tiene que huir cuando pacman va hacia una powepill o no
		boolean[] close = new boolean[GHOST.values().length];
		
		//Si pacman est� cerca de una powerpill
		boolean closePacman = false;
		
		//Powepill m�s cercana (se puede hacer considerando el movimiento)
		int nearestPowerPill = game.getClosestNodeIndexFromNodeIndex(pacman, powerpills, distanceUnit);
		
		
		//Si existe powepill y la distancia es menor que el l�mite ponemos que pacman est� cerca, para cada fantasma va a mirar si 
		//tiene que huir o si por el contrario est� lo suficientemente cerca como para llegar antes que Mspacman a la powepill
		//y se la puede comer 
		
		if((nearestPowerPill !=  -1) && game.getDistance(pacman, nearestPowerPill, game.getPacmanLastMoveMade(), distanceUnit) <= limit) {
			int i = 0;
			closePacman = true;

			for(GHOST ghostType: GHOST.values()) {
				
				//se actualiza el array 'close' en todas las posiciones. Por cada fantasma:
				// false -> si el fantasma ha salido de la carcel
				//			y su camino hasta la power pill es m�s corto que el camino de MsPacMan hasta la power pill
				//			y el camino de pacman a la powerpill sea m�s corto que el de pacman hasta el fantasma (es decir que el fantasma
				//			no se encuentre entre MsPacMan y la powerpill)
				if(
						game.getGhostLairTime(ghostType) == 0 &&
						game.getDistance(game.getGhostCurrentNodeIndex(ghostType), nearestPowerPill, game.getGhostLastMoveMade(ghostType), distanceUnit) < 
						game.getDistance(pacman, nearestPowerPill, game.getPacmanLastMoveMade(), distanceUnit) &&
						game.getDistance(pacman, nearestPowerPill, game.getPacmanLastMoveMade(), distanceUnit) < //sujeto a cambios
						game.getDistance(pacman, game.getGhostCurrentNodeIndex(ghostType), game.getPacmanLastMoveMade(), distanceUnit)
				) {
					close[i] = false;
				}else {
					close[i] = true;
				}
				i++;
			}
			
			//GameView.addLines(game,Color.CYAN,pacman, nearestPowerPill);
		}
		
		//Para cada fantasma
		int j = 0;
		for (GHOST ghostType : GHOST.values()) {
			
			//Si el fantasma se encuentra en una intersecci�n
			if (game.doesGhostRequireAction(ghostType)) {
				
				int ghostNode = game.getGhostCurrentNodeIndex(ghostType);
				
				MOVE nextMove;
				//Si el fantasma es comestible o existe una powerpill cerca de  y willRun devuelve true
				//(willRun mira si estando comestible el fantasma debe huir o no de msPacMan por distancia) 
				if((game.isGhostEdible(ghostType) || close[j]) && willRun( game, ghostNode, ghostType, pacman)) {
					
					//Movimiento para escapar de MsPacman
					nextMove = ifEscaping(game, ghostType, nearestPowerPill, ghostNode, pacman, close[j], closePacman);
					
				} else {
					
					//Movimiento para ir hacia MsPacman
					nextMove = ifFollowing(game, ghostType, nearestPowerPill, ghostNode, pacman, close[j], closePacman); 
					
				}
				
				moves.put(ghostType, nextMove);
				
			}
			
			j++;
		}
		return moves;
	}
	
	
	/**Devuelve si un fantasma tiene o no que huir estando comestible
	 * por ejemplo, si un fantasma se encuentra lo suficientemente lejos de msPacMan (no lo puede alcanzar antes de que deje de ser comestible)
	 *  aunque est� comestible se va a acercar a ella para poder perseguirla mejor cuando deje de serlo
	 * 
	 * @param game
	 * @param ghostNode
	 * @param ghostType
	 * @param pacman
	 * @return
	 */
	public boolean willRun(Game game, int ghostNode, GHOST ghostType, int pacman) {
		int time = game.getGhostEdibleTime(ghostType);
		if(game.isGhostEdible(ghostType) && (game.getDistance(pacman, ghostNode, game.getPacmanLastMoveMade(), distanceUnit)*2/3 > time)
				) { //time = distancia*2/3, 1 velocidad Pacman + 1/2 velocidad Fantasma
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * M�todo que devuelve un movimiento para acercarnos a MsPacman cuando la estamos siguiendo
	 * @param game
	 * @param ghostType
	 * @param nearestPowerPill
	 * @param ghostNode
	 * @param pacman
	 * @param close
	 * @param closePacman
	 * @return
	 */
	public MOVE ifFollowing(Game game, GHOST ghostType, int nearestPowerPill, int ghostNode, int pacman, boolean close, boolean closePacman)
	{

		MOVE nextMove = null;
		
		//Si Mspacman est� cerca de una powepill (closePacman) pero un fantasma est� m�s cerca !close hacer el siguiente movimiento hacia la powepill
		if(
				closePacman &&
				!close &&
				game.getDistance(ghostNode, nearestPowerPill, game.getGhostLastMoveMade(ghostType), distanceUnit) > 0
		) {
			//vVmos hacia la powerpill para intentar comernos a Mspacman
			nextMove = game.getApproximateNextMoveTowardsTarget(ghostNode, nearestPowerPill, game.getGhostLastMoveMade(ghostType), distanceUnit);
		
			
			/*
			 * Si un fantasma est� muy cerca de otro Y los dos van por el mismo camino, en la siguiente intersecci�n que 
			 * encuentre dicho fantasma, ir� por otro lado. 
			 * Si no devolvemos el movimiento que se acerque m�s al pacman o el de la intersecci�n m�s cercana
			 */
			
		} else { 
			Coords cGhost = getCoords(ghostNode, game);
			
			
			for(GHOST g: GHOST.values()) {
				
				if( ghostType == g || game.getGhostLairTime(g) > 0 ) continue;
				
				int gIndex = game.getGhostCurrentNodeIndex(g);
				Coords cG= getCoords(gIndex, game);
				
				if(	
						//se encuentran en el mismo eje
						(cGhost.x == cG.x || cGhost.y == cG.y) &&
						//est�n cerca el uno del otro
						game.getDistance(ghostNode, gIndex, game.getGhostLastMoveMade(ghostType), distanceUnit) < ghostsNearLimit &&
						//Si el otro fantasma es tambi�n no comestible
						!game.isGhostEdible(g)
				) {
					//Los posible indices a los que nos podemos mover
					int[] posibleIndices = game.getNeighbouringNodes(ghostNode, game.getGhostLastMoveMade(ghostType));
				
					double distMin = Double.MAX_VALUE;
					double distTemp;
					MOVE dontMake = game.getGhostLastMoveMade(g);
					
					//Caso de que exista un fantasma encima de otro evitamos realizar los mismos movimientos
					if(moves.containsKey(g)) {
						dontMake = moves.get(g);
					}
					
					for(int i = 0; i< posibleIndices.length; i++) {
						if(game.getMoveToMakeToReachDirectNeighbour(ghostNode, posibleIndices[i]) != dontMake) {
							
							distTemp = game.getDistance(pacman, posibleIndices[i], game.getPacmanLastMoveMade(), distanceUnit);
							
							if(distTemp < distMin) {
								distMin = distTemp;
								nextMove = game.getMoveToMakeToReachDirectNeighbour(ghostNode, posibleIndices[i]);
								
							}
							
						}
					}
					
				}
			}
			
			//Si no est� cerca de otro fantasma 
			if(nextMove == null ) {
				//Intersecci�n a la que se dirije pacman
				int pcNextJunc = this.getIndexOfNextPacmanJunction(game, pacman);
				
				//Si la distancia a la intersecci�n a la que se dirije pacman es menor que la distancia de pacman a esa intersecci�n ir en esa
				//direcci�n y la intersecci�n es distinta a en la que nos encontramos
				if(
					(pcNextJunc != ghostNode) &&
					game.getDistance(ghostNode, pcNextJunc, game.getGhostLastMoveMade(ghostType), distanceUnit) < game.getDistance(pacman, pcNextJunc, game.getPacmanLastMoveMade(), distanceUnit)
				){
					nextMove = game.getApproximateNextMoveTowardsTarget(ghostNode, pcNextJunc, game.getGhostLastMoveMade(ghostType), distanceUnit);
				}else {
					
					//Si no, nos acercamos a pacman
					//En vez de usar la funci�n game.getAproximateNextMoveTowardsTarget que aparece comentada buscamos nosotros
					//el nodo m�s cercano, usando la distancia desde pacman en vez de la distancia del fantasma, ya que queremos
					//acercarnos a pacman por delante suyo y no por detr�s (en cuyo caso no la vamos a poder alcanzar)
					
					nextMove = getMoveTowards(game, ghostType, ghostNode, pacman);
					//nextMove = game.getApproximateNextMoveTowardsTarget(ghostNode, pacman, game.getGhostLastMoveMade(ghostType), distanceUnit);}
				}
			}
			
		}
		
		return nextMove;
	}
	
	

	/**
	 * Devuelve un movimiento en caso de que el fantasma huya
	 * @param game
	 * @param ghostType
	 * @param nearestPowerPill
	 * @param ghostNode
	 * @param pacman
	 * @param close
	 * @param closePacman
	 * @return
	 */
	public MOVE ifEscaping(Game game, GHOST ghostType, int nearestPowerPill, int ghostNode, int pacman, boolean close, boolean closePacman) {

		Coords cGhost = getCoords(ghostNode, game);
		MOVE nextMove = null;
		
		//Comprueba que no est� muy cerca de otros fantasmas comestibles, si es as� cambia su movimiento
		//Esto es para evitar que mspacman se coma a los dos porque es m�s r�pida
		for(GHOST g: GHOST.values()) {
			
			//Si el fantasma con el que nos comparamos est� en la guarida o somos nosotros mismos saltamos el bucle
			if( ghostType == g || game.getGhostLairTime(g) > 0) continue;
			
			int gIndex = game.getGhostCurrentNodeIndex(g);
			Coords cG= getCoords(gIndex, game);
			
			if(
					//coinciden en alguna coordenada
					(cGhost.x == cG.x || cGhost.y == cG.y) &&
					//se encuentran uno cerca del otro (dist<19)
					game.getDistance(ghostNode, gIndex, game.getGhostLastMoveMade(ghostType), distanceUnit) < ghostNearLimitLeave 
					//El otro fantasma es tambi�n comestible
					&& game.isGhostEdible(g)
			) {

				//Posible indices a los que podemos movernos
				int[] posibleIndices = game.getNeighbouringNodes(ghostNode, game.getGhostLastMoveMade(ghostType));
				
				double distMax = 0;
				double distTemp;
				
				//Movimiento que no queremos hacer, el mismo que el del fantasma cerca nuestro
				MOVE dontMake = game.getGhostLastMoveMade(g);
				
				//Caso de que exista un fantasma encima de otro evitamos realizar los mismos movimientos
				if(moves.containsKey(g)) {
					dontMake = moves.get(g);
				}
				
				for(int i = 0; i< posibleIndices.length; i++) {
					if(game.getMoveToMakeToReachDirectNeighbour(ghostNode, posibleIndices[i]) != dontMake) {
						distTemp = game.getDistance(pacman, posibleIndices[i], game.getPacmanLastMoveMade(), distanceUnit);
						//Nos quedamos con el movimientos que nos aleje m�s de Mspacman
						if(distTemp > distMax) {
							distMax = distTemp;
							nextMove = game.getMoveToMakeToReachDirectNeighbour(ghostNode, posibleIndices[i]);
							
						}
						
					}
				}
			}
		}
		
		//Si no existen dos fantasmas cerca pueden pasar dos cosas:
		
		if(nextMove == null ) {
			//Hacer un movimiento que nos acerque a un fantasma no comestible que se pueda comer a MsPacMan
			nextMove = goToOtherGhost(game, ghostNode, pacman, ghostType);
			
		}
			
		//En el caso de que este fantasma no exista nos alejamos de MsPacman
		if(nextMove == null) {
			
			//En vez de usar la funci�n game.getAproximateNextMoveAwayFromTarget que aparece comentada buscamos nosotros
			//el nodo m�s alejado, usando la distancia desde pacman en vez de la distancia del fantasma, ya que queremos
			//maximizar la distancia desde pacman, pero no necesariamente desde el fantasma 
			
			nextMove = getMoveAway(game, ghostType, ghostNode, pacman);
		
			//nextMove = game.getApproximateNextMoveAwayFromTarget(ghostNode, pacman, game.getGhostLastMoveMade(ghostType), distanceUnit);
		}
			
					
			
		return nextMove;
	}
	
	
	
	/**M�todo que trata de predecir la posici�n de la siguiente junction hacia la que se mueve msPacMan
	 * para ello hace movimientos 'fantasmas' hasta que llega a una junction y devuelve el �ndice
	 * @param game
	 * @param pacman
	 * @return
	 */
	
	public int getIndexOfNextPacmanJunction(Game g, int pacman) {

		//Comienza en la posici�n de pacman
		int index = pacman;
		//Movimiento inicial es el de MsPacman
		MOVE pacman_movement = g.getPacmanLastMoveMade();
		
		//Los indices vecinos del que est�
		int indexes[] = g.getNeighbouringNodes(index, g.getPacmanLastMoveMade());
		
		//Si es necesario cambiar el movimiento para que vaya al siguiente �ndice
		if((g.getMoveToMakeToReachDirectNeighbour(index, indexes[0]) != pacman_movement) && (indexes.length == 1)) {
			pacman_movement = g.getMoveToMakeToReachDirectNeighbour(index, indexes[0]);
		}
		
		//Repetimos el proceso hasta que llega a una intersecci�n 
		while(indexes.length == 1) {
			index = indexes[0];
			
			indexes = g.getNeighbouringNodes(index, pacman_movement);
			
			if((g.getMoveToMakeToReachDirectNeighbour(index, indexes[0]) != pacman_movement) && (indexes.length == 1)) {
				pacman_movement = g.getMoveToMakeToReachDirectNeighbour(index, indexes[0]);
			}
	
			
		}
		
		//Devolvemos el �ndice de la intersecci�n
		return index;
	}
	
	
	/**
	 * Cuando nos encontramos cerca de un fantasma no comestible y nosotros somos comestibles nos interesa ir en esa direcci�n para que
	 * pacman nos siga y el otro fantasma se la pueda comer
	 * @param game
	 * @param ghostNode
	 * @param pacman
	 * @param ghostType
	 * @return
	 */
	MOVE goToOtherGhost(Game game, int ghostNode, int pacman, GHOST ghostType) {
		
		MOVE nextMove = null;
		for(GHOST g: GHOST.values()) {
			
			//Si el fantasma con el que nos comparamos est� en la guarida o somos nosotros mismos saltamos el bucle
			if( ghostType == g || game.getGhostLairTime(g) > 0) continue;
			
			if(		game.isGhostEdible(ghostType) && !game.isGhostEdible(g) && 
					
					//Si la distancia al fantasma no comestible es menor que la distancia desde Pacman a nosotros (nos puede comer)
					(game.getDistance(pacman, ghostNode, game.getPacmanLastMoveMade(), distanceUnit) >
					game.getDistance(game.getGhostCurrentNodeIndex(g), ghostNode, game.getGhostLastMoveMade(g), distanceUnit))
					
					
					//En las siguientes dos condiciones comprobamos que estamos en medio de pacman y este otro fantasma y no otra configuraci�n para la cual no nos interesa ir hacia el otro fantasma no comestible
					//Si la distancia de pacman a nosotros es menor que la distancia de pacman al otro fantasma no comestible
				&& (game.getDistance(pacman, ghostNode,game.getPacmanLastMoveMade(), distanceUnit) < game.getDistance(pacman, game.getGhostCurrentNodeIndex(g),game.getPacmanLastMoveMade(), distanceUnit))	
				
					//Que se cumpla tambi�n lo opuesto
				&& (game.getDistance(game.getGhostCurrentNodeIndex(g), ghostNode,game.getGhostLastMoveMade(g), distanceUnit) < game.getDistance( game.getGhostCurrentNodeIndex(g),pacman, game.getGhostLastMoveMade(g), distanceUnit))) {
				
				//Nos dirigimos hacia el otro fantasma
				nextMove = game.getApproximateNextMoveTowardsTarget(ghostNode, game.getGhostCurrentNodeIndex(g), game.getGhostLastMoveMade(ghostType), distanceUnit);
			}
			
		}
		
		return nextMove;
	}
	
	/**
	 * Movimiento alejando a un fantasma de Mspacman, todas las distancias se calculan apartir de Mspacman ya que no nos interesa alejarnos desde
	 * nuestra posici�n sino desde la suya
	 * @param game
	 * @param ghostType
	 * @param ghostNode
	 * @param pacman
	 * @return
	 */
	MOVE getMoveAway(Game game, GHOST ghostType, int ghostNode, int pacman) {
		MOVE nextMove = null;
		
		int[] posibleIndices = game.getNeighbouringNodes(ghostNode, game.getGhostLastMoveMade(ghostType));
		
		double distMax = 0;
		double distTemp;
		for(int i = 0; i< posibleIndices.length; i++) {
		
			distTemp = game.getDistance(pacman, posibleIndices[i], game.getPacmanLastMoveMade(), distanceUnit);
			
			if(distTemp > distMax) {
				distMax = distTemp;
				nextMove = game.getMoveToMakeToReachDirectNeighbour(ghostNode, posibleIndices[i]);
				
			}
				
		}

		
		return nextMove;
	}
	
	/**
	 * Movimiento acercando a un fantasma a Mspacman, todas las distancias se calculan apartir de Mspacman ya que no nos interesa acercarnos desde
	 * nuestra posici�n sino desde la suya (no quedarnos detr�s porque si no no la vamos a poder alcanzar nunca)
	 * @param game
	 * @param ghostType
	 * @param ghostNode
	 * @param pacman
	 * @return
	 */
	MOVE getMoveTowards(Game game, GHOST ghostType, int ghostNode, int pacman) {
		MOVE nextMove = null;
		int[] posibleIndices = game.getNeighbouringNodes(ghostNode, game.getGhostLastMoveMade(ghostType));
		
		double distMax = Integer.MAX_VALUE;
		double distTemp;
		for(int i = 0; i< posibleIndices.length; i++) {
		
			distTemp = game.getDistance(pacman, posibleIndices[i], game.getPacmanLastMoveMade(), distanceUnit);
			
			if(distTemp < distMax) {
				distMax = distTemp;
				nextMove = game.getMoveToMakeToReachDirectNeighbour(ghostNode, posibleIndices[i]);
				
			}
				
		}
		return nextMove;
	}
	
	public String getTeam() {
		return "Team 01";
	}
	public String getName() {
		return "Ghost 01";
	}
	
}
