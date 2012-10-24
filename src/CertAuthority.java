/**
 * Name: David Byrne
 * Student Id: 09068783
 * @author david
 */


import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.X509V3CertificateGenerator;

/**
 * A certificate authority which issues certs and keeps a list
 * of those certs so their validity can be checked.
 */

public class CertAuthority {
	
	private static Hashtable certList = new Hashtable();
	
	public CertAuthority(){
		
	}
	
	public X509Certificate createX09V3Cert(String name ,KeyPair pair, int daysValid) throws InvalidKeyException,
	NoSuchProviderException, SignatureException, CertificateEncodingException, IllegalStateException, NoSuchAlgorithmException{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		Date startDate = new Date(System.currentTimeMillis());
		Calendar expiry = Calendar.getInstance();
		expiry.add(Calendar.DAY_OF_YEAR, daysValid);
		BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
		X500Principal dnName = new X500Principal("CN="+name);
		
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
		
		certGen.setSerialNumber(serialNumber);
		certGen.setIssuerDN(dnName);
		certGen.setNotBefore(startDate);
		certGen.setNotAfter(expiry.getTime());
		certGen.setSubjectDN(dnName);                       // note: same as issuer
		certGen.setPublicKey(pair.getPublic());
		certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
		
		certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
		certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature
				| KeyUsage.keyEncipherment));
		certGen.addExtension(X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage(
				KeyPurposeId.id_kp_serverAuth));
		
		X509Certificate resultCert = certGen.generate(pair.getPrivate(), "BC");
		
		certList.put(dnName ,resultCert);
		
		return resultCert;
	}
	
	public boolean checkCert(X509Certificate cert, PublicKey pubKey){
		X509Certificate retrieveCert = (X509Certificate)certList.get(cert.getSubjectX500Principal());
		if(retrieveCert == null){System.out.println("We did not issue this cert.");return false;}
		
		try{
			cert.checkValidity(new Date());
			cert.verify(pubKey);
		}catch(Exception e){
			System.out.println("Not a valid cert");
			return false;
		}
		return true;
		
	}
	
}
