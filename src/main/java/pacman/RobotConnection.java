package pacman;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Random;

import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class RobotConnection {

	Socket client = null;
	OutputStream output ;
	
	public RobotConnection() {
		openServer();
	}
	
	private void openServer() {
		try {
			ServerSocket server = new ServerSocket(5050);
			System.out.println("Servidor creado");
			client = server.accept();
			System.out.println(client.getInetAddress());
			System.out.println("Conexcion aceptada");
			output = client.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void notifyMoves(MOVE pacManMove, Map<GHOST, MOVE> ghostMoves) {
		Integer ivalue = pacManMove.ordinal();
		byte bvalue = ivalue.byteValue();
		byte[] data = new byte[1];
		data[0]=bvalue;
		//System.out.println(data);
		try {
			output.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*public static void main(String[] args) {
		RobotConnection rConn = new RobotConnection();
		MOVE moves [] = {MOVE.RIGHT,MOVE.LEFT,MOVE.UP,MOVE.DOWN};
		Random rd = new Random();
		while(true)
		{
			try {
				MOVE t=moves[rd.nextInt(moves.length)];
			rConn.notifyMoves(t, null);
			System.out.println("Notificando: "+t);

			Thread.sleep(2000);				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}*/
	
	
}
