package menu.list;

import java.awt.event.ActionListener;
import java.util.ArrayList;

import menu.ActionableItem;

/**
 * Expands the list of subItems when clicked.
 *
 * This ExpandableMenuItem can contain more menu items if needed.
 */
public class ExpandableMenuItem extends ActionableItem {
	private boolean mISExpandable;
	private ArrayList<ActionableItem> mSubItems;

	public ExpandableMenuItem(String name, String label, ActionListener action) {
		super(name, label, action);
		mSubItems = new ArrayList<ActionableItem>();
	}

	/**
	 * This action will expand the list and execute the given item if needed.
	 */
	@Override
	public void executeAction() {
	}

	public void addSubItem(ActionableItem subItem) {
		mSubItems.add(subItem);
	}
	public ArrayList<ActionableItem> getSubItems() {
		return mSubItems;
	}
}
