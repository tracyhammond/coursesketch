package menu.list;

import java.awt.event.ActionListener;

import menu.ActionableItem;

/**
 * Has a different style than a normal menu item but can serve the same function.
 */
public class HeaderMenuItem extends ActionableItem {

	public HeaderMenuItem(String name, String label, ActionListener action) {
		super(name, label, action);
	}

}
