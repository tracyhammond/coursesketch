package main;

import srl.core.sketch.*;
import srl.recognition.*;
import srl.recognition.paleo.PaleoConfig;
import srl.recognition.paleo.PaleoSketchRecognizer;
import srl.recognition.paleo.paleoNN.PaleoNNRecognizer;

public class paleotest {
	public static void main(String[] args) {
		Sketch sketch = new Sketch();

		Stroke stroke = new Stroke();
		for (int i = 0; i < 20; i++) {
			stroke.addPoint(new Point(i, i));
		}

		sketch.add(stroke);

		/*
		 * Run basic shape recognition on the first stroke of the sketch (the
		 * one we just created) This should result in a best shape label of
		 * "Line"
		 */
		
		IRecognitionResult result = null ;

		// Paleo Original
		PaleoSketchRecognizer recognizer = new PaleoSketchRecognizer(PaleoConfig.allOn()) ;
		result = recognizer.recognize(sketch.getFirstStroke());
		
		// Paleo Neural Network
		PaleoNNRecognizer paleonn = new PaleoNNRecognizer(PaleoConfig.allOn());
		paleonn.submitForRecognition(sketch.getFirstStroke());
		result = paleonn.recognize();
		
		System.out.println(result.getBestShape().getInterpretation().label.toString());
		
		if (result.getBestShape().getInterpretation().label.equalsIgnoreCase("line"))
			System.out.println("Correctly recognized as a line");
	}
}
