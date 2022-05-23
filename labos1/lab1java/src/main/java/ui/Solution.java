package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

//čvor stabla pretraživanja
class Node {
	private Node parent;
	private String state;
	//cijena prijelaza između roditelja i ovog čvora
	private Double price;
	//ukupna cijena puta do ovog stanja
	private Double cost;
	//cost + heuristika ovog stanja
	private Double totalEstimatedCost;

	public Node(Node parent, String state, Double price) {
		super();
		this.parent = parent;
		this.state = state;
		this.price = price;
		this.cost = costCalcRecursive(this);
	}

	public Node(Node parent, String state, Double price, Double totalEstimatedCost) {
		super();
		this.parent = parent;
		this.state = state;
		this.price = price;
		this.cost = costCalcRecursive(this);
		this.totalEstimatedCost = totalEstimatedCost;
	}

	public Node getParent() {
		return parent;
	}
	public void setParent(Node parent) {
		this.parent = parent;
	}

	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}

	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}

	public int getDepth() {
		int d = 0;
		Node current = this.getParent();
		while(current != null) {
			d++;
			current = current.getParent();
		}
		return d;
	}

	@Override
	public String toString() {
		return String.format("%s", state);
	}

	public String getPath() {
		StringBuilder stringBuilder = new StringBuilder();
		getPathRecursive(stringBuilder, this);
		return stringBuilder.toString();
	}

	private static void getPathRecursive(StringBuilder sb, Node node) {
		if (node.getParent() != null) {
			getPathRecursive(sb, node.getParent());
			sb.append(" => ");
		}
		sb.append(node);
	}

	public Double getCost() {
		return cost;
	}

	private static Double costCalcRecursive(Node node) {
		if (node.getParent() != null ) {
			return node.getPrice() + costCalcRecursive(node.getParent());
		} else return node.getPrice();
	}


	public Double getTotalEstimatedCost() {
		return totalEstimatedCost;
	}

	public void setTotalEstimatedCost(Double totalEstimatedCost) {
		this.totalEstimatedCost = totalEstimatedCost;
	}
}

public class Solution {

	private static String algorithm;
	private static String statePathString;
	private static String heuristicPathString;
	private static boolean checkOptimistic = false;
	private static boolean checkConsistent = false;
	//HashMap: key - String, value - HashMap
	private static Map<String, Map<String, Double>> stateMap = new HashMap<>();
	private static Map<String, Double> heuristicMap;
	private static String s0;
	private static Set<String> endStates = new HashSet<>();
	private static Double hZvjezdica;

	//pomocna funkcija za ucitavanje argumenata
	private static void argLoader(String ... args) {
		for(int i=0; i<args.length;) {
			switch (args[i]) {
			case "--alg":
				algorithm = args[i+1];
				i += 2;
				break;
			case "--ss":
				statePathString = args[i+1];
				i += 2;
				break;
			case "--h":
				heuristicPathString = args[i+1];
				i += 2;
				break;
			case "--check-optimistic":
				checkOptimistic = true;
				i++;
				break;
			case "--check-consistent":
				checkConsistent = true;
				i++;
				break;
			default:
				i++;
				break;
			}
		}
	}

