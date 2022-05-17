package com.daretodebug.xwords;

import java.lang.ref.*;

public class WeakBoardModelListener implements BoardModelListener {
	
	private WeakReference<BoardModelListener> listener;
	private BoardModel holder;
	
	public WeakBoardModelListener(BoardModelListener listener, BoardModel holder) {
		
		if(listener == null || holder == null) throw new NullPointerException();
		
		this.listener = new WeakReference<BoardModelListener>(listener);
		this.holder = holder;
		
	}
	
	private BoardModelListener removeIfDead() {
		
		BoardModelListener listener = this.listener.get();
		
		if(listener == null && holder != null) {
			
			holder.removeBoardModelListener(this);
			holder = null;
			
		}
		
		return listener;
		
	}

	@Override
	public void tileChanged(BoardModelEvent e) {
		
		BoardModelListener listener = removeIfDead();
		if(listener != null) listener.tileChanged(e);
		
	}

	@Override
	public void tileAppearanceChanged(BoardModelEvent e) {
		
		BoardModelListener listener = removeIfDead();
		if(listener != null) listener.tileAppearanceChanged(e);
		
	}
	
}
