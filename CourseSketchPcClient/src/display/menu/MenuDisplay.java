package display.menu;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import menu.ActionableItem;
import menu.MenuBackend;
import menu.list.ExpandableMenuItem;
import menu.list.HeaderMenuItem;

public class MenuDisplay extends MenuBackend {

	JPanel panel;
	JTree tree;
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode();

	public MenuDisplay() {
		addFakeMenu();
		tree = new JTree(root);
		panel = new JPanel();
		panel.add(tree);
		addListener();
	}

	public void addMenuItem(ActionableItem item) {
		addMenuItem(root, item);
	}

	/**
	 * Adds
	 */
	public void addMenuItem(DefaultMutableTreeNode parentNode, ActionableItem item) {
		System.out.println(item.getLabel());
		ActionableTreeNode node = null;
		if(item instanceof ExpandableMenuItem) {
			node = new ActionableTreeNode(item, true);
			recursivelyAddMenuItem(node, (ExpandableMenuItem) item);
		} else if(item instanceof HeaderMenuItem) {
			node = new ActionableTreeNode(item, false);
		} else {
			node = new ActionableTreeNode(item);
		}
		parentNode.add(node);
	}

	/**
	 * Adds the menu items of an {@link ExpandableMenuItem} to the given {@link DefaultMutableTreeNode}.
	 */
	public void recursivelyAddMenuItem(DefaultMutableTreeNode node,
			ExpandableMenuItem listItem) {
		ArrayList<ActionableItem> list = listItem.getSubItems();
		int length = list.size();
		for (int i = 0; i < length; i++) {
			addMenuItem(node, list.get(i));
		}
	}

	public Component getDisplay() {
		return panel;
	}


	public void addListener() {
		MouseListener ml = new MouseAdapter() {
			ActionableTreeNode highlight = null;
			@Override
			public void mousePressed(MouseEvent e) {
				TreePath selectedPath = tree.getPathForLocation(e.getX(),
						e.getY());
				if (selectedPath != null) {
					if (e.getClickCount() == 1) {
						DefaultMutableTreeNode node = ((DefaultMutableTreeNode) selectedPath
								.getLastPathComponent());
						if (node == root) {
							// Do nothing
							return;
						}
						ActionableTreeNode realNode = (ActionableTreeNode) node;
						realNode.onClickItem.executeAction();
					} else if (e.getClickCount() == 2) {
						// Ignore double click for now.
					}
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				TreePath selectedPath = tree.getPathForLocation(e.getX(),
						e.getY());
				if (selectedPath != null) {
					DefaultMutableTreeNode node = ((DefaultMutableTreeNode) selectedPath
							.getLastPathComponent());
					if (node != root) {
						ActionableTreeNode realNode = (ActionableTreeNode) node;
						realNode.onClickItem.executeAction();
						// HIGHLIGHT
						return;
					}
				}
			}
			// CLEAR HIGHLIGHT
		};
		tree.addMouseListener(ml);
	}
}
