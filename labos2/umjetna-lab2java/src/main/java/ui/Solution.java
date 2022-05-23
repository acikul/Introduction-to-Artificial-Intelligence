package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Solution {
	
	private static String podzadatak;
	private static String klauzulaFilePathString;
	private static String naredbeFilePathString;
	
	private static Set<Klauzula> startKlauzule;
	private static Set<Klauzula> skupPotpore;
	private static Set<Klauzula> sveKlauzule;
	private static Set<Naredba> naredbe;
	
	private static Klauzula cilj;
	private static Klauzula NIL;
	
	private static void argLoader(String ... args) {
			podzadatak = args[0];
			klauzulaFilePathString = args[1];
			if (podzadatak.equals("cooking")) {
				naredbeFilePathString = args[2];
			}
	}
	
	private static void pocKlauzLoader() {
		startKlauzule = new LinkedHashSet<>();
		skupPotpore = new LinkedHashSet<>();
		sveKlauzule = new LinkedHashSet<>();
		
		// za zamjenu zadnje klauzule njezinom negacijom
		// (jer je to ciljna klauzula kad je podzadatak resolution)
		LinkedList<Klauzula> tempKlauzList = new LinkedList<>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(klauzulaFilePathString));
			String mybGood = reader.readLine();

			while (mybGood != null) {
				if (!mybGood.startsWith("#")) {
					Set<String> tempAtomSet = new LinkedHashSet<>();
					tempAtomSet.addAll(Arrays.asList(mybGood.toLowerCase().split(" v ")));
					tempKlauzList.add(new Klauzula(tempAtomSet, null, null));
				}
				mybGood = reader.readLine();
			}

			// ako je podzatak resolution, svaki literal ciljne klauzule se negira i dodaje kao zasebna klauzula u pocetne
			// ako je podzadatk cooking, sve unesene klauzule su pocetne, a ciljna ce se zasebno unosit naredbom
			if (podzadatak.equals("resolution")) {
				Klauzula zadnjiRed = tempKlauzList.remove(tempKlauzList.size()-1);
				cilj = zadnjiRed;
				startKlauzule.addAll(tempKlauzList);
				for (String s : zadnjiRed.literali) {
					s = negation(s);

					Set<String> tempSet = new LinkedHashSet<>();
					tempSet.add(s);

					startKlauzule.add(new Klauzula(tempSet, null, null));
					skupPotpore.add(new Klauzula(tempSet, null, null));
				}
			} else {
				startKlauzule.addAll(tempKlauzList);
			}
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void naredbeLoader() {
		naredbe = new LinkedHashSet<>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(naredbeFilePathString));
			String mybGood = reader.readLine();
			
			while (mybGood != null) {
				if (!mybGood.startsWith("#")) {
					String id = new String();
					Set<String> tempLiteraliSet = new LinkedHashSet<>();
					tempLiteraliSet.addAll(Arrays.asList(mybGood.toLowerCase().split(" ")));
					tempLiteraliSet.remove("v");
					
					if (tempLiteraliSet.contains("?")) {
						tempLiteraliSet.remove("?");
						id = "?";
					} else if (tempLiteraliSet.contains("-")) {
						tempLiteraliSet.remove("-");
						id = "-";
					} else if (tempLiteraliSet.contains("+")) {
						tempLiteraliSet.remove("+");
						id = "+";
					}
					Naredba naredba = new Naredba(new Klauzula(tempLiteraliSet, null, null), id);
					naredbe.add(naredba);
				}
				mybGood = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String negation(String s) {
		String ret;
		if (s.contains("~")) {
			ret = s.substring(1);
		} else {
			ret = "~" + s;
		}
		return ret;
	}
	
	private static LinkedHashSet<Klauzula> resolve(Klauzula prva, Klauzula druga) {
		LinkedHashSet<Klauzula> result = new LinkedHashSet<>();
		
		for (String prvaLiteral : prva.literali) {
			String prvaLiteralNeg = negation(prvaLiteral);
			if (druga.literali.contains(prvaLiteralNeg)) {
				Set<String> prvaLiteralSet = new LinkedHashSet<>();
				Set<String> drugaLiteralSet = new LinkedHashSet<>();
				Set<String> tempLiteralSet = new LinkedHashSet<>();
				
				prvaLiteralSet.addAll(prva.literali);
				drugaLiteralSet.addAll(druga.literali);
				
				prvaLiteralSet.remove(prvaLiteral);
				drugaLiteralSet.remove(prvaLiteralNeg);
				
				tempLiteralSet.addAll(prvaLiteralSet);
				tempLiteralSet.addAll(drugaLiteralSet);
				
				Klauzula tempKlauz = new Klauzula(tempLiteralSet, prva, druga);
				
				result.add(tempKlauz);
			}
		}
		
		return result;
	}
	
	private static void cleanRedundant() {
		LinkedHashSet<Klauzula> toBeDeleted = new LinkedHashSet<>();
		for (Klauzula baseKl : sveKlauzule) {
			for (Klauzula comparKl : sveKlauzule) {
				if (!baseKl.equals(comparKl)) {
					if (comparKl.literali.containsAll(baseKl.literali)) {
						toBeDeleted.add(comparKl);
					}
				}
			}
		}
		sveKlauzule.removeAll(toBeDeleted);
		skupPotpore.removeAll(toBeDeleted);
	}
	
	private static LinkedHashSet<Klauzula> cleanTautology(LinkedHashSet<Klauzula> nove) {
		LinkedHashSet<Klauzula> rez = new LinkedHashSet<>();
		for (Klauzula klauz : nove) {
			boolean taut = false;
			for (String atom : klauz.literali) {
				if (klauz.literali.contains(negation(atom))) {
					taut = true;
				}
			}
			if (!taut) {
				rez.add(klauz);
			}
		}
			
		
		return rez;
	}
	
	private static boolean rezolucija() {
		//baza znanja - pocetne + izvedene klauzule
		sveKlauzule.addAll(startKlauzule);
		sveKlauzule.addAll(skupPotpore);
		
		LinkedHashSet<Klauzula> toBeChecked = new LinkedHashSet<>();
		toBeChecked.addAll(skupPotpore);

		while (toBeChecked.size()>0) {
			Iterator<Klauzula> sosIter = toBeChecked.iterator();
			LinkedHashSet<Klauzula> nove = new LinkedHashSet<>();
			
			// sosIter.next() - c1 - prva roditeljska klauzula iz skupa potpore
			while (sosIter.hasNext()) {
				Klauzula prva = sosIter.next();
				Iterator<Klauzula> allIter = sveKlauzule.iterator();
				
				// allIter.next() - c2 - druga roditeljska klauzula iz baze znanja
				while (allIter.hasNext()) {
					// sve klauzule koje dobivamo iz c1 i c2
					LinkedHashSet<Klauzula> rezolvente = resolve(prva, allIter.next());

					// ako smo dosli do proturjecja cilj je dokazan
					for (Klauzula kl : rezolvente) {
						if (kl.literali.size() == 0) {
							NIL = kl;
							return true;
						}
					}
					nove.addAll(rezolvente);
				}
				sosIter.remove();
			}
			// ako nije dobiveno nista novo onda se dodatnim iteracijama ne može magicno dobiti nesto
			if (sveKlauzule.containsAll(nove)) {
				return false;
			}
			
			// micanje duplikata
			Iterator<Klauzula> noveIt = nove.iterator();
			while (noveIt.hasNext()) {
				if (sveKlauzule.contains(noveIt.next())) {
					noveIt.remove();
				}
			}
			// micanje nevaznih
			nove = cleanTautology(nove);
			
			// dodavanje novih
			sveKlauzule.addAll(nove);
			skupPotpore.addAll(nove);
			toBeChecked.addAll(nove);
			
			// micanje redundantnih
			cleanRedundant();
		}
		return false;
	}

	private static void cooking() {
		System.out.println("Constructed with knowledge:");
		for (Klauzula kl :startKlauzule) {
			System.out.println(kl);
		}
		System.out.println();

		for (Naredba naredba : naredbe) {
			System.out.println("User's command: " + naredba);

			if (naredba.naredbaID.equals("+")) {
				startKlauzule.add(naredba.klauzNaredbe);
				System.out.println("added: " + naredba.klauzNaredbe + "\n");

			} else if (naredba.naredbaID.contains("-")) {
				if (startKlauzule.remove(naredba.klauzNaredbe)) {
					System.out.println("removed: " + naredba.klauzNaredbe + "\n");
				} else {
					System.out.println("not in knowledge base : " + naredba.klauzNaredbe + "\n");
				}
				

			} else if (naredba.naredbaID.contains("?")) {
				LinkedHashSet<Klauzula> previousStartKlauz = new LinkedHashSet<>();
				previousStartKlauz.addAll(startKlauzule);


				Klauzula zadnjiRed = naredba.klauzNaredbe;
				cilj = zadnjiRed;
				for (String s : zadnjiRed.literali) {
					s = negation(s);

					Set<String> tempSet = new LinkedHashSet<>();
					tempSet.add(s);

					startKlauzule.add(new Klauzula(tempSet, null, null));
					skupPotpore.add(new Klauzula(tempSet, null, null));
				}
				if (rezolucija()) {
					ispis();
					System.out.println("===============");
					System.out.println("[CONCLUSION]: " + cilj + " is true");

				} else {
					System.out.println("[CONCLUSION]: " + cilj + " is unknown");
				}
				
				// epilog
				startKlauzule.clear();
				skupPotpore.clear();
				sveKlauzule.clear();
				startKlauzule.addAll(previousStartKlauz);
				System.out.println();
			}
		}
	}
	
	@SuppressWarnings("unused")
	private static void helpPrintout() {
		System.out.println("startKlauzule " + startKlauzule);
		System.out.println("sos " + skupPotpore);
		System.out.println("cilj " + cilj);
		System.out.println("bazaZnanja " + sveKlauzule);
		if (podzadatak.equals("cooking")) System.out.println("naredbe " + naredbe);
		System.out.println();
	}

	private static void ispis() {
		LinkedHashSet<Klauzula> sveKoristeneZaRezoluciju = new LinkedHashSet<>();
		LinkedList<Klauzula> toBeChecked = new LinkedList<>();

		Klauzula current;
		toBeChecked.add(NIL);

		// penjanje po stablu izvodenja/roditeljima od NIL do početnih klauzula
		while (!toBeChecked.isEmpty()) {
			current = toBeChecked.getFirst();
			sveKoristeneZaRezoluciju.add(current);

			if (current.parent1 != null) {
				sveKoristeneZaRezoluciju.add(current.parent1);
				toBeChecked.add(current.parent1);
			}
			if (current.parent2 != null) {
				sveKoristeneZaRezoluciju.add(current.parent2);
				toBeChecked.add(current.parent2);
			}
			toBeChecked.remove(current);
		}
		
		// lista klauzula za ispis - bitno za indexOf
		LinkedList<Klauzula> zaIspis = new LinkedList<>();
		// dodavanje i ispis početnih klauzula "iznad linije"
		for (Klauzula klauz : startKlauzule) {
			if (sveKoristeneZaRezoluciju.contains(klauz)) {
				zaIspis.add(klauz);
				System.out.println(zaIspis.indexOf(klauz)+1 + ". " + klauz);
				sveKoristeneZaRezoluciju.remove(klauz);
			}
		}
		System.out.println("===============");
		
		// dodavanje izvedenih klauzula "ispod linije"
		Klauzula nilKlauz = new Klauzula(null, null, null);
		LinkedList<Klauzula> sveKorZaRezLista= new LinkedList<>(sveKoristeneZaRezoluciju);
		Iterator<Klauzula> sveKorZaRezIT = sveKorZaRezLista.descendingIterator();
		while (sveKorZaRezIT.hasNext()) {
			Klauzula currKlauz = sveKorZaRezIT.next();
			if (currKlauz.literali.size() != 0) {
				zaIspis.add(currKlauz);
				sveKorZaRezIT.remove();
				sveKoristeneZaRezoluciju.remove(currKlauz);
			} else {
				nilKlauz = currKlauz;
				sveKorZaRezIT.remove();
				sveKoristeneZaRezoluciju.remove(currKlauz);
			}
		}
		zaIspis.add(nilKlauz);
		
		// ispis "ispod linije"
		for (Klauzula klauz : zaIspis) {
			if (!startKlauzule.contains(klauz)) {
				System.out.println(zaIspis.indexOf(klauz)+1 + ". " + klauz + " (" + (zaIspis.indexOf(klauz.parent1)+1) + ", " + (zaIspis.indexOf(klauz.parent2)+1) + ")");
			}
		}
	}

	public static void main(String ... args) {

		argLoader(args);
		pocKlauzLoader();
		
		if (podzadatak.equals("resolution")) {
			if (rezolucija()) {
				ispis();
				System.out.println("===============");
				System.out.println("[CONCLUSION]: " + cilj + " is true");

			} else {
				System.out.println("[CONCLUSION]: " + cilj + " is unknown");

			}
		} else if (podzadatak.equals("cooking")) {
			naredbeLoader();
			cooking();
		}
	}
}