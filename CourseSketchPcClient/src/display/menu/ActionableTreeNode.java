package display.menu;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;

import menu.ActionableItem;

public class ActionableTreeNode extends DefaultMutableTreeNode {
	private boolean mIsHighlighted = false;
	public ActionableTreeNode() {
		super();
	}

	public ActionableTreeNode(ActionableItem item, boolean allowsChildren) {
		super(item, allowsChildren);
	}

	public ActionableTreeNode(ActionableItem item) {
		super(item);
	}

	public void highlight() {
		mIsHighlighted = true;
	}

	public void clearHighlight() {
		mIsHighlighted = false;
	}

	public boolean isHighlighted() {
		return mIsHighlighted;
	}
}
