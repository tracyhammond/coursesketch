package androidclient.menu;

import menu.MenuBackend;
import android.widget.ListView;

public class MenuDisplay extends MenuBackend {
	/* package-private */ ListView display;

	public void setSlidingMenu(ListView view) {
		display = view;
	}
}
