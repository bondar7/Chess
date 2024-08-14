package piece;

import main.GamePanel;

public class Rook extends Piece{

	public Rook(int color, int col, int row) {
		super(color, col, row);
		
		if (color == GamePanel.WHITE) {
			image = getImage("/piece/w-rook");
		} 
		else {
			image = getImage("/piece/b-rook");
		}
	}
	
	public boolean canMove(int targetCol, int targetRow) {
		if (isWithinTable(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)) {
			
			// Rook can move as long as either its col or row is the same
			if (targetCol == preCol || targetRow == preRow) {
				if (isValidSquare(targetCol, targetRow) && !isPieceOnStraightLine(targetCol, targetRow)) {
					return true;
				}
			}
		}
		
		return false;
	}

}
