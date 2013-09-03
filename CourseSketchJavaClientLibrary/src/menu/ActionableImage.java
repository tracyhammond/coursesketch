package menu;

import java.awt.event.ActionListener;

public class ActionableImage extends ActionableItem {
	private String mImageLocation;

	public ActionableImage(String name, String label, ActionListener action) {
		super(name, label, action);
	}

}
