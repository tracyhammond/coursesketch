package main;

import java.util.List;

import org.ladder.core.sketch.IStroke;
import org.ladder.recognition.constraint.domains.DomainDefinition;
//import org.ladder.recognition.constraint.domains.io.DomainDefinitionInputDOM;

import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;

public class deepgreentest {
	public static void recognize(DomainDefinition domainDef, List<IStroke> strokes) {
		//String domainFile = "../LadderDomains/domainDescriptions/domains/COA.xml";
		
		try {
			//DomainDefinition domainDef = new DomainDefinitionInputDOM()
				//	.readDomainDefinitionFromFile(domainFile);
			DeepGreenRecognizer recognizer = new DeepGreenRecognizer(domainDef);
			
			for(IStroke s : strokes)
				recognizer.addStroke(s);
			
			recognizer.recognize();
		} catch (Exception e) {
			System.err.print("ERROR: " + e.getMessage());
		}
	}
}
