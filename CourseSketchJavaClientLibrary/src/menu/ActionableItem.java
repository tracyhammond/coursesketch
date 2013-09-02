package menu;

import java.awt.event.ActionListener;

/**
 * Holds a name and an action and is placed in a menu.
 * @author gigemjt
 *
 */
public class ActionableItem {
	private String name;
	private String label;
	private ActionListener action;

	public ActionableItem(String name, String label, ActionListener action) {
		this.name = name;
		this.label = label;
		this.action = action;
	}

	/**
	 * Executes the action that is given to this {@code ActionableItem}.
	 */
	public void executeAction() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setAction(ActionListener action) {
		this.action = action;
	}
}
