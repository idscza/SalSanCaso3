package caso3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.net.Socket;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.sql.Date;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;


public class ClienteCon{
	
	//Strings de los diferentes algoritmos
	public static final String AES = "AES";
	public static final String BLOWFISH = "Blowfish";
	public static final String RSA = "RSA";
	public static final String HMACSHA1 = "HMACSHA1";
	public static final String HMACSHA256 = "HMACSHA256";
	public static final String HMACSHA384 = "HMACSHA384";
	public static final String HMACSHA512 = "HMACSHA512";
	public static final String SEP = ":";
	
	//Variables usadas en todo el cliente
	BufferedReader in, consola;
	
	PrintWriter out;
	
	Socket socket;
	
	KeyPair keys;
		
	String[] algoritmos;
	
	PublicKey publicserverkey;
	
	SecretKey secretKey;
	
	Certificate certirecibido;
	
	byte[] hmacgenerado;
	
	
	//TODO CAMBIO
	private static LoadGenerator spammer;
	private int id;
	
	
	public ClienteCon(String host, int puerto, int pid){
		
		KeyPairGenerator genllaves;
		try {
			genllaves = KeyPairGenerator.getInstance("RSA");
			genllaves.initialize(1024);
			keys = genllaves.generateKeyPair();
			socket = new Socket(host,puerto);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			consola = new BufferedReader(new InputStreamReader(System.in));
			algoritmos = new String[3];	
			algoritmos[1] = RSA;
			id = pid;
		}catch(Exception e) {
			System.out.println("Erorr :" +e.getMessage());
			e.printStackTrace();
		}
	}
	
	//M�todo que lee el mensaje del servidor, lo imprime en consola y lo retorna como String
	String recibirservidor() throws IOException{
		String rta = in.readLine();
		System.out.println("SERVIDOR: " + rta);
		return rta;
	}

	//Men� para la selecci�n de los algorimos Sim�tricos y de HMAC que se van a usar en la sesi�n
	//TODO CAMBIOS
	private String configurarAlgoritmos() {
		
		//Siempre env�a el mismo algoritmo para ahorrar tiempo
		String mensaje = "ALGORITMOS"+SEP+AES+SEP+RSA+SEP+HMACSHA256;
		algoritmos[0] = AES;
		algoritmos[2] = HMACSHA256;
		return mensaje;
	}
	
	//Cifrador Asim�trico
	public byte[] cifrarAsimetrico(Key llave, byte[] texto) {
		byte[] textoCifrado;
		
		try {
			Cipher cifrador = Cipher.getInstance(RSA);	
			cifrador.init(Cipher.ENCRYPT_MODE, llave);
			textoCifrado = cifrador.doFinal(texto);
			return textoCifrado;
		} catch(Exception e) {
			System.out.println("Error codificando: "+ e.getMessage());
			return null;
		}
	}
	
