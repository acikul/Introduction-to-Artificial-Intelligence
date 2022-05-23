package ui;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

public class Id3 {
	
	private int brojZnacajki;
	private int brojPrimjera;
	private String ciljnaKlasa;
	private Node korijen;
	private LinkedHashMap<String, LinkedList<String>> mapaTrening;
	private LinkedHashMap<String, LinkedList<String>> mapaZnacUniqVrijednost;
	private LinkedHashMap<String, Integer> mapaZnacBrojUniqVrij; 
	
	public Id3() {
		this.brojZnacajki = 0;
		this.brojPrimjera = 0;
		this.ciljnaKlasa = null;
		this.korijen = null;
		this.mapaTrening = null;
		this.mapaZnacUniqVrijednost = new LinkedHashMap<>();
		this.mapaZnacBrojUniqVrij = new LinkedHashMap<>();
	}

	public void fit(LinkedHashMap<String, LinkedList<String>> ulazMapa, int cutoff) {
		ulazTools(ulazMapa);
		korijen = fit(new LinkedList<String>(), new LinkedList<Integer>(), 1, cutoff);
	}
	
	private Node fit(LinkedList<String> pregledaneZnacajke, LinkedList<Integer> pregledaniPrimjeri, int level, int cutoff) {

		//ako se desava cutoff -> Leaf(mostCommonCurrent)
		if (level > cutoff ) {
			String mostCommon = findMostCommon(pregledaniPrimjeri);
			return new Node(null, mostCommon, mapaZnacUniqVrijednost.get(ciljnaKlasa).indexOf(mostCommon), level);
		}
		
		//ako su pregledani svi primjeri vrati null i onda roditeljski node umeće -> Leaf(mostCommonRoditelja)
		if (pregledaniPrimjeri.size() == brojPrimjera) {
			return null;
			//return new Node(null, mostCommonRoditelja, mapaZnacUniqVrijednost.get(ciljnaKlasa).indexOf(mostCommonRoditelja), level);
		}
		
		//ako su pregledane sve značajke -> Leaf(mostCommonCurrent)
		if (pregledaneZnacajke.size() >= brojZnacajki-1) {
			String mostCommon = findMostCommon(pregledaniPrimjeri);
			return new Node(null, mostCommon, mapaZnacUniqVrijednost.get(ciljnaKlasa).indexOf(mostCommon), level);

		}
		
		//ako svi pripadaju istom razredu -> Leaf(klasaSvih)
		String mybKlasa = sviIstaKlasa(pregledaniPrimjeri);
		if (mybKlasa != null) {
			return new Node(null, mybKlasa, mapaZnacUniqVrijednost.get(ciljnaKlasa).indexOf(mybKlasa), level);
		}
		
		//nalaženje značajke s najvećom informacijskom dobiti
		String maxInfoGainZnacajka = maxInfoGain(pregledaneZnacajke, pregledaniPrimjeri);
		LinkedList<String> pregledanePlusMaxInfoGain = new LinkedList<>(pregledaneZnacajke);
		pregledanePlusMaxInfoGain.add(maxInfoGainZnacajka);
		
		//za svaku vrijednost te značajke nova grana iz čvora
		Node[] subtrees = new Node[mapaZnacBrojUniqVrij.get(maxInfoGainZnacajka)];
		
		for (int i=0; i<subtrees.length; i++) {
			//podjela po vrijednosti atributa
			LinkedList<Integer> podjelaPrimjera = dodajPregledanePrimjere(maxInfoGainZnacajka, i, pregledaniPrimjeri);
			
			subtrees[i] = fit(pregledanePlusMaxInfoGain, podjelaPrimjera, level+1, cutoff);
			if (subtrees[i] == null) {
				String mostCommon = findMostCommon(pregledaniPrimjeri);
				
				subtrees[i] = new Node(null, mostCommon, mapaZnacUniqVrijednost.get(ciljnaKlasa).indexOf(mostCommon), level+1);
			}
		}
		
		String mostCommon = findMostCommon(pregledaniPrimjeri);
		//mapaZnacUniqVrijednost.get(ciljnaKlasa).indexOf(mostCommon)
		return new Node(subtrees, maxInfoGainZnacajka, mapaZnacUniqVrijednost.get(ciljnaKlasa).indexOf(mostCommon), level);
	}
	
