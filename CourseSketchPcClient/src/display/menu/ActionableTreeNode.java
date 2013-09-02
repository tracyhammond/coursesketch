package display.menu;

import javax.swing.tree.DefaultMutableTreeNode;

import menu.ActionableItem;

public class ActionableTreeNode extends DefaultMutableTreeNode {

	ActionableItem onClickItem;
	public ActionableTreeNode() {
		super();
	}

	public ActionableTreeNode(ActionableItem item, boolean allowsChildren) {
		super(item.getLabel(), allowsChildren);
	}

	public ActionableTreeNode(ActionableItem item) {
		super(item.getLabel());
	}

}
