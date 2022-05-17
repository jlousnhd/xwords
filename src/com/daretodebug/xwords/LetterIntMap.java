package com.daretodebug.xwords;

import java.util.*;

public final class LetterIntMap implements Map<Character, Integer> {
	
	private static final int SIZE = 26;
	private static final Character[] KEY_ARRAY = generateKeys();
	private static final SortedSet<Character> GLOBAL_KEYS = Collections.unmodifiableSortedSet(new TreeSet<Character>(Arrays.asList(KEY_ARRAY)));
	
	private int[] array;
	
	private static Character[] generateKeys() {
		
		Character[] keys = new Character[SIZE];
		
		for(int i = 0; i < SIZE; ++i) {
			keys[i] = (char) ('A' + i);
		}
		
		return keys;
		
	}
	
	public LetterIntMap() {
		array = new int[SIZE];
	}
	
	public void copyValuesTo(LetterIntMap dst) {
		System.arraycopy(array, 0, dst.array, 0, SIZE);
	}
	
	public LetterIntMap copy() {
		
		LetterIntMap copy = new LetterIntMap();
		copyValuesTo(copy);
		
		return copy;
		
	}
	
	public void clearValues() {
		Arrays.fill(array, 0);
	}
	
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsKey(Object key) {
		return containsKey(((Character) key).charValue());
	}
	
	public boolean containsKey(char key) {
		return key >= 'A' && key <= 'Z';
	}
	
	@Override
	public boolean containsValue(Object value) {
		return containsValue(((Integer) value).intValue());
	}
	
	public boolean containsValue(int value) {
		
		for(int item : array) {
			if(item == value) return true;
		}
		
		return false;
		
	}
	
	@Override
	public Set<Map.Entry<Character, Integer>> entrySet() {
		
		HashSet<Map.Entry<Character, Integer>> set = new HashSet<Map.Entry<Character,Integer>>(SIZE); 
		
		for(int i = 0; i < SIZE; ++i) {
			set.add(new Entry(i));
		}
		
		return Collections.unmodifiableSet(set);
		
	}

	@Override
	public Integer get(Object key) {
		
		char charKey = ((Character) key).charValue();
		
		if(containsKey(charKey)) return array[charKey - 'A'];
		return null;
		
	}
	
	public int get(char key) {
		
		int index = key - 'A';
		if(index < 0 || index >= array.length)
			throw new IllegalArgumentException("Key must be a letter in the range A-Z.");
		
		return array[index];
		
	}
	
	public int increment(char key) {
		
		int index = key - 'A';
		if(index < 0 || index >= array.length)
			throw new IllegalArgumentException("Key must be a letter in the range A-Z.");
		
		return ++array[index];
		
	}
	
	public int decrement(char key) {
		
		int index = key - 'A';
		if(index < 0 || index >= array.length)
			throw new IllegalArgumentException("Key must be a letter in the range A-Z.");
		
		return --array[index];
		
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public Set<Character> keySet() {
		return GLOBAL_KEYS;
	}

	@Override
	public Integer put(Character key, Integer value) {
		return put(key.charValue(), value.intValue());
	}
	
	public int put(char key, int value) {
		
		int index = key - 'A';
		if(index < 0 || index >= array.length)
			throw new IllegalArgumentException("Key must be a letter in the range A-Z.");
		
		int old = array[index];
		array[index] = value;
		
		return old;
		
	}

	@Override
	public void putAll(Map<? extends Character, ? extends Integer> m) {
		
		for(Map.Entry<? extends Character, ? extends Integer> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
		
	}

	@Override
	public Integer remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return SIZE;
	}

	@Override
	public Collection<Integer> values() {
		
		ArrayList<Integer> values = new ArrayList<Integer>(SIZE);
		
		for(int value : array) {
			values.add(value);
		}
		
		return Collections.unmodifiableList(values);
		
	}
	
	private final class Entry implements Map.Entry<Character, Integer> {
		
		private int index;
		
		public Entry(int index) { this.index = index; }
		
		@Override
		public int hashCode() { return index; }
		
		@Override
		public Character getKey() {
			return KEY_ARRAY[index];
		}
		
		@Override
		public Integer getValue() {
			return array[index];
		}
		
		@Override
		public Integer setValue(Integer value) {
			
			Integer old = array[index];
			array[index] = value;
			
			return old;
			
		}
		
	}

}
