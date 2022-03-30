package test;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import stealth_game.*;

/**
 * Contains tests for GameBoard, movement, etc
 */

public class GameBoardTest {
    
    // test basic ability to load & parse file without error
    @Test
    public void loadSimpleTest() {
        GameBoard gb = new GameBoard("files/archive/test_Level1.csv", false, 0);
        
        // check contents of some squares
        assertEquals(gb.getElement(2, 3), "P");
        assertEquals(gb.getElement(1, 1), "G");
        assertEquals(gb.getElement(0, 0), "E");
        
        // check player location
        assertEquals(gb.getPlayerLocation(), new Point(2, 3));
    }    
    
    // test simple player movement
    @Test
    public void simpleMovement() {
        GameBoard gb = new GameBoard("files/archive/test_Level1.csv", false, 0);
        Player player = gb.getPlayer();
        
        assertEquals(new Point(2, 3), gb.getPlayerLocation());
        gb.moveObject(player, Direction.UP);
        assertEquals(gb.getElement(2, 3), "E");
        assertEquals(gb.getElement(2, 2), "P");
        assertEquals(new Point(2, 2), gb.getPlayerLocation());
        gb.moveObject(player, Direction.LEFT);
        assertEquals(new Point(1, 2), gb.getPlayerLocation());
    }    
    
    // test edge player movement
    @Test
    public void edgeMovement() {
        GameBoard gb = new GameBoard("files/archive/test_Level1.csv", false, 0);
        Player player = gb.getPlayer();
        assertEquals(new Point(2, 3), gb.getPlayerLocation());
        gb.moveObject(player, Direction.RIGHT);
        assertEquals(new Point(3, 3), gb.getPlayerLocation());
        gb.moveObject(player, Direction.RIGHT);
        assertEquals(new Point(4, 3), gb.getPlayerLocation());
        gb.moveObject(player, Direction.RIGHT);
        assertEquals(new Point(4, 3), gb.getPlayerLocation());
    }    
    
    // test wall player movement
    @Test
    public void wallMovement() {
        GameBoard gb = new GameBoard("files/archive/test_Level1.csv", false, 0);
        Player player = gb.getPlayer();
        assertEquals(new Point(2, 3), gb.getPlayerLocation());
        gb.moveObject(player, Direction.DOWN);
        assertEquals(new Point(2, 3), gb.getPlayerLocation());
    }   
    
    // test player reaches goal
    @Test
    public void goalMovement() {
        GameBoard gb = new GameBoard("files/archive/test_Level1.csv", false, 0);
        Player player = gb.getPlayer();
        assertEquals(new Point(2, 3), gb.getPlayerLocation());
        assertFalse(gb.isVictory());
        gb.moveObject(player, Direction.UP);
        gb.moveObject(player, Direction.UP);
        gb.moveObject(player, Direction.LEFT);
        assertEquals(new Point(1, 1), gb.getPlayerLocation());
        assertTrue(gb.isVictory());
    }    
    
    // test movement in larger level
    @Test
    public void largeMovement() {
        GameBoard gb = new GameBoard("files/archive/test_Level2.csv", false, 0);
        Player player = gb.getPlayer();
        assertEquals(new Point(2, 0), gb.getPlayerLocation());
        gb.moveObject(player, Direction.DOWN);
        gb.moveObject(player, Direction.DOWN);
        gb.moveObject(player, Direction.DOWN);
        gb.moveObject(player, Direction.DOWN);
        assertEquals(new Point(2, 4), gb.getPlayerLocation());
    }    
    
    // test loading several guards
    @Test
    public void testGuardLoad() {
        GameBoard gb = new GameBoard("files/archive/test_ManyObjects.csv", false, 0);
        Player player = gb.getPlayer();
        HashSet<Guard> guards = gb.getGuards();
        assertEquals(6, guards.size());
        
        // move the player several times to ensure movement executes without crash
        gb.moveObject(player, Direction.DOWN);
        gb.moveObject(player, Direction.UP);
        gb.moveObject(player, Direction.RIGHT);
    }  
    
    // test guard detection
    @Test
    public void testDetection() {
        GameBoard gb = new GameBoard("files/archive/test_DetectionTest.csv", false, 0);
        Player player = gb.getPlayer();
        
        // ensure player lost game
        assertFalse(gb.isDefeat());
        gb.moveObject(player, Direction.RIGHT);
        assertTrue(gb.isDefeat());
        
        // ensure guard moved to expected position
        assertEquals("M", gb.getElement(1, 1));
        
        // ensure detection consists of proper number of squares
        assertEquals(4, gb.getDetectionZone().size());
    }  
    
    // test Random guard impossible move
    @Test
    public void testImpossibleMove() {
        GameBoard gb = new GameBoard("files/archive/test_ImpossibleMoveTest.csv", false, 0);
        Player player = gb.getPlayer();
        
        // move player to verify no infinite loop occurs
        gb.moveObject(player, Direction.RIGHT);
        
        // ensure guard remained in expected position
        assertEquals("M", gb.getElement(4, 2));
    } 
    
    
    // test RouteGuard collision
    @Test
    public void testRouteCollision() {
        GameBoard gb = new GameBoard("files/archive/test_CollisionTest.csv", false, 0);
        Player player = gb.getPlayer();
        
        // move player into path of guard & ensure guard didn't move
        gb.moveObject(player, Direction.RIGHT);
        assertEquals("M", gb.getElement(1, 2));
        
        // move player out of the way and ensure guard correctly moves up instead of right
        gb.moveObject(player, Direction.RIGHT);
        assertEquals("M", gb.getElement(1, 1));
    }  
    
    // test chaseGuard movement
    @Test
    public void testChaseGuard() {
        GameBoard gb = new GameBoard("files/archive/test_ChaseTest.csv", false, 0);
        Player player = gb.getPlayer();
        assertEquals("M", gb.getElement(0, 8));
        gb.moveObject(player, Direction.RIGHT);
        assertEquals("M", gb.getElement(0, 7));
        gb.moveObject(player, Direction.RIGHT);
        assertEquals("M", gb.getElement(0, 6));
    }   
}
