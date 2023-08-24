package pacman;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

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
		try {
			output.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		RobotConnection rConn = new RobotConnection();
		while(true)
		{
			rConn.notifyMoves(MOVE.RIGHT, null);
			System.out.println("Notificando");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
}
