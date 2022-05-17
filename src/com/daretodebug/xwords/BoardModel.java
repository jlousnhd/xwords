package com.daretodebug.xwords;

import java.util.*;

public interface BoardModel {
	
	public void addBoardModelListener(BoardModelListener listener);
	public void removeBoardModelListener(BoardModelListener listener);
	
	public int getWidth();
	public int getHeight();
	
	public Tile getTileOverlayAt(int x, int y);
	public BoardTileData getTileDataAt(int x, int y);
	
	public Tile getTileAt(int x, int y);
	public void setTileAt(int x, int y, Tile tile);
	
	public void playMove(Move move);
	public void overlayMove(Move move);
	public int addBonus(int score, BoardModel.UnpositionedMove move, BoardModel.Position position);
	
	public static final class Position {
		
		public static final boolean HORIZONTAL = true;
		public static final boolean VERTICAL = false;
		
		public final int x;
		public final int y;
		public final boolean orientation;
		
		public Position(int x, int y, boolean orientation) {
			
			this.x = x;
			this.y = y;
			this.orientation = orientation;
			
		}
		
		@Override
		public boolean equals(Object o) {
			
			if(o == null) return false;
			if(!(o instanceof Position)) return false;
			
			Position p = (Position) o;
			
			return this.x == p.x && this.y == p.y && this.orientation == p.orientation;
			
		}
		
		public boolean equals(Position p) {
			
			if(p == null) return false;
			
			return this.x == p.x && this.y == p.y && this.orientation == p.orientation;
			
		}
		
		@Override
		public int hashCode() {
			return x ^ ((y << 16) | (y >>> 16)) ^ (orientation ? ~0 : 0);
		}
		
	}
	
	public static final class SlotMask {
		
		private WordDictionary.Selection doableWords;
		private Hand hand;
		private final boolean[] mask;
		private final int firstEmpty;
		private final int emptyCount;
		private final int hash;
		
		public SlotMask(Tile[] row, int fromIndex, int toIndex) {
			
			if(row == null) throw new NullPointerException();
			if(fromIndex < 0) throw new IndexOutOfBoundsException("From index must be non-negative.");
			if(toIndex > row.length) throw new IndexOutOfBoundsException("To index must less than or equal to the given row's length.");
			
			int length = toIndex - fromIndex;
			if(length < 2) throw new IllegalArgumentException("To index must be at least two greater than from index.");
			
			mask = new boolean[length];
			
			int emptyCount = 0;
			int firstEmpty = -1;
			int hash = length;
			
			for(int i = 0; i < mask.length; ++i) {
				
				hash = (hash << 1) | (hash >>> 31);
				
				Tile tile = row[fromIndex + i];
				
				if(tile == null) {
					
					if(firstEmpty == -1) firstEmpty = i;
					++emptyCount;
					
				}
				
				else {
					
					mask[i] = true;
					hash ^= 1;
					
				}
				
			}
			
			if(emptyCount == 0) throw new IllegalArgumentException("Given range does not contain any empty spaces.");
			
			this.firstEmpty = firstEmpty;
			this.emptyCount = emptyCount;
			this.hash = hash;
			
		}
		
		public SlotMask(boolean[] mask, int firstEmpty, int emptyCount, int hash) {
			
			this.mask = mask;
			this.firstEmpty = firstEmpty;
			this.emptyCount = emptyCount;
			this.hash = hash;
			
		}
		
		public static SlotMask tryNew(Tile[] row, int fromIndex, int toIndex) {
			
			if(row == null) throw new NullPointerException();
			if(fromIndex < 0) throw new IndexOutOfBoundsException("From index must be non-negative.");
			if(toIndex > row.length) throw new IndexOutOfBoundsException("To index must less than or equal to the given row's length.");
			
			int length = toIndex - fromIndex;
			if(length < 2) throw new IllegalArgumentException("To index must be at least two greater than from index.");
			
			boolean[] mask = new boolean[length];
			
			int emptyCount = 0;
			int firstEmpty = -1;
			int hash = length;
			
			for(int i = 0; i < mask.length; ++i) {
				
				hash = (hash << 1) | (hash >>> 31);
				
				Tile tile = row[fromIndex + i];
				
				if(tile == null) {
					
					if(firstEmpty == -1) firstEmpty = i;
					++emptyCount;
					
				}
				
				else {
					
					mask[i] = true;
					hash ^= 1;
					
				}
				
			}
			
			if(emptyCount == 0) return null;
			
			return new SlotMask(mask, firstEmpty, emptyCount, hash);
			
		}
		
