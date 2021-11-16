package de.hs_mannheim.plugins;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Plugin1 implements PluginInterface{
	
	/**
	 * Wenn die übergebene Nachricht einen Namen aus der NameList enthält,
	 * wird an die erste Zeile der Nachricht "Vertraulich: enthält personenbezogene Daten" angehangen
	 */
	@Override
	public String transformString(String message) {
		
		if (message.isEmpty()) {
			return message;
		}
		
		ArrayList<String> names = NameList.getNames();
		
		for (int i = 0; i < names.size(); i++) {
			// Pattern matcht alle alleinstehenden Namen aus der NameList.
			// Also wenn "Jens" in der NameList vorhanden ist, wird in der Nachricht "Hier ist Jens aus dem Jenseits" 
			// nur das erste "Jens" gematcht aber nicht das "Jens" in "Jenseits"
			String regexPattern = "(?=\\b)" + names.get(i) + "(?=\\b)";
			
			Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(message);
			
			if (matcher.find()) {
				return "Vertraulich: enthält personenbezogene Daten\n" + message;
			}
		}
		
		return message;
	}
}
