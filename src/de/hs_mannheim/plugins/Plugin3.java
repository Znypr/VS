package de.hs_mannheim.plugins;

public class Plugin3 implements PluginInterface{
	
	@Override
	public String transformString(String message) {
		// Ersetzt einfach den Text der Nachricht, dient nur dazu um ein weiteres Plugin zum auswechseln während der Laufzeit zu haben
		return "Drittes Plugin";
	}
	
}
