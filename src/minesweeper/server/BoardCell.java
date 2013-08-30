package minesweeper.server;

/**
 * Class representing a single cell on the board. 
 * Representation invariant : Each cell has a boolean field 
 * determining whether or not it holds a bomb as well as
 * an enum type determining its status
 * This class is thread safe because all methods are synchronized and
 * there is no representation exposure (because mutable fields cannot 
 * be accessed from outside of the class) 
 *
 */
public class BoardCell {
	
	private boolean containsBomb;
	private Status status;
	private int numberOfAdjacentBombs;
	/**
	 * Declares the different types of tokens for the cell Status
	 */
	private static enum Status { 
		FLAGGED,DUG,UNTOUCHED
		}
    /**
     * Randomly constructs an instance of BoardCell by adding a 
     * bomb with probability 1/4 and not adding one with probability 3/4
     */
     public BoardCell() { 
		if (Math.random() > 0.25) 
			this.containsBomb=false;
		else 
			this.containsBomb=true;
		
		this.status=Status.UNTOUCHED;
		this.numberOfAdjacentBombs=0;
		checkRepresentationInvariant();
	}
	
    /**
     * Deterministically constructs an instance of BoardCell given
     * a boolean parameter 
     * @param isBomb : boolean that determines whether or not the cell 
     * contains a bomb
     */
	public BoardCell(boolean isBomb) { 
		this.containsBomb=isBomb;
		this.status=Status.UNTOUCHED;
		this.numberOfAdjacentBombs=0;
		checkRepresentationInvariant();
	}
	/**
	 * Changes the status of a cell to Dug
	 */
    public synchronized void setStatusToDug() {
        status = Status.DUG;
        checkRepresentationInvariant();
    }
	/**
	 * Checks if the cell is in the Flagged state
	 * @return boolean determining whether or not the cell is in the Flagged state
	 */
    public synchronized boolean isStatusFlagged() {
        return (status == Status.FLAGGED);
    }
	/**
	 * Changes the status of a cell to flagged
	 * only if it was in the untouched status before
	 */
   public synchronized void SetStatusToFlagged() {
        if(status == Status.UNTOUCHED) 
            status = Status.FLAGGED;
        checkRepresentationInvariant();
        }

	/**
	 * Counts the number of adjacent bombs to a cell
	 * @return integer representing the number of adjacent bombs to a cell
	 */
    public synchronized int getAdjacentBombs() {
        return numberOfAdjacentBombs;
    }
	/**
	 * Checks if the cell contains a bomb
	 * @return boolean determining whether or not the cell contains a bomb
	 */
    public synchronized boolean containsBomb() {
        return containsBomb;
    }

    /**
	 * Checks if the cell is in the Untouched state
	 * @return boolean determining whether or not the cell is in the Untouched state
	 */
    public synchronized boolean isStatusUntouched() {
        return (status == Status.UNTOUCHED);
    }
	/**
	 * Changes the status of a cell to Untouched
	 * only if it was in the flagged status before
	 */
    public synchronized void SetStatusToUntouched() {
        if(status == Status.FLAGGED) 
            status = Status.UNTOUCHED;
        checkRepresentationInvariant();
    }

    /**
     * Changes the status of a cell from flagged to unflagged
     */
    public synchronized void removeFlag() {
        status = Status.UNTOUCHED;
        checkRepresentationInvariant();
    }
	/**
	 * sets the number of adjacent bombs to the cell
	*/
    public synchronized void setNumberOfAdjacentBombs(int number) {
        this.numberOfAdjacentBombs = number;
        checkRepresentationInvariant();
    }
	/**
	 * Removes the bomb from the cell
	 */
    public synchronized void removeBomb() {
    	if (containsBomb)
    		containsBomb = false;
        checkRepresentationInvariant();
    }
    
	/**
	 * Displays the status of a single cell
	 * @return String representing the cell's status
	 */
    public synchronized String toString() {
    	if(status == Status.FLAGGED) 
    		{ return "F"; }
        else  if(status == Status.UNTOUCHED)  
        	{ return "-"; }
        else {
            if(numberOfAdjacentBombs == 0) 
                return " ";
             else 
                return "" + numberOfAdjacentBombs;
          
           }
    }
    /**
	 * Checks the representation invariant 
	 */
    private synchronized void checkRepresentationInvariant() {
        assert status != null;
        assert (numberOfAdjacentBombs >=0 && numberOfAdjacentBombs <= 8);
    }
}

