package com.daretodebug.xwords;

import java.awt.*;
import java.io.*;
import java.util.prefs.*;
import javax.swing.*;

public class Program {
	
	public static String promptForDictionary(Component parent) {
		
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Choose a Dictionary File...");
		
		int result = chooser.showOpenDialog(parent);
		
		if(result != JFileChooser.APPROVE_OPTION) return null;
		
		return chooser.getSelectedFile().getAbsolutePath();
		
	}
	
	public static String getSavedDictionaryPath() {
		
		Preferences pref = Preferences.userNodeForPackage(Program.class);
		
		return pref.get("dictionary", null);
		
	}
	
	public static void saveDictionaryPath(String path) {
		
		Preferences pref = Preferences.userNodeForPackage(Program.class);
		
		pref.put("dictionary", path);
		
	}
	
	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				swingMain();
			}
			
		});
		
	}
	
	public static String getStartingPath() {
		
		String path = getSavedDictionaryPath();
		if(path != null) return path;
		
		showUsage(null);
		return promptForDictionary(null);
		
	}
	
	public static WordDictionary loadDictionary(Component parent, String path) {
		
		for(;;) {
			
			FileInputStream fis = null;
			
			try {
				
				fis = new FileInputStream(path);
				InputStreamReader reader = new InputStreamReader(fis);
				
				WordDictionary dictionary = new WordDictionary(new ReaderLineIterator(reader));
				saveDictionaryPath(path);
				return dictionary;
				
			} catch(IOException e) {
				
				int result = JOptionPane.showConfirmDialog(parent, "Unable to read \"" + path + "\":\n\n" + e.getMessage() + "\n\nTry another file?", "Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
				
				if(result != JOptionPane.YES_OPTION) return null;
				path = promptForDictionary(parent);
				
				if(path == null) return null;
				
			} catch(RuntimeException e) {
				
				Throwable cause = e.getCause();
				
				if(cause instanceof IOException) {
					
					int result = JOptionPane.showConfirmDialog(parent, "Unable to read \"" + path + "\":\n\n" + cause.getMessage() + "\n\nTry another file?", "Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
					
					if(result != JOptionPane.YES_OPTION) return null;
					path = promptForDictionary(parent);
					
					if(path == null) return null;
					
				} else {
					
					int result = JOptionPane.showConfirmDialog(parent, "Unable to load \"" + path + "\":\n\n" + e.getMessage() + "\n\nTry another file?", "Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
					
					if(result != JOptionPane.YES_OPTION) return null;
					path = promptForDictionary(parent);
					
					if(path == null) return null;
					
				}
				
			} finally {
				
				if(fis != null) try {
					fis.close();
				} catch(Exception e) {}
				
			}
			
		}
		
	}
	
	public static void swingMain() {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
		
		String path = getStartingPath();
		
		if(path == null) {
			
			JOptionPane.showMessageDialog(null, "Xwords will not run without a dictionary.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
			
		}
		
		WordDictionary dictionary = loadDictionary(null, path);
		
		if(dictionary == null) {
			
			JOptionPane.showMessageDialog(null, "Xwords will not run without a dictionary.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
			
		}
		
		MainWindow frame = new MainWindow(dictionary);
		
		frame.setVisible(true);
		
	}
	
	public static void showUsage(Component parent) {
		
		JOptionPane.showMessageDialog(
			parent,
			"Dictionary Files:\n" +
			"Dictionaries must be simple text files with one word per line and no extraneous\n" +
			"characters.  Case is ignored.\n\n" +
			"Board Usage:\n" +
			"To set a tile on the board, simply click on the board in the space where you\n" +
			"would like to change a tile.  Enter normal tiles as lower case and blank tiles\n" +
			"as upper case.\n\n" +
			"Hand Entry:\n" +
			"Enter the tiles in your hand into the 'Hand' text field.  Blank tiles are\n" +
			"represented by a wildcard (*) character.\n\n" +
			"Move List:\n" +
			"The list automatically updates whenever the hand, board or dictionary are\n" +
			"changed.  Hover the cursor over any move on the list to see it previewed on the\n" +
			"board.  For convenience, you can double-click any move on the list to place it\n" +
			"on the board.  By default, moves are sorted in descending order of the score\n" +
			"they would get, but order can be changed by clicking the headers above the list.\n\n" +
			"Access this help screen at any time by clicking Help -> Usage on the menu.",
			"Xwords Usage",
			JOptionPane.INFORMATION_MESSAGE
		);
		
	}

}
