package menu;

import java.awt.event.ActionListener;

public class ActionableImage extends ActionableItem {
	private String imageLocation;

	public ActionableImage(String name, String label, ActionListener action) {
		super(name, label, action);
	}

}
