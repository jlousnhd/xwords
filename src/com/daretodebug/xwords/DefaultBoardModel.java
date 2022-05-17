package com.daretodebug.xwords;

import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.table.*;

public class DefaultBoardModel extends AbstractBoardModel {
	
	private final WorkerTask workerTask;
	private final Tile[][] columns;
	private final Tile[][] rows;
	private final Tile[][] overlay;
	private final BoardTileData[][] tileData;
	private final boolean[][] intersectionPoints;
	private final TileCoords[] startingIntersectionPoints;
	
	private Object boardRevision;
	private WordDictionary dictionary;
	
	private Hand hand;
	private final PlainDocument handModel;
	private final ProgressBar progressBar;
	
	private final MoveModel moveModel;
	
	public DefaultBoardModel(WordDictionary dictionary, int width, int height, BoardModelPopulator populator) {
		
		if(width < 2) throw new IllegalArgumentException("Width must be at least two.");
		if(height < 2) throw new IllegalArgumentException("Height must be at least two.");
		
		if(dictionary == null) throw new NullPointerException();
		this.dictionary = dictionary;
		
		Thread workerThread = new Thread(workerTask = new WorkerTask());
		workerThread.setDaemon(true);
		workerThread.setPriority(Thread.MIN_PRIORITY);
		
		columns = new Tile[width][];
		for(int i = 0; i < columns.length; ++i)
			columns[i] = new Tile[height];
		
		rows = new Tile[height][];
		for(int i = 0; i < rows.length; ++i)
			rows[i] = new Tile[width];
		
		overlay = new Tile[width][];
		for(int i = 0; i < overlay.length; ++i)
			overlay[i] = new Tile[height];
		
		tileData = new BoardTileData[width][];
		for(int x = 0; x < tileData.length; ++x) {
			
			tileData[x] = new BoardTileData[height];
			
			for(int y = 0; y < tileData[x].length; ++y)
				tileData[x][y] = populator.getDataAt(x, y);
			
		}
		
		intersectionPoints = new boolean[width][];
		for(int i = 0; i < intersectionPoints.length; ++i)
			intersectionPoints[i] = new boolean[height];
		
		TileCoords[] startingIntersectionPoints = populator.getStartingIntersectionPoints();
		this.startingIntersectionPoints = new TileCoords[startingIntersectionPoints.length];
		System.arraycopy(startingIntersectionPoints, 0, this.startingIntersectionPoints, 0, startingIntersectionPoints.length);
		
		setStartingIntersectionPoints();
		
		boardRevision = 0;
		
		handModel = new PlainDocument() {

			private static final long serialVersionUID = 1L;
			
			private void syncHand() {
				
				int length = getLength();
				
				if(length == 0) {
					
					setNullHand();
					return;
					
				}
				
				String text = null;
				
				try {
					text = getText(0, length);
				} catch(BadLocationException e) {}
				
				LetterIntMap letterCounts = new LetterIntMap();
				int blankCount = 0;
				
				for(int i = 0; i < text.length(); ++i) {
					
					char ch = text.charAt(i);
					
					if(ch == '*') ++blankCount;
					else letterCounts.increment(ch);
					
				}
				
				setHand(letterCounts, blankCount);
				
			}
			
			@Override
			public void insertString(int offset, String text, AttributeSet attr) throws BadLocationException {
				
				text = text.toUpperCase();
				
				for(int i = 0; i < text.length(); ++i) {
					
					char ch = text.charAt(i);
					
					if(ch >= 'A' && ch <= 'Z')
						continue;
					
					else if(ch == '*')
						continue;
						
					else return;
					
				}
				
				super.insertString(offset, text, attr);
				syncHand();
				
			}
			
			@Override
			public void remove(int start, int end) throws BadLocationException {
				
				super.remove(start, end);
				syncHand();
				
			}
			
		};
		
		moveModel = new MoveModel();
		progressBar = new ProgressBar();
		
		workerThread.start();
		
	}
	
	private static <T> T intern(T item, Hashtable<T, T> oldPool, Hashtable<T, T> newPool) {
		
		T intern = newPool.get(item);
		if(intern != null) return intern;
		
		intern = oldPool.get(item);
		if(intern != null) {
			
			newPool.put(intern, intern);
			return intern;
			
		}
		
		newPool.put(item, item);
		return item;
		
	}
	
