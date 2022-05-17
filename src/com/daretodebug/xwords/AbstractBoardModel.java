package com.daretodebug.xwords;

import java.util.*;

public abstract class AbstractBoardModel implements BoardModel {
	
	private HashSet<BoardModelListener> listeners;
	
	protected AbstractBoardModel() {
		listeners = new HashSet<BoardModelListener>();
	}
	
	@Override
	public void addBoardModelListener(BoardModelListener listener) {
		
		if(listener == null) return;
		listeners.add(listener);
		
	}

	@Override
	public void removeBoardModelListener(BoardModelListener listener) {
		listeners.remove(listener);
	}
	
	public void fireTileChanged(int x, int y) {
		
		BoardModelEvent e = new BoardModelEvent(this, new TileCoords(x, y));
		
		for(BoardModelListener listener : listeners)
			listener.tileChanged(e);
		
		fireTileAppearanceChanged(e);
		
	}
	
	private void fireTileAppearanceChanged(BoardModelEvent e) {
		
		for(BoardModelListener listener : listeners)
			listener.tileAppearanceChanged(e);
		
	}
	
	public void fireTileAppearanceChanged(int x, int y) {
		
		BoardModelEvent e = new BoardModelEvent(this, new TileCoords(x, y));
		
		fireTileAppearanceChanged(e);
		
	}

}
