package menu;

import java.awt.event.ActionListener;

/**
 * Holds a name and an action and is placed in a menu.
 * @author gigemjt
 *
 */
public class ActionableItem {
	private String mName;
	private String mLabel;
	private ActionListener mAction;

	public ActionableItem(String name, String label, ActionListener action) {
		this.mName = name;
		this.mLabel = label;
		this.mAction = action;
	}

	/**
	 * Executes the action that is given to this {@code ActionableItem}.
	 */
	public void executeAction() {
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getLabel() {
		return mLabel;
	}

	public void setLabel(String label) {
		this.mLabel = label;
	}

	public void setAction(ActionListener action) {
		this.mAction = action;
	}
}