	private static <T> AbstractMap.SimpleEntry<T, ArrayList<Position>> internList(T item, Hashtable<T, AbstractMap.SimpleEntry<T, ArrayList<Position>>> oldPool, Hashtable<T, AbstractMap.SimpleEntry<T, ArrayList<Position>>> newPool) {
		
		AbstractMap.SimpleEntry<T, ArrayList<Position>> intern = newPool.get(item);
		if(intern != null) return intern;
		
		intern = oldPool.get(item);
		if(intern != null) {
			
			intern.getValue().clear();
			newPool.put(intern.getKey(), intern);
			return intern;
			
		}
		
		intern = new AbstractMap.SimpleEntry<T, ArrayList<Position>>(item, new ArrayList<Position>());
		newPool.put(item, intern);
		return intern;
		
	}
	
	private static int getFirstStartPointAtOrBefore(Tile[] row, int index) {
		
		--index;
		for(; index >= 0; --index)
			if(row[index] == null) break;
		
		return ++index;
		
	}
	
	private static int getFirstEndPointAfter(Tile[] row, int index) {
		
		++index;
		for(; index < row.length; ++index)
			if(row[index] == null) break;
		
		return index;
		
	}
	
	private static boolean isStartPoint(Tile[] row, int index) {
		
		if(index == 0 || row[index - 1] == null) return true;
		
		return false;
		
	}
	
	private static boolean isEndPoint(Tile[] row, int index) {
		
		if(index == row.length || row[index] == null) return true;
		
		return false;
		
	}
	
	private void setStartingIntersectionPoints() {
		
		for(TileCoords coords : startingIntersectionPoints) {
			intersectionPoints[coords.x][coords.y] = true;
		}
		
	}
	
	@Override
	public int getWidth() {
		return columns.length;
	}

	@Override
	public int getHeight() {
		return rows.length;
	}

	@Override
	public Tile getTileOverlayAt(int x, int y) {
		return overlay[x][y];
	}

	@Override
	public BoardTileData getTileDataAt(int x, int y) {
			return tileData[x][y];
	}
	
	@Override
	public Tile getTileAt(int x, int y) {
		return columns[x][y];
	}
	
	public Document getHandModel() {
		return handModel;
	}
	
	public MoveModel getMoveModel() {
		return moveModel;
	}
	
	public JProgressBar getProgressBar() {
		return progressBar;
	}
	
	public WordDictionary getDictionary() {
		return dictionary;
	}
	
	public void setDictionary(WordDictionary dictionary) {
		
		if(dictionary == null) throw new NullPointerException();
		if(dictionary == this.dictionary) return;
		
		this.dictionary = dictionary;
		workerTask.doSearch(this);
		
	}
	
	private boolean isIntersectionPoint(int x, int y) {
		
		// Check tile left
		if(x > 0 && columns[x - 1][y] != null) return true;
		
		// Check tile right
		if(x + 1 < columns.length && columns[x + 1][y] != null) return true;
		
		// Check tile above
		if(y > 0 && rows[y - 1][x] != null) return true;
		
		// Check tile below
		if(y + 1 < rows.length && rows[y + 1][x] != null) return true;
		
		return false;
		
	}
	
	// Only to be called inside dataLock
	private boolean internalSetTileAt(int x, int y, Tile tile) {
		
		Tile oldTile = columns[x][y];
		if(oldTile == tile) return false;
		
		columns[x] = Arrays.copyOf(columns[x], rows.length);
		rows[y] = Arrays.copyOf(rows[y], columns.length);
		
		columns[x][y] = tile;
		rows[y][x] = tile;
		
		intersectionPoints[x] = Arrays.copyOf(intersectionPoints[x], columns.length);
		
		// If tile is now empty, check intersection points
		if(tile == null) {
			
			// Check tile left
			if(x > 0) {
				
				int left = x - 1;
				intersectionPoints[left] = Arrays.copyOf(intersectionPoints[left], columns.length);
				intersectionPoints[left][y] = isIntersectionPoint(left, y);
				
			}
			
			// Check tile right
			int right = x + 1;
			if(right < intersectionPoints.length) {
				
				intersectionPoints[right] = Arrays.copyOf(intersectionPoints[right], columns.length);
				intersectionPoints[right][y] = isIntersectionPoint(right, y);
				
			}
			
			// Check tile above
			if(y > 0) intersectionPoints[x][y - 1] = isIntersectionPoint(x, y - 1);
			
			// Check tile below
			if(y + 1 < intersectionPoints.length) intersectionPoints[x][y + 1] = isIntersectionPoint(x, y + 1);
			
			setStartingIntersectionPoints();
			
		}
		
		// If the tile was empty but now is not
		else if(oldTile == null) {
			
			// Check tile left
			if(x > 0) {
				
				int left = x - 1;
				intersectionPoints[left] = Arrays.copyOf(intersectionPoints[left], columns.length);
				intersectionPoints[left][y] = true;
				
			}
			
			// Check tile right
			int right = x + 1;
			if(right < intersectionPoints.length) {
				
				intersectionPoints[right] = Arrays.copyOf(intersectionPoints[right], columns.length);
				intersectionPoints[right][y] = true;
				
			}
			
			// Check tile above
			if(y > 0) intersectionPoints[x][y - 1] = true;
			
			// Check tile below
			if(y + 1 < intersectionPoints.length) intersectionPoints[x][y + 1] = true;
			
		}
		
		fireTileChanged(x, y);
		return true;
		
	}
	
