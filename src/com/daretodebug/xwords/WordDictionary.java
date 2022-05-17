package com.daretodebug.xwords;

import java.util.*;

public final class WordDictionary {
	
	private static final Comparator<String> WORD_COMPARATOR = new Comparator<String>() {
		
		@Override
		public int compare(String arg0, String arg1) {
			
			if(arg0 == null) {
				
				if(arg1 == null) return 0;
				return -1;
				
			}
			
			if(arg1 == null) return 1;
			
			if(arg0.length() > arg1.length()) return 1;
			if(arg0.length() < arg1.length()) return -1;
			
			return arg0.compareTo(arg1);
			
		}
		
	};
	
	private static boolean isValidWord(String word) {
		
		for(int i = 0; i < word.length(); ++i) {
			
			char letter = word.charAt(i);
			if(letter >= 'A' && letter <= 'Z') continue;
			
			return false;
			
		}
		
		return true;
		
	}
	
	private static int compare(String a, char[] b) {
		
		if(a.length() > b.length) return 1;
		if(a.length() < b.length) return -1;
		
		for(int i = 0; i < b.length; ++i) {
			
			char chA = a.charAt(i);
			char chB = b[i];
			
			if(chA > chB) return 1;
			if(chA < chB) return -1;
			
		}
		
		return 0;
		
	}
	
	public static int binarySearch(String[] words, char[] word) {
		
		int low = 0;
		int high = words.length - 1;
		int mid;
		
		while(low <= high) {
			
			mid = low + (high - low) / 2;
			
			if(compare(words[mid], word) < 0)
				low = mid + 1;
			
			else if(compare(words[mid], word ) > 0)
				high = mid - 1;
			
			else return mid;
			
		}
		
		return -1;
		
    }
	
	private int wordCount;
	private final String[][] words;
	private final List<String>[] readonlyWords;
	private final LetterMap<BitSet[]>[] characterPositions;
	private final Selection[] emptySelections;
	
	@SuppressWarnings("unchecked")
	public WordDictionary(Iterator<String> wordIterator) {
		
		TreeSet<String> wordSet = new TreeSet<String>(WORD_COMPARATOR);
		int longestWordLength = 0;
		
		while(wordIterator.hasNext()) {
			
			String word = wordIterator.next().toUpperCase();
			
			if(word == null) throw new NullPointerException("Given word iterable returned a null word.");
			if(!isValidWord(word)) throw new IllegalArgumentException("Given word iterable returned an invalid word:\n" + word);
			
			// Ignore any word shorter than two characters
			if(word.length() < 2) continue;
			
			wordSet.add(word);
			longestWordLength = Math.max(longestWordLength, word.length());
			
		}
		
		wordCount = wordSet.size();
		if(wordCount < 1) throw new IllegalArgumentException("No valid words were contained in the given iterable.");
		
		char[] as = new char[longestWordLength + 1];
		Arrays.fill(as, 'A');
		
		// Populate words arrays and tables
		words = new String[longestWordLength - 1][];
		characterPositions = (LetterMap<BitSet[]>[]) new LetterMap[words.length];
		emptySelections = new Selection[words.length];
		readonlyWords = new List[words.length];
		
		for(int i = 0; i < words.length; ++i) {
			
			int wordLength = i + 2;
			
			String start = new String(as, 0, wordLength);
			String end = new String(as, 0, wordLength + 1);
			
			SortedSet<String> wordsOfLength = wordSet.subSet(start, end);
			String[] wordList = wordsOfLength.toArray(new String[wordsOfLength.size()]);
			words[i] = wordList;
			readonlyWords[i] = Collections.unmodifiableList(Arrays.asList(wordList));
			
			emptySelections[i] = new Selection(i, new BitSet(0), true);
			
			LetterMap<BitSet[]> chPos = new LetterMap<BitSet[]>(); 
			characterPositions[i] = chPos;
			
			for(char ch = 'A'; ch <= 'Z'; ++ch) {
				
				BitSet[] pos = new BitSet[wordLength];
				
				for(int j = 0; j < pos.length; ++j)
					pos[j] = new BitSet(words.length);
				
				chPos.put(ch, pos);
				
			}
			
			for(int wordIndex = 0; wordIndex < wordList.length; ++wordIndex) {
				
				String word = wordList[wordIndex];
				
				for(int letterIndex = 0; letterIndex < word.length(); ++letterIndex) {
					
					char letter = word.charAt(letterIndex);
					chPos.get(letter)[letterIndex].set(wordIndex);
					
				}
				
			}
			
		}
		
	}
	
	private static int wordLengthToTableIndex(int wordLength) {
		return wordLength - 2;
	}
	
	private static int tableIndexToWordLength(int tableIndex) {
		return tableIndex + 2;
	}
	
	public List<String> getWordsOfLength(int wordLength) {
		
		int tableIndex = wordLengthToTableIndex(wordLength);
		
		if(tableIndex < 0 || tableIndex >= readonlyWords.length) return Collections.emptyList();
		return readonlyWords[tableIndex];
		
	}
	
	public boolean containsWord(String word) {
		
		if(word == null) return false;
		int tableIndex = wordLengthToTableIndex(word.length());
		
		if(tableIndex < 0 || tableIndex >= words.length) return false;
		
		return Arrays.binarySearch(words[tableIndex], word, WORD_COMPARATOR) >= 0;
		
	}
	
	public String internWord(String word) {
		
		if(word == null) return word;
		int tableIndex = wordLengthToTableIndex(word.length());
		
		if(tableIndex < 0 || tableIndex >= words.length) return word;
		
		String[] table = words[tableIndex];
		int index = Arrays.binarySearch(table, word, WORD_COMPARATOR);
		
		return (index >= 0) ? table[index] : word;
		
	}
	
