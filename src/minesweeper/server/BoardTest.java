package minesweeper.server;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class BoardTest {

    @Test (expected = RuntimeException.class)
    public void invalidFileWithExtraColumnTest() {
        new Board(new File("sample_test/FileWithExtraColumn.txt"));
    }
    
    @Test (expected = RuntimeException.class)
    public void invalidFileWithExtraRowTest() {
        new Board(new File("sample_test/FileWithRowColumn.txt"));
    }
    
    @Test (expected = RuntimeException.class)
    public void invalidFileWithInvalidNumbersTest() {
        new Board(new File("sample_test/invalidNumbers.txt"));
    }
    @Test (expected = RuntimeException.class)
    public void ImaginaryFileTest() {
        new Board(new File("sample_test/absent.txt"));
    }

    
    @Test
    public void flagTest() {
        Board b = new Board(new File("sample_test/twoByTwo.txt"));
        b.SetStatusToFlagged(1,0);
        String output = "- -\r\nF -\r\n";
        assertEquals(output, b.toString());
    }
    
    @Test
    public void SelfLoopTest() {
        Board b = new Board(new File("sample_test/twoByTwo.txt"));
        b.SetStatusToUntouched(1, 1);
        String output = "- -\r\n- -\r\n";
        assertEquals(output, b.toString());
    }
    
    @Test
    public void digCellTest() {
        Board b = new Board(new File("sample_test/twoByTwo.txt"));
        b.digCell(0,1);
        String output = "- 2\r\n- -\r\n";
        assertEquals(output, b.toString());
    }

    
    @Test
    public void initialTest() {
        Board b = new Board(new File("sample_test/twoByTwo.txt"));
        String output = "- -\r\n- -\r\n";
        assertEquals(output, b.toString());
    }
    
    @Test
    public void SetStatusToUntouchedTest() {
        Board b = new Board(new File("sample_test/twoByTwo.txt"));
        b.SetStatusToFlagged(0,0);
        b.SetStatusToUntouched(0, 0);
        String output = "- -\r\n- -\r\n";
        assertEquals(output, b.toString());
    }
  
  
    
    @Test
    public void digThenFlagTest() {
        Board b = new Board(new File("sample_test/twoByTwo.txt"));
        b.digCell(0,0);
        b.SetStatusToFlagged(0,0);
        String output = "2 -\r\n- -\r\n";
        assertEquals(output, b.toString());
    }
    
    @Test
    public void digCellBombTest() {
        Board b = new Board(new File("sample_test/twoByTwo.txt"));
        assertEquals(MinesweeperServer.BOOM_MSG, b.digCell(1,0));
    }

    @Test
    public void recursiveUncoverTest() {
        Board b = new Board(new File("sample_test/fourByFour.txt"));
        b.digCell(2,2);
        String output = "- - - - -\r\n- 5 3 5 -\r\n- 3   3 -\r\n- 5 3 5 -\r\n- - - - -\r\n";
        assertEquals(output, b.toString());
    }
    @Test
    public void testTest() {
        Board b = new Board(new File("sample_test/test.txt"));
        //b.digCell(3,1);
       // System.out.print(b.neighborContainsBomb(3,1));
        System.out.print(b.getBombCount(3,1));
        
       
    }
    @Test
    public void repeatFlaggingTest() {
    	 Board singleCell = new Board(1);
    	 for (int i=0; i<10; i++)
         singleCell.SetStatusToFlagged(0, 0);
    	 assertTrue(singleCell.isStatusFlagged(0, 0));
        
       
    }
}
