package com.daretodebug.xwords;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;

public class BoardControl extends JLayeredPane {
	
	private static final long serialVersionUID = 1L;
	
	private static final Rectangle2D.Double RECTANGLE = new Rectangle2D.Double(0.0, 0.0, 1.0, 1.0);
	private static final float[] GRADIENT_TRANSITION = new float[] { 0.0f, 0.5f, 1.0f };
	
	private BoardRenderer renderer;
	private BoardModel model;
	private double tileSize;
	private double lineWidth;
	private Listener listener;
	private WeakBoardModelListener weakListener;
	private JTextField editField;
	private PlainDocument editDoc;
	private boolean isEditing;
	private Rectangle editingRect;
	private int editingX, editingY;
	
	public BoardControl(BoardModel model) {
		
		editDoc = new PlainDocument() {

			private static final long serialVersionUID = 1L;
			
			@Override
			public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
				
				if(this.getLength() != 0 || str.isEmpty()) return;
				
				char letter = str.charAt(0);
				if((letter < 'a' || letter > 'z') && (letter < 'A' || letter > 'Z')) return;
				
				str = str.substring(0, 1);
				
				super.insertString(offset, str, a);
				
			}
			
		};
		
		listener = new Listener();
		
		renderer = new BoardRenderer();
		renderer.addMouseListener(listener);
		
		editField = new JTextField(editDoc, null, 0);
		editField.addActionListener(listener);
		editField.addFocusListener(listener);
		editField.addKeyListener(listener);
		
		setModel(model);
		
		add(renderer, JLayeredPane.DEFAULT_LAYER);
		
		addComponentListener(listener);
		
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(model.getWidth() * 24, model.getHeight() * 24);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(model.getWidth() * 32, model.getHeight() * 32);
	}
	
	public void setModel(BoardModel model) {
		
		if(model == null) throw new NullPointerException();
		if(model == this.model) return;
		
		if(this.model != null) this.model.removeBoardModelListener(weakListener);
		model.addBoardModelListener(weakListener = new WeakBoardModelListener(listener, model));
		
		this.model = model;
		calculateDimensions();
		
		renderer.redrawAll();
		
	}
	
	private void startEdit(int x, int y) {
		
		if(isEditing) {
			
			repaint(editingRect);
			saveEdit();
			
		} else add(editField, JLayeredPane.MODAL_LAYER);
		
		int editX = (int) (tileSize * x) + renderer.getX();
		int editY = (int) (tileSize * y) + renderer.getY();
		
		editingRect = new Rectangle(editX, editY, (int) tileSize, (int) tileSize);
		
		Tile tile = model.getTileAt(x, y);
		
		String text;
		
		if(tile == null) text = "";
		else if(tile.isBlank()) text = Character.toString(tile.letter);
		else text = Character.toString(Character.toLowerCase(tile.letter));
		
		editField.setText(text);
		editField.setBounds(editingRect);
		editField.requestFocusInWindow();
		editField.setSelectionStart(0);
		editField.setSelectionEnd(text.length());
		editField.repaint();
		
		isEditing = true;
		editingX = x;
		editingY = y;
		
	}
	
	private void cancelEdit() {
		
		if(!isEditing) return;
		
		remove(editField);
		repaint(editingRect);
		
		isEditing = false;
		
	}
	
	private void finishEdit() {
		
		if(!isEditing) return;
		
		remove(editField);
		repaint(editingRect);
		
		isEditing = false;
		
		saveEdit();
		
	}
	
	private void saveEdit() {
		
		String text = editField.getText();
		
		if(text.isEmpty()) model.setTileAt(editingX, editingY, null);
		
		else {
			
			char letter = text.charAt(0);
			
			if(Character.isUpperCase(letter))
				model.setTileAt(editingX, editingY, Tile.getBlankLetter(letter));
			
			else model.setTileAt(editingX, editingY, Tile.getLetter(Character.toUpperCase(letter)));
			
		}
		
	}
	
	private void calculateDimensions() {
		
		int width = getWidth();
		int height = getHeight();
		
		int columns = model.getWidth();
		int rows = model.getHeight();
		
		double tileWidth = (width - 1) / (double) columns;
		double tileHeight = (height - 1) / (double) rows;
		
		if(tileWidth > tileHeight) tileSize = tileHeight;
		else tileSize = tileWidth;
		
		lineWidth = Math.min(0.5 / tileSize, 1.0);
		
		Rectangle boardRect = new Rectangle((int) Math.ceil(tileSize * columns) + 1, (int) Math.ceil(tileSize * rows) + 1);
		boardRect.x = (width - boardRect.width) / 2;
		boardRect.y = (height - boardRect.height) / 2;
		
		Rectangle oldRect = renderer.getBounds();
		renderer.setBounds(boardRect);
		
		if(!oldRect.getSize().equals(boardRect.getBounds().getSize())) {
			renderer.redrawAll();
		} else renderer.repaint();
		
	}
	
	private final class Listener implements ComponentListener, BoardModelListener, ActionListener, MouseListener, FocusListener, KeyListener {

		@Override
		public void componentHidden(ComponentEvent arg0) {}

		@Override
		public void componentMoved(ComponentEvent arg0) {}

		@Override
		public void componentResized(ComponentEvent arg0) {
			
			finishEdit();
			calculateDimensions();
			
		}

		@Override
		public void componentShown(ComponentEvent arg0) {}

		@Override
		public void tileChanged(BoardModelEvent e) {}

		@Override
		public void tileAppearanceChanged(BoardModelEvent e) {
			
			TileCoords coords = e.getTileCoords();
			renderer.redraw(coords.x, coords.y);
			
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			finishEdit();
		}
		
		@Override
		public void mouseClicked(MouseEvent arg0) {
			
			if(arg0.getButton() != 1) return;
			
			Point pt = arg0.getPoint();
			
			int x = (int) Math.floor(pt.x / tileSize);
			int y = (int) Math.floor(pt.y / tileSize);
			
			if(x < 0 || x >= model.getWidth()) return;
			if(y < 0 || y >= model.getHeight()) return;
			
			startEdit(x, y);
			
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {}

		@Override
		public void mouseExited(MouseEvent arg0) {}

		@Override
		public void mousePressed(MouseEvent arg0) {}

		@Override
		public void mouseReleased(MouseEvent arg0) {}

		@Override
		public void focusGained(FocusEvent arg0) {}

		@Override
		public void focusLost(FocusEvent arg0) {
			finishEdit();
		}

		@Override
		public void keyPressed(KeyEvent e) {}

		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ESCAPE) cancelEdit();
		}

		@Override
		public void keyTyped(KeyEvent e) {}
		
	}
	
	private final class BoardRenderer extends JComponent {
		
		private static final long serialVersionUID = 1L;
		
		private static final int MAX_PADDING = 64;
		private static final int START_PADDING = MAX_PADDING / 2;
		
		private HashSet<TileCoords> redraw;
		private boolean redrawAll;
		private BufferedImage buffer;
		private Color lineColor;
		
		private Font letterFont;
		private Font valueFont;
		
		public BoardRenderer() {
			
			setDoubleBuffered(false);
			
			lineColor = new Color(0xb1, 0xae, 0xa2);
			
			Font baseFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
			
			letterFont = baseFont.deriveFont(0.6f);
			valueFont = baseFont.deriveFont(0.3f);
			
			redraw = new HashSet<TileCoords>();
			
		}
		
		public void redrawAll() {
			
			redraw.clear();
			redrawAll = true;
			repaint();
			
		}
		
		public void redraw(int x, int y) {
			
			if(redrawAll) return;
			
			redraw.add(new TileCoords(x, y));
			
			int repaintX = (int) Math.floor(tileSize * x);
			int repaintY = (int) Math.floor(tileSize * y);
			
			int repaintWidth = (int) Math.ceil(tileSize * (x + 1)) - repaintX;
			int repaintHeight = (int) Math.ceil(tileSize * (y + 1)) - repaintY;
			
			repaint(repaintX, repaintY, repaintWidth, repaintHeight);
			
		}
		
		private void prepareBuffer() {
			
			int width = getWidth();
			int height = getHeight();
			
			if(buffer == null || buffer.getWidth() < width || buffer.getHeight() < height || buffer.getWidth() > width + MAX_PADDING || buffer.getHeight() > height + MAX_PADDING) {
				
				buffer = (BufferedImage) createImage(width + START_PADDING, height + START_PADDING);
				
				redraw.clear();
				redrawAll = true;
				
			}
			
			if(redrawAll) {
				
				Graphics2D g = buffer.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
				
				g.clearRect(0, 0, width, height);
				
				int columns = model.getWidth();
				int rows = model.getHeight();
				
				for(int y = 0; y < rows; ++y)
					for(int x = 0; x < columns; ++x) 
						renderTile(g, x, y);
				
				redrawAll = false;
				redraw.clear();
				
				g.dispose();
				
			} else if(redraw.size() > 0) {
				
				Graphics2D g = buffer.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
				
				for(TileCoords coords : redraw)
					renderTile(g, coords.x, coords.y);
				
				redraw.clear();
				
				g.dispose();
				
			}
			
		}
		
		private void renderTile(Graphics2D g, int x, int y) {
			
			AffineTransform oldTransform = g.getTransform();
			
			AffineTransform transform = new AffineTransform();
			transform.translate(x * tileSize, y * tileSize);
			transform.scale(tileSize, tileSize);
			
			g.transform(transform);
			
			BoardTileData data = model.getTileDataAt(x, y);
			
			Color fg;
			Color bg = data.getColor();
			
			Tile tile;
			Tile overlay = model.getTileOverlayAt(x, y);
			
			if(overlay != null) {
				
				tile = overlay;
				fg = bg;
				bg = Color.BLACK;
				
			} else {
				
				tile = model.getTileAt(x, y);
				fg = Color.BLACK;
				
			}
			
			Color[] colorTransition = new Color[3];
			
			colorTransition[0] = new Color(Math.min(bg.getRed() + 26, 255), Math.min(bg.getGreen() + 26, 255), Math.min(bg.getBlue() + 26, 255));
			colorTransition[1] = bg;
			colorTransition[2] = new Color(Math.max(bg.getRed() - 26, 0), Math.max(bg.getGreen() - 26, 0), Math.max(bg.getBlue() - 26, 0));
			
			LinearGradientPaint paint = new LinearGradientPaint(0.0f, 0.0f, 1.0f, 1.0f, GRADIENT_TRANSITION, colorTransition);
			
			g.setPaint(paint);
			g.fill(RECTANGLE);
			
			g.setPaint(lineColor);
			g.setStroke(new BasicStroke((float) lineWidth));
			g.draw(RECTANGLE);
			
			if(tile != null) {
				
				g.setPaint(fg);
				
				String letter = Character.toString(tile.letter);
				String value = Integer.toString(tile.value);
				
				FontRenderContext frc = g.getFontRenderContext();
				GlyphVector letterVector = letterFont.createGlyphVector(frc, letter);
				GlyphVector valueVector = valueFont.createGlyphVector(frc, value);
				
				Rectangle2D letterBounds = letterVector.getVisualBounds();
				Rectangle2D valueBounds = valueVector.getVisualBounds();
				
				double letterHeight = letterBounds.getHeight();
				double valueHeight = valueBounds.getHeight();
				
				double letterWidth = letterBounds.getWidth();
				double valueWidth = valueBounds.getWidth();
				
				double emptyWidth = 1.0 - letterWidth - valueWidth;
				
				double letterX = emptyWidth * (2.0 / 5.0);
				double letterY = 1.0 - (1.0 - letterHeight) * 0.5;
				
				double valueX = letterX + letterWidth + emptyWidth * (1.0 / 5.0);
				double valueY = 1.0 - (1.0 - valueHeight) * 0.25;
				
				g.drawGlyphVector(letterVector, (float) letterX, (float) letterY);
				g.drawGlyphVector(valueVector, (float) valueX, (float) valueY);
				
			}
			
			g.setTransform(oldTransform);
			
		}
		
		@Override
		public void paintComponent(Graphics g) {
			
			prepareBuffer();
			g.drawImage(buffer, 0, 0, null);
			
		}
		
	}
	
}
