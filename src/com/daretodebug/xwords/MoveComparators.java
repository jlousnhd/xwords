package com.daretodebug.xwords;

import java.util.*;

import com.daretodebug.xwords.BoardModel.Move;

public class MoveComparators {
	
	private static final Comparator<BoardModel.Move> SCORE_COMPARATOR = new Comparator<BoardModel.Move>() {

		@Override
		public int compare(Move o1, Move o2) {
			
			int s1 = o1.getScore();
			int s2 = o2.getScore();
			
			if(s1 > s2) return -1;
			if(s1 < s2) return 1;
			
			return 0;
			
		}
		
	};
	
	private static final Comparator<BoardModel.Move> MAIN_WORD_COMPARATOR = new Comparator<BoardModel.Move>() {

		@Override
		public int compare(Move o1, Move o2) {
			return o1.getUnpositionedMove().getMainWord().compareTo(o2.getUnpositionedMove().getMainWord());
		}
		
	};
	
	private static final Comparator<BoardModel.Move> TILES_USED_COMPARATOR = new Comparator<BoardModel.Move>() {

		@Override
		public int compare(Move o1, Move o2) {
			
			int t1 = o1.getUnpositionedMove().getTilesUsed();
			int t2 = o2.getUnpositionedMove().getTilesUsed();
			
			if(t1 > t2) return -1;
			if(t1 < t2) return 1;
			
			return 0;
			
		}
		
	};
	
	private static final Comparator<BoardModel.Move> SCORE_THEN_TILES_USED_COMPARATOR = new Comparator<BoardModel.Move>() {

		@Override
		public int compare(Move o1, Move o2) {
			
			int s1 = o1.getScore();
			int s2 = o2.getScore();
			
			if(s1 > s2) return -1;
			if(s1 < s2) return 1;
			
			int t1 = o1.getUnpositionedMove().getTilesUsed();
			int t2 = o2.getUnpositionedMove().getTilesUsed();
			
			if(t1 > t2) return 1;
			if(t1 < t2) return -1;
			
			return 0;
			
		}
		
	};
	
	public static Comparator<BoardModel.Move> getScoreComparator() {
		return SCORE_COMPARATOR;
	}
	
	public static Comparator<BoardModel.Move> getMainWordComparator() {
		return MAIN_WORD_COMPARATOR;
	}
	
	public static Comparator<BoardModel.Move> getTilesUsedComparator() {
		return TILES_USED_COMPARATOR;
	}
	
	public static Comparator<BoardModel.Move> getScoreThenTilesUsedComparator() {
		return SCORE_THEN_TILES_USED_COMPARATOR;
	}
	
}
