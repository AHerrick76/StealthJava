package test;

import org.junit.jupiter.api.*;

import stealth_game.LevelReader;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * You can use this file (and others) to test your
 * implementation.
 */

public class LevelReaderTest {

    @Test
    public void test() {
        assertNotEquals("CIS 120", "CIS 160");
    }
    
    // test basic ability to load & parse file without error
    @Test
    public void loadSimpleTest() {
        LevelReader lr = new LevelReader("files/archive/test_Level2.csv");
        assertEquals(lr.getHeight(), 5);
        assertEquals(lr.getWidth(), 5);
        
        LinkedList<String[]> contents = lr.getBoardList();
        assertEquals(contents.get(1)[1], "G");
    }    
    
    // test crashes when provided with null / invalid file paths
    @Test
    public void loadNullFilepath() {
        assertThrows(IllegalArgumentException.class, () -> 
            new LevelReader(null));
    }    
    
    @Test
    public void loadWrongFilepath() {
        assertThrows(IllegalArgumentException.class, () -> 
            new LevelReader("files/archive/LevelNotExist.csv"));
    }    
    
    // test crashes when provided with file with inconsistent widths
    @Test
    public void loadNonRectangular() {
        assertThrows(IllegalArgumentException.class, () -> 
            new LevelReader("files/archive/test_Level_NonRectangular.csv"));
    }    
}
