/**
 * Name: David Byrne
 * Student Id: 09068783
 * @author david
 */


import java.io.Serializable;

/**
 * A message class which can be flattened into bytes
 * and send through a socket. Contains the digital signature
 * of the message and simply encrypted message.
 */

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	byte[] digitalSig;
	byte[] encryptedMsg;
	
	public Message(byte[] sig, byte[] msg){
		digitalSig = sig;
		encryptedMsg = msg;
	}
	
	public byte[] getSig(){
		return digitalSig;
	}
	
	public byte[] getMsg(){
		return encryptedMsg;
	}
}
