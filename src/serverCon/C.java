package serverCon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import caso3.Medidas;

public class C {
	private static ServerSocket ss;	
	private static final String MAESTRO = "MAESTRO: ";
	private static X509Certificate certSer; /* acceso default */
	private static KeyPair keyPairServidor; /* acceso default */
	
	//TODO CAMBIOS
	private static D[] threads;
	public static boolean over = false;
	private static int tasks;
	
	/**
	 * @param args
	 */
	//TODO CAMBIOS
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		System.out.println(MAESTRO + "Establezca puerto de conexion:");
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		int ip = Integer.parseInt(br.readLine());
		System.out.println(MAESTRO + "Empezando servidor maestro en puerto " + ip);
		// Adiciona la libreria como un proveedor de seguridad.
		// Necesario para crear llaves.
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());		
		//Decide el tamaño del pool y el número de tareas
		System.out.println(MAESTRO + " Escoja el tamaño del pool:");
		int tam = Integer.parseInt(br.readLine());
		System.out.println(MAESTRO + " Escoja el numero de tareas");
		int tar = Integer.parseInt(br.readLine());
		ExecutorService threader = Executors.newFixedThreadPool(tam);
		System.out.println(MAESTRO + "Creado pool de tamanio "+ tam);
		threads = new D[tar]; 
		int idThread = 0;
		tasks = tar;
		// Crea el socket que escucha en el puerto seleccionado.
		ss = new ServerSocket(ip);
		System.out.println(MAESTRO + "Socket creado.");
		
		keyPairServidor = S.grsa();
		certSer = S.gc(keyPairServidor);
		D.initCertificate(certSer, keyPairServidor);
		while (idThread < tar) {
			try { 
				Socket sc = ss.accept();
				System.out.println(MAESTRO + "Cliente " + idThread + " aceptado.");
				D d = new D(sc,idThread);
				threads[idThread%tar] = d;
				
				threader.execute(d);
				idThread++;
				
			} catch (IOException e) {
				System.out.println(MAESTRO + "Error creando el socket cliente.");
				e.printStackTrace();
			}
		}
		try{
			
			for(int i = 0; i < threads.length; i++) {
				while((threads[i].alive));
			}
			over = true;
			System.out.println("Finalizaron todos los threads");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		over = true;
		System.out.println("Finalizaron todos los threads");
		Medidas.darMedidas(tar);
	}
	
	
}
