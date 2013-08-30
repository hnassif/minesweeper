package minesweeper.server;

import java.io.File; 
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/** 
 * A board representing the entire Game
 * Invariant: The board is represented as a 2D array that has to be a square and not null. 
 * Why Thread Safe ?
 * All the access methods are private synchronized methods, 
 * which have to acquire a lock on the board object.
 * Also the representation invariant is safe from any exposure 
 * since the observer methods return boolean or immutable (string) types.
 */

public class Board {
	
    private final BoardCell[][] board;
    /**
     * Creates a random board given its size
     * @param s Integer representing the size of the board
     */
    public Board(int s) {
        board = new BoardCell[s][s];
        for(int i = 0; i < board.length; i++) 
            for(int j = 0; j<board[i].length; j++) 
                board[i][j] = new BoardCell(); 
        SetAdjacentBombs();
        checkRepresentationInvariant();
    }
    
    public Board(File file) {
        Scanner currentLine;
        
        try 
        {
        	currentLine = new Scanner(file);
        } 
        catch (FileNotFoundException e) 
        {
            throw new RuntimeException("The file cannot be found");
        }
        
        int numberOfRows = 0;
        int firstNumberOfColumns=0;
        int numberOfColumns = 0;
       
        
        while(currentLine.hasNextLine()) {
            Scanner cellsPerLine = new Scanner(currentLine.nextLine());
            numberOfRows++;

            numberOfColumns = 0;
            while(cellsPerLine.hasNextInt()) {
                int number = cellsPerLine.nextInt();
                if(number != 0 && number != 1) {
                    throw new IllegalArgumentException("Some inputs are different than 1 and 0");
                }
                numberOfColumns++;
            }
           if (firstNumberOfColumns!=0 && firstNumberOfColumns!=numberOfColumns)
        	   throw new RuntimeException(" invalid row length ");
           else 
        	   firstNumberOfColumns=numberOfColumns;
        	   }
        if (numberOfRows!=numberOfColumns)
        	throw new RuntimeException(" different number of rows and columns");
        	
 
        int size = numberOfRows;
        
        currentLine.close();
        try {
            currentLine = new Scanner(file);
        } 
        catch (FileNotFoundException e) 
        {
            throw new RuntimeException("The File cannot be found");
        }        
               
        board = new BoardCell[size][size];
        for(int i = 0; i < size; i++) {
            for(int j = 0; j<size; j++) {
                board[i][j] = new BoardCell(currentLine.nextInt() == 1 ? true : false);
            }
        }       
        
        SetAdjacentBombs();
        checkRepresentationInvariant();
    }
    /**
     * Finds the neighbors of each cell on the board
     * Cell must have valid board coordinates and be within the board bounds
     * @param i Integer representing the x coordinate of the cell
     * @param j Integer representing the y coordinate of the cell
     * @return a List<BoardCell> of all the neighboring cells
     */
    private List<BoardCell> getNeighbors(int i, int j){
    	List<BoardCell> neighborsList = new ArrayList<BoardCell>();
        for(int k = i-1; k <= i+1; k++) 
            for(int l = j-1; l <= j+1; l++) 
            	if((k != i || l != j) && isWithinBoardBounds(k, l))
            		neighborsList.add(board[k][l]);
        return neighborsList;
    }
    private boolean neighborContainsBomb(int i, int j) {
    	for (BoardCell cell : getNeighbors(i,j))
    		if (cell.containsBomb())
    			return true;
        return false;
    }
    /**
     * Counts all the bombs adjacent to each cell on the board 
     * and sets the field numberOfAdjacentBombs for each cell on the board  
     */
      public synchronized void SetAdjacentBombs() {
        for(int i = 0; i < board.length; i++) 
            for(int j = 0; j<board[i].length; j++) {
            	int number = 0;
            	for (BoardCell cell : getNeighbors(i,j))
                    if(cell.containsBomb()) 
                                number++;
                board[i][j].setNumberOfAdjacentBombs(number);
            }
        
    }
                     


