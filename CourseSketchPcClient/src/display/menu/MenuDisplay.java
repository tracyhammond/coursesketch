package display.menu;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JComponent;
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
import display.SideBorder;
import display.style.Colors;

public class MenuDisplay extends MenuBackend implements TreeCellRenderer {
	private JPanel mPanel;
	private JTree mTree;
	private DefaultMutableTreeNode mRoot = new DefaultMutableTreeNode();

	public MenuDisplay() {
		addFakeMenu();
		mTree = new JTree(mRoot);
		mTree.setRootVisible(false);
		mTree.setUI(getTreeUI()); // This must be set before the Renderer.
		mTree.setCellRenderer(this);
		mTree.setBackground(Colors.MENU_BACKGROUND);
		mTree.setRowHeight(0); // This allows the elements to have different heights.
		mTree.setBorder(new SideBorder(false, false, false, true,
				Colors.BOTTOM_MENU_ITEM_OUTSIDE_BORDER));
		mPanel = new JPanel();
		mPanel.setBackground(Color.green);
		mPanel.add(mTree);
		addListener();
	}

	public void addMenuItem(ActionableItem item) {
		addMenuItem(mRoot, item);
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
		return mPanel;
	}

	public void addListener() {
		MouseListener ml = new MouseAdapter() {
			ActionableTreeNode highlightedNode = null;

			@Override
			public void mousePressed(MouseEvent e) {
				TreePath selectedPath = mTree.getPathForLocation(e.getX(),
						e.getY());
				if (selectedPath != null) {
					if (e.getClickCount() == 1) {
						DefaultMutableTreeNode node = ((DefaultMutableTreeNode) selectedPath
								.getLastPathComponent());
						if (node == mRoot) {
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
				TreePath selectedPath = mTree.getPathForLocation(e.getX(),
						e.getY());
				if (selectedPath != null) {
					DefaultMutableTreeNode node = ((DefaultMutableTreeNode) selectedPath
							.getLastPathComponent());
					if (node != mRoot) {
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
						mTree.repaint();
						return;
					}
				}
				// Clear the highlight.
				if (highlightedNode != null) {
					highlightedNode.clearHighlight();
					highlightedNode = null;
					mTree.repaint();
				}
			}
		};
		mTree.addMouseListener(ml);
		mTree.addMouseMotionListener((MouseMotionListener) ml);
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
		if (node == mRoot || !(node instanceof ActionableTreeNode)) {
			return new JPanel();
		}
		ActionableTreeNode actionableNode = (ActionableTreeNode) node;
		ActionableItem actionableItem = (ActionableItem) actionableNode
				.getUserObject();
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
						dimensions.width = Math.max(200, tree.getWidth());
						dimensions.width -= getRowX(row, depth);
						return dimensions;
					}
				};
			}

			 @Override
		        protected void paintHorizontalLine(Graphics g, JComponent c,
		                                           int y, int left, int right) {
		            // do nothing.
				// do nothing.
				 /*
					Graphics2D g2d = (Graphics2D) g;
					float w = right;
					float h = c.getHeight();
					GradientPaint gp = new GradientPaint(
					    0, 0, Colors.MENU_GRADIENT_DARK, right, 0 , Colors.MENU_BACKGROUND);
					g2d.setPaint(gp);
					g2d.fillRect(0, y, (int)w, (int)h);
					*/
					g.setColor(Color.white);
					g.drawLine(left,y,right,y);
		        }

		        @Override
		        protected void paintVerticalLine(Graphics g, JComponent c, int x, int top, int bottom) {
		     //   	g.setColor(Color.white);
			//		g.drawLine(x,top,x,bottom);
		        }
		};
	}
}
