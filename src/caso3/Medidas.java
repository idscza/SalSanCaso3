package caso3;

import java.util.ArrayList;

public class Medidas {
	
	private static ArrayList<Double> ts = new ArrayList<Double>();
	private static ArrayList<Double> us = new ArrayList<Double>();
	
	
	
	public static synchronized void almacenar(Double t, Double u) {
		ts.add(t);
		us.add(u);
	}
	
	public static synchronized void darMedidas(int tasks) {
		double sumts = 0;
		for(Double tt: ts) {
			sumts+=tt;
		}
		if(sumts!=0) sumts = (sumts/ts.size());
		double sumus = 0;
		for(Double uu: us) {
			sumus+=uu;
		}
		if(sumus!=0) sumus = (sumus/us.size());
		double perd = 0;
		if(ts.size()!=0) perd = (double)(tasks/ts.size());
		
		System.out.println("Se tardó " + sumts+ " ms en promedio");
		System.out.println("el uso del cpu fue " + sumus*100+ "% en promedio");
		System.out.println("Se resolvió el " + perd*100 + "% de solicitudes");
	}

}