		public int getEmptyCount() {
			return emptyCount;
		}
		
		public int getFirstEmpty() {
			return firstEmpty;
		}
		
		public int getLength() {
			return mask.length;
		}
		
		public int getFullCount() {
			return mask.length - emptyCount;
		}
		
		public void releaseWords() {
			
			doableWords = null;
			hand = null;
			
		}
		
		public boolean getMasked(int index) {
			return mask[index];
		}
		
		public WordDictionary.Selection getDoableWords(Hand hand, WordDictionary dictionary) {
			
			if(doableWords != null && doableWords.getDictionary() == dictionary && this.hand.equals(hand))
				return doableWords;
			
			this.hand = hand;
			
			if(hand.getTileCount() < emptyCount) {
				
				this.doableWords = dictionary.selectNone(mask.length);
				this.doableWords.freeze();
				
				return this.doableWords;
				
			}
			
			List<String> words = dictionary.getWordsOfLength(mask.length);
			BitSet doableWords = new BitSet(words.size());
			
			LetterIntMap letterCounts = new LetterIntMap();
			int blankCount;
			
			nextWord:
			for(int i = 0; i < words.size(); ++i) {
				
				hand.getLetterCounts(letterCounts);
				blankCount = hand.getBlankCount();
				
				String word = words.get(i);
				
				for(int j = 0; j < mask.length; ++j) {
					
					// If this space is already filled, continue to next space
					if(mask[j]) continue;
					
					// We have to fill this space; find out if we can
					if(letterCounts.decrement(word.charAt(j)) >= 0)
						continue;
					
					if((--blankCount) >= 0)
						continue;
					
					// We don't have enough letters/blanks; skip to next word
					continue nextWord;
					
				}
				
				// This word is doable
				doableWords.set(i);
				
			}
			
			this.doableWords = dictionary.new Selection(mask.length, doableWords);
			this.doableWords.freeze();
			
			return this.doableWords;
			
		}
		
		@Override
		public int hashCode() {
			return hash;
		}
		
		@Override
		public boolean equals(Object o) {
			
			if(this == o) return true;
			if(o == null) return false;
			if(!(o instanceof SlotMask)) return false;
			
			return Arrays.equals(this.mask, ((SlotMask) o).mask);
			
		}
		
		public boolean equals(SlotMask o) {
			
			if(this == o) return true;
			if(o == null) return false;
			
			return Arrays.equals(this.mask, o.mask);
			
		}
		
	}
	
	public static final class SimpleSlot {
		
		private WordDictionary.Selection fittingWords;
		private SlotMask mask;
		private final char[] slot;
		private final int hash;
		
		public SimpleSlot(Tile[] row, int fromIndex, int toIndex) {
			
			mask = new SlotMask(row, fromIndex, toIndex);
			slot = new char[mask.getLength()];
			
			int hash = slot.length;
			
			for(int i = 0; i < slot.length; ++i) {
				
				hash = (hash << 7) | (hash >>> 25);
				
				Tile tile = row[fromIndex + i];
				
				if(tile != null) {
					
					slot[i] = tile.letter;
					hash ^= tile.letter;
					
				}
				
			}
			
			this.hash = hash;
			
		}
		
		private SimpleSlot(char[] slot, SlotMask mask, int hash) {
			
			this.slot = slot;
			this.mask = mask;
			this.hash = hash;
			
		}
		
		public static SimpleSlot tryNew(Tile[] row, int fromIndex, int toIndex) {
			
			SlotMask mask = SlotMask.tryNew(row, fromIndex, toIndex);
			if(mask == null) return null;
			
			char[] slot = new char[mask.getLength()];
			
			int hash = slot.length;
			
			for(int i = 0; i < slot.length; ++i) {
				
				hash = (hash << 7) | (hash >>> 25);
				
				Tile tile = row[fromIndex + i];
				
				if(tile != null) {
					
					slot[i] = tile.letter;
					hash ^= tile.letter;
					
				}
				
			}
			
			return new SimpleSlot(slot, mask, hash);
			
		}
		
		public int getEmptyCount() {
			return mask.getEmptyCount();
		}
		
		public int getLength() {
			return slot.length;
		}
		
		public int getFullCount() {
			return mask.getFullCount();
		}
		
		public void releaseWords() {
			fittingWords = null;
		}
		
		public char getLetter(int index) {
			return slot[index];
		}
		
		public char[] getLetters() {
			
			char[] letters = new char[slot.length];
			System.arraycopy(slot, 0, letters, 0, letters.length);
			
			return letters;
			
		}
		
