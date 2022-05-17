package com.daretodebug.xwords;

import java.awt.*;

public final class DefaultBoard extends DefaultBoardModel {
	
	public DefaultBoard(WordDictionary dictionary) {
		super(dictionary, 15, 15, Populator.INSTANCE);
	}
	
	private static class Populator implements BoardModelPopulator {
		
		public static final Populator INSTANCE = new Populator();
		private static final TileCoords CENTER = new TileCoords(7, 7);
		
		private static final BoardTileData NORMAL = new BoardTileData(new Color(240, 230, 220), (byte) 1, (byte) 1);
		private static final BoardTileData DOUBLE_LETTER = new BoardTileData(new Color(160, 212, 255), (byte) 1, (byte) 2);
		private static final BoardTileData TRIPLE_LETTER = new BoardTileData(new Color(0, 148, 232), (byte) 1, (byte) 3);
		private static final BoardTileData DOUBLE_WORD = new BoardTileData(new Color(255, 176, 176), (byte) 2, (byte) 1);
		private static final BoardTileData TRIPLE_WORD = new BoardTileData(new Color(240, 80, 80), (byte) 3, (byte) 1);
		
		private Populator() {}
		
		@Override
		public BoardTileData getDataAt(int x, int y) {
			
			if(x > 7) x = 14 - x;
			if(y > 7) y = 14 - y;
			
			switch(x) {
				
			case 0:
				switch(y) {
					
				case 0: return TRIPLE_WORD;
				case 1: return NORMAL;
				case 2: return NORMAL;
				case 3: return DOUBLE_LETTER;
				case 4: return NORMAL;
				case 5: return NORMAL;
				case 6: return NORMAL;
				case 7: return TRIPLE_WORD;
					
				}
				
				break;
				
			case 1:
				switch(y) {
					
				case 0: return NORMAL;
				case 1: return DOUBLE_WORD;
				case 2: return NORMAL;
				case 3: return NORMAL;
				case 4: return NORMAL;
				case 5: return TRIPLE_LETTER;
				case 6: return NORMAL;
				case 7: return NORMAL;
					
				}
				
				break;
				
			case 2:
				switch(y) {
				
				case 0: return NORMAL;
				case 1: return NORMAL;
				case 2: return DOUBLE_WORD;
				case 3: return NORMAL;
				case 4: return NORMAL;
				case 5: return NORMAL;
				case 6: return DOUBLE_LETTER;
				case 7: return NORMAL;
					
				}
				
				break;
				
			case 3:
				switch(y) {
					
				case 0: return DOUBLE_LETTER;
				case 1: return NORMAL;
				case 2: return NORMAL;
				case 3: return DOUBLE_WORD;
				case 4: return NORMAL;
				case 5: return NORMAL;
				case 6: return NORMAL;
				case 7: return DOUBLE_LETTER;
					
				}
				
				break;
				
			case 4:
				switch(y) {
				
				case 0: return NORMAL;
				case 1: return NORMAL;
				case 2: return NORMAL;
				case 3: return NORMAL;
				case 4: return DOUBLE_WORD;
				case 5: return NORMAL;
				case 6: return NORMAL;
				case 7: return NORMAL;
					
				}
				
				break;
				
			case 5:
				switch(y) {
				
				case 0: return NORMAL;
				case 1: return TRIPLE_LETTER;
				case 2: return NORMAL;
				case 3: return NORMAL;
				case 4: return NORMAL;
				case 5: return TRIPLE_LETTER;
				case 6: return NORMAL;
				case 7: return NORMAL;
					
				}
				
				break;
				
			case 6:
				switch(y) {
				
				case 0: return NORMAL;
				case 1: return NORMAL;
				case 2: return DOUBLE_LETTER;
				case 3: return NORMAL;
				case 4: return NORMAL;
				case 5: return NORMAL;
				case 6: return DOUBLE_LETTER;
				case 7: return NORMAL;
					
				}
				
				break;
				
			case 7:
				switch(y) {
					
				case 0: return TRIPLE_WORD;
				case 1: return NORMAL;
				case 2: return NORMAL;
				case 3: return DOUBLE_LETTER;
				case 4: return NORMAL;
				case 5: return NORMAL;
				case 6: return NORMAL;
				case 7: return DOUBLE_WORD;
					
				}
				
				break;
				
			}
			
			throw new IllegalArgumentException("The given coordinates do not fall within this board's range.");
			
		}

		@Override
		public TileCoords[] getStartingIntersectionPoints() {
			return new TileCoords[] { CENTER };
		}
		
	}
	
	@Override
	public int addBonus(int score, BoardModel.UnpositionedMove move, BoardModel.Position position) {
		
		if(move.getTilesUsed() >= 7) score += 50;
		return score;
		
	}
	
}
