package test.test;

import javax.swing.JFrame;

import display.menu.MenuDisplay;

public class MenuTester {
	public static void main(String args[]) {
		JFrame frame = new JFrame();
		frame.setSize(200,600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		MenuDisplay menu = new MenuDisplay();
		frame.add(menu.getDisplay());
		frame.setVisible(true);
	}
}
