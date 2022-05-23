package ui;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Solution {

	public static void main(String ... args) {
		if (args.length < 2) {
			System.err.println("fali/e argument/i");
			System.exit(1);
		}
		
		LinkedHashMap<String, LinkedList<String>> mapaTrening = DataLoader.load(args[0]);
		LinkedHashMap<String, LinkedList<String>> mapaTest = DataLoader.load(args[1]);
		
		if (args.length > 3) {
			System.err.println("previše argumenata");
			System.exit(1);
		} else {
			Id3 id3 = new Id3();
			
			if (args.length == 2) { //zovi ID3 bez hiperparametra
				id3.fit(mapaTrening, Integer.MAX_VALUE);

			} else if (args.length == 3) { //zovi ID3 s hiperparametrom
				id3.fit(mapaTrening, Integer.parseInt(args[2]));
			}
			
			id3.branches();
			id3.predict(mapaTest);
		}
		
		
	}
}