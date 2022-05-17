package com.daretodebug.xwords;

import java.util.*;

public class BoardModelEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	
	private TileCoords coords;
	
	public BoardModelEvent(Object source, TileCoords coords) {
		
		super(source);
		
		this.coords = coords;
		
	}
	
	public TileCoords getTileCoords() { return coords; }
	
}