	//pomocna funkcija za ucitavanje opisnika prostora stanja
	private static void stateLoader() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(statePathString));
		//ucitavanje s0 iz opisnika
		String mybS0 = reader.readLine();
		while (mybS0.startsWith("#") && mybS0 != null) {
			mybS0 = reader.readLine();
		}
		s0 = mybS0;
		//ucitavanje ciljnih stanja iz opisnika
		String mybEnd = reader.readLine();
		while (mybEnd.startsWith("#") && mybEnd != null) {
			mybEnd = reader.readLine();
		}
		endStates.addAll(Arrays.asList(mybEnd.split(" ")));

		//ucitavanje funkcija prijelaza
		String mybSucc = reader.readLine();
		while (mybSucc != null) {
			if (!mybSucc.startsWith("#")) {
				Map<String, Double> tempMap = new HashMap<>();
				String[] tempStringArray = mybSucc.split(": ");
				if (tempStringArray.length > 1) {
					String[] tempInnerStringArray = tempStringArray[1].split(" ");
					for (String s : tempInnerStringArray) {
						String[] a = s.split(",");
						Double potentialPrice = Double.parseDouble(a[1]);
						if (tempMap.containsKey(a[0])) {
							if (tempMap.get(a[0]) <= potentialPrice) continue;
						}
						tempMap.put(a[0], potentialPrice);
					}
				}
				stateMap.put(tempStringArray[0], tempMap);
			}
			mybSucc = reader.readLine();
		}
		reader.close();
	}

	//pomocna funkcija za ucitavanje heuristicke funkcije
	private static void heuristicLoader() throws IOException {
		//ucitavanje heuristika
		heuristicMap = new HashMap<>();
		BufferedReader hReader = new BufferedReader(new FileReader(heuristicPathString));
		String mybH = hReader.readLine();
		while (mybH != null) {
			if (!mybH.startsWith("#")) {
				String state = mybH.split(": ")[0];
				Double heuristic_value = Double.parseDouble(mybH.split(": ")[1]);
				heuristicMap.put(state, heuristic_value);
			}
			mybH = hReader.readLine();
		}
		hReader.close();
	}

	//debugging funkcija za ispis vremena izvođenja
	public static void timeBetween(Instant start, Instant end){
		Duration timeElapsed = Duration.between(start, end);
		long timeSpent = timeElapsed.toMillis();
		long diffSeconds = timeSpent / 1000 % 60;
		long diffMinutes = timeSpent / (60 * 1000) % 60;
		long diffHours = timeSpent / (60 * 60 * 1000) % 24;
		long diffDays = timeSpent / (24 * 60 * 60 * 1000);
		System.out.println("\nTime taken: " + diffDays+"days " + diffHours+"h "+diffMinutes+"m "+diffSeconds+"s "+timeSpent%1000+"ms" );
	}

	//pomocna funkcija za ispis pretrazivanja algoritmom
	public static void algPrint(Node n, boolean found, int statesVisited) {
		switch (algorithm) {
		case "bfs":
			System.out.println("# BFS");
			break;
		case "ucs":
			System.out.println("# UCS");
			break;
		case "astar":
			System.out.println("# A-STAR " + heuristicPathString);
			break;
		}

		if (!found) System.out.println("[FOUND_SOLUTION]: no");
		else {
			System.out.println("[FOUND_SOLUTION]: yes");
			System.out.println("[STATES_VISITED]: " + statesVisited);
			String path = n.getPath();
			System.out.println("[PATH_LENGTH]: " + path.split("=>").length);
			System.out.printf("[TOTAL_COST]: %.1f\n" , n.getCost());
			System.out.println("[PATH]: " + path);
		}
	}

	public static void bfs() {
		Deque<Node> open = new LinkedList<>();
		Set<String> openSet = new HashSet<>();
		Set<String> visited = new HashSet<>();

		Node n0 = new Node(null, s0, 0.0);
		if (endStates.contains(s0)) {
			algPrint(n0, true, 1);
			return;
		}
		open.add(n0);
		visited.add(n0.getState());

		while (!open.isEmpty()) {
			Node n = open.removeFirst();
			openSet.remove(n.getState());
			
			if(stateMap.get(n.getState()) != null) {
				List<String> succList = new LinkedList<String>(stateMap.get(n.getState()).keySet());
				Collections.sort(succList);
//				Collections.sort(succList, Comparator.comparing(Node::getState).thenComparing(Node::getCost));
				
				for(String child : succList) {
					if (!openSet.contains(child) && !visited.contains(child)) {
						Node childNode = new Node(n, child, stateMap.get(n.getState()).get(child));
						visited.add(child);
						if (endStates.contains(child)) {
							algPrint(childNode, true, visited.size());
							return;
						}
						open.addLast(childNode);
						openSet.add(child);
					}
				}
			}
		}
		algPrint(n0, false, visited.size());
		return;
	}

	public static void ucs() {
		Queue<Node> open = new PriorityQueue<>(Comparator.comparing(Node::getCost).thenComparing(Node::getState));
		Map<String, Double> openMap = new HashMap<>();
		Set<String> visited = new HashSet<>();

		Node n0 = new Node(null, s0, 0.0);
		open.add(n0);
		openMap.put(s0, 0.0);

		while (!open.isEmpty()) {
			Node n = open.poll();
			openMap.remove(n.getState());
			visited.add(n.getState());
			if (endStates.contains(n.getState())) {
				//u slucaju da je pozvan za provjeru optimisticnosti
				//heuristike vrati cijenu puta preko h*
				if (checkOptimistic) {
					hZvjezdica = n.getCost();
				} else {
					algPrint(n, true, visited.size());
				}
				return;
			}
			if(stateMap.get(n.getState()) != null) {
				Set<String> succSet = stateMap.get(n.getState()).keySet();
				for (String child : succSet) {
					if (visited.contains(child)) continue;
					double childCost = n.getCost() + stateMap.get(n.getState()).get(child);
					boolean openHasCheaper = false;

					//optimizacija zakomentiranog bloka ispod
					if (openMap.containsKey(child)) {
						if (openMap.get(child) <= childCost) {
							openHasCheaper = true;
						} else {
							Iterator<Node> it = open.iterator();
							while (it.hasNext()) { 
								Node m = it.next();
								if (m.getState().equals(child)) {
									it.remove();
									break;
								}
							}
							openMap.remove(child);
						}
					}
					if (!openHasCheaper) {
						Node childNode = new Node(n, child, childCost-n.getCost());
						open.add(childNode);
						openMap.put(child, childCost);
					}
//					Iterator<Node> it = open.iterator();
//					while (it.hasNext()) { 
//						Node m = it.next(); 
//						if (!m.getState().equals(child)) continue;
//						if (m.getCost() <= childCost) {
//							openHasCheaper = true;
//						} else {
//							it.remove();
//						} break;
//					}
//					if (!openHasCheaper) {
//						Node childNode = new Node(n, child, stateMap.get(n.getState()).get(child));
//						open.add(childNode); 
//					}
				}
			}
		}
		if (checkOptimistic) {
			hZvjezdica = -100.0;
		} else {
			algPrint(n0, false, visited.size());
		}
		return;
	}

	public static void astar() {
		Queue<Node> open = new PriorityQueue<>(Comparator.comparing(Node::getTotalEstimatedCost).thenComparing(Node::getState));
		Map<String, Double> openMap = new HashMap<>();
		Set<String> visited = new HashSet<>();

		Node n0 = new Node(null, s0, 0.0, heuristicMap.get(s0));
		open.add(n0);
		openMap.put(s0, heuristicMap.get(s0));

		while(!open.isEmpty()) {
			Node n = open.poll();
			openMap.remove(n.getState());
			visited.add(n.getState());
			if (endStates.contains(n.getState())) {
				algPrint(n, true, visited.size());
				return;
			}
			if(stateMap.get(n.getState()) != null) {
				Set<String> succSet = stateMap.get(n.getState()).keySet();
			
				for (String child : succSet) {
					if (visited.contains(child)) continue;
					double cost = n.getCost() + stateMap.get(n.getState()).get(child);
					double total = cost + heuristicMap.get(child);
					boolean openHasCheaper = false;

					if (openMap.containsKey(child)) {
						if (openMap.get(child) <= total) {
							openHasCheaper = true;
						} else {
							Iterator<Node> it = open.iterator();
							while (it.hasNext()) {
								Node m = it.next();
								if (m.getState().equals(child)) {
									it.remove();
									break;
								}
							}
							openMap.remove(child);
						}
					}
					if (!openHasCheaper) {
						Node childNode = new Node(n, child, cost-n.getCost(), total);
						open.add(childNode);
						openMap.put(child, total);
					}
				}
			}
		}
		algPrint(n0, false, visited.size());
		return;

	}

	public static void consistency() {
		System.out.println("# HEURISTIC-CONSISTENT " + heuristicPathString);
		boolean isConsistent = true;
		List<String> sortedHeur = new LinkedList<>(heuristicMap.keySet());
		Collections.sort(sortedHeur);

		for (String currentState : sortedHeur) {
			if (stateMap.get(currentState) != null) {
				for (Map.Entry<String, Double> neighbor : stateMap.get(currentState).entrySet()) {
					if (heuristicMap.get(currentState) <= heuristicMap.get(neighbor.getKey()) + neighbor.getValue()) {
						System.out.printf("[CONDITION]: [OK] h(%s) <= h(%s) + c: %.1f <= %.1f + %.1f\n", currentState, neighbor.getKey(), heuristicMap.get(currentState), heuristicMap.get(neighbor.getKey()), neighbor.getValue());
					} else {
						isConsistent = false;
						System.out.printf("[CONDITION]: [ERR] h(%s) <= h(%s) + c: %.1f <= %.1f + %.1f\n", currentState, neighbor.getKey(), heuristicMap.get(currentState), heuristicMap.get(neighbor.getKey()), neighbor.getValue());
					}
				}
			}
		}
		if (isConsistent) {
			System.out.println("[CONCLUSION]: Heuristic is consistent.");
		} else {
			System.out.println("[CONCLUSION]: Heuristic is not consistent.");
		}
	}

	public static void optimism() {
		System.out.println("# HEURISTIC-OPTIMISTIC " + heuristicPathString);
		boolean isOptimistic = true;
		List<String> sortedHeur = new LinkedList<>(heuristicMap.keySet());
		Collections.sort(sortedHeur);
				
		for (String currentState : sortedHeur) {
			s0 = currentState;
			ucs();
			
			if (heuristicMap.get(currentState) <= hZvjezdica) {
				System.out.printf("[CONDITION]: [OK] h(%s) <= h*: %.1f <= %.1f\n", currentState, heuristicMap.get(currentState), hZvjezdica);
			} else {
				isOptimistic = false;
				System.out.printf("[CONDITION]: [ERR] h(%s) <= h*: %.1f <= %.1f\n", currentState, heuristicMap.get(currentState), hZvjezdica);
			}
		}
		if (isOptimistic) {
			System.out.println("[CONCLUSION]: Heuristic is optimistic.");
		} else {
			System.out.println("[CONCLUSION]: Heuristic is not optimistic.");
		}
	}
	
	public static void main(String ... args) throws IOException {
//		Instant start = Instant.now();
		
		//ucitavanje argumenata
		argLoader(args);
		//ucitavanje opisinika prostora stanja
		stateLoader();

		if (checkConsistent) {
			heuristicLoader();
			consistency();
		} else if (checkOptimistic) {
			heuristicLoader();
			optimism();
		} else {
			switch (algorithm) {
			case "bfs":
				bfs();
				break;
			case "ucs":
				ucs();
				break;
			case "astar":
				heuristicLoader();
				astar();
				break;
			}
		}

//		Instant end = Instant.now();
//		timeBetween(start,end);
	}

}
