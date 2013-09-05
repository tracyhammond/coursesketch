package androidclient.main;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import display.style.Colors;

public class SlidingMenu extends LinearLayout {

	public SlidingMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public SlidingMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SlidingMenu(Context context) {
		super(context);
		init();
	}

	public void init() {
		this.setBackgroundColor(Colors.MENU_BACKGROUND);
	}
}
