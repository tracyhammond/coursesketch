package main;

import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.io.DomainDefinitionInputDOM;

import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;

public class deepgreentest {
	public static void main() {
		String domainFile = "../LadderDomains/domainDescriptions/domains/COA.xml";
		
		try {
			DomainDefinition domainDef = new DomainDefinitionInputDOM()
					.readDomainDefinitionFromFile(domainFile);
			DeepGreenRecognizer recognizer = new DeepGreenRecognizer(domainDef);
			
		} catch (Exception e) {
			System.err.print("ERROR: " + e.getMessage());
		}
	}
}
