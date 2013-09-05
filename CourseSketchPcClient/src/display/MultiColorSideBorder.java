package display;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

public class MultiColorSideBorder extends SideBorder {
	Color mTopColor = null;
	Color mRightColor = null;
	Color mBottomColor = null;
	public MultiColorSideBorder(boolean left, boolean top, boolean right, boolean bottom, Color leftC, Color topC, Color rightC, Color bottomC) {
		super(left, top, right, bottom, leftC);
		mTopColor = topC;
		mRightColor = rightC;
		mBottomColor = bottomC;
	}

	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		Insets insets = getBorderInsets(c);


		if (mIsTopVisible) {
			g.setColor(mTopColor);
			g.fillRect(0, 0, width - insets.right + 1, insets.top);
		}

		if (mIsRightVisible) {
			g.setColor(mRightColor);
			g.fillRect(width - insets.right, 0, insets.right, height
					- insets.bottom + 1);
		}

		if (mIsBottomVisible) {
			g.setColor(mBottomColor);
			g.fillRect(insets.left - 1, height - insets.bottom, width
					- insets.left + 1, insets.bottom);
		}

		if (mIsLeftVisible) {
			g.setColor(mBorderColor);
			g.fillRect(0, 0, insets.right, height - insets.bottom + 1);
		}
	}
}