	@Override
	public void setTileAt(int x, int y, Tile tile) {
		
		if(internalSetTileAt(x, y, tile)) {
			
			boardRevision = new int[0];
			workerTask.doSearch(this);
			
		}
		
	}
	
	@Override
	public void playMove(Move move) {
		
		if(move.getBoard() != this)
			throw new IllegalArgumentException("This move does not correspond to this board.");
		
		boolean search = false;
		
		UnpositionedMove unpos = move.getUnpositionedMove();
		Position pos = move.getPosition();
		
		if(pos.orientation == Position.HORIZONTAL) {
			
			int y = pos.y;
			
			for(int i = 0; i < unpos.getLength(); ++i) {
				
				Tile tile = unpos.getTile(i);
				if(tile == null) continue;
				
				int x = pos.x + i;
				search |= internalSetTileAt(x, y, tile);
				
			}
			
		} else { // VERTICAL
			
			int x = pos.x;
			
			for(int i = 0; i < unpos.getLength(); ++i) {
				
				Tile tile = unpos.getTile(i);
				if(tile == null) continue;
				
				int y = pos.y + i;
				search |= internalSetTileAt(x, y, tile);
				
			}
			
		}
		
		if(search) {
			
			boardRevision = new int[0];
			workerTask.doSearch(this);
			
		}

	}
	
	@Override
	public void overlayMove(Move move) {
		
		if(move != null && move.getBoard() != this)
			throw new IllegalArgumentException("This move does not correspond to this board.");
		
		for(int x = 0; x < overlay.length; ++x)
			for(int y = 0; y < overlay[x].length; ++y) {
				
				Tile old = overlay[x][y];
				
				if(old != null) {
					
					overlay[x][y] = null;
					fireTileAppearanceChanged(x, y);
					
				}
				
			}
		
		if(move == null) return;
		
		UnpositionedMove unpos = move.getUnpositionedMove();
		Position pos = move.getPosition();
		
		if(pos.orientation == Position.HORIZONTAL) {
			
			int y = pos.y;
			
			for(int i = 0; i < unpos.getLength(); ++i) {
				
				int x = pos.x + i;
				overlay[x][y] = unpos.getTile(i);
				
				fireTileAppearanceChanged(x, y);
				
			}
			
		} else { // VERTICAL
			
			int x = pos.x;
			
			for(int i = 0; i < unpos.getLength(); ++i) {
				
				int y = pos.y + i;
				overlay[x][y] = unpos.getTile(i);
				
				fireTileAppearanceChanged(x, y);
				
			}
			
		}

	}
	
	private void setHand(LetterIntMap letterCounts, int blankCount) {
		
		Hand hand = new Hand(letterCounts, blankCount);
		
		if(hand.equals(this.hand)) return;
		
		this.hand = hand;
		workerTask.doSearch(this);
		
	}
	
	private void setNullHand() {
		
		if(hand == null) return;
		
		hand = null;
		workerTask.doSearch(this);
		
	}
	
	@Override
	public int addBonus(int score, BoardModel.UnpositionedMove move, BoardModel.Position position) {
		return score;
	}
	
	public final static class MoveModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		
		private ArrayList<Move> currentStore;
		private ArrayList<Move> futureStore;
		private final Runnable updater;
		
