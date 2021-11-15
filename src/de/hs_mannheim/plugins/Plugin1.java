package de.hs_mannheim.plugins;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Plugin1 implements PluginInterface{
	
	@Override
	public String transformString(String message) {
		
		if (message.isEmpty()) {
			return message;
		}
		
		ArrayList<String> names = NameList.getNames();
		
		for (int i = 0; i < names.size(); i++) {
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
