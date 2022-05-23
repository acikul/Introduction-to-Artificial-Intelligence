package ui;

public class Naredba {
	Klauzula klauzNaredbe;
	String naredbaID;
	
	public Naredba(Klauzula klauzNaredbe, String naredbaID) {
		this.klauzNaredbe = klauzNaredbe;
		this.naredbaID = naredbaID;
	}

	@Override
	public String toString() {
		return klauzNaredbe + " " + naredbaID;
	}
	
}