    /**
     * Removes the flag from a flagged cell
     * @param x Integer representing the x coordinate of the cell
     * @param y Integer representing the y coordinate of the cell
     */
    public synchronized void unflag(int x, int y) {
        if(isWithinBoardBounds(x,y) && board[x][y].isStatusFlagged()) 
            board[x][y].removeFlag();
        
    }
    /**
     * Set the status of a cell to Untouched
     * @param x Integer representing the x coordinate of the cell
     * @param y Integer representing the y coordinate of the cell
     */
    public synchronized void SetStatusToUntouched(int x, int y) {
        if(isWithinBoardBounds(x,y) && board[x][y].isStatusFlagged()) 
            board[x][y].SetStatusToUntouched();
        
    }
    /**
     * Set the status of a cell to flagged
     * @param x Integer representing the x coordinate of the cell
     * @param y Integer representing the y coordinate of the cell
     */
    public synchronized void SetStatusToFlagged(int x, int y) {
        if(isWithinBoardBounds(x,y) && board[x][y].isStatusUntouched()) 
            board[x][y].SetStatusToFlagged();
        
    }
    /**
     * Set the status of a cell to flagged
     * @param x Integer representing the x coordinate of the cell
     * @param y Integer representing the y coordinate of the cell
     */
    public synchronized String digCell(int x, int y) {
        if(!isWithinBoardBounds(x,y) || !isStatusUntouched(x,y)) 
            return "out of bounds / cannot be dug";
         
        if(!containsBomb(x,y)) 
        {
            uncoverUntouched(x,y);
            return "no bomb";
        } 
        else
        {
            removeBombFromCell(x,y);
            setStatusDug(x,y);
            SetAdjacentBombs();
                
            return MinesweeperServer.BOOM_MSG;
        } 

    }
    /**
     * Removes a bomb from a cell
     * @param x Integer representing the x coordinate of the cell
     * @param y Integer representing the y coordinate of the cell
     */
    private synchronized void removeBombFromCell(int x, int y) {
        if(isWithinBoardBounds(x,y)) 
            board[x][y].removeBomb();
        
    }
    /**
     * recursively dog all adjacent cells that do not hold bombs
     * @param x Integer representing the x coordinate of a cell
     * @param y Integer representing the y coordinate of a cell
     */
    private synchronized void uncoverUntouched(int x, int y) {
        if(isWithinBoardBounds(x,y) && isStatusUntouched(x,y)) 
            setStatusDug(x,y);
        
        else {
            return;
        }
       if(getBombCount(x,y) == 0) {            
          for(int i = x-1; i <= x+1; i++) {
                for(int j = y-1; j <= y+1; j++) {
                    if(i != x || j != y) {
                        uncoverUntouched(i,j);                     
                    }
                }
            }
        }
      }
    /**
     * @return an integer representing the number of adjacent bombs
     */
    synchronized int getBombCount(int x, int y) {
        if(!isWithinBoardBounds(x,y)) 
        	return -1;
        else 
            return board[x][y].getAdjacentBombs();
    }
    /**
     * Sets the status of a cell to Dug
     * @param x Integer representing the x coordinate of the cell
     * @param y Integer representing the y coordinate of the cell
     */
    private synchronized void setStatusDug(int x, int y) {
        if(isWithinBoardBounds(x,y)) 
            board[x][y].setStatusToDug();
        
    }
    /**
     * Checks whether or not the status of the cell is Untouched
     * @param x Integer representing the x coordinate of the cell
     * @param y Integer representing the y coordinate of the cell
     * @return boolean indicating whether the status of the cell is Untouched
     */
    private synchronized boolean isStatusUntouched(int x, int y) {
        if(!isWithinBoardBounds(x,y)) 
        	return false;
         else 
        	 return board[x][y].isStatusUntouched();
        
    }
    /**
     * @return a boolean representing whether or not the cell contains a bomb
     */
    private synchronized boolean containsBomb(int x, int y) {
        if(!isWithinBoardBounds(x,y)) 
        	return false;
         else 
        	 return board[x][y].containsBomb();
     }
    /**
     * Checks whether or not the cell is within the bounds of the board
     * @param x Integer representing the x coordinate of the cell
     * @param y Integer representing the y coordinate of the cell
     * @return boolean indicating whether the cell is within the bounds of the board
     */ 
   private synchronized boolean isWithinBoardBounds(int x, int y) {
	   if ((x>=0 && x<board.length) && (y>=0 && y<board[0].length) )
		   return true;
	   else
		   return false;
   }
    /**
     * Checks whether or not the status of the cell is Flagged
     * @param x Integer representing the x coordinate of the cell
     * @param y Integer representing the y coordinate of the cell
     * @return boolean indicating whether the status of the cell is Flagged
     */
    public synchronized boolean isStatusFlagged(int x, int y) {
        if(!isWithinBoardBounds(x,y)) 
        	return false;
       	else 
            return board[x][y].isStatusFlagged();
       
    }

   

   /**
    * Displays a board as a String            
    */
   public synchronized String toString() {
       String toReturn = "";
       for(int k = 0; k < board.length; k++) 
       {
       	
       	toReturn = toReturn + board[k][0] ;
           for(int l = 1; l<board[k].length; l++) 
           {
           	toReturn = toReturn + " " + board[k][l] ;
           }
           toReturn = toReturn + "\r\n";
       }
       
       return toReturn;
   }
   /**
    * Checks the representation invariant 
    */
   private synchronized void checkRepresentationInvariant() {
       assert board != null;
       assert board.length == board[0].length;
   }
}
