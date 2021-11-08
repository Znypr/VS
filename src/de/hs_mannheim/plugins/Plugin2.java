package de.hs_mannheim.plugins;

import java.util.ArrayList;

public class Plugin2 implements PluginInterface{

	ArrayList<String> names;
	
	public Plugin2() {
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
		
		String returnMessage = message;
			
		for (int i = 0; i < names.size(); i++) {
			if (returnMessage.contains(names.get(i))) {
				StringBuilder builder = new StringBuilder(returnMessage);
				
				int startIndex = returnMessage.indexOf(names.get(i));
				int stopIndex = startIndex + names.get(i).length();
				
				returnMessage = builder.replace(startIndex, stopIndex, "[*** Name ***]").toString();
			}
		}
		
		return returnMessage;
	}
}
