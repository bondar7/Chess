package main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
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
	ArrayList<Piece> promoPieces = new ArrayList<>();
	Piece activeP, checkingP;
	public static Piece castlingP;

	// COLOR
	public static final int WHITE = 0;
	public static final int BLACK = 1;
	public static int currentColor = WHITE;

	// BOOLEANS
	boolean isValidSquare;
	boolean canMove;
	boolean promotion;
	boolean stalemate;
	boolean gameover;

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

		if (promotion) {
			promoting();
		} else if (!gameover && !stalemate) {
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

//			 Mouse button released
			if (mouse.pressed == false) {
				if (activeP != null) {
					if (isValidSquare) {

						// MOVE CONFIRMED

						// Update the piece list in case a piece has been captured and removed during
						// the simulation
						if (simPieces.size() != pieces.size()) {
							playSound("src/sounds/capture.wav");
							copyPieces(simPieces, pieces);
							activeP.updatePosition();
						} else {
							playSound("src/sounds/move.wav");
							activeP.updatePosition();
						}

						if (castlingP != null) {
							playSound("src/sounds/castling.wav");
							castlingP.updatePosition();
						}

						if (isKingInCheck() && isCheckmate()) {
							playSound("src/sounds/checkmate.wav");
							gameover = true;
						} else if (isStalemate()) {
							playSound("src/sounds/checkmate.wav");
							stalemate = true;
						} else {
							// The game is still going on
							if (canPromote()) {
								playSound("src/sounds/promote.wav");
								promotion = true;
							} else {
								changePlayer();
							}
						}

					} else {
						// The move is not valid so reset everything
						copyPieces(pieces, simPieces);
						activeP.resetPosition();
						activeP = null;
					}
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

			if (!isIllegal(activeP) && !opponentCanCaptureKing()) {
				isValidSquare = true;
			}

		}
	}

	private void changePlayer() {
		if (currentColor == WHITE) {
			currentColor = BLACK;
			// Reset black's two stepped status
			for (Piece p : pieces) {
				if (p.color == BLACK) {
					p.twoStepped = false;
				}
			}
		} else {
			currentColor = WHITE;
			// Reset black's two stepped status
			for (Piece p : pieces) {
				if (p.color == WHITE) {
					p.twoStepped = false;
				}
			}
		}
		activeP = null;
	}

	private boolean isIllegal(Piece king) {
		if (king.type == Type.KING) {
			for (Piece p : simPieces) {
				if (p != king && p.color != king.color && p.canMove(king.col, king.row)) {
					return true;
				}
				;
			}
		}
		return false;
	}

	private boolean opponentCanCaptureKing() {

		Piece king = getKing(false);

		for (Piece p : simPieces) {
			if (p.color != king.color && p.canMove(king.col, king.row)) {
				return true;
			}
		}

		return false;
	}

	private boolean isKingInCheck() {

		Piece king = getKing(true);

		if (activeP.canMove(king.col, king.row)) {
			checkingP = activeP;
			return true;
		} else {
			checkingP = null;
		}

		return false;
	}

	private Piece getKing(boolean opponent) {
		Piece king = null;

		for (Piece p : simPieces) {
			if (opponent) {
				if (p.type == Type.KING && p.color != currentColor) {
					king = p;
				}
			} else {
				if (p.type == Type.KING && p.color == currentColor) {
					king = p;
				}
			}
		}

		return king;
	}

	private boolean isStalemate() {
		int count = 0;
		// Count the number of pieces
		// Stalemate happends only when one piece is left which is king
		for (Piece p : simPieces) {
			if (p.color != currentColor) {
				count++;
			}
		}

		// If count is 1 - only king is left
		if (count == 1) {
			if (!kingCanMove(getKing(true))) {
				return true;
			}
		}

		return false;
	}

	private boolean isCheckmate() {

		Piece king = getKing(true); // get the opponent king

		if (kingCanMove(king)) {
			return false;
		} else {
			// But you still have a chance
			// Check if can block the attack with your piece

			// Check the position of the checking piece and the king in check
			int colDiff = Math.abs(checkingP.col - king.col);
			int rowDiff = Math.abs(checkingP.row - king.row);

			if (colDiff == 0) {
				// The checking piece is attacking vertically
				if (checkingP.row < king.row) {
					// The checking piece is above the king
					for (int row = checkingP.row; row < king.row; row++) {
						for (Piece p : simPieces) {
							if (p != king && p.color != currentColor && p.canMove(checkingP.col, row)) {
								return false;
							}
						}
					}
				}
				if (checkingP.row > king.row) {
					// The checking piece is below the king
					for (int row = checkingP.row; row > king.row; row--) {
						for (Piece p : simPieces) {
							if (p != king && p.color != currentColor && p.canMove(checkingP.col, row)) {
								return false;
							}
						}
					}

				}
			} else if (rowDiff == 0) {
				// The checking piece is attacking horizontally
				if (checkingP.col < king.col) {
					// The checking piece is left of the king
					for (int col = checkingP.col; col < king.col; col++) {
						for (Piece p : simPieces) {
							if (p != king && p.color != currentColor && p.canMove(col, checkingP.row)) {
								return false;
							}
						}
					}
				}
				if (checkingP.col > king.col) {
					// The checking piece is right of the king
					for (int col = checkingP.col; col > king.col; col--) {
						for (Piece p : simPieces) {
							if (p != king && p.color != currentColor && p.canMove(col, checkingP.row)) {
								return false;
							}
						}
					}
				}

			} else if (colDiff == rowDiff) {
				// The checking piece is attacking diagonally
				if (checkingP.row < king.row) {
					// The checking piece is above the king
					if (checkingP.col < king.col) {
						// The checking piece is in the upper left

						for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row++) {
							for (Piece p : simPieces) {
								if (p != king && p.color != currentColor && p.canMove(col, row)) {
									return false;
								}
								;
							}
						}
					}
					if (checkingP.col > king.col) {
						// The checking piece is in the upper right

						for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row++) {
							for (Piece p : simPieces) {
								if (p != king && p.color != currentColor && p.canMove(col, row)) {
									return false;
								}
								;
							}
						}
					}
				}

				if (checkingP.row > king.row) {
					// The checking piece is below the king
					if (checkingP.col < king.col) {
						// The checking piece is in the lower left

						for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row--) {
							for (Piece p : simPieces) {
								if (p != king && p.color != currentColor && p.canMove(col, row)) {
									return false;
								}
								;
							}
						}
					}
					if (checkingP.col > king.col) {
						// The checking piece is in the lower right
						for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row--) {
							for (Piece p : simPieces) {
								if (p != king && p.color != currentColor && p.canMove(col, row)) {
									return false;
								}
								;
							}
						}
					}
				}
			} else {
				// The checking piece is Knight - cannot block it

			}
		}

		return true;
	}

	private boolean kingCanMove(Piece king) {
		// Simulate if there is any square where the king can move to
		if (isValidMove(king, -1, -1))
			return true;
		if (isValidMove(king, 0, -1))
			return true;
		if (isValidMove(king, 1, -1))
			return true;
		if (isValidMove(king, -1, 0))
			return true;
		if (isValidMove(king, 1, 0))
			return true;
		if (isValidMove(king, -1, 1))
			return true;
		if (isValidMove(king, 0, 1))
			return true;
		if (isValidMove(king, 1, 1))
			return true;

		return false;
	}

	private boolean isValidMove(Piece king, int colPlus, int rowPlus) {

		boolean isValidMove = false;

		// Update the king's position for a second
		king.col += colPlus;
		king.row += rowPlus;

		if (king.canMove(king.col, king.row)) {
			if (king.hittingP != null) {
				simPieces.remove(king.hittingP);
			}
			if (!isIllegal(king)) {
				isValidMove = true;
			}
		}

		// Reset the king's position and restore the removed pieces
		king.resetPosition();
		copyPieces(pieces, simPieces);

		return isValidMove;
	}

	private void checkCastling() {
		if (castlingP != null) {
			if (castlingP.col == 0) {
				castlingP.col += 3;
			} else if (castlingP.col == 7) {
				castlingP.col -= 2;
			}
			// update x position
			castlingP.x = castlingP.getX(castlingP.col);
		}
	}

	private boolean canPromote() {

		if (activeP.type == Type.PAWN) {
			if (currentColor == WHITE && activeP.row == 0 || currentColor == BLACK && activeP.row == 7) {
				promoPieces.clear();
				promoPieces.add(new Rook(currentColor, 9, 2));
				promoPieces.add(new Knight(currentColor, 9, 3));
				promoPieces.add(new Bishop(currentColor, 9, 4));
				promoPieces.add(new Queen(currentColor, 9, 5));
				return true;
			}
		}

		return false;
	}

	private void promoting() {
		if (mouse.pressed) {
			for (Piece p : promoPieces) {
				if (p.col == mouse.x / Board.SQUARE_SIZE && p.row == mouse.y / Board.SQUARE_SIZE) {
					switch (p.type) {
					case ROOK:
						simPieces.add(new Rook(currentColor, activeP.col, activeP.row));
						break;
					case KNIGHT:
						simPieces.add(new Knight(currentColor, activeP.col, activeP.row));
						break;
					case BISHOP:
						simPieces.add(new Bishop(currentColor, activeP.col, activeP.row));
						break;
					case QUEEN:
						simPieces.add(new Queen(currentColor, activeP.col, activeP.row));
						break;
					default:
						break;
					}
					simPieces.remove(activeP);
					copyPieces(simPieces, pieces);
					activeP = null;
					promotion = false;
					changePlayer();
				}
			}
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
				if (isIllegal(activeP) || opponentCanCaptureKing()) {
					g2d.setColor(Color.gray);
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
					g2d.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE,
							Board.SQUARE_SIZE);
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				} else {
					g2d.setColor(Color.white);
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
					g2d.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE,
							Board.SQUARE_SIZE);
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				}
			}
			// Draw the active piece in the end so it won't be hidden by board or colored
			// square
			activeP.draw(g2d);
		}

		// Status messages
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setFont(new Font("Book Antiqua", Font.PLAIN, 40));
		g2d.setColor(Color.white);

		if (promotion) {
			g2d.drawString("Promote to: ", 840, 150);
			for (Piece p : promoPieces) {
				g2d.drawImage(p.image, p.getX(p.col), p.getY(p.row), Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
			}
		} else {

			if (currentColor == WHITE) {
				g2d.drawString("White's turn", 840, 550);
				if (checkingP != null && checkingP.color == BLACK) {
					g2d.setColor(Color.red);
					g2d.drawString("The King", 840, 650);
					g2d.drawString("is in check!", 840, 700);
				}
			} else {
				g2d.drawString("Black's turn", 840, 250);
				if (checkingP != null && checkingP.color == WHITE) {
					g2d.setColor(Color.red);
					g2d.drawString("The King", 840, 100);
					g2d.drawString("is in check!", 840, 150);
				}
			}
		}

		if (gameover) {
			String s = "";
			if (currentColor == WHITE) {
				s = "White wins!";
			} else {
				s = "Black wins!";
			}
			g2d.setFont(new Font("Arial", Font.PLAIN, 90));
			g2d.setColor(Color.green);
			g2d.drawString(s, 200, 400);
		}
		if (stalemate) {
			String s = "Stalemate";
			g2d.setFont(new Font("Arial", Font.PLAIN, 90));
			g2d.setColor(Color.gray);
			g2d.drawString(s, 200, 400);
		}
	}
	
	public static void playSound(String filePath) {
		try {
			File soundFile = new File(filePath);
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
			
			AudioFormat format = audioStream.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			
			Clip audioClip = (Clip) AudioSystem.getLine(info);
			
			audioClip.open(audioStream);
			audioClip.start();
			
            // Umožnění přehrání audia (blokování hlavního vlákna, dokud zvuk nedohraje)
            while (!audioClip.isRunning())
                Thread.sleep(5);
            while (audioClip.isRunning())
                Thread.sleep(10);

            // Uzavření zdrojů
            audioClip.close();
            audioStream.close();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
            e.printStackTrace();
        }
			
	}

}
