package de.hs_mannheim.plugins;

import java.util.ArrayList;

public class NameList {
	
	private static ArrayList<String> names;
	
	static {
		names = new ArrayList<>();
		
		names.add("Mike");
		names.add("Eirich");
		names.add("Patrick");
		names.add("Lorenz");
		names.add("Candra");
		names.add("Mulyadhi");
		names.add("Jens");
		names.add("Schehlmann");
		names.add("Katharina");
		names.add("Salewski");
    }
	
	public static ArrayList<String> getNames() {
		return names;
	}
	
	public static void addName(String name) {
		names.add(name);
	}
	
	public static void removeName(String name) {
		names.remove(name);
	}
}
