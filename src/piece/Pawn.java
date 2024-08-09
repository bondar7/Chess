package piece;

import main.GamePanel;

public class Pawn extends Piece{

	public Pawn(int color, int col, int row) {
		super(color, col, row);
		
		if (color == GamePanel.WHITE) {
			image = getImage("/piece/w-pawn");
		} 
		else {
			image = getImage("/piece/b-pawn");
		}
	}

}
