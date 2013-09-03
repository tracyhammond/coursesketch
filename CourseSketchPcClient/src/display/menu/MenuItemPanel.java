package display.menu;

import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import menu.ActionableItem;
import menu.list.HeaderMenuItem;
import display.SideBorder;
import display.style.Colors;

public class MenuItemPanel extends JPanel {
	private boolean mIsHeader;

	public MenuItemPanel(ActionableItem actionableItem, boolean isHighlighted,
			boolean lastItem) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBackground(isHighlighted);
		if (actionableItem instanceof HeaderMenuItem) {
			mIsHeader = true;
			add(createLabelPanel(actionableItem));
		} else {
			add(createLabel(actionableItem));
		}
		setBorder(lastItem);
		// setPreferredSize(getPreferredSize());
	}

	private void setBorder(boolean lastItem) {
		Border inside = new SideBorder(false, false, false, true,
				Colors.MENU_ITEM_BORDER);
		if (mIsHeader) {
			setBorder(new SideBorder(false, true, false, false,
					Colors.BOTTOM_MENU_ITEM_OUTSIDE_BORDER));
		} else {
			setBorder(inside);
		}
	}

	private void setBackground(boolean isHighlighted) {
		if (isHighlighted) {
			setBackground(Colors.MENU_ITEM_HIGHLIGHT_BACKGROUND);
		} else {
			setBackground(Colors.MENU_BACKGROUND);
		}
	}

	private JPanel createLabelPanel(ActionableItem actionableItem) {
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setOpaque(false);
		panel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 0));
		JLabel label = createLabel(actionableItem);
		panel.add(label);
		return panel;
	}

	private JLabel createLabel(ActionableItem actionableItem) {
		JLabel label = new JLabel(actionableItem.getLabel());
		if (actionableItem instanceof HeaderMenuItem) {
			label.setFont(new Font("arial", Font.BOLD + Font.ITALIC, 14));
			label.setForeground(Colors.HEADER_3_TEXT);
			label.setBorder(new SideBorder(false, false, false, true,
					Colors.HEADER_3_TEXT));
		} else {
			label.setFont(new Font("arial", Font.BOLD, 14));
			label.setForeground(Colors.MENU_ITEM_TEXT);
			label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
		}
		return label;
	}

	@Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(mIsHeader) {
	        Graphics2D g2d = (Graphics2D) g;
	        float w = getWidth();
	        float h = getHeight();
	        GradientPaint gp = new GradientPaint(
	            0, 0, Colors.MENU_BACKGROUND, 0, h*.85f , Colors.MENU_GRADIENT_DARK);
	        g2d.setPaint(gp);
	        g2d.fillRect(0, 0, (int)w, (int)h);
        }
    }
}