	//Descifrador Asim�trico
	public byte[] descifrarAsimetrico(Key llave, byte[] texto) {
		byte[] textoClaro;
		
		try {
			Cipher cifrador = Cipher.getInstance(RSA);
			cifrador.init(Cipher.DECRYPT_MODE, llave);
			textoClaro = cifrador.doFinal(texto);
			return textoClaro;
		}catch(Exception e) {
			System.out.println("Error codificando: "+ e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	//Cifrador Sim�trico
	public byte[] cifrarSimetrico(SecretKey llave, String texto) {
		byte[] textoCifrado;
		
		try {
			Cipher cifrador = Cipher.getInstance(algoritmos[0]);
			byte[] textoClaro = texto.getBytes();
			
			cifrador.init(Cipher.ENCRYPT_MODE, llave);
			textoCifrado = cifrador.doFinal(textoClaro);
			
			return textoCifrado;
		} catch(Exception e) {
			System.out.println("Error Codificando: "+ e.getMessage());
			return null;
		}
	}
	
	//Productor de HMAC cifrado
	public byte[] obtenerhash(SecretKey llave, String texto) {
	try {
			byte[] cifrado = texto.getBytes();

			Mac mac = Mac.getInstance(algoritmos[2]);
			mac.init(llave);
			cifrado = mac.doFinal(texto.getBytes());
			
			return cifrado;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public double getSystemCpuLoad() throws Exception {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
		AttributeList list = mbs.getAttributes(name, new String[]{ "SystemCpuLoad" });
		if (list.isEmpty()) return Double.NaN;
		Attribute att = (Attribute)list.get(0);
		Double value = (Double)att.getValue();
		// usually takes a couple of seconds before we get real values
		if (value == -1.0) return Double.NaN;
		// returns a percentage value with 1 decimal point precision
		return ((int)(value * 1000) / 10.0);
		}
	
	
	//Generador de Certificado en x509 v3
	public Certificate generarcertificado() throws OperatorCreationException, CertificateException {
        
        long ahora = System.currentTimeMillis();
        Date desde = new Date(ahora);
        X500Name firmante = new X500Name("cn=Transportalpes");
        BigInteger serial = new BigInteger(Long.toString(ahora));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(desde);
        calendar.add(Calendar.YEAR, 1);
        Date hasta = new Date(calendar.getTimeInMillis());

        String signatureAlgorithm = "SHA256WithRSA";
   
        ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).build(keys.getPrivate());	
        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(keys.getPublic().getEncoded());
        X509v3CertificateBuilder cb = new X509v3CertificateBuilder(firmante,serial,desde,hasta,firmante,subjectPublicKeyInfo);
        X509CertificateHolder certificateHolder = cb.build(contentSigner);

        Certificate selfSignedCert = new JcaX509CertificateConverter().getCertificate(certificateHolder);

        return selfSignedCert;
	}
	
	//Enviador de certificado al servidor
	public void enviarcertificado() throws OperatorCreationException, CertificateException {

		Certificate certificado = generarcertificado();

		byte[] certificadoEnBytes = certificado.getEncoded( );
		String certificadoEnString = DatatypeConverter.printHexBinary(certificadoEnBytes);
		out.println(certificadoEnString);
	}
	
	//decodificador de certificado recibido
	private Certificate recibircertificado(String rtaserver) throws CertificateException, IOException {   
        byte[] c= DatatypeConverter.parseHexBinary(rtaserver);
        return new JcaX509CertificateConverter().getCertificate(new X509CertificateHolder(c));
	}

	//Encripta asim�tricamente y env�a la llave a utilizar
	public void enviarllave() {
		KeyGenerator keygen;
		try {
			keygen = KeyGenerator.getInstance(algoritmos[0]);
			secretKey = keygen.generateKey();
			byte[] llavebytes = secretKey.getEncoded( );
			llavebytes = cifrarAsimetrico(publicserverkey,llavebytes);
			String llaveString = DatatypeConverter.printHexBinary(llavebytes);
			
			out.println(llaveString);
			System.out.println("Cliente: "+ llaveString);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	//Verifica que la llave sim�trica recibida sea la misma enviada 
	//Permite seguir la conexi�n si lo son, de lo contrario detiene el proceso
	private void verificarllaves(String llave) {
		byte[] llavebytes= DatatypeConverter.parseHexBinary(llave);
		llavebytes = descifrarAsimetrico(keys.getPrivate(),llavebytes);
		
		if(compararbytes(secretKey.getEncoded(),llavebytes)) {
			System.out.println("CLIENTE: OKAY");
			out.println("OK");
		}else {
			System.out.println("CLIENTE: Las llaves no coinciden");
			out.println("NOT OK");
		}
		
	}
	
	//Compara que dos arreglos de bytes sean iguales
	//Se usa en la comparacion de llaves y de Hmac
	public boolean compararbytes(byte[] llave1, byte[] llave2) {
		boolean iguales = true;
		
		if (llave1.length != llave2.length) {
			return false;
		}
		
		for (int i = 0; i < llave2.length && iguales; i++) {
			if(llave1[i]!=llave2[i])iguales = false;
		}
		
		return iguales;
	}

	//Env�a los datos de la localizaci�n, cifrados sim�tricamente
	private void enviardatos(String rtacliente) {
		
		byte[] rtacifrada = cifrarSimetrico(secretKey, rtacliente);
		String rtaenviada = DatatypeConverter.printHexBinary(rtacifrada);
		out.println(rtaenviada);
		System.out.println("CLIENTE: "+rtaenviada);
	}
	
	//Env�a el HMac de los datos de la localizaci�n, cifrados sim�tricamente
	private void enviarhmac(String rtacliente) {
		byte[] mac = obtenerhash(secretKey,rtacliente);
		String macstring = DatatypeConverter.printHexBinary(mac);
		out.println(macstring);
		hmacgenerado = mac;
		System.out.println("CLIENTE: "+macstring);
		
	}
	
	//Verifica que el HMac recibido sea el mismo que el enviado
	private boolean verificarhmac(String rtaserver) {
		byte[] rtaenbytes= DatatypeConverter.parseHexBinary(rtaserver);
		rtaenbytes = descifrarAsimetrico(publicserverkey,rtaenbytes);
		return compararbytes(hmacgenerado,rtaenbytes);
	}
	
	//PROTOCOLO
	//TODO CAMBIOS
	public void ejecutar() throws ProtocoloException{
		String rtaserver = "";
		String rtacliente = "";
		String datosaserver = "";
		System.out.println("----Caso 3 Infracomp-----");
		System.out.println("CLIENTE: HOLA");
		out.println("HOLA");
		try {
			
			//TODO CAMBIOS
			
			//Primer Saludo
			rtaserver = recibirservidor();
			if(!rtaserver.equals("OK")) throw new ProtocoloException("Conexi�n Rechazada");
			
			//Permite configurar los algoritmos a usar en esta sesi�n
			rtacliente = configurarAlgoritmos();
			out.println(rtacliente);
			System.out.println("CLIENTE: " +rtacliente);
			rtaserver = recibirservidor();
			if(!rtaserver.equals("OK")) throw new ProtocoloException("Algoritmos Rechazados");
			
			//Genera, env�a, recibe y procesa los certificados
			System.out.println("CLIENTE: Enviando Certificado");
			enviarcertificado();
			rtaserver = recibirservidor();
			certirecibido = recibircertificado(rtaserver);
			publicserverkey = certirecibido.getPublicKey();
			
			//Env�a y genera la llave sim�trica
			//Desde aqu� agarra el tiempo
			double inic = System.currentTimeMillis(); 
			
			System.out.println("Enviando llaves");
			enviarllave();
			rtaserver = recibirservidor();
			verificarllaves(rtaserver);

			//Permite que el usuario ingrese los datos por consola
			//TODO CAMBIO Envia cualquier cosa
			System.out.println("Env�e los Datos: Ej: 15;41 24.2028,2 10.4418 ");
			//rtacliente = consola.readLine();
			rtacliente = "15;41 24.2028,2 10.4418";
			//Env�a los datos y el Hash
			enviardatos(rtacliente);
			enviarhmac(rtacliente);
			
			//confirma que se haya enviado adecuadamente
			rtaserver= recibirservidor();
			if(rtaserver.equals("ERROR")) throw new ProtocoloException("Mensaje Rechazado");
			
			//Verifica el mac recibido por el servidor
			if(verificarhmac(rtaserver)) {
			System.out.println("Mensaje correctamente recibido");
			}else System.out.println("El mensaje no se recibi� correctamente");
			
			//Aqu� se recibio
			//TODO CAMBIOS
			Double time = System.currentTimeMillis() - inic;
			Double usocpu = getSystemCpuLoad();
			rtacliente = (time + ";"+usocpu);
			out.println(rtacliente);
			
		}catch(IOException e) {
			e.printStackTrace();
		} catch (OperatorCreationException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Medidas.darMedidas(20);
		
	}

	//El maaaaain xd
	public static void main(String[] args) {

		//Cambiar Puerto Aqu�
		//ClienteCon client = new ClienteCon("localhost", 6969,0);
		//S� se est� usando BouncyCastle
		Security.addProvider(new BouncyCastleProvider());
		
		Task work =  new ElTask();
		spammer = new LoadGenerator("TRIAL", 20, work, 20);
		spammer.generate();
		
		/*try {
			client.ejecutar();
		}catch(ProtocoloException e) {
			e.printStackTrace();
		}*/

	}

}
