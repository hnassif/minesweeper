package minesweeper.server;

import static org.junit.Assert.*; 

import java.io.File;



import org.junit.Test;

public class ConcurrencyTest {

    
    public static final int ITERATIONS = 1000000;
    
    public class uncoveringThread implements Runnable {
        private Board b;
        
        public uncoveringThread(Board a) {
            this.b = a;
        }
        
        public void run() {
            b.digCell(0,0);
        }
    }
    
    public class boardSnapShot implements Runnable {
        private Board b;
        private String[] boardState;
        
        public boardSnapShot(String[] stringPasser, Board a) {
            this.boardState = stringPasser;
            this.b = a;
        }
        
        public void run() {
            boardState[0] = b.toString();
        }
    }
    
    public class digThread implements Runnable {
        private final Board b;
        
        public digThread(Board board) 
        {
            this.b = board;
        }
        
        public void run() {
            for(int i = 0; i < ITERATIONS; i++) 
                b.digCell(0,0);
           }
    }
    public class UntouchToFlagThread implements Runnable {
        private final Board b;
        
        public UntouchToFlagThread(Board board) 
        {
            this.b = board;
        }
        
        public void run() {
            for(int i = 0; i < ITERATIONS; i++) 
                synchronized(b) {
                    b.SetStatusToUntouched(0,0);
                    b.SetStatusToFlagged(0,0);
                }
            
        }
    }
    public class FlagThread implements Runnable {
        private final Board b;
        
        public FlagThread(Board board) 
        {
            this.b = board;
        }
        
        public void run() {
            for(int i = 0; i < ITERATIONS; i++) 
                synchronized(b) {
                    
                    b.SetStatusToFlagged(0,0);
                }
            
        }
    }
 

    /**
     * checks whether the actions on the cells are
     * truly atomic. If they are, then the cell should never be dug.
     * @throws InterruptedException
     */
    @Test
    public void concTest() throws InterruptedException {
        Board singleCell = new Board(1);
        singleCell.SetStatusToFlagged(0, 0);
        
        Thread FlaggingAndUnflagging = new Thread(new UntouchToFlagThread(singleCell));
        Thread digging = new Thread(new digThread(singleCell));

        FlaggingAndUnflagging.start();
        digging.start();
        
        FlaggingAndUnflagging.join();
        digging.join();
        
        assertTrue(singleCell.isStatusFlagged(0, 0));
    }
    @Test 
    public void keepFlaggingTest() throws InterruptedException { 
        Board singleCell = new Board(1);
        singleCell.SetStatusToFlagged(0, 0);
        
        Thread alwaysFlag1 = new Thread(new UntouchToFlagThread(singleCell));
        Thread alwaysFlag2 = new Thread(new UntouchToFlagThread(singleCell));
        
        alwaysFlag1.start();
        alwaysFlag2.start();
        
       alwaysFlag1.join();
        alwaysFlag2.join();
        
        assertTrue(singleCell.isStatusFlagged(0, 0));
    }
    @Test
    public void uncoverRaceTest() throws InterruptedException {
        
        String[] StateOfBoard = new String[1]; 
        Board b;
        
        String Singleline1 = "-";
        String dashedBoard = "";
        String emptyBoard = "";
        String Singleline2 = " ";
        for(int i = 1; i < 35; i++) {
            Singleline1 = Singleline1 + " -";
            Singleline2 = Singleline2 + " " + " ";
        }
        Singleline1 = Singleline1 + "\r\n";
        Singleline2 = Singleline2 + "\r\n";
        
        for(int i = 0; i < 35; i++) {
            dashedBoard = dashedBoard + Singleline1;
            emptyBoard = emptyBoard + Singleline2;
        }
        
    
        for(int i = 0; i < 100; i++) {
            b = new Board(new File("sample_test/empty.txt"));
            StateOfBoard[0] = "";
            Thread first = new Thread(new boardSnapShot(StateOfBoard,b ));
            Thread second = new Thread(new uncoveringThread(b));
            
            first.start();
            second.start();
            first.join();
            second.join();
            boolean value = StateOfBoard[0].equals(emptyBoard) || StateOfBoard[0].equals(dashedBoard);
            
            assertTrue(value);
        }
    }
    

}