package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class DataLoader {
	
	public static LinkedHashMap<String, LinkedList<String>> load(String arg) {
		String[] header = null;
		LinkedList<String[]> primjeri = new LinkedList<>();
		LinkedHashMap<String, LinkedList<String>> mapRet = new LinkedHashMap<>();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(arg))) {
			// header redak
			header = reader.readLine().split(",");
			
			// svi ostali retci
			String temp = reader.readLine();
			while (temp != null) {
				primjeri.add(temp.split(","));
				temp = reader.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// za svaki header(stupac) skupi sve vrijednosti po redovima u listu
		// i dodaj u mapu entry header -> lista vrijednosti
		for (int i=0; i<header.length; i++) {
			LinkedList<String> tempList = new LinkedList<>();
			
			for (String[] primjer : primjeri) {
				tempList.add(primjer[i]);
			}
			
			mapRet.put(header[i], tempList);
		}	
		return mapRet;
	}
}