		public void internSlotMask(Hashtable<SlotMask, SlotMask> oldPool, Hashtable<SlotMask, SlotMask> newPool) {
			
			SlotMask intern = newPool.get(mask);
			if(intern != null) {
				
				mask = intern;
				return;
				
			}
			
			intern = oldPool.get(mask);
			if(intern != null) {
				
				newPool.put(intern, intern);
				mask = intern;
				return;
				
			}
			
			newPool.put(mask, mask);
			
		}
		
		public WordDictionary.Selection getFittingWords(WordDictionary dictionary) {
			
			if(fittingWords != null && fittingWords.getDictionary() == dictionary)
				return fittingWords;
			
			fittingWords = dictionary.new Selection(slot.length);
			
			for(int i = 0; i < slot.length; ++i) {
				
				char letter = slot[i];
				if(letter != 0) fittingWords = fittingWords.and(letter, i);
				
			}
			
			fittingWords.freeze();
			return fittingWords;
			
		}
		
		public SlotMask getMask() {
			return mask;
		}
		
		@Override
		public int hashCode() {
			return hash;
		}
		
		@Override
		public boolean equals(Object o) {
			
			if(this == o) return true;
			if(o == null) return false;
			if(!(o instanceof SimpleSlot)) return false;
			
			return Arrays.equals(this.slot, ((SimpleSlot) o).slot);
			
		}
		
		public boolean equals(SimpleSlot o) {
			
			if(this == o) return true;
			if(o == null) return false;
			
			return Arrays.equals(this.slot, o.slot);
			
		}
		
	}
	
	public static final class ComplexSlot {
		
		private WordDictionary.Selection fittingWords;
		private List<UnpositionedMove> playableMoves;
		private WordDictionary dictionary;
		private Hand hand;
		private final SimpleSlot mainSlot;
		private final SimpleSlot[] attachedSlots;
		private final int hash;
		
		public ComplexSlot(SimpleSlot mainSlot, SimpleSlot[] attachedSlots) {
			
			if(mainSlot == null) throw new NullPointerException();
			if(mainSlot.getLength() != attachedSlots.length)
				throw new IllegalArgumentException("Attached slots list must be the same length as main slot.");
			
			this.mainSlot = mainSlot;
			this.attachedSlots = new SimpleSlot[attachedSlots.length];
			
			int hash = mainSlot.hashCode();
			
			for(int i = 0; i < attachedSlots.length; ++i) {
				
				SimpleSlot attachedSlot = attachedSlots[i];
				
				hash = (hash << 15) | (hash >>> 17);
				
				if(attachedSlot != null) {
					
					if(mainSlot.getLetter(i) != 0)
						throw new IllegalArgumentException("Slots can only be attached to empty spaces in the main slot.");
					
					if(attachedSlot.getEmptyCount() != 1)
						throw new IllegalArgumentException("All non-null attached slots must have exactly one empty space.");
					
					hash ^= attachedSlot.hashCode();
					
					this.attachedSlots[i] = attachedSlot;
					
				}
				
			}
			
			this.hash = hash;
			
		}
		
		public int getLength() {
			return attachedSlots.length;
		}
		
		public SimpleSlot getMainSlot() {
			return mainSlot;
		}
		
		public SimpleSlot getAttachedSlot(int index) {
			return attachedSlots[index];
		}
		
		public void releaseWords() {
			
			fittingWords = null;
			playableMoves = null;
			dictionary = null;
			hand = null;
			
		}
		
		public WordDictionary.Selection getFittingWords(WordDictionary dictionary) {
			
			if(fittingWords != null && fittingWords.getDictionary() == dictionary)
				return fittingWords;
			
			fittingWords = mainSlot.getFittingWords(dictionary);
			
			for(int i = 0; i < attachedSlots.length; ++i) {
				
				SimpleSlot attachedSlot = attachedSlots[i];
				if(attachedSlot == null) continue;
				
				WordDictionary.Selection attachedWords = attachedSlot.getFittingWords(dictionary);
				WordDictionary.Selection possibleLetters = null;
				
				for(String attachedWord : attachedWords) {
					
					char possibleLetter;
					
					try {
						possibleLetter = attachedWord.charAt(attachedSlot.getMask().getFirstEmpty());
					} catch(StringIndexOutOfBoundsException e) {
						
						System.out.println("Attached word: " + attachedWord + "\nChar index: " + attachedSlot.getMask().getFirstEmpty());
						
						throw e;
						
					}
					
					if(possibleLetters == null) possibleLetters = dictionary.new Selection(attachedSlots.length, possibleLetter, i);
					else possibleLetters = possibleLetters.or(possibleLetter, i);
					
				}
				
				if(possibleLetters == null) {
					
					fittingWords = dictionary.selectNone(attachedSlots.length);
					break;
					
				}
				
				fittingWords = fittingWords.and(possibleLetters);
				
			}
			
			fittingWords.freeze();
			return fittingWords;
			
		}
		
