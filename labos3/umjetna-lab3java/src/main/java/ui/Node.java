package ui;

import java.util.HashMap;
import java.util.LinkedList;

public class Node {
	
	Node[] djeca;
	String znacajka;	// za unutarnji čvor
	int klasaRedak;		// za Leaf -> oznaka vrijednosti ciljne varijable
	int level;
	
	public Node(Node[] djeca, String znac, int klasaRedak, int lvl) {
		this.djeca = djeca;
		this.znacajka = znac;
		this.klasaRedak = klasaRedak;
		this.level = lvl;
	}

	public String stringify(String indent, HashMap<String, LinkedList<String>> mapaTrening, HashMap<String, LinkedList<String>> mapaZnacUniqVrijednost, String ciljnaKlasa) {
		
		// ako je Leaf
		if (djeca == null) {

			return indent + mapaZnacUniqVrijednost.get(ciljnaKlasa).get(klasaRedak) + "\n";
		} else {	// ako je korijen ili unutarnji cvor
			String ret = "";
			for (int i=0; i<djeca.length; i++) {
				String anchor = indent + level + ":" + znacajka + "=" + mapaZnacUniqVrijednost.get(znacajka).get(i) + " ";
				ret += djeca[i].stringify(anchor, mapaTrening, mapaZnacUniqVrijednost, ciljnaKlasa);
			}
			return ret;
		}
	}
	
	
}
