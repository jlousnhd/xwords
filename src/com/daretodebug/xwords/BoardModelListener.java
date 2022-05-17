package com.daretodebug.xwords;

import java.util.*;

public interface BoardModelListener extends EventListener {
	
	public void tileChanged(BoardModelEvent e);
	public void tileAppearanceChanged(BoardModelEvent e);
	
}
