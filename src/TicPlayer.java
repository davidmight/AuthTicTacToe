/**
 * Name: David Byrne
 * Student Id: 09068783
 * @author david
 */


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * A class that controls the player and its communication with the
 * other player. 
 */

public class TicPlayer implements Runnable {
	
	private SocketAddress otherAddress;
	private DatagramSocket socket;
	private DatagramPacket packet;
	
	private boolean inProgress;
	private boolean playerTurn;
	private TicTacToe game;
	
	private PrivateKey privateKey;
	public PublicKey publicKey;
	public PublicKey otherPublicKey;
	
	public CertAuthority certAuth;
	X509Certificate cert;
	
	int playerNum;
	
	/*
	 * Sets up all the variable and creates a X09 Certificate.
	 */
	public TicPlayer(DatagramSocket s, SocketAddress other, boolean turn, CertAuthority certAuth){
		Security.addProvider(new BouncyCastleProvider());
		try{
			socket = s;
			otherAddress = other;
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			KeyPair kp = kpg.genKeyPair();
			publicKey = kp.getPublic();
			privateKey = kp.getPrivate();
			
			playerTurn = turn;
			if(playerTurn){playerNum=1;}else{playerNum=2;}
			//inProgress = true;
			System.out.println("Player "+playerNum+": Public key is: "+publicKey);
			
			this.certAuth = certAuth;
			cert = certAuth.createX09V3Cert("Player "+playerNum, kp, 1);
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
		
	}
	
	/*
	 * Creates a digital signature and encrypts the message and puts them together
	 * into a object of type Message. It then serializes the object and sends it on 
	 * to the other player.
	 */
	public void sendMove(String move){
		System.out.println("Player "+playerNum+": Sending Move..."+move);
		byte[] buffer = new byte[1024];
		try{
			byte[] digitalSignature = createSig(move);
			byte[] encryptedMessage = encryptWithPub(move.getBytes(), otherPublicKey);
			//DatagramPacket p = new DatagramPacket(msgToSend, msgToSend.length, otherAddress);
			Message message = new Message(digitalSignature, encryptedMessage);
			ByteArrayOutputStream fis = new ByteArrayOutputStream();
			ObjectOutputStream is = new ObjectOutputStream(fis);
			is.writeObject(message);
			is.flush();
			buffer = fis.toByteArray();
			DatagramPacket p = new DatagramPacket(buffer, buffer.length, otherAddress);
			socket.send(p);
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/*
	 * Encrypts a array of bytes with the private key.
	 */
	public byte[] encryptWithPriv(byte[] data)throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		byte[] cipherData = cipher.doFinal(data);
		return cipherData;
	}
	
	/*
	 * Creates a signature by hashing the message and then
	 * encrypts it with the private key.
	 */
	public byte[] createSig(String msg) throws Exception{
		byte[] hashedMessage = hashMessage(msg.getBytes("UTF-8"));
		byte[] digSignature = encryptWithPriv(hashedMessage);
		
		return digSignature;
	}
	
	/*
	 * Hashes the message using SHA-512
	 */
	public byte[] hashMessage(byte[] msg) throws Exception{
		MessageDigest sha = MessageDigest.getInstance("SHA-512");
		sha.update(msg);
		byte[] digest = sha.digest();
		return digest;
	}
	
	/*
	 * Encrypts the data with a public key.
	 */
	public byte[] encryptWithPub(byte[] data, PublicKey pubKey) throws Exception{
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] cipherData = cipher.doFinal(data);
		return cipherData;
	}
	
	public byte[] decryptPrivateKey(byte[] data) throws Exception{
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] plainData = cipher.doFinal(data);
		return plainData;
	}
	
	public byte[] decryptPublicKey(byte[] data, PublicKey pubKey) throws Exception{
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, pubKey);
		byte[] sig = cipher.doFinal(data);
		return sig;
	}
	
	/*
	 * Checks the hashes against each other.
	 */
	public boolean verifyIntegrity(byte[] sig, byte [] msg) throws Exception{
		Security.addProvider(new BouncyCastleProvider());
		
		return MessageDigest.isEqual(sig, hashMessage(msg));
	}
	
	/*
	 * First players introduce themselves.
	 * Then they exchange certificates. Then the public keys.
	 * After that they just exchange coordinates.
	 */
	public void run(){
		System.out.println("Player "+playerNum+": Starting...");
		byte[] buf = new byte[1024];
		
		try{
			
			String msg = "hi from "+(playerNum);
			packet = new DatagramPacket(msg.getBytes(), msg.length(), otherAddress);
			socket.send(packet);
			packet = new DatagramPacket(buf, buf.length);
			socket.setSoTimeout(5000);
			socket.receive(packet);
			buf = packet.getData();
			System.out.println("Player "+playerNum+": Received: " + 
					new String(buf, 0, packet.getLength()));
			
			buf = cert.getEncoded();
			packet = new DatagramPacket(buf, buf.length, otherAddress);
			socket.send(packet);
			buf = new byte[1024];
			packet = new DatagramPacket(buf, buf.length);
			socket.setSoTimeout(5000);
			socket.receive(packet);
			buf = packet.getData();
			ByteArrayInputStream bisb = new ByteArrayInputStream(buf);
			CertificateFactory cfb = CertificateFactory.getInstance("X.509");
			X509Certificate otherCert = (X509Certificate)cfb.generateCertificate(bisb);
			bisb.close();
			System.out.println("Player "+playerNum+": Received: "+otherCert.toString());
			
			byte[] buffer = new byte[1024];
			packet = new DatagramPacket(publicKey.getEncoded(), publicKey.getEncoded().length, otherAddress);
			socket.send(packet);
			packet = new DatagramPacket(buffer, buffer.length);
			socket.setSoTimeout(0);
			socket.receive(packet);
			buffer = packet.getData();
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(buffer);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			otherPublicKey = keyFactory.generatePublic(pubKeySpec);
			System.out.println("Player "+playerNum+": Other Public key is: "+otherPublicKey);
			if(certAuth.checkCert(otherCert, otherPublicKey)){inProgress=true;}
		}catch (SocketTimeoutException E) {
			System.out.println("ST: Hit timeout !");
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
		String newCoord;
		byte[] plainMessage, hashedMsg;
		
		if(inProgress){game = new TicTacToe(this, playerTurn);}
		while(inProgress){
			try{
				buf = new byte[1024];
				packet = new DatagramPacket(buf, buf.length);
				//socket.setSoTimeout(0);
				socket.receive(packet);
				buf = packet.getData();
				
				ByteArrayInputStream fis = new ByteArrayInputStream(buf);
				ObjectInputStream in = new ObjectInputStream(fis);
				Message message = (Message)in.readObject();
				
				plainMessage = decryptPrivateKey(message.getMsg());
				hashedMsg = decryptPublicKey(message.getSig(), otherPublicKey);
				
				System.out.println(message.getMsg());
				System.out.println(plainMessage);
				
				if(verifyIntegrity(hashedMsg, plainMessage)){
					newCoord = new String(plainMessage, 0, plainMessage.length);
					game.updateBoard(splitCoords(newCoord));
				}
				System.out.println("Player "+playerNum+": Received: " + plainMessage + " "+hashedMsg);
				System.out.println("From: "+packet.getAddress()+":"+packet.getPort());
			}catch(Exception e){
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
	}
	
	public int[] splitCoords(String coord){
		int[] result = new int[2];
		String[] digits = coord.split("\\,");
		
		for(int i=0; i<digits.length; i++){
			result[i] = Integer.parseInt(digits[i]);
		}
		
		return result;
	}
	
	public void endGame(){
		inProgress = false;
	}
}
