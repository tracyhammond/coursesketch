package converter.s2l;

public class Sketch {
	org.ladder.core.sketch.Sketch Convert(srl.core.sketch.Sketch srl_Sketch) {
		org.ladder.core.sketch.Sketch ladder_Sketch = new org.ladder.core.sketch.Sketch();
		
		for (srl.core.sketch.Stroke stroke : srl_Sketch.getStrokes()) {
			ladder_Sketch.addStroke(Stroke.Convert(stroke));
		}
		
		for (srl.core.sketch.Shape shape : srl_Sketch.getShapes()) {
			ladder_Sketch.addShape(Shape.Convert(shape));
		}
		
		ladder_Sketch.setID(srl_Sketch.getId());
		
		return ladder_Sketch;
	}
}
