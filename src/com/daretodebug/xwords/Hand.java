package com.daretodebug.xwords;

public final class Hand {
	
	private LetterIntMap letterCounts;
	private int blankCount;
	private int tileCount;
	private int hash;
	
	public Hand(LetterIntMap letterCounts, int blankCount) {
		
		this.letterCounts = letterCounts.copy();
		this.blankCount = blankCount;
		this.tileCount = blankCount;
		
		hash = blankCount;
		
		for(char ch = 'A'; ch <= 'Z'; ++ch) {
			
			int letterCount = letterCounts.get(ch);
			
			hash = (hash << 3) | (hash >>> 29);
			hash ^= letterCount;
			
			tileCount += letterCount;
			
		}
		
	}
	
	public LetterIntMap getLetterCounts() {
		return letterCounts.copy();
	}
	
	public void getLetterCounts(LetterIntMap map) {
		letterCounts.copyValuesTo(map);
	}
	
	public int getLetterCount(char letter) {
		return letterCounts.get(letter);
	}
	
	public int getBlankCount() {
		return blankCount;
	}
	
	public int getTileCount() {
		return tileCount;
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if(o == this) return true;
		if(o == null) return false;
		if(!(o instanceof Hand)) return false;
		
		Hand h = (Hand) o;
		
		if(this.blankCount != h.blankCount) return false;
		
		for(char ch = 'A'; ch <= 'Z'; ++ch)
			if(this.letterCounts.get(ch) != h.letterCounts.get(ch)) return false;
		
		return true;
		
	}
	
	public boolean equals(Hand h) {
		
		if(h == this) return true;
		if(h == null) return false;
		
		if(this.blankCount != h.blankCount) return false;
		
		for(char ch = 'A'; ch <= 'Z'; ++ch)
			if(this.letterCounts.get(ch) != h.letterCounts.get(ch)) return false;
		
		return true;
		
	}
	
}
