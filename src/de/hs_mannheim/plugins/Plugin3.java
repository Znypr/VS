package de.hs_mannheim.plugins;

public class Plugin3 implements PluginInterface{
	
	@Override
	public String transformString(String message) {
		return "Drittes Plugin";
	}
	
}
