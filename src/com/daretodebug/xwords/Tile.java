package com.daretodebug.xwords;

public enum Tile {
	
	A('A', 1),
	B('B', 3),
	C('C', 3),
	D('D', 2),
	E('E', 1),
	F('F', 4),
	G('G', 2),
	H('H', 4),
	I('I', 1),
	J('J', 8),
	K('K', 5),
	L('L', 1),
	M('M', 3),
	N('N', 1),
	O('O', 1),
	P('P', 3),
	Q('Q', 10),
	R('R', 1),
	S('S', 1),
	T('T', 1),
	U('U', 1),
	V('V', 4),
	W('W', 4),
	X('X', 8),
	Y('Y', 4),
	Z('Z', 10),
	
	BLANK_A('A', 0),
	BLANK_B('B', 0),
	BLANK_C('C', 0),
	BLANK_D('D', 0),
	BLANK_E('E', 0),
	BLANK_F('F', 0),
	BLANK_G('G', 0),
	BLANK_H('H', 0),
	BLANK_I('I', 0),
	BLANK_J('J', 0),
	BLANK_K('K', 0),
	BLANK_L('L', 0),
	BLANK_M('M', 0),
	BLANK_N('N', 0),
	BLANK_O('O', 0),
	BLANK_P('P', 0),
	BLANK_Q('Q', 0),
	BLANK_R('R', 0),
	BLANK_S('S', 0),
	BLANK_T('T', 0),
	BLANK_U('U', 0),
	BLANK_V('V', 0),
	BLANK_W('W', 0),
	BLANK_X('X', 0),
	BLANK_Y('Y', 0),
	BLANK_Z('Z', 0);
	
	private static final LetterMap<Tile> NORMAL_LETTERS;
	private static final LetterMap<Tile> BLANK_LETTERS;
	
	static {
		
		NORMAL_LETTERS = new LetterMap<Tile>();
		BLANK_LETTERS = new LetterMap<Tile>();
		
		for(Tile tile : values()) {
			
			if(tile.isBlank()) BLANK_LETTERS.put(tile.letter, tile);
			else NORMAL_LETTERS.put(tile.letter, tile);
			
		}
		
	}
	
	public static Tile getBlankLetter(char letter) {
		return BLANK_LETTERS.get(letter);
	}
	
	public static Tile getLetter(char letter) {
		return NORMAL_LETTERS.get(letter);
	}
	
	public final char letter;
	public final int value;
	
	Tile(char letter, int value) {
		
		this.letter = letter;
		this.value = value;
		
	}
	
	public boolean isBlank() {
		return value == 0;
	}
	
}
