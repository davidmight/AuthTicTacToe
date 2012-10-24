/**
 * Name: David Byrne
 * Student Id: 09068783
 * @author david
 */


import java.net.DatagramSocket;
import java.net.SocketAddress;

/**
 * A Network class which sets up the connection between the two players
 * as well set up the Certificate Authority. Passes each player to
 * a separate thread.
 */

public class Network {
	
	public static void main (String args[]){
		try{
			DatagramSocket socket1 = new DatagramSocket();
			SocketAddress address1 = socket1.getLocalSocketAddress();
			System.out.println("Player1 address is: "+address1);
			
			DatagramSocket socket2 = new DatagramSocket();
			SocketAddress address2 = socket2.getLocalSocketAddress();
			System.out.println("Player2 address is: "+address2);
			
			CertAuthority certAuth = new CertAuthority();
			
			Thread t, r;
			t = new Thread(new TicPlayer(socket1, address2, true, certAuth));
			r = new Thread(new TicPlayer(socket2, address1, false, certAuth));
			
			t.start();
			r.start();
			
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
}
