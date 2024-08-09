package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class GamePanel extends JPanel{

	
	public static final int WIDTH = 1100;
	public static final int HEIGHT = 800;
	final int FPS = 60;
	Thread gameThread;
	Runnable runnable;
	Board board = new Board();
	
	GamePanel() {
		setPreferredSize(new Dimension(WIDTH,HEIGHT));
		setBackground(Color.black);
	}
	
	public void launchGame() {
		runnable = new Runnable() {

			@Override
			public void run() {
				// Game loop
				// Calls update() and repaint() 60 times per second
				double drawInterval = 1000000000/FPS;
				double delta = 0;
				long lastTime = System.nanoTime();
				long currentTime;
				
				while (gameThread != null) {
					currentTime = System.nanoTime();
					
					delta += (currentTime - lastTime) / drawInterval;
					lastTime = currentTime;
					
					if (delta >= 1) {
						update();
						repaint();
						delta--;
					}
				}
			}
			
		};
		
		gameThread = new Thread(runnable);
		gameThread.start();
	}
	
	private void update() {
		
	}
	
	public void paintComponent( Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g;
		
		board.draw(g2d);
	}

}
