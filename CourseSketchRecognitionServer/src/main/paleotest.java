package main;

import srl.core.sketch.*;
import srl.recognition.*;
import srl.recognition.paleo.PaleoConfig;
import srl.recognition.paleo.PaleoSketchRecognizer;

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
		PaleoSketchRecognizer recognizer = new PaleoSketchRecognizer(PaleoConfig.allOn());
		IRecognitionResult result = recognizer.recognize(sketch.getFirstStroke());

		if (result.getBestShape().getInterpretation().label.equalsIgnoreCase("line"))
			System.out.println("Correctly recognized as a line");
	}
}
