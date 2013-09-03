package display.menu;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import menu.ActionableItem;
import menu.list.HeaderMenuItem;
import display.SideBorder;
import display.style.Colors;

public class MenuItemPanel extends JPanel {
	int mySize;
	private JComponent mLabel;

	public MenuItemPanel(ActionableItem actionableItem, boolean isHighlighted,
			boolean lastItem) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBackground(isHighlighted);
		add(createLabelPanel(actionableItem));
		setBorder(lastItem);
		// setPreferredSize(getPreferredSize());
	}

	private void setBorder(boolean lastItem) {
		Border inside = new SideBorder(false, false, false, true,
				Colors.MENU_ITEM_BORDER);
		if (lastItem) {
			setBorder(BorderFactory.createCompoundBorder(
					new SideBorder(false, false, false, true,
							Colors.BOTTOM_MENU_ITEM_OUTSIDE_BORDER), inside));
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
		panel.setOpaque(false);
		JLabel label = createLabel(actionableItem);
		panel.add(label);
		mLabel = panel;
		return panel;
	}

	private JLabel createLabel(ActionableItem actionableItem) {
		JLabel label = new JLabel(actionableItem.getLabel());

		if (actionableItem instanceof HeaderMenuItem) {
			Font font = new Font("arial", Font.ITALIC, 14);
			Map attributes = font.getAttributes();
			attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			label.setFont(font.deriveFont(attributes));
			label.setForeground(Colors.HEADER_3_TEXT);
			label.setBorder(BorderFactory.createEmptyBorder(-3, 3, -3, 0));
		} else {
			label.setFont(new Font("arial", Font.BOLD, 14));
			label.setForeground(Colors.MENU_ITEM_TEXT);
			label.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 0));
		}
		return label;
	}
}
