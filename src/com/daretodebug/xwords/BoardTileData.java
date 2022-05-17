package com.daretodebug.xwords;

import java.awt.*;

public final class BoardTileData {
	
	private final Color color;
	private final byte wordMultiplier;
	private final byte letterMultiplier;
	
	public BoardTileData(Color color, byte wordMultiplier, byte letterMultiplier) {
		
		this.color = color;
		this.wordMultiplier = wordMultiplier;
		this.letterMultiplier = letterMultiplier;
		
	}
	
	public Color getColor() { return color; }
	public byte getWordMultiplier() { return wordMultiplier; }
	public byte getLetterMultiplier() { return letterMultiplier; }
	
}
