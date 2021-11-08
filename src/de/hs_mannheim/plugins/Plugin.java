package de.hs_mannheim.plugins;

import java.util.ArrayList;

public class Plugin implements PluginInterface{

	ArrayList<String> names;
	
	public Plugin() {
		this.names = new ArrayList<>();
		
		this.names.add("Mike");
		this.names.add("Eirich");
		this.names.add("Patrick");
		this.names.add("Lorenz");
		this.names.add("Candra");
		this.names.add("Mulyadhi");
		this.names.add("Jens");
		this.names.add("Schehlmann");
		this.names.add("Katharina");
		this.names.add("Salewski");
	}
	
	@Override
	public String transformString(String message) {
		
		if (message.isEmpty()) {
			return message;
		}
		
		for (int i = 0; i < names.size(); i++) {
			if (message.contains(names.get(i))) {
				return "Vertraulich: enthält personenbezogene Daten\n" + message;
			}
		}
		
		return message;
	}
}