	private String findMostCommon(LinkedList<Integer> pregledaniPrimjeri) {
		LinkedHashMap<String, Integer> mapaVrijednostCount = new LinkedHashMap<>();
		//za sve primjere koji nisu pregledani pobrojat će najčešću vrijednost ciljne varijable
		for (int i=0; i<brojPrimjera; i++) {
			if (!pregledaniPrimjeri.contains(i)) {
				String vrijCiljne = mapaTrening.get(ciljnaKlasa).get(i);
				if (mapaVrijednostCount.keySet().contains(vrijCiljne)) {
					int temp = mapaVrijednostCount.get(vrijCiljne);
					mapaVrijednostCount.put(vrijCiljne, ++temp);
				} else {
					mapaVrijednostCount.put(vrijCiljne, 1);
				}
			}
		}
		//pronalazi i vraća najčešću vrijednost ciljne varijable
		String najcesca = mapaZnacUniqVrijednost.get(ciljnaKlasa).get(0);
		int najcesciSize = 0;
		for (Entry<String, Integer> s : mapaVrijednostCount.entrySet()) {
			
			if (s.getValue() > najcesciSize) {
				najcesca = s.getKey();
				najcesciSize = s.getValue();
			}
			if ((s.getValue() == najcesciSize) && (s.getKey().toLowerCase().compareTo(najcesca.toLowerCase())<0)) {
				najcesca = s.getKey();
			}
		}
		return najcesca;
	}
	
	private String sviIstaKlasa(LinkedList<Integer> pregledaniPrimjeri) {
		boolean sviIsti = true;
		String tempKlasa = null;
		
		//za svaki primjer koji nije vec pregledan, ako je razlicit od "bazne" tempKlase -> break
		for (int i=0; i<brojPrimjera; i++) {
			if (!pregledaniPrimjeri.contains(i)) {
				if (tempKlasa == null) tempKlasa = mapaTrening.get(ciljnaKlasa).get(i);
				
				if (!tempKlasa.equals(mapaTrening.get(ciljnaKlasa).get(i))) {
					sviIsti = false;
					break;
				}
			}
		}
		if (sviIsti) {
			return tempKlasa;
		} else {
			return null;
		}
	}
	
	private String maxInfoGain(LinkedList<String> pregledaneZnacajke, LinkedList<Integer> pregledaniPrimjeri) {
		double prevEntropy = getEntropy(pregledaniPrimjeri);
		int ukBrPrimjera = brojPrimjera-pregledaniPrimjeri.size();		
		
		LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>> znacVrijCount = countVrijKlasaZnac(pregledaneZnacajke, pregledaniPrimjeri);
		
		String maxIGznacajka = null;
		double  maxIG = Double.NEGATIVE_INFINITY;
		
		// za svaku znacajku s pobrojanim klasama po vrijednostima -> 
		// entropija za svaku podjelu po određenoj vrijednosti značajke ->
		// svaka ta entropija(skalirana s udijelom) se oduzima od previousEntropy i dobivamo IG(znacajka)
		for (String znac : znacVrijCount.keySet()) {
			LinkedHashMap<String, LinkedHashMap<String, Integer>> vrijZnacKlasaCnt = znacVrijCount.get(znac);
			double infoGain = prevEntropy;
			
			for (String vrijZnac : vrijZnacKlasaCnt.keySet()) {
				int ukBrVrijZnac = 0;
				for (int i : vrijZnacKlasaCnt.get(vrijZnac).values()) ukBrVrijZnac += i;
				
				double entropy = 0.0;
				for (Entry<String, Integer> kc : vrijZnacKlasaCnt.get(vrijZnac).entrySet()) {
					entropy -= xlogx((double)kc.getValue()/(double)ukBrVrijZnac);
				}
				double udio = (double)ukBrVrijZnac/(double)ukBrPrimjera;
				infoGain -= udio * entropy;
			}
			
			if (infoGain > maxIG) {
				maxIG = infoGain;
				maxIGznacajka = znac;
			}
			if ((infoGain == maxIG) && (znac.toLowerCase().compareTo(maxIGznacajka.toLowerCase()) < 0)) {
				maxIGznacajka = znac;
			}
		}
		return maxIGznacajka;
	}
	
