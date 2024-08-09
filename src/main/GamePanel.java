package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JPanel;

import piece.Bishop;
import piece.King;
import piece.Knight;
import piece.Pawn;
import piece.Piece;
import piece.Queen;
import piece.Rook;

public class GamePanel extends JPanel{

	
	public static final int WIDTH = 1100;
	public static final int HEIGHT = 800;
	final int FPS = 60;
	Thread gameThread;
	Runnable runnable;
	Board board = new Board();
	
	// PIECES
	public static ArrayList<Piece> pieces = new ArrayList<>();
	public static ArrayList<Piece> simPieces = new ArrayList<>();
	
	// COLOR
	public static final int WHITE = 0;
	public static final int BLACK = 1;
	int currentColor = WHITE;
	
	GamePanel() {
		setPreferredSize(new Dimension(WIDTH,HEIGHT));
		setBackground(Color.black);
		
		setPieces();
		copyPieces(pieces, simPieces);
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
	
	public void setPieces() {
		// White team
		// Adding pawns
		for (int i = 0; i <= 7; i++) {
			// i represents column
			pieces.add(new Pawn(WHITE, i, 6));
		}
		
		pieces.add(new Rook(WHITE, 0, 7));
		pieces.add(new Rook(WHITE, 7, 7));
		pieces.add(new Knight(WHITE, 1, 7));
		pieces.add(new Knight(WHITE, 6, 7));
		pieces.add(new Bishop(WHITE, 2, 7));
		pieces.add(new Bishop(WHITE, 5, 7));
		pieces.add(new Queen(WHITE, 4, 7));
		pieces.add(new King(WHITE, 3, 7));
		
		// Black team
		// Adding pawns
		for (int i = 0; i <= 7; i++) {
			// i represents column
			pieces.add(new Pawn(BLACK, i, 1));
		}
		
		pieces.add(new Rook(BLACK, 0, 0));
		pieces.add(new Rook(BLACK, 7, 0));
		pieces.add(new Knight(BLACK, 1, 0));
		pieces.add(new Knight(BLACK, 6, 0));
		pieces.add(new Bishop(BLACK, 2, 0));
		pieces.add(new Bishop(BLACK, 5, 0));
		pieces.add(new Queen(BLACK, 4, 0));
		pieces.add(new King(BLACK, 3, 0));
	}
	
	private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
		target.clear();
		for (int i = 0; i < source.size(); i++) {
			target.add(source.get(i));
		}
	}
	
	private void update() {
		
	}
	
	public void paintComponent( Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g;
		
		// BOARD
		board.draw(g2d);
		
		// PIECES
		for (Piece p : simPieces) {
			p.draw(g2d);
		}
	}

}