		private void addMovesForWord(String mainWord, String[] attachedWords, int startingIndex, Tile[] placedTiles, LetterIntMap remainingLetters, int remainingBlanks, ArrayList<UnpositionedMove> moves) {
			
			// Find empty spot
			for(;;) {
				
				// We're at the end, add the move
				if(startingIndex >= mainSlot.getLength()) {
					
					Tile[] move = new Tile[placedTiles.length];
					System.arraycopy(placedTiles, 0, move, 0, placedTiles.length);
					moves.add(new UnpositionedMove(this, move, mainWord, attachedWords));
					
					return;
					
				}
				
				if(mainSlot.getLetter(startingIndex) == 0) break;
				++startingIndex;
				
			}
			
			// Empty slot found
			
			char letter = mainWord.charAt(startingIndex);
			
			// First put a real tile there if we can
			if(remainingLetters.decrement(letter) >= 0) {
				
				placedTiles[startingIndex] = Tile.getLetter(letter);
				addMovesForWord(mainWord, attachedWords, startingIndex + 1, placedTiles, remainingLetters, remainingBlanks, moves);
				
			}
			
			remainingLetters.increment(letter);
			
			// Then put a blank tile there if we can
			if(remainingBlanks > 0) {
				
				placedTiles[startingIndex] = Tile.getBlankLetter(letter);
				addMovesForWord(mainWord, attachedWords, startingIndex + 1, placedTiles, remainingLetters, remainingBlanks - 1, moves);
				
			}
			
		}
		
		public List<UnpositionedMove> getPlayableMoves(WordDictionary dictionary, Hand hand) {
			
			if(playableMoves != null && this.dictionary == dictionary && hand.equals(this.hand))
				return playableMoves;
			
			WordDictionary.Selection fittingWords = getFittingWords(dictionary);
			WordDictionary.Selection doableWords = mainSlot.getMask().getDoableWords(hand, dictionary);
			
			WordDictionary.Selection playableWords = fittingWords.and(doableWords);
			
			ArrayList<UnpositionedMove> playableMoves = new ArrayList<BoardModel.UnpositionedMove>();
			
			LetterIntMap remainingLetters = new LetterIntMap();
			
			for(String mainWord : playableWords) {
				
				String[] attachedWords = new String[attachedSlots.length];
				for(int i = 0; i < attachedSlots.length; ++i) {
					
					SimpleSlot attachedSlot = attachedSlots[i];
					if(attachedSlot == null) continue;
					
					char[] attachedWord = attachedSlot.getLetters();
					attachedWord[attachedSlot.getMask().getFirstEmpty()] = mainWord.charAt(i);
					
					attachedWords[i] = dictionary.internWord(attachedWord);
					
				}
				
				hand.getLetterCounts(remainingLetters);
				addMovesForWord(mainWord, attachedWords, 0, new Tile[mainWord.length()], remainingLetters, hand.getBlankCount(), playableMoves);
				
			}
			
			this.dictionary = dictionary;
			this.hand = hand;
			
			return (this.playableMoves = Collections.unmodifiableList(playableMoves));
			
		}
		
		@Override
		public int hashCode() {
			return hash;
		}
		
		@Override
		public boolean equals(Object o) {
			
			if(this == o) return true;
			if(o == null) return false;
			if(!(o instanceof ComplexSlot)) return false;
			
			ComplexSlot c = (ComplexSlot) o;
			
			if(!this.mainSlot.equals(c.mainSlot)) return false;
			
			for(int i = 0; i < attachedSlots.length; ++i) {
				
				SimpleSlot attachedSlot = this.attachedSlots[i];
				SimpleSlot otherSlot = c.attachedSlots[i];
				
				if(attachedSlot == null) {
					
					if(otherSlot != null) return false;
					continue;
					
				}
				
				if(!attachedSlot.equals(otherSlot)) return false;
				
			}
			
			return true;
			
		}
		
	}
	
	public final static class UnpositionedMove {
		
		private final Tile[] move;
		private final ComplexSlot slot;
		
		private final String mainWord;
		private final String[] attachedWords;
		
