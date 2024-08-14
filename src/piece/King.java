package piece;

import main.GamePanel;
import main.Type;

public class King extends Piece {

	public King(int color, int col, int row) {
		super(color, col, row);
		
		type = Type.KING;

		if (color == GamePanel.WHITE) {
			image = getImage("/piece/w-king");
		} else {
			image = getImage("/piece/b-king");
		}
	}

	@Override
	public boolean canMove(int targetCol, int targetRow) {
		if (isWithinTable(targetCol, targetRow)) {

			// Movement
			if (Math.abs(targetCol - preCol) + Math.abs(targetRow - preRow) == 1
					|| Math.abs(targetCol - preCol) * Math.abs(targetRow - preRow) == 1) {
				if (isValidSquare(targetCol, targetRow)) {
					return true;
				}
			}

			// Castling
			if (!moved) {

				// Right castling
				if (targetCol == preCol + 2 && targetRow == preRow && !isPieceOnStraightLine(targetCol, targetRow)) {
					for (Piece p : GamePanel.simPieces) {
						if (p.col == preCol + 3 && p.row == preRow && !p.moved) {
							GamePanel.castlingP = p;
							return true;
						}
					}
				}

				// Left castling
				if (targetCol == preCol - 2 && targetRow == preRow && !isPieceOnStraightLine(targetCol, targetRow)) {
					Piece pieces[] = new Piece[2];
					for (Piece p : GamePanel.simPieces) {
						if (p.col == preCol - 3 && p.row == preRow) {
							pieces[0] = p;
						}
						if (p.col == preCol - 4 && p.row == targetRow) {
							pieces[1] = p;
						}
						
						if (pieces[0] == null &&  pieces[1] != null && pieces[1].moved == false) {
							GamePanel.castlingP = pieces[1];
							return true;
						}
					}
				}
			}
		}

		return false;
	}

}
