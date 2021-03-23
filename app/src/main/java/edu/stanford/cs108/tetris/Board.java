// Board.java
package edu.stanford.cs108.tetris;
import java.util.*;

/**
 CS108 Tetris Board.
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearing.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 Does not do any drawing or have any idea of pixels. Instead,
 just represents the abstract 2-d board.
 */
public class Board	{
	// Some ivars are stubbed out for you:
	private int width;
	private int height;
	private boolean[][] grid;

	private boolean DEBUG = true;
	boolean committed;

	private int[] widths;
	private int[] heights;
	private int maxHeight;

	private boolean[][] gridPrev;
	private int[] widthsPrev;
	private int[] heightsPrev;
	private int maxHeightPrev;


	// Here a few trivial methods are provided:

	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	 */
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		grid = new boolean[width][height];
		widths = new int[height];
		heights = new int[width];
		maxHeight = 0;
		gridPrev = new boolean[width][height];
		widthsPrev = new int[height];
		heightsPrev = new int[width];
		maxHeightPrev = 0;

		committed = true;
		// YOUR CODE HERE
	}


	/**
	 Returns the width of the board in blocks.
	 */
	public int getWidth() {
		return width;
	}


	/**
	 Returns the height of the board in blocks.
	 */
	public int getHeight() {
		return height;
	}


	/**
	 Returns the max column height present in the board.
	 For an empty board this is 0.
	 */
	public int getMaxHeight() {
		return maxHeight; // YOUR CODE HERE
	}


	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	 */
	public void sanityCheck() {
		// YOUR CODE HERE
		if (DEBUG) {
			int[] widthsCheck = new int[getHeight()];
			int[] heightsCheck = new int[getWidth()];
			int maxHeightCheck = 0;
			for(int x=0; x<getWidth(); x++) {//x coordinate
				for(int y=0; y<getHeight(); y++) {//y coordinate
					if(grid[x][y]) {
						widthsCheck[y]++;
						heightsCheck[x] = y+1;
					}
				}
				maxHeightCheck = Math.max(maxHeightCheck,heightsCheck[x]);
			}
			if(!Arrays.equals(widthsCheck,widths) || !Arrays.equals(heightsCheck,heights) || maxHeight != maxHeightCheck) {
				throw new RuntimeException("description");
			}
		}else {
			return;
		}
	}


	/**
	 Given a piece and an x, returns the y
	 value where the piece would come to rest
	 if it were dropped straight down at that x.

	 <p>
	 Implementation: use the skirt and the col heights
	 to compute this fast -- O(skirt length).
	 */
	public int dropHeight(Piece piece, int x) {

		int dropH = 0;
		int[] skirt = piece.getSkirt();
		for(int i=0; i<piece.getWidth(); i++) {
			dropH = Math.max(getColumnHeight(x+i)-skirt[i], dropH);//avoid overlap
		}
		return dropH; // YOUR CODE HERE
	}

	/**
	 Returns the height of the given column --
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	 */
	public int getColumnHeight(int x) {
		return heights[x]; // YOUR CODE HERE
	}


	/**
	 Returns the number of filled blocks in
	 the given row.
	 */
	public int getRowWidth(int y) {
		return widths[y]; // YOUR CODE HERE
	}


	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	 */
	public boolean getGrid(int x, int y) {
		if(x>=getWidth() || y>=getHeight() ||x<0 || y<0) {
			return true;
		}

		return grid[x][y]; // YOUR CODE HERE
	}


	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;

	/**
	 Attempts to add the body of a piece to the board.
	 Copies the piece blocks into the board grid.
	 Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
	 for a regular placement that causes at least one row to be filled.

	 <p>Error cases:
	 A placement may fail in two ways. First, if part of the piece may falls out
	 of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 Or the placement may collide with existing blocks in the grid
	 in which case PLACE_BAD is returned.
	 In both error cases, the board may be left in an invalid
	 state. The client can use undo(), to recover the valid, pre-place state.
	 */
	public int place(Piece piece, int x, int y) {
		// flag !committed problem
		if (!committed) throw new RuntimeException("place commit problem");

		int result = PLACE_OK;
		TPoint[] body = piece.getBody();
		for(int i=0; i<body.length; i++) {
			int newX = x + body[i].x;
			int newY = y + body[i].y;
			//check if valid
			if(getGrid(newX, newY)) {
				if(newX>=getWidth() || newY>=getHeight() || newX<0 || newY<0) {
					return PLACE_OUT_BOUNDS;
				}
				return PLACE_BAD;

			}

			grid[newX][newY] = true;
			widths[newY]++;
			heights[newX] = Math.max(heights[newX], newY+1);
			maxHeight = Math.max(maxHeight, heights[newX]);
			if(getRowWidth(newY)==getWidth()) {
				result = PLACE_ROW_FILLED;
			}
			sanityCheck();
			committed = false;

			// YOUR CODE HERE
		}
		return result;
	}



	/**
	 Deletes rows that are filled all the way across, moving
	 things above down. Returns the number of rows cleared.
	 */
	public int clearRows() {
		//place first(set committed false) and then clearRows
		int rowsCleared = 0;
		for(int i=0; i<getMaxHeight(); i++) {
			if(getRowWidth(i) == getWidth()) {
				copyDown(i+1);
				rowsCleared++;
				i--;
			}
		}
		//update the info
		int maxH = 0;
		Arrays.fill(heights,0);
		for(int x=0; x<getWidth(); x++) {
//			heights[x]=0;
			for(int y=0; y<getMaxHeight(); y++) {
				if(getGrid(x,y)) {
					heights[x]=y+1;
				}
			}
			maxH = Math.max(maxH, getColumnHeight(x));
		}
		maxHeight = maxH;

		if(rowsCleared > 0) {
			committed = false;//need to commit/undo after clearing row
		}

		// YOUR CODE HERE
		sanityCheck();

		return rowsCleared;
	}
	private void copyDown(int fromY) {//copy down one by one
		for(int y=fromY; y<=getMaxHeight(); y++) {
			for(int x=0; x<getWidth(); x++) {
				if(y<getMaxHeight()) {
					grid[x][y-1] = getGrid(x,y);
				}else {
					grid[x][y-1] = false;
				}
			}
			widths[y-1] = (y==getMaxHeight()) ? 0:widths[y];// if the top row filled or not
		}
	}



	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	 See the overview docs.
	 */
	public void undo() {//backward
		if(committed) {//do nothing if already committed
			return;
		}

		//reverse the state
		boolean[][] gridTemp = grid;
		grid = gridPrev;
		gridPrev = gridTemp;

		int[] widthsTemp = widths;
		widths = widthsPrev;//change to prev_setting
		widthsPrev = widthsTemp;//reset prev_setting

		int[] heightsTemp = heights;
		heights = heightsPrev;
		heightsPrev = heightsTemp;

		int maxHeightTemp = maxHeight;
		maxHeight = maxHeightPrev;
		maxHeightPrev = maxHeightTemp;

		// YOUR CODE HERE

		backup();
		sanityCheck();
		committed = true;

	}

	/**
	 Puts the board in the committed state.
	 */
	public void commit() {
		if(committed) {
			return;
		}
		backup();//backup all the data
		committed = true;
	}
	private void backup() {
		for(int i=0; i<grid.length; i++) {
			System.arraycopy(grid[i],  0,  gridPrev[i],0, grid[i].length);
		}
		System.arraycopy(widths,  0,  widthsPrev, 0, widths.length);
		System.arraycopy(heights,  0,  heightsPrev, 0, heights.length);
		maxHeightPrev = maxHeight;
	}


	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility)

	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height-1; y>=0; y--) {
			buff.append('|');
			for (int x=0; x<width; x++) {
				if (getGrid(x,y)) buff.append('+');
				else buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x=0; x<width+2; x++) buff.append('-');
		return(buff.toString());
	}
}