		private UnpositionedMove(ComplexSlot slot, Tile[] move, String mainWord, String[] attachedWords) {
			
			this.slot = slot;
			this.move = move;
			
			this.mainWord = mainWord;
			this.attachedWords = attachedWords;
			
		}
		
		public Tile getTile(int index) {
			return move[index];
		}
		
		public int getLength() {
			return move.length;
		}
		
		public String getMainWord() {
			return mainWord;
		}
		
		public String getAttachedWord(int index) {
			return attachedWords[index];
		}
		
		public ComplexSlot getSlot() {
			return slot;
		}
		
		public int getTilesUsed() {
			return slot.getMainSlot().getEmptyCount();
		}
		
		public Move position(BoardModel board, Position position) {
			
			int height = board.getHeight();
			int width = board.getWidth();
			
			int score = 0;
			int mult = 1;
			int totalAttached = 0;
			
			if(position.orientation == Position.HORIZONTAL) {
				
				if(position.y < 0 || position.y >= height)
					throw new IllegalArgumentException("Y must be within the board's bounds.");
				
				if(position.x < 0 || position.x + getLength() > width)
					throw new IllegalArgumentException("X must be within the board's bounds and have enough space following it for this move.");
				
				// Calculate score
				for(int i = 0; i < move.length; ++i) {
					
					int x = position.x + i;
					
					Tile tile;
					
					// If we're not placing a tile here, just add the tile value with no modifiers
					if((tile = move[i]) == null)
						score += board.getTileAt(x, position.y).value;
					
					// If we're placing a tile here, account for modifiers and attached word (if any)
					else {
						
						BoardTileData data = board.getTileDataAt(x, position.y);
						
						score += tile.value * data.getLetterMultiplier();
						mult *= data.getWordMultiplier();
						
						// If there's an attached word
						SimpleSlot attachedSlot = slot.getAttachedSlot(i);
						if(attachedSlot != null) {
							
							int attachedScore = tile.value * data.getLetterMultiplier();
							int slotOffset = position.y - attachedSlot.getMask().getFirstEmpty();
							
							for(int j = 0; j < attachedSlot.getLength(); ++j) {
								
								Tile attachedTile = board.getTileAt(x, slotOffset + j);
								
								if(attachedTile != null)
									attachedScore += attachedTile.value;
								
							}
							
							attachedScore *= data.getWordMultiplier();
							totalAttached += attachedScore;
							
						}
						
					}
					
				}
				
			} else { // VERTICAL
				
				if(position.x < 0 || position.x >= width)
					throw new IllegalArgumentException("X must be within the board's bounds.");
				
				if(position.y < 0 || position.y + getLength() > height)
					throw new IllegalArgumentException("Y must be within the board's bounds and have enough space following it for this move.");
				
				// Calculate score
				for(int i = 0; i < move.length; ++i) {
					
					int y = position.y + i;
					
					Tile tile;
					
					// If we're not placing a tile here, just add the tile value with no modifiers
					if((tile = move[i]) == null)
						score += board.getTileAt(position.x, y).value;
					
					// If we're placing a tile here, account for modifiers and attached word (if any)
					else {
						
						BoardTileData data = board.getTileDataAt(position.x, y);
						
						score += tile.value * data.getLetterMultiplier();
						mult *= data.getWordMultiplier();
						
						// If there's an attached word
						SimpleSlot attachedSlot = slot.getAttachedSlot(i);
						if(attachedSlot != null) {
							
							int attachedScore = tile.value * data.getLetterMultiplier();
							int slotOffset = position.x - attachedSlot.getMask().getFirstEmpty();
							
							for(int j = 0; j < attachedSlot.getLength(); ++j) {
								
								Tile attachedTile = board.getTileAt(slotOffset + j, y);
								
								if(attachedTile != null)
									attachedScore += attachedTile.value;
								
							}
							
							attachedScore *= data.getWordMultiplier();
							totalAttached += attachedScore;
							
						}
						
					}
					
				}
				
			}
			
			return new Move(board, this, position, board.addBonus(score * mult + totalAttached, this, position));
			
		}
		
	}
	
	public final static class Move {
		
		private final BoardModel board;
		private final UnpositionedMove move;
		private final Position position;
		private final int score;
		
		private Move(BoardModel board, UnpositionedMove move, Position position, int score) {
			
			this.board = board;
			this.move = move;
			this.position = position;
			this.score = score;
			
		}
		
		public UnpositionedMove getUnpositionedMove() {
			return move;
		}
		
		public Position getPosition() {
			return position;
		}
		
		public int getScore() {
			return score;
		}
		
		public BoardModel getBoard() {
			return board;
		}
		
	}
	
}
