package test;

import org.junit.jupiter.api.*;

import stealth_game.*;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Point;
import java.text.StringCharacterIterator;

public class GuardTest {
    
    private Boolean forwardMode;
    
    // retrieves the next character
    private Character invokeIter(StringCharacterIterator myIter) {
        Integer currentIndex = myIter.getIndex();
        Character returnChar = myIter.current();
        if (forwardMode) {
            // check if end of iterator has been reached, and swap directions if so
            if (currentIndex.equals(myIter.getEndIndex() - 1)) {
                forwardMode = false;
            } else {
                myIter.next();
            }
        } else {
            if (currentIndex.equals(myIter.getBeginIndex())) {
                forwardMode = true;
            } else {
                myIter.previous();
            }
        }
        return returnChar;
    }
    
    // test StringCharacterIterator functionality
    @Test
    public void testStringCharacterIterator() {
        String exRoute = "DRL";
        StringCharacterIterator myIter = new StringCharacterIterator(exRoute);
        forwardMode = true;
        
        assertEquals(myIter.getIndex(), 0);
        assertEquals(invokeIter(myIter), 'D');
        assertEquals(invokeIter(myIter), 'R');
        assertEquals(invokeIter(myIter), 'L');
        assertEquals(invokeIter(myIter), 'L');
        assertEquals(invokeIter(myIter), 'R');
        assertEquals(invokeIter(myIter), 'D');
        assertEquals(invokeIter(myIter), 'D');
    }    
    
    // test ability of guard to follow basic route
    @Test
    public void testGuardMovement() {
        RouteGuard guard = new RouteGuard(new Point(5, 5), "PM;C1;UDLR");
        
        // move along route
        assertEquals(Direction.UP, guard.getMoveDirection());
        guard.moveSucceeded();
        assertEquals(Direction.DOWN, guard.getMoveDirection());
        guard.moveSucceeded();
        assertEquals(Direction.LEFT, guard.getMoveDirection());
        guard.moveSucceeded();
        assertEquals(Direction.RIGHT, guard.getMoveDirection());
        guard.moveSucceeded();
        
        // move backwards along route
        assertEquals(Direction.LEFT, guard.getMoveDirection());
        guard.moveSucceeded();
        assertEquals(Direction.RIGHT, guard.getMoveDirection());
        guard.moveSucceeded();
        assertEquals(Direction.UP, guard.getMoveDirection());
        guard.moveSucceeded();
        assertEquals(Direction.DOWN, guard.getMoveDirection());
    }      
    
    // test guard detection radii & type
    @Test
    public void testGuardDetection() {
        RouteGuard guard = new RouteGuard(new Point(5, 5), "PM;C1;UDLR");
        assertEquals(1, guard.getDetectionRadius());
        assertEquals(DetectionType.CIRCLE, guard.getDetectionType());
        RandomGuard guard2 = new RandomGuard(new Point(5, 5), "RM;B2");
        assertEquals(2, guard2.getDetectionRadius());
        assertEquals(DetectionType.BREATH, guard2.getDetectionType());
    }   
    
}
