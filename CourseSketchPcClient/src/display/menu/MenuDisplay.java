package display.menu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import menu.ActionableItem;
import menu.MenuBackend;
import menu.list.ExpandableMenuItem;
import menu.list.HeaderMenuItem;
import display.style.Colors;

public class MenuDisplay extends MenuBackend implements TreeCellRenderer {
	private JPanel panel;
	private JTree tree;
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode();

	public MenuDisplay() {
		addFakeMenu();
		tree = new JTree(root);
		tree.setRootVisible(false);
		tree.setUI(getTreeUI()); // This must be set before the Renderer.
		tree.setCellRenderer(this);
		tree.setBackground(Colors.MENU_BACKGROUND);
		tree.setRowHeight(0); // 
		panel = new JPanel();
		panel.setBackground(Color.green);
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
			ActionableTreeNode highlightedNode = null;

			@Override
			public void mousePressed(MouseEvent e) {
				TreePath selectedPath = tree.getPathForLocation(e.getX(),
						e.getY());
				if (selectedPath != null) {
					if (e.getClickCount() == 1) {
						DefaultMutableTreeNode node = ((DefaultMutableTreeNode) selectedPath
								.getLastPathComponent());
						if (node == root) {
							// Do nothing.
							return;
						}
						if (node instanceof ActionableTreeNode) {
							((ActionableItem) node.getUserObject())
									.executeAction();
						}
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
						if (node instanceof ActionableTreeNode) {
							// Highlight.
							if (highlightedNode != null) {
								highlightedNode.clearHighlight();
							}
							if(!(node.getUserObject() instanceof HeaderMenuItem)) {
								highlightedNode = ((ActionableTreeNode) node);
								highlightedNode.highlight();
							}else {
								highlightedNode = null;
							}
						}
						tree.repaint();
						return;
					}
				}
				// Clear the highlight.
				if (highlightedNode != null) {
					highlightedNode.clearHighlight();
					highlightedNode = null;
					tree.repaint();
				}
			}
		};
		tree.addMouseListener(ml);
		tree.addMouseMotionListener((MouseMotionListener) ml);
	}

	/**
	 * Makes a Component depending on the type of TreeNode it is.
	 *
	 * This supports Three different types,
	 *
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		TreePath path = tree.getPathForRow(row);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
				.getLastPathComponent();
		if (node == root || !(node instanceof ActionableTreeNode)) {
			return new JPanel();
		}
		ActionableTreeNode actionableNode = (ActionableTreeNode) node;
		ActionableItem actionableItem = (ActionableItem) actionableNode
				.getUserObject();
		System.out.println(row +" "+ actionableItem.getLabel());
		MenuItemPanel panel = new MenuItemPanel(actionableItem, actionableNode.isHighlighted(), true);
		return panel;
	}

	/**
	 * Creates a {@link BasicTreeUI} that ensures that the width of the Node expands to  the right.
	 *
	 * All nodes that implement this will be expanded to the right most edge of the {@link JTree}.
	 */
	public BasicTreeUI getTreeUI() {
		return new BasicTreeUI() {
			@Override
			protected AbstractLayoutCache.NodeDimensions createNodeDimensions() {
				return new NodeDimensionsHandler() {
					@Override
					public Rectangle getNodeDimensions(Object value, int row,
							int depth, boolean expanded, Rectangle size) {
						Rectangle dimensions = super.getNodeDimensions(value,
								row, depth, expanded, size);
						dimensions.width = Math.max(200, tree.getWidth()
								- getRowX(row, depth));
						return dimensions;
					}
				};

			}
		};
	}
}
