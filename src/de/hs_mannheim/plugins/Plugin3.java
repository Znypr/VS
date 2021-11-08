package de.hs_mannheim.plugins;

public class Plugin3 implements PluginInterface{

	public Plugin3() {
		
	}
	
	@Override
	public String transformString(String message) {
		return "Drittes Plugin";
	}
	
}
