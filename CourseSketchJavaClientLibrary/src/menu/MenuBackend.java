package menu;

import java.util.ArrayList;

import menu.list.ExpandableMenuItem;
import menu.list.HeaderMenuItem;

/**
 * A bass class that should be subclassed for the frontend portion.
 *
 * The subclass should make the display based on what is added to it.
 */
public class MenuBackend {
	public static final String MENU_HEADER1 = "Classes I'm In";
	public static final String MENU_HEADER2 = "Classes I'm Teaching";
	public static final String MENU_HEADER3 = "Account";

	private final ArrayList<ActionableItem> mMenuList = new ArrayList<ActionableItem>();

	public void addMenuItem(ActionableItem item) {
		mMenuList.add(item);
	}

	// TODO: remove this!
	public void addFakeMenu() {
		addMenuItem(new ActionableItem("home","Home",null));
		addMenuItem(new HeaderMenuItem("currentClasses","Classes I'm In",null));
		ExpandableMenuItem expander = new ExpandableMenuItem("viewClasses","View All Classes",null);
		addSubItems(expander);
		addMenuItem(expander);
		addMenuItem(new ActionableItem("newClass","Add New Class",null));
		addMenuItem(new ActionableItem("grades","My Grades",null));
		addMenuItem(new ActionableItem("hideClass","Hide Class",null));
		addMenuItem(new HeaderMenuItem("teachingClasses","Classes I'm Teaching",null));
		addMenuItem(new ActionableItem("classGrade","Grades",null));
		addMenuItem(new ActionableItem("viewKey","View Class Keys",null));
		addMenuItem(new HeaderMenuItem("account","Account",null));
		addMenuItem(new ActionableItem("changepw","Change Password",null));
		addMenuItem(new ActionableItem("out","Sign Out",null)); // obvious one!
	}

	public void addSubItems(ExpandableMenuItem expander) {
		expander.addSubItem(new ActionableItem("class1", "Physics", null));
		expander.addSubItem(new ActionableItem("class2", "Chemistry", null));
		expander.addSubItem(new ActionableItem("class3", "Japanese", null));
	}
}
