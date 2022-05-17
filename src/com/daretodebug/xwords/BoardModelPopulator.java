package com.daretodebug.xwords;

public interface BoardModelPopulator {
	
	public BoardTileData getDataAt(int x, int y);
	public TileCoords[] getStartingIntersectionPoints();
	
}
