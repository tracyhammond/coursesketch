package test.test;

import javax.swing.JFrame;

import display.menu.MenuDisplay;

public class MenuTester {
	public static void main(String args[]) {
		JFrame frame = new JFrame();
		MenuDisplay menu = new MenuDisplay();
		System.out.println(menu.getDisplay());
		frame.add(menu.getDisplay());
		frame.setVisible(true);
	}
}
