package com.daretodebug.xwords;

import java.util.*;

public final class LetterMap<T> implements Map<Character, T> {
	
	private static final int SIZE = 26;
	private static final Character[] KEY_ARRAY = generateKeys();
	private static final SortedSet<Character> GLOBAL_KEYS = Collections.unmodifiableSortedSet(new TreeSet<Character>(Arrays.asList(KEY_ARRAY)));
	
	private T[] array;
	
	private static Character[] generateKeys() {
		
		Character[] keys = new Character[SIZE];
		
		for(int i = 0; i < SIZE; ++i) {
			keys[i] = (char) ('A' + i);
		}
		
		return keys;
		
	}
	
	@SuppressWarnings("unchecked")
	public LetterMap() {
		array = (T[]) new Object[SIZE];
	}
	
	public void copyValuesTo(LetterMap<T> dst) {
		System.arraycopy(array, 0, dst, 0, SIZE);
	}
	
	public LetterMap<T> shallowCopy() {
		
		LetterMap<T> copy = new LetterMap<T>();
		copyValuesTo(copy);
		
		return copy;
		
	}
	
	public void clearValues() {
		Arrays.fill(array, null);
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
		
		if(value == null) for(T item : array) {
			if(item == null) return true;
		} else for(T item : array) {
			if(value.equals(item)) return true;
		}
		
		return false;
		
	}

	@Override
	public Set<Map.Entry<Character, T>> entrySet() {
		
		HashSet<Map.Entry<Character, T>> set = new HashSet<Map.Entry<Character,T>>(SIZE); 
		
		for(int i = 0; i < SIZE; ++i) {
			set.add(new Entry(i));
		}
		
		return Collections.unmodifiableSet(set);
		
	}

	@Override
	public T get(Object key) {
		return get(((Character) key).charValue());
	}
	
	public T get(char key) {
		
		if(containsKey(key)) return array[key - 'A'];
		return null;
		
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
	public T put(Character key, T value) {
		return put(key.charValue(), value);
	}
	
	public T put(char key, T value) {
		
		if(!containsKey(key)) throw new IllegalArgumentException("Key must be a letter in the range A-Z.");
		
		int index = key - 'A';
		
		T old = array[index];
		array[index] = value;
		
		return old;
		
	}

	@Override
	public void putAll(Map<? extends Character, ? extends T> m) {
		
		for(Map.Entry<? extends Character, ? extends T> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
		
	}

	@Override
	public T remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return SIZE;
	}

	@Override
	public Collection<T> values() {
		return Collections.unmodifiableList(Arrays.asList(array));
	}
	
	private final class Entry implements Map.Entry<Character, T> {
		
		private int index;
		
		public Entry(int index) { this.index = index; }
		
		@Override
		public int hashCode() { return index; }
		
		@Override
		public Character getKey() {
			return KEY_ARRAY[index];
		}
		
		@Override
		public T getValue() {
			return array[index];
		}
		
		@Override
		public T setValue(T value) {
			
			T old = array[index];
			array[index] = value;
			
			return old;
			
		}
		
	}

}
