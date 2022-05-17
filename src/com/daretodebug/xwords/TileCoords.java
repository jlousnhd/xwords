package com.daretodebug.xwords;

public final class TileCoords implements Comparable<TileCoords> {
	
	public final int x;
	public final int y;
	
	public TileCoords(int x, int y) {
		
		this.x = x;
		this.y = y;
		
	}

	@Override
	public int compareTo(TileCoords o) {
		
		if(this.x > o.x) return 1;
		if(this.x < o.x) return -1;
		
		if(this.y > o.y) return 1;
		if(this.y < o.y) return -1;
		
		return 0;
		
	}
	
	@Override
	public boolean equals(Object o) {
		
		if(o instanceof TileCoords) return equals((TileCoords) o);
		return false;
		
	}
	
	@Override
	public int hashCode() {
		return x ^ ((y << 16) | (y >>> 16));
	}
	
	public boolean equals(TileCoords o) {
		return this.x == o.x && this.y == o.y;
	}
	
}