	public String internWord(char[] word) {
		
		if(word == null) return new String(word);
		int tableIndex = wordLengthToTableIndex(word.length);
		
		if(tableIndex < 0 || tableIndex >= words.length) return new String(word);
		
		String[] table = words[tableIndex];
		int index = binarySearch(table, word);
		
		return (index >= 0) ? table[index] : new String(word);
		
	}
	
	public Selection selectNone(int wordLength) {
		
		int tableIndex = wordLengthToTableIndex(wordLength);
		
		if(tableIndex < 0 || tableIndex >= emptySelections.length) return new Selection(tableIndex, new BitSet(0), false);
		return emptySelections[tableIndex];
		
	}
	
	public final class Selection implements Iterable<String> {
		
		private int tableIndex;
		private BitSet selection; // null means all words fit
		private boolean readonly;
		
		public Selection(int wordLength) {
			
			int tableIndex = wordLengthToTableIndex(wordLength);
			this.tableIndex = tableIndex;
			
			readonly = false;
			
			if(tableIndex < 0 || tableIndex >= characterPositions.length)
				selection = new BitSet(0);
				
			else selection = null;
			
		}
		
		public Selection(int wordLength, BitSet selectedWords) {
			
			int tableIndex = wordLengthToTableIndex(wordLength);
			this.tableIndex = tableIndex;
			
			readonly = false;
			
			if(tableIndex < 0 || tableIndex >= characterPositions.length)
				selection = new BitSet(0);
			
			else {
				
				selection = (BitSet) selectedWords.clone();
				
				int wordCount = words[tableIndex].length;
				int selectionCount = selectedWords.length();
				
				if(selectionCount > wordCount)
					selection.clear(wordCount, selectionCount);
				
			}
			
		}
		
		public Selection(int wordLength, char letter, int position) {
			
			if(letter < 'A' || letter > 'Z') throw new IllegalArgumentException();
			
			int tableIndex = wordLengthToTableIndex(wordLength);
			this.tableIndex = tableIndex;
			
			if(position < 0 || position >= getWordsLength())
				throw new IndexOutOfBoundsException();
			
			if(tableIndex < 0 || tableIndex >= characterPositions.length) {
				
				selection = new BitSet(0);
				readonly = false;
				
			} else {
				
				selection = characterPositions[tableIndex].get(letter)[position];
				readonly = true;
				
			}
			
		}
		
		private Selection(int tableIndex, BitSet selection, boolean readonly) {
			
			this.tableIndex = tableIndex;
			this.selection = selection;
			this.readonly = readonly;
			
		}
		
		private Selection(Selection selection) {
			
			this.selection = (selection.selection == null) ? null : (BitSet) selection.selection.clone();
			this.tableIndex = selection.tableIndex;
			this.readonly = false;
			
		}
		
		public void freeze() {
			readonly = true;
		}
		
		public Selection and(char letter, int position) {
			
			if(letter < 'A' || letter > 'Z') throw new IllegalArgumentException();
			
			if(position < 0 || position >= getWordsLength())
				throw new IndexOutOfBoundsException();
			
			if(tableIndex < 0 || tableIndex >= characterPositions.length)
				return this;
			
			BitSet and = characterPositions[tableIndex].get(letter)[position];
			Selection writable;
			
			if(readonly) writable = new Selection(this);
			else writable = this;
			
			if(writable.selection == null) {
				
				writable.selection = and;
				writable.readonly = true;
				
			} else writable.selection.and(and);
			
			return writable;
			
		}
		
		public Selection and(Selection other) {
			
			if(other == null) throw new NullPointerException();
			
			if(this.getDictionary() != other.getDictionary())
				throw new IllegalArgumentException("The given selection's dictionary does not match this selection's dictionary.");
			
			if(this.tableIndex != other.tableIndex)
				throw new IllegalArgumentException("The given selection's table index does not match this selection's table index.");
			
			BitSet and = other.selection;
			Selection writable;
			
			if(readonly) writable = new Selection(this);
			else writable = this;
			
			if(writable.selection == null) writable.selection = (BitSet) and.clone();
			else writable.selection.and(and);
			
			return writable;
			
		}
		
		public Selection or(char letter, int position) {
			
			if(letter < 'A' || letter > 'Z') throw new IllegalArgumentException();
			
			if(position < 0 || position >= getWordsLength())
				throw new IndexOutOfBoundsException();
			
			if(tableIndex < 0 || tableIndex >= characterPositions.length)
				return this;
			
			if(this.selection == null) return this;
			
			BitSet or = characterPositions[tableIndex].get(letter)[position];
			Selection writable;
			
			if(readonly) writable = new Selection(this);
			else writable = this;
			
			writable.selection.or(or);
			
			return writable;
			
		}
		
		public int getWordsLength() {
			return tableIndexToWordLength(tableIndex);
		}
		
		public WordDictionary getDictionary() {
			return WordDictionary.this;
		}

		@Override
		public java.util.Iterator<String> iterator() {
			
			if(selection != null) return new Iterator();
			else return Arrays.asList(words[tableIndex]).iterator();
			
		}
		
		public final class Iterator implements java.util.Iterator<String> {
			
			private int nextIndex;
			
			public Iterator() {
				nextIndex = selection.nextSetBit(0);
			}
			
			@Override
			public boolean hasNext() {
				return nextIndex >= 0;
			}

			@Override
			public String next() {
				
				if(!hasNext()) throw new NoSuchElementException();
				
				String word = words[tableIndex][nextIndex];
				nextIndex = selection.nextSetBit(nextIndex + 1);
				
				return word;
				
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		}
		
	}
	
}
