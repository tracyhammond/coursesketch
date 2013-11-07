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
		PaleoNNRecognizer recognizer = new PaleoNNRecognizer(PaleoConfig.allOn());
		recognizer.submitForRecognition(sketch.getFirstStroke());
		IRecognitionResult result = recognizer.recognize();
		
		if (result.getBestShape().getInterpretation().label.equalsIgnoreCase("line"))
			System.out.println("Correctly recognized as a line");
	}
}
