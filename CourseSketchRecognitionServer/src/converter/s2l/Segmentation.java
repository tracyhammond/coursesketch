package converter.s2l;

public class Segmentation {
	public static org.ladder.core.sketch.Segmentation Convert(srl.core.sketch.Segmentation srl_Segmentation) {
		org.ladder.core.sketch.Segmentation ladder_Segmentation = new org.ladder.core.sketch.Segmentation();
		
		ladder_Segmentation.setID(srl_Segmentation.getId());
		ladder_Segmentation.setLabel(srl_Segmentation.label);
		ladder_Segmentation.setConfidence(srl_Segmentation.confidence);
		ladder_Segmentation.setSegmenterName(srl_Segmentation.segmenterName);
		
		for  (srl.core.sketch.Stroke stroke : srl_Segmentation.getSegmentedStrokes()) {
			ladder_Segmentation.getSegmentedStrokes().add(Stroke.Convert(stroke));
		}
		return ladder_Segmentation;
	}
}
