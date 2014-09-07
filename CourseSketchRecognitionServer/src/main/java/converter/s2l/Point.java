package converter.s2l;

import java.util.UUID;

public class Point {
	public static org.ladder.core.sketch.Point Convert(srl.core.sketch.Point srl_Point) {
		org.ladder.core.sketch.Point ladder_Point = new org.ladder.core.sketch.Point();
		Double x = srl_Point.getX();
		Double y = srl_Point.getY();
		long time = srl_Point.getTime();
		UUID id = srl_Point.getId();
		Double tiltX = srl_Point.getTiltX();
		Double tiltY = srl_Point.getTiltY();
		Double pressure = srl_Point.getPressure();
		
		ladder_Point.set(x, y, time);
		ladder_Point.setID(id);
		ladder_Point.setTilt(tiltX, tiltY);
		ladder_Point.setPressure(pressure);
		
		return ladder_Point;
	}
}
