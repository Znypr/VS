package de.hs_mannheim.plugins;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Plugin2 implements PluginInterface{

	/**
	 * Jeder Name aus der NameList, der in der Nachricht vorkommt wird durch "[*** Name ***]" ersetzt
	 */
	@Override
	public String transformString(String message) {
		
		if (message.isEmpty()) {
			return message;
		}
		
		ArrayList<String> names = NameList.getNames();
		String returnMessage = message;		
		
		for (int i = 0; i < names.size(); i++) {
			// Pattern matcht alle alleinstehenden Namen aus der NameList.
			// Also wenn "Jens" in der NameList vorhanden ist, wird in der Nachricht "Hier ist Jens aus dem Jenseits" 
			// nur das erste "Jens" gematcht aber nicht das "Jens" in "Jenseits"
			String regexPattern = "(?=\\b)" + names.get(i) + "(?=\\b)";
			
			Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(returnMessage);
					
			returnMessage = matcher.replaceAll("[*** Name ***]");
		}
		
		return returnMessage;
	}
}
