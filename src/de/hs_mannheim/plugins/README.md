
## CA-2 Plugins für JMS/Socket-Adapter
Die Abgabe dieser Teilaufgabe besteht aus den folgenden 8 Klassen *Conf*, *Main*, *ProducerNode*, *TCPClient*, *NameList*, *Plugin1*, *Plugin2*, *Plugin3*, dem Interface *PluginInterface* und der *plugin.properties*- sowie *jndi.properties*-Datei.

Die Klassen *Conf*, *ProducerNode* und *TCPClient* haben sich zur vorherigen Teilaufgabe nicht verändert. Deren kurze Beschreibung kann in der README-Datei der letzten Teilaufgabe nachgelesen werden.

## Erstellen eines eigenen Plugins
Plugins müssen Java-Klassen sein und das Interface *"PluginInterface"* und dessen Methode *"public String transformString(String message)"* implementieren. Diese transformString-Methode eines Plugins wird anschließend in *Main* aufgerufen um die eingegangene Nachricht zu transformieren.

## Nutzen eines Plugins
In *Main* kann an zwei Stellen ein Plugin verwendet werden. 

1. Bei Nachrichten vom JMS-Broker zu den TCP-Clients
2. Bei Nachrichten von den TCP-Clients zum JMS-Broker

Um auszuwählen, welches Plugin an welcher Stelle benutzt werden soll, existiert die *"plugins.properties"*-Datei. Diese Datei enthält zwei Properties *"JMSToClientPlugin"* und *"clientToJMSPlugin"*. Bei *"JMSToClientPlugin"* wird angegeben, welches Plugin genutzt werden soll um Nachrichten vom JMS-Broker zu den TCP-Clients zu transformieren. Bei *"clientToJMSPlugin"* welches Plugin genutzt werden soll um Nachrichten von den TCP-Clients zum JMS-Broker zu transformieren. Dabei gibt man den Klassennamen des Plugins an. Bei einer Klasse namens *"Plugin1"* im Package *"de.hs_mannheim.plugins"* würde es folgendermaßen aussehen, wenn man dieses Plugin bei Nachrichten vom JMS-Broker zu den TCP-Clients nutzen möchte:
> JMSToClientPlugin = de.hs_mannheim.plugins.Plugin1

Wenn man für einen der oberen beiden Fälle die Nachrichten nicht transformieren und somit kein Plugin nutzen möchte, so kann man "None" in der *"plugins.properties"*-Datei bei der entsprechenden Property angeben.

Bsp:
> clientToJMSPlugin = None

Für den Fall, dass bei einer Property kein gültiger Klassenname eines Plugins genutzt wird, verhält sich das Programm wie wenn "None" als Property gesetzt wird und kein Plugin genutzt wird. Das heißt, die Nachricht wird nicht transformiert.

## Main
Der einzige Unterschied zu *Main* aus der vorherigen Teilaufgabe ist, dass in *Main* bei jeder eingegangenen Nachricht (egal ob vom JMS-Broker oder von den Clients) in der *"plugins.properties"*-Datei geschaut wird, welches Plugin für den konkreten Anwendungsfall hinterlegt ist. Dieses Plugin wird dann mit Hilfe von Java-Reflections geladen. Somit kann man bequem während der Laufzeit die Plugins wechseln ohne *Main* oder die *TCPClients* neu starten zu müssen. Man muss lediglich die *plugins.properties*-Datei anpassen.

## NameList
Enthält eine erweiterbare Liste mit Namen. Initial sind die Vor- und Nachnamen aller Teammitglieder enthalten. Es ist jedoch auch die Funktionalität enthalten nachträglich noch weiteren Namen hinzuzufügen bzw. zu entfernen. Die List selbst ist *static*, wodurch jedes Plugin auf die selbe Liste zugreifen kann.

## Plugin1
Wenn die übergebene Nachricht an dieses Plugin mindestens einen Namen aus der *NameList* enthält, wird die Nachricht so transformiert, dass in der ersten Zeile vor der eigentlichen Nachricht *"Vertraulich: enthält personenbezogene Daten"* steht. Dieses Plugin wird standardmäßig bei Nachrichten vom JMS-Broker zu den Clients verwendet.

## Plugin2
Bei diesem Plugin wird die übergebene Nachricht so transformiert, dass alle vorkommen der Namen aus der *NameList* in der übergebenen Nachricht durch den Text "[\*\*\* Name \*\*\*]" ersetzt werden. Dieses Plugin wird standardmäßig bei Nachrichten von den Clients zum JMS-Broker verwendet.

## Plugin3
Dieses Plugin dient nur dazu, um ein weiteres Plugin zur Verfügung zu haben um zu Testzwecken während der Laufzeit zwischen mehr als 2 Plugins wechseln zu können. Aus diesem Grund ersetzt dieses Plugin die übergebene Nachricht einfach durch den Text "Drittes Plugin".