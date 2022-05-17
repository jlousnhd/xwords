package com.daretodebug.xwords;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private final DefaultBoard board;
	private final JTable moveTable;
	private final Box moveBox;
	
	public MainWindow(WordDictionary startingDictionary) {
		
		super("Xwords");
		
		JMenuItem openItem = new JMenuItem("Load Dictionary...", KeyEvent.VK_O);
		openItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String path = Program.promptForDictionary(MainWindow.this);
				if(path == null) return;
				
				WordDictionary dictionary = Program.loadDictionary(MainWindow.this, path);
				if(dictionary == null) return;
				
				board.setDictionary(dictionary);
				
			}
			
		});
		
		JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
		exitItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
			
		});
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(openItem);
		fileMenu.add(exitItem);
		
		JMenuItem usageItem = new JMenuItem("Usage", KeyEvent.VK_U);
		usageItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Program.showUsage(MainWindow.this);
			}
			
		});
		
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		helpMenu.add(usageItem);
		
		JMenuBar mainMenu = new JMenuBar();
		mainMenu.add(fileMenu);
		mainMenu.add(helpMenu);
		
		setJMenuBar(mainMenu);
		
		board = new DefaultBoard(startingDictionary);
		
		Box boardPane = Box.createHorizontalBox();
		boardPane.add(new BoardControl(board));
		boardPane.setBorder(BorderFactory.createTitledBorder("Board"));
		
		Box handPane = Box.createHorizontalBox();
		handPane.add(new JTextField(board.getHandModel(), "", 20) {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			public Dimension getMaximumSize() {
				
				Dimension max = super.getMaximumSize();
				max.height = getPreferredSize().height;
				
				return max;
				
			}
			
		});
		
		handPane.setBorder(BorderFactory.createTitledBorder("Hand (use '*' for blank)"));
		
		Box boardBox = Box.createVerticalBox();
		boardBox.add(boardPane);
		boardBox.add(handPane);
		
		JScrollPane movePane = new JScrollPane(moveTable = new JTable(board.getMoveModel()), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		moveTable.setAutoCreateRowSorter(true);
		moveTable.getRowSorter().toggleSortOrder(1);
		moveTable.getRowSorter().toggleSortOrder(1);
		moveTable.setPreferredScrollableViewportSize(new Dimension(300, 0));
		movePane.setMinimumSize(new Dimension(200, 0));
		
		moveBox = Box.createVerticalBox();
		moveBox.add(movePane);
		moveBox.add(board.getProgressBar());
		moveBox.setBorder(BorderFactory.createTitledBorder("Moves (0)"));
		
		board.getMoveModel().addTableModelListener(new TableModelListener() {
			
			@Override
			public void tableChanged(TableModelEvent e) {
				
				String moveCount = DecimalFormat.getIntegerInstance().format(board.getMoveModel().getRowCount());
				
				moveBox.setBorder(BorderFactory.createTitledBorder("Moves (" + moveCount + ")"));
				
			}
			
		});
		
		JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, boardBox, moveBox);
		mainPane.setResizeWeight(0.8);
		
		MoveListener listener = new MoveListener();
		moveTable.addMouseMotionListener(listener);
		moveTable.addMouseListener(listener);
		
		setContentPane(mainPane);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		
		Insets insets = getInsets();
		Dimension minimum = getMinimumSize();
		
		setMinimumSize(new Dimension(minimum.width + insets.left + insets.right, minimum.height + insets.top + insets.bottom));
		
	}
	
	private final class MoveListener implements MouseMotionListener, MouseListener {

		@Override
		public void mouseDragged(MouseEvent e) {}

		@Override
		public void mouseMoved(MouseEvent e) {
			
			Point point = e.getPoint();
			DefaultBoardModel.MoveModel model = board.getMoveModel();
			
			int index = moveTable.rowAtPoint(point);
			index = moveTable.getRowSorter().convertRowIndexToModel(index);
			
			if(index < 0 || index >= model.getRowCount())
				board.overlayMove(null);
			
			else board.overlayMove(model.getMove(index));
			
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			
			if(e.getButton() == 1 && e.getClickCount() == 2) {
				
				int index = moveTable.getSelectedRow();
				index = moveTable.getRowSorter().convertRowIndexToModel(index);
				
				DefaultBoardModel.MoveModel model = board.getMoveModel();
				
				if(index < 0 || index >= model.getRowCount())
					return;
				
				BoardModel.Move move = model.getMove(index);
				board.playMove(move);
				board.overlayMove(null);
				
			}
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {
			board.overlayMove(null);
		}

		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {}
		
	}
	
}
