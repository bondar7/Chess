package piece;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.Board;
import main.GamePanel;
import main.Type;

public class Piece {

	public Type type;
	public BufferedImage image;
	public int x, y;
	public int col, row, preCol, preRow;
	public int color;
	public Piece hittingP;
	public boolean moved, twoStepped;

	public Piece(int color, int col, int row) {
		this.color = color;
		this.col = col;
		this.row = row;
		x = getX(col);
		y = getY(row);
		preCol = col;
		preRow = row;
	}

	public BufferedImage getImage(String imagePath) {
		BufferedImage image = null;

		try {
			image = ImageIO.read(getClass().getResourceAsStream(imagePath + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return image;
	}

	public int getX(int col) {
		return col * Board.SQUARE_SIZE;
	}

	public int getY(int row) {
		return row * Board.SQUARE_SIZE;
	}

	public int getCol(int x) {
		return (x + Board.HALF_SQUARE_SIZE) / Board.SQUARE_SIZE;
	}

	public int getRow(int y) {
		return (y + Board.HALF_SQUARE_SIZE) / Board.SQUARE_SIZE;
	}

	public void updatePosition() {
		
		// To check En Passant
		if (type == Type.PAWN) {
			if (Math.abs(row - preRow) == 2) {
				twoStepped = true;
			}
		}
		
		x = getX(col);
		y = getY(row);
		preCol = col;
		preRow = row;
		
		moved = true;
	}

	public void resetPosition() {
		col = preCol;
		row = preRow;
		x = getX(col);
		y = getY(row);
	}

	public boolean canMove(int targetCol, int targetRow) {
		return false;
	}

	public boolean isWithinTable(int targetCol, int targetRow) {
		if (targetCol >= 0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7) {
			return true;
		} else {
			return false;
		}
	}

	public Piece getHittingP(int targetCol, int targetRow) {
		for (Piece p : GamePanel.simPieces) {
			if (p.col == targetCol && p.row == targetRow && p != this) {
				return p;
			}
		}
		return null;
	}
	
	public boolean isSameSquare(int targetCol, int targetRow) {
		if (targetCol == preCol && targetRow == preRow) {
			 return true;
		}
		return false;
	}

	public boolean isValidSquare(int targetCol, int targetRow) {
		
		hittingP = getHittingP(targetCol, targetRow);
		
		if (hittingP == null) {
			return true;
		} else {
			if (hittingP.color != this.color) {
				return true;
			} else {
				hittingP = null;
			}
		}
		
		return false;
	}
	
	public boolean isPieceOnStraightLine(int targetCol, int targetRow) {
		
		// When this piece is moving to the left
		for (int c = preCol-1; c > targetCol; c--) {
			for (Piece p : GamePanel.simPieces) {
				if (p.col == c && p.row == targetRow) {
					hittingP = p;
					return true;
				}
			}
		}
		
		// When this piece is moving to the right
		for (int c = preCol+1; c < targetCol; c++) {
			for (Piece p : GamePanel.simPieces) {
				if (p.col == c && p.row == targetRow) {
					hittingP = p;
					return true;
				}
			}
		}
		// When this piece is moving to the bottom
		for (int r = preRow-1; r > targetRow; r--) {
			for (Piece p : GamePanel.simPieces) {
				if (p.row == r && p.col == targetCol) {
					hittingP = p;
					return true;
				}
			}
		}
		
		// When this piece is moving to the top
		for (int r = preRow+1; r < targetRow; r++) {
			for (Piece p : GamePanel.simPieces) {
				if (p.row == r && p.col == targetCol) {
					hittingP = p;
					return true;
				}
			}
		}
		
		
		return false;
	}
	
	public boolean isPieceOnDiagonalLine(int targetCol, int targetRow) {
		
		if (targetRow < preRow) {
			// Up left
			for (int c = preCol-1; c > targetCol; c--) {
				int diff = Math.abs(c - preCol);
				for (Piece p : GamePanel.simPieces) {
					if (p.col == c && p.row == preRow - diff) {
						hittingP = p;
						return true;
					}
				}
			}
			
			// Up right
			for (int c = preCol+1; c < targetCol; c++) {
				int diff = Math.abs(c - preCol);
				for (Piece p : GamePanel.simPieces) {
					if (p.col == c && p.row == preRow - diff) {
						hittingP = p;
						return true;
					}
				}
			}
		}
		
		if (targetRow > preRow) {
			// Down left
			for (int c = preCol-1; c > targetCol; c--) {
				int diff = Math.abs(c - preCol);
				for (Piece p : GamePanel.simPieces) {
					if (p.col == c && p.row == preRow + diff) {
						hittingP = p;
						return true;
					}
				}
			}
			
			// Down right
			for (int c = preCol+1; c < targetCol; c++) {
				int diff = Math.abs(c - preCol);
				for (Piece p : GamePanel.simPieces) {
					if (p.col == c && p.row == preRow + diff) {
						hittingP = p;
						return true;
					}
				}
			}
		}
		
		return false;
	}

	public void draw(Graphics2D g2d) {
		g2d.drawImage(image, x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
	}
}
