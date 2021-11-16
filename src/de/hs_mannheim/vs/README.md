# CA-2 JMS/Socket-Adapter
Die Abgabe dieser Teilaufgabe besteht aus den folgenden 4 Klassen *Conf*, *Main*, *ProducerNode* und *TCPClient* im package *de.hs_mannheim.vs*, sowie der *jndi.properties*-Datei.

## *Conf* und *jndi.properties*
Dienen zur Konfiguration der zu nutzenden Queues mit Hilfe von *Apache ActiveMQ* als Message Broker.

## ProducerNode
Dient als MessageProducer für eine vorgegebene Destination. Standardmäßig ist dies unser *topic* *"de.hs_mannheim.vs.channel" (topic1337)*, von dem unser *JMS-Client (Main)* die Nachrichten vom JMS-Broker empfängt. Die Nachricht, welche an den JMS-Broker gesendet werden soll, muss als Kommandozeilenparameter übergeben werden.

Bsp:
> java ProducerNode.java "Das ist eine Nachricht"

## Main
Fungiert als JMS-Client und TCP-Server gleichzeitig. TCP-Clients können sich per TCP mit diesem TCP-Server über Port 8888 verbinden. Empfängt *Main* eine Nachricht aus dem topic *"de.hs_mannheim.vs.channel" (topic1337)*, so wird diese Nachricht an alle verbundenen TCP-Clients weitergeleitet. Erhält *Main* eine Nachricht von einem TCP-Client, so wird diese Nachricht an eine vordefinierte Destination am Message Broker geschickt. Diese Destination kann im Quellcode frei gewählt werden und ist aktuell die *Queue* *"de.hs_mannheim.vs.queue" (queue1337)*.

## TCPClient
*TCPClients* können sich per TCP mit dem TCP-Server *Main* verbinden und diesem Nachrichten schicken, welche dann von *Main* an eine vordefinierte Destination im JMS-Broker weitergleitet werden. Außerdem erhalten die *TCPClients* von *Main* alle Nachrichten vom JMS-Broker aus dem topic *"de.hs_mannheim.vs.channel" (topic1337)*.