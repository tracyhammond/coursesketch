package converter.s2l;

import converter.s2l.Point;

public class Stroke {
	public static org.ladder.core.sketch.Stroke Convert(srl.core.sketch.Stroke srl_Stroke) {
		org.ladder.core.sketch.Stroke ladder_Stroke = new org.ladder.core.sketch.Stroke();
		
		for (srl.core.sketch.Point point : srl_Stroke.getPoints()) {
			ladder_Stroke.addPoint(Point.Convert(point));
		}
		
		for (srl.core.sketch.Segmentation segmentation : srl_Stroke.getSegmentations()) {
			ladder_Stroke.addSegmentation(Segmentation.Convert(segmentation));		
		}

		ladder_Stroke.setID(srl_Stroke.getId());
		
		ladder_Stroke.setParent(Convert(srl_Stroke.getParent()));
		
		ladder_Stroke.getBoundingBox();
		ladder_Stroke.getPathLength();
		ladder_Stroke.getMinInterPointDistance();
		ladder_Stroke.setLabel(srl_Stroke.getInterpretation().label);
		
		return ladder_Stroke;
	}
}
