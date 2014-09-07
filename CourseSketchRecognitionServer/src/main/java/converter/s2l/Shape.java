package converter.s2l;

public class Shape {
	public static org.ladder.core.sketch.Shape Convert(srl.core.sketch.Shape srl_Shape) {
		org.ladder.core.sketch.Shape ladder_Shape = new org.ladder.core.sketch.Shape();
		
		for (srl.core.sketch.Stroke stroke : srl_Shape.getStrokes()) {
			ladder_Shape.addStroke(Stroke.Convert(stroke));
		}
		
		for (srl.core.sketch.Shape subShape : srl_Shape.getShapes()) {
			ladder_Shape.addSubShape(Convert(subShape));
		}
		
		for (srl.core.sketch.Alias alias : srl_Shape.getAliases()) {
			ladder_Shape.addAlias(Alias.Convert(alias));
		}
		
		ladder_Shape.setLabel(srl_Shape.getName());
		// TODO confidence not found in srl shape
		//ladder_Shape.setConfidence(srl_Shape.);
		
		ladder_Shape.setID(srl_Shape.getId());

		return ladder_Shape;
	}
}