		public MoveModel() {
			
			currentStore = new ArrayList<Move>();
			
			updater = new Runnable() {
				
				@Override
				public void run() {
					
					synchronized(updater) {
						
						currentStore = futureStore;
						futureStore = null;
						
						fireTableDataChanged();
						
					}
					
				}
				
			};
			
		}
		
		private void update(ArrayList<Move> moves) {
			
			if(moves == null) throw new NullPointerException();
			
			synchronized(updater) {
				
				if(futureStore != null) futureStore = moves;
				
				else {
					
					futureStore = moves;
					SwingUtilities.invokeLater(updater);
					
				}
				
			}
			
		}
		
		public Move getMove(int index) {
			return currentStore.get(index);
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			
			switch(columnIndex) {
			
			case 0: return String.class;
			case 1: return Integer.class;
			case 2: return Integer.class;
			
			default: return null;
			
			}
			
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			
			switch(columnIndex) {
			
			case 0: return "Word(s)";
			case 1: return "Score";
			case 2: return "Tiles Used";
			
			default: return null;
			
			}
			
		}
		
		@Override
		public int getColumnCount() {
			return 3;
		}
		
		@Override
		public int getRowCount() {
			return currentStore.size();
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			
			if(rowIndex < 0 || rowIndex >= currentStore.size()) return null;
			
			Move move = currentStore.get(rowIndex);
			
			switch(columnIndex) {
			
			case 0:
				UnpositionedMove unpos = move.getUnpositionedMove();
				String list = unpos.getMainWord();
				
				for(int i = 0; i < unpos.getLength(); ++i) {
					
					String attachedWord = unpos.getAttachedWord(i);
					
					if(attachedWord != null) list += ", " + attachedWord;
					
				}
				
				return list;
				
			case 1: return move.getScore();
			case 2: return move.getUnpositionedMove().getTilesUsed();
			
			}
			
			return null;
			
		}
		
	}
	
	private final static class ProgressBar extends JProgressBar {
		
		private static final long serialVersionUID = 1L;
		
		private Boolean futureValue;
		private String futureString;
		private final Runnable updater;
		
		public ProgressBar() {
			
			setIndeterminate(true);
			setVisible(false);
			setStringPainted(true);
			
			updater = new Runnable() {
				
				@Override
				public void run() {
					
					synchronized(updater) {
						
						setVisible(futureValue);
						setString(futureString);
						
						futureValue = null;
						futureString = null;
						
					}
					
				}
				
			};
			
		}
		
		public void update(boolean searching, String text) {
			
			synchronized(updater) {
				
				if(futureValue != null) {
					
					futureValue = searching;
					futureString = text;
					
				}
				
				else {
					
					futureValue = searching;
					futureString = text;
					SwingUtilities.invokeLater(updater);
					
				}
				
			}
			
		}
		
	}
	
	private final static class Search {
		
		public final Object boardRevision;
		public final Tile[][] rows;
		public final Tile[][] columns;
		public final boolean[][] intersectionPoints;
		public final Hand hand;
		public final WordDictionary dictionary;
		
		public Search(DefaultBoardModel board) {
			
			boardRevision = board.boardRevision;
			rows = Arrays.copyOf(board.rows, board.rows.length);
			columns = Arrays.copyOf(board.columns, board.columns.length);
			intersectionPoints = Arrays.copyOf(board.intersectionPoints, board.intersectionPoints.length);
			hand = board.hand;
			dictionary = board.dictionary;
			
		}
		
		public boolean rowContainsIntersection(int y, int startPoint, int endPoint) {
			
			for(int i = startPoint; i < endPoint; ++i)
				if(intersectionPoints[i][y]) return true;
			
			return false;
			
		}
		
		public boolean columnContainsIntersection(int x, int startPoint, int endPoint) {
			
			for(int i = startPoint; i < endPoint; ++i)
				if(intersectionPoints[x][i]) return true;
			
			return false;
			
		}
		
	}
	
	private final class WorkerTask implements Runnable {
		
		private volatile Search search;
		
		private Hashtable<SlotMask, SlotMask> slotMasks;
		private Hashtable<SimpleSlot, SimpleSlot> simpleSlots;
		private Hashtable<ComplexSlot, AbstractMap.SimpleEntry<ComplexSlot, ArrayList<Position>>> complexSlots;
		
		public WorkerTask() {}
		
		public synchronized void doSearch(DefaultBoardModel board) {
			
			board.moveModel.currentStore = new ArrayList<Move>();
			board.moveModel.fireTableDataChanged();
			
			this.search = new Search(board);
			notifyAll();
			
		}
		
		private void computeSlots(Search search) {
			
			Hashtable<SlotMask, SlotMask> slotMasks = new Hashtable<SlotMask, SlotMask>();
			Hashtable<SimpleSlot, SimpleSlot> simpleSlots = new Hashtable<SimpleSlot, SimpleSlot>();
			Hashtable<ComplexSlot, AbstractMap.SimpleEntry<ComplexSlot, ArrayList<Position>>> complexSlots = new Hashtable<ComplexSlot, AbstractMap.SimpleEntry<ComplexSlot, ArrayList<Position>>>();
			
			// Go through rows
			for(int y = 0; y < search.rows.length; ++y) {
				
				Tile[] row = search.rows[y];
				int lastStart = row.length - 2;
				
				for(int startPoint = 0; startPoint <= lastStart; ++startPoint) {
					
					if(!isStartPoint(row, startPoint)) continue;
					int lastEnd = startPoint + 2;
					
					for(int endPoint = row.length; endPoint >= lastEnd; --endPoint) {
						
						if(!isEndPoint(row, endPoint)) continue;
						
						if(!search.rowContainsIntersection(y, startPoint, endPoint))
							break;
						
						SimpleSlot mainSlot = SimpleSlot.tryNew(row, startPoint, endPoint);
						
						// If we got null, there are no empty spaces, and decreasing the range certainly will not increase that
						if(mainSlot == null) break;
						
						// Intern the slot into the new pool
						mainSlot = intern(mainSlot, this.simpleSlots, simpleSlots);
						
						SimpleSlot[] attachedSlots = new SimpleSlot[mainSlot.getLength()];
						
						// Get attached slots
						for(int x = startPoint; x < endPoint; ++x) {
							
							// If we're not placing a tile here, no need to attach a slot
							if(row[x] != null) continue;
							
							// Get associated column
							Tile[] column = search.columns[x];
							
							int start = getFirstStartPointAtOrBefore(column, y);
							int end = getFirstEndPointAfter(column, y);
							
							// If there's nothing above and below, no need to attach a slot
							if(end - start < 2) continue;
							
							attachedSlots[x - startPoint] = intern(new SimpleSlot(column, start, end), this.simpleSlots, simpleSlots);
							
						}
						
						// Intern complex slot and add this position to its list
						AbstractMap.SimpleEntry<ComplexSlot, ArrayList<Position>> complexList =
							internList(new ComplexSlot(mainSlot, attachedSlots), this.complexSlots, complexSlots);
						
						complexList.getValue().add(new Position(startPoint, y, Position.HORIZONTAL));
						
					}
					
				}
				
			}
			
			// Go through columns
			for(int x = 0; x < search.columns.length; ++x) {
				
				Tile[] column = search.columns[x];
				int lastStart = column.length - 2;
				
				for(int startPoint = 0; startPoint <= lastStart; ++startPoint) {
					
					if(!isStartPoint(column, startPoint)) continue;
					int lastEnd = startPoint + 2;
					
					for(int endPoint = column.length; endPoint >= lastEnd; --endPoint) {
						
						if(!isEndPoint(column, endPoint)) continue;
						
						if(!search.columnContainsIntersection(x, startPoint, endPoint))
							break;
						
						SimpleSlot mainSlot = SimpleSlot.tryNew(column, startPoint, endPoint);
						
						// If we got null, there are no empty spaces, and decreasing the range certainly will not increase that
						if(mainSlot == null) break;
						
						// Intern the slot into the new pool
						mainSlot = intern(mainSlot, this.simpleSlots, simpleSlots);
						
						SimpleSlot[] attachedSlots = new SimpleSlot[mainSlot.getLength()];
						
						// Get attached slots
						for(int y = startPoint; y < endPoint; ++y) {
							
							// If we're not placing a tile here, no need to attach a slot
							if(column[y] != null) continue;
							
							// Get associated row
							Tile[] row = search.rows[y];
							
							int start = getFirstStartPointAtOrBefore(row, x);
							int end = getFirstEndPointAfter(row, x);
							
							// If there's nothing left and right, no need to attach a slot
							if(end - start < 2) continue;
							
							attachedSlots[y - startPoint] = intern(new SimpleSlot(row, start, end), this.simpleSlots, simpleSlots);
							
						}
						
						// Intern complex slot and add this position to its list
						AbstractMap.SimpleEntry<ComplexSlot, ArrayList<Position>> complexList =
							internList(new ComplexSlot(mainSlot, attachedSlots), this.complexSlots, complexSlots);
						
						complexList.getValue().add(new Position(x, startPoint, Position.VERTICAL));
						
					}
					
				}
				
			}
			
			// Intern all the slot masks
			for(SimpleSlot slot : simpleSlots.keySet())
				slot.internSlotMask(this.slotMasks, slotMasks);
			
			this.slotMasks = slotMasks;
			this.simpleSlots = simpleSlots;
			this.complexSlots = complexSlots;
			
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public void run() {
			
			this.slotMasks = new Hashtable<SlotMask, SlotMask>();
			this.simpleSlots = new Hashtable<SimpleSlot, SimpleSlot>();
			this.complexSlots = new Hashtable<ComplexSlot, AbstractMap.SimpleEntry<ComplexSlot, ArrayList<Position>>>();
			
			Object boardRevision = null;
			Search search = null;
			
			mainLoop:
			for(;;) {
				
				progressBar.update(false, "");
				
				synchronized(this) {
					
					while(search == this.search) {
						
						try {
							this.wait();
						} catch(InterruptedException e) {}
						
					}
					
					search = this.search;
					
				}
				
				// If this is a null search
				if(search.hand == null) {
					
					// Update GUI with empty list of moves
					synchronized(this) {
						
						// If a new search has been assigned
						if(search != this.search) continue;
						
						// Notify GUI
						moveModel.update(new ArrayList<Move>());
						
						this.search = null;
						search = null;
						
						continue;
						
					}
					
				}
				
				progressBar.update(true, "Updating move list...");
				
				// If the board's contents were changed, recompute slots
				if(boardRevision != search.boardRevision) {
					
					computeSlots(search);
					boardRevision = search.boardRevision;
					
				}
				
				// Figure out moves
				ArrayList<Move> moves = new ArrayList<Move>();
				
				EnumSet<Tile>[][] singleTileMoves = new EnumSet[search.columns.length][];
				for(int i = 0; i < singleTileMoves.length; ++i) {
					
					singleTileMoves[i] = new EnumSet[search.rows.length];
					
					for(int j = 0; j < singleTileMoves[i].length; ++j)
						singleTileMoves[i][j] = EnumSet.noneOf(Tile.class);
					
				}
				
				for(AbstractMap.SimpleEntry<ComplexSlot, ArrayList<Position>> entry : complexSlots.values()) {
					
					// If a new search has been assigned
					if(search != this.search) continue mainLoop;
					
					ComplexSlot slot = entry.getKey();
					ArrayList<Position> positions = entry.getValue();
					
					List<UnpositionedMove> unpositionedMoves = slot.getPlayableMoves(search.dictionary, search.hand);
					
					for(UnpositionedMove unpositionedMove : unpositionedMoves)
						if(unpositionedMove.getTilesUsed() == 1) {
							
							int firstEmpty = unpositionedMove.getSlot().getMainSlot().getMask().getFirstEmpty();
							Tile tile = unpositionedMove.getTile(firstEmpty);
							
							for(int i = 0; i < positions.size(); ++i) {
								
								Position position = positions.get(i);
								
								// Check move
								int x = position.x;
								int y = position.y;
								
								if(position.orientation == Position.HORIZONTAL)
									x += firstEmpty;
								else // VERTICAL
									y += firstEmpty;
								
								if(singleTileMoves[x][y].add(tile))
									moves.add(unpositionedMove.position(DefaultBoardModel.this, position));
								
							}
							
						} else
							for(int i = 0; i < positions.size(); ++i)
								moves.add(unpositionedMove.position(DefaultBoardModel.this, positions.get(i)));
					
				}
				
				// If a new search has been assigned
				if(search != this.search) continue;
				
				// Update GUI with moves
				synchronized(this) {
					
					// If a new search has been assigned
					if(search != this.search) continue;
					
					// Notify GUI
					moveModel.update(moves);
					
					this.search = null;
					search = null;
					
				}
				
			}
			
		}
		
	}
	
}