	private LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>> countVrijKlasaZnac(LinkedList<String> pregledaneZnacajke, LinkedList<Integer> pregledaniPrimjeri) {
		
		LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>> znacajkaVrijKlasaCount = new LinkedHashMap<>();
		// za sve znacajke koje nisu vec pregledane
		// pobroji razlicite vrijednosti ciljne carijable u svim redovima koji nisu vec pregledani
		for (String znacajka : mapaTrening.keySet()) {
			if (!pregledaneZnacajke.contains(znacajka) && !znacajka.equals(ciljnaKlasa)) {
				LinkedHashMap<String, LinkedHashMap<String, Integer>> vrijCount = new LinkedHashMap<>();
				for (int i=0; i<brojPrimjera; i++) {
					if (!pregledaniPrimjeri.contains(i)) {
						String vrijednostZnac = mapaTrening.get(znacajka).get(i);
						String klasa = mapaTrening.get(ciljnaKlasa).get(i);
						
						if (vrijCount.keySet().contains(vrijednostZnac)) {
							LinkedHashMap<String, Integer> klasaCount = vrijCount.get(vrijednostZnac);
							if (klasaCount.containsKey(klasa)) {
								int temp = klasaCount.get(klasa);
								klasaCount.put(klasa, ++temp);
							} else {
								klasaCount.put(klasa, 1);
							}
							vrijCount.put(vrijednostZnac, klasaCount);
						} else {
							LinkedHashMap<String, Integer> novaKlasaCount = new LinkedHashMap<>();
							novaKlasaCount.put(klasa, 1);
							vrijCount.put(vrijednostZnac, novaKlasaCount);
						}
					}
				}
				znacajkaVrijKlasaCount.put(znacajka, vrijCount);
			}
		}
		return znacajkaVrijKlasaCount;
	}
	
	private double getEntropy(LinkedList<Integer> pregledaniPrimjeri) {
		LinkedHashMap<String, Integer> mapaVrijednostCount = new LinkedHashMap<>();
		
		//za svaku vrijednost ciljne klase pobroji broj pojavljivanja
		for (int i=0; i<brojPrimjera; i++) {
			if (!pregledaniPrimjeri.contains(i)) {
				String vrijCiljne = mapaTrening.get(ciljnaKlasa).get(i);
				if (mapaVrijednostCount.keySet().contains(vrijCiljne)) {
					int temp = mapaVrijednostCount.get(vrijCiljne);
					mapaVrijednostCount.put(vrijCiljne, ++temp);
				} else {
					mapaVrijednostCount.put(vrijCiljne, 1);
				}
			}
		}
		//njihova entropija 
		double entropy = 0.0;
		int ukBrojPrimjera = brojPrimjera-pregledaniPrimjeri.size();
		for (Integer vrijCount : mapaVrijednostCount.values()) {
			entropy -= xlogx(((double)vrijCount/(double)ukBrojPrimjera));
		}
		return entropy;
	}
	
	private LinkedList<Integer> dodajPregledanePrimjere(String maxInfoGainZnac, int i, LinkedList<Integer> pregledaniPrimjeri) {
		String vrijZnac = mapaZnacUniqVrijednost.get(maxInfoGainZnac).get(i);
		
		LinkedList<Integer> noviPregledaniPrimjeri = new LinkedList<>(pregledaniPrimjeri);
		for (int j=0; j<brojPrimjera; j++) {
			if (!pregledaniPrimjeri.contains(j) && !vrijZnac.equals(mapaTrening.get(maxInfoGainZnac).get(j))) {
				noviPregledaniPrimjeri.add(j);
			}
		}
		return noviPregledaniPrimjeri;
	}
	
	double xlogx(double x) {
		return x==0 ? 0 : x * Math.log(x)/Math.log(2.0);
	}
	
