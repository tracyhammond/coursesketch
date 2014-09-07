package converter.s2l;

public class Alias {
	public static org.ladder.core.sketch.Alias Convert (srl.core.sketch.Alias srl_Alias) {
		org.ladder.core.sketch.Point point = Point.Convert(srl_Alias.getPoint());
		String name = srl_Alias.getName();
		org.ladder.core.sketch.Alias ladder_alias = new org.ladder.core.sketch.Alias(name, point);
		return ladder_alias;
	}
}
