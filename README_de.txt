# 1. Einführung

- Micromata macht Projekte im Kundenauftrag, meistens DAX-30-Unternehmen (DHL, VW, Wingas, Wintershall, BBraun, ...).
- Wir haben oft größere Spezifikationsphasen in denen vielen Dokumente in unterschiedlichsten Formaten verfasst werden. Meistens geht es mit Confluence oder anderen modernen kollaborativen Tools los, dann muss aber noch ein CI oben drauf oder eine Vorlage verwendet werden und am Ende landet man doch bei Word, Excel, Powerpoint, PDF und Konsorten.
- Diese Spezifikationen legen wir klassischerweise in einem Dokumentations-Repository in Git oder SVN ab.
- Das letzte Projekt, welches ich begleitet habe, hatte über zweitausend Leistungstage und eine sehr ausgedehnte Spezifikationsphase. Dabei haben wir über 300 Dokumente vom Kunden geliefert bekommen und haben selber über 100 Dokumente angefertigt mit jeweils zwischen 40 und 100 Seiten Text.
- Egal wie gut man strukturiert, organisiert, referenziert und verlinkt, ab einer bestimmten Größe ist das Auffinden von Informationen äußerst schwierig und zeitintensiv.
- Daher hatten ein Kollege und ich die Idee, dass wir eine Suchmaschine in unseren bestehende Entwicklungsinfrastruktur integrieren und dabei möglichst sinnvolle Technologien einsetzen wollen um möglichst wenig Zeit für die Implementierung zu verwenden und maximal viel Spaß dabei haben ;-)

# 2. Architektur
- Für die Implementierung haben wir genommen, was wir kannten und konnten. So sind wir bei dem Playframework mit Scala, Elasticsearch und Git gelandet.
- Dabei ist die Idee, dass wir eine Webanwendung bauen, welche auf dem lokalen Dateisystem arbeitet in welchem die Dokumente liegen und es einen CronActor gibt, welcher alle 5 Minunten ein ``git pull`` macht und danach ein ``reindex`` der Dokumente in Elasticsearch anstößt.
- Jede Suchanfrage wird von der Webanwendung für den Elasticsearch-Server aufbereitet und das Ergebnis aufgehübscht angezeigt.
- Elasticsearch kann dabei entweder ``embedded`` oder ``standalone`` gestartet werden.

# 3. Implementierung
## Schritt 1: Ein neues Projekt
``activator new documentsearch``<br/>
Wir machen ein neues play-scala-Projekt und löschen alle Dateien, die wir nicht aus dem Template brauchen und räumen die Konfigurationsdateien auf.

## Schritt 2: build.sbt
Wir fügen die Abhängigkeiten zu ``elasticsearch``, ``commons-io`` hinzu. Weiterhin verwenden wir eine ``logger.xml`` und implementieren eine ``Global.scala`` um Elasticsearch zu starten und implementieren die Helper Objekte ``HashHelper.scala`` und ``ElasticSearchHelper.scala``.<br/>

Das coole bei Elasticsearch plugins im ``embedded`` Modus: Die Jar-Datei muss nur im Classpath liegen damit das Plugin aktiviert ist.<br/>
Wir verwenden folgendes Plugin: https://github.com/elasticsearch/elasticsearch-mapper-attachments <br/>
``"org.elasticsearch" % "elasticsearch-mapper-attachments" % "2.3.1"``
vs.
``bin/plugin -install elasticsearch/elasticsearch-mapper-attachments/2.3.1``

Siehe: http://localhost:9200/_nodes?settings=true&pretty=true

## Schritt 3: Suchindex anlegen in ElasticSearch
Beim hochfahren muss der Suchindex mittels der Datei ``conf/createIndex.sh`` angelegt werden.

Siehe: http://localhost:9200/documentsearch/_mapping?pretty=true

## Schritt 4.1: Erste Schritt in Richtung Synchronisierung
Konfiguration der Anwendung und erste Implementierung der Synchronisierung zwischen Dateisystem und ElasticSearch.

## Schritt 4.2: BulkIndex für alle Dokumente bauen
Konfigurierten Ordner rekursiv durchsuchen und für jedes Dokument einen prepared Index erstellen und zu einem BulkIndex zusammenfassen. Anschließend den BulkIndex gesammelt an den Server senden.


# 4. Rückblick


# 5. Ausblick