	private void ulazTools(LinkedHashMap<String, LinkedList<String>> ulazMapa) {
		mapaTrening = ulazMapa;
		brojZnacajki = mapaTrening.keySet().size();
		
		for (Entry<String, LinkedList<String>> entry : mapaTrening.entrySet()) {
			brojPrimjera = entry.getValue().size();
			ciljnaKlasa = entry.getKey();
			
			LinkedHashSet<String> tempSet = new LinkedHashSet<>(entry.getValue());
			mapaZnacUniqVrijednost.put(entry.getKey(), new LinkedList<String>(tempSet));
			mapaZnacBrojUniqVrij.put(entry.getKey(), tempSet.size());
		}
		
	}

	public void branches() {
		if (korijen == null) {
			System.err.println("nema stabla odluke");
			System.exit(1);
		} else {
			// ispis svih grana
			System.out.println("[BRANCHES]:");
			String ispis = korijen.stringify("", mapaTrening, mapaZnacUniqVrijednost, ciljnaKlasa);
			
			System.out.println(ispis.substring(0, ispis.length()-1));
		}
	}
	
	
	public void predict(LinkedHashMap<String, LinkedList<String>> mapaTest) {
		System.out.print("[PREDICTIONS]: ");
		LinkedList<String> predictions = new LinkedList<>();
		String ret = "";
		for (int i=0; i<mapaTest.get(ciljnaKlasa).size(); i++) {
			String predictRedak = predict(mapaTest, i, korijen);
			predictions.add(predictRedak);
			ret += predictRedak + " ";
		}
		System.out.println(ret);
		
		accuracy(predictions, mapaTest);
		confusionMatrix(predictions, mapaTest);
	}
	
	private String predict(LinkedHashMap<String, LinkedList<String>> mapaTest, int redak, Node node) {
		if (node.djeca == null) {
			return mapaZnacUniqVrijednost.get(ciljnaKlasa).get(node.klasaRedak);
		}
		String vrijZnac = mapaTest.get(node.znacajka).get(redak);
		
		for (int i=0; i<node.djeca.length; i++) {
			if (vrijZnac.equals(mapaZnacUniqVrijednost.get(node.znacajka).get(i))) {
				return predict(mapaTest, redak, node.djeca[i]);
			}
		}
		return mapaZnacUniqVrijednost.get(ciljnaKlasa).get(node.klasaRedak);
	}
	
	private void accuracy(LinkedList<String> predictions, LinkedHashMap<String, LinkedList<String>> mapaTest) {
		int correct = 0;
		for (int i=0; i<predictions.size(); i++) {
			if (predictions.get(i).equals(mapaTest.get(ciljnaKlasa).get(i))) {
				correct++;
			}

		}
		double acc = (double) correct / (double) predictions.size();
		String accStr = String.format("%.5f", acc);

		System.out.println("[ACCURACY]: " + accStr);
	}
	
	private void confusionMatrix(LinkedList<String> predictions, LinkedHashMap<String, LinkedList<String>> mapaTest) {

		TreeSet<String> klase = new TreeSet<>();
		for (String k : mapaTest.get(ciljnaKlasa)) {
			klase.add(k);
		}
		
		TreeMap<String, TreeMap<String, Integer>> matrix = new TreeMap<>();
		for (String k : klase) {
			TreeMap<String, Integer> matrixRedak = new TreeMap<>();
			for (String u : klase) {
				matrixRedak.put(u, 0);
			}
			matrix.put(k, matrixRedak);
		}
		
		for (int i=0; i<predictions.size(); i++) {
			String predicted = predictions.get(i);
			String stvarna = mapaTest.get(ciljnaKlasa).get(i);
			
			int br = matrix.get(stvarna).get(predicted);
			TreeMap<String, Integer> tempRedak = matrix.get(stvarna);
			tempRedak.put(predicted, br+1);
			matrix.put(stvarna, tempRedak);
		}
		
		System.out.println("[CONFUSION_MATRIX]:");
		for (TreeMap<String, Integer> s : matrix.values()) {
			for (Integer b : s.values()) {
				System.out.print(b + " ");
			}
			System.out.println();
		}
	}
	
}
