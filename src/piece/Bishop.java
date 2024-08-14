package piece;

import main.GamePanel;

public class Bishop extends Piece{

	public Bishop(int color, int col, int row) {
		super(color, col, row);
		
		if (color == GamePanel.WHITE) {
			image = getImage("/piece/w-bishop");
		} 
		else {
			image = getImage("/piece/b-bishop");
		}
	}
	
	public boolean canMove(int targetCol, int targetRow) {
		
		if (isWithinTable(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)) {
			if (Math.abs(targetCol - preCol) == Math.abs(targetRow - preRow)) {
				if (isValidSquare(targetCol, targetRow) && !isPieceOnDiagonalLine(targetCol, targetRow)) {
					return true;
				}
			}
		}
		
		return false;
	}

}
