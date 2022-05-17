package com.daretodebug.xwords;

import java.io.*;
import java.util.*;

public class ReaderLineIterator implements Iterator<String> {
		
	private final BufferedReader reader;
	private String next;
	
	public ReaderLineIterator(Reader reader) {
		
		this.reader = new BufferedReader(reader);
		
		next = getNext();
		
	}
	
	private String getNext() {
		
		try {
			return reader.readLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public String next() {
		
		String next = this.next;
		if(next == null) throw new NoSuchElementException();
		
		this.next = getNext();
		
		return next;
		
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
