package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class GamePanel extends JPanel{

	
	public static final int WIDTH = 1100;
	public static final int HEIGHT = 800;
	
	GamePanel() {
		setPreferredSize(new Dimension(WIDTH,HEIGHT));
		setBackground(Color.black);
	}
	
	private void update() {
		
	}
	
	public void paintComponent( Graphics g) {
		super.paintComponent(g);
	}
}
