package ui;

import java.util.Set;

public class Klauzula {

	Set<String> literali;
	Klauzula parent1;
	Klauzula parent2;

	public Klauzula(Set<String> literali, Klauzula parent1, Klauzula parent2) {
		this.literali = literali;
		this.parent1 = parent1;
		this.parent2 = parent2;
	}

	@Override
	public String toString() {

		if (literali.size()==0) {
			return "NIL";
		} else {
			String s = new String();
			for (String literal : literali) {
				s += literal + " v ";
			}
			s = s.substring(0, s.length()-3);
			return s;	
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((literali == null) ? 0 : literali.hashCode());
//		result = prime * result + ((parent1 == null) ? 0 : parent1.hashCode());
//		result = prime * result + ((parent2 == null) ? 0 : parent2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Klauzula other = (Klauzula) obj;
		if (literali == null) {
			if (other.literali != null)
				return false;
		} else if (!literali.equals(other.literali))
			return false;
		return true;
	}
}
