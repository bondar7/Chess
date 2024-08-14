package main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

import javax.swing.JPanel;

import piece.Bishop;
import piece.King;
import piece.Knight;
import piece.Pawn;
import piece.Piece;
import piece.Queen;
import piece.Rook;

public class GamePanel extends JPanel {

	public static final int WIDTH = 1100;
	public static final int HEIGHT = 800;
	final int FPS = 60;
	Thread gameThread;
	Runnable runnable;
	Board board = new Board();
	Mouse mouse = new Mouse();

	// PIECES
	public static ArrayList<Piece> pieces = new ArrayList<>();
	public static ArrayList<Piece> simPieces = new ArrayList<>();
	Piece activeP;
	public static Piece castlingP;

	// COLOR
	public static final int WHITE = 0;
	public static final int BLACK = 1;
	int currentColor = WHITE;

	// BOOLEANS
	boolean isValidSquare;
	boolean canMove;

	GamePanel() {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setBackground(Color.black);

		// Mouse listeners
		addMouseMotionListener(mouse);
		addMouseListener(mouse);

		setPieces();
		copyPieces(pieces, simPieces);
	}

	public void launchGame() {
		runnable = new Runnable() {

			@Override
			public void run() {
				// Game loop
				// Calls update() and repaint() 60 times per second
				double drawInterval = 1000000000 / FPS;
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
		pieces.add(new Queen(WHITE, 3, 7));
		pieces.add(new King(WHITE, 4, 7));

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
		pieces.add(new Queen(BLACK, 3, 0));
		pieces.add(new King(BLACK, 4, 0));
	}

	private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
		target.clear();
		for (int i = 0; i < source.size(); i++) {
			target.add(source.get(i));
		}
	}

	private void update() {

		// Mouse button pressed
		if (mouse.pressed) {
			if (activeP == null) {
				// If the activeP is null, check if you can pick up a piece
				for (Piece piece : simPieces) {
					// If the mouse is on an ally piece, pick it up as the activeP
					if (piece.color == currentColor && piece.col == mouse.x / Board.SQUARE_SIZE
							&& piece.row == mouse.y / Board.SQUARE_SIZE) {
						activeP = piece;
					}
				}
			} else {
				// If the player is holding a piece, simulate the move
				simulate();
			}
		}

//		 Mouse button released
		if (mouse.pressed == false) {
			if (activeP != null) {
				if (isValidSquare) {
					
					// MOVE CONFIRMED
					
					
					// Update the piece list in case a piece has been captured and removed during
					// the simulation
					copyPieces(simPieces, pieces);
					activeP.updatePosition();
					
					if (castlingP != null) {
						castlingP.updatePosition();
					}
					
					changePlayer();
				} else {
					// The move is not valid so reset everything
					copyPieces(pieces, simPieces);
					activeP.resetPosition();
					activeP = null;
				}
			}
		}
	}


	private void simulate() {

		canMove = false;
		isValidSquare = false;

		// Reset the piece list in every loop
		// This is basically for restoring the removed piece during the simulation
		copyPieces(pieces, simPieces);
		
		// Reset the castling piece's position
		if (castlingP != null) {
			castlingP.col = castlingP.preCol;
			castlingP.x = castlingP.getX(castlingP.col);
			castlingP = null;
		}

		// If a piece is being held, update its position
		activeP.x = mouse.x - Board.HALF_SQUARE_SIZE;
		activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;
		activeP.col = activeP.getCol(activeP.x);
		activeP.row = activeP.getCol(activeP.y);

		// Check if the piece is hovering over a reachable square
		if (activeP.canMove(activeP.col, activeP.row)) {
			canMove = true;

			// if hitting a piece, remove it from the list
			if (activeP.hittingP != null) {
				simPieces.remove(activeP.hittingP);
			}
			
			checkCastling();

			isValidSquare = true;
		}
	}

	private void changePlayer() {
		if (currentColor == WHITE) {
			currentColor = BLACK;
		} else {
			currentColor = WHITE;
		}
		activeP = null;
	}
	
	private void checkCastling() {
		if (castlingP != null) {
			if (castlingP.col == 0) {
				castlingP.col += 3;
			}
			else if (castlingP.col == 7) {
				castlingP.col -= 2;
			}
			// update x position
			castlingP.x = castlingP.getX(castlingP.col);
		}
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;

		// BOARD
		board.draw(g2d);

		// PIECES
		for (Piece p : simPieces) {
			p.draw(g2d);
		}

		if (activeP != null) {
			if (canMove) {
				g2d.setColor(Color.white);
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
				g2d.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE,
						Board.SQUARE_SIZE);
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
			}
			// Draw the active piece in the end so it won't be hidden by board or colored
			// square
			activeP.draw(g2d);
		}

		// Status messages
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setFont(new Font("Book Antiqua", Font.PLAIN, 40));
		g2d.setColor(Color.white);
		
		if (currentColor == WHITE) {
			g2d.drawString("White's turn", 840, 550);
		} else {
			g2d.drawString("Black's turn", 840, 250);
		}
	}

}
