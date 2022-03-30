package stealth_game;

import java.awt.Point;
import java.text.StringCharacterIterator;

/**
 * @author Austin Herrick
 * 
 * Implements Guard for guards which move according to a set route. Tracks how these guards detect
 * players, their current location, and how new directions of movement are generated.
 * 
 * Route (or Patrol) Guards move along a set schedule, then reverse that schedule, so direction
 * of travel along an iterator must be tracked. Additionally, traversal of the iterator must
 * be handled to ensure that guards cannot misalign their routes following a failed move (due
 * to collision)
 *
 */
public class RouteGuard implements Guard {

    // Core information
    private Point location;
    private int detectionRadius;
    private DetectionType detectionType;
    
    // Directional information
    private StringCharacterIterator route;
    private Boolean forwardMode;
    private Direction lastMove;
    
    /**********************************************************************************
     * Constructor and Setup Methods
     **********************************************************************************/

    /**
     * Sets initial location, parses detection string to find radius/type, parses route string
     * to define pathfinding, and initializes lastMove to UP (used for Breath detection to 
     * find "facing")
     */
    public RouteGuard(Point locationInitial, String guardBuilder) {
        location = locationInitial;
        route = parseRoute(guardBuilder);
        parseDetection(guardBuilder);
        forwardMode = true;
        lastMove = Direction.UP;
    }
    
    /**
     * Parses the csv's string representation of a guard's route into a StringCharacterIterator
     * Technique for splitting strings and docs for StringCharacterIterator found below:
     * https://stackoverflow.com/questions/14316487/java-getting-a-substring-from-a-string-starting
     * -after-a-particular-character
     * https://docs.oracle.com/javase/8/docs/api/java/text/StringCharacterIterator.html
     * 
     * @param guardBuilder - csv string containing guard route
     */
    private StringCharacterIterator parseRoute(String guardBuilder) {
        String routeString = guardBuilder.substring(guardBuilder.lastIndexOf(";") + 1);
        StringCharacterIterator route = new StringCharacterIterator(routeString);
        return route;
    }
    
    /**
     * Parses the detection radius and type of the guard, which is stored after the first
     * semicolon
     */
    private void parseDetection(String guardBuilder) {
        String[] segments = guardBuilder.split(";");
        String detectInfo = segments[segments.length - 2];
        detectionRadius = Character.getNumericValue(detectInfo.charAt(1));
        Character detectType = detectInfo.charAt(0);
        if (detectType.equals('C')) {
            detectionType = DetectionType.CIRCLE;
        } else if (detectType.equals('B')) {
            detectionType = DetectionType.BREATH;
        }
    }
    
    /**********************************************************************************
     * Movement Handling
     **********************************************************************************/

    /**
     * Finds the intended move direction of a guard
     * 
     * @return intended direction of movement
     */
    @Override
    public Direction getMoveDirection() {
        
        // retrieve character of intended movement by checking the current stop of the iter
        Character moveLetter = route.current();
        Direction intendMove = null;
        if (moveLetter.equals('U')) {
            if (forwardMode) {
                intendMove = Direction.UP;
            } else {
                intendMove = Direction.DOWN;
            }
        } else if (moveLetter.equals('D')) {
            if (forwardMode) {
                intendMove = Direction.DOWN;
            } else {
                intendMove = Direction.UP;
            }
        } else if (moveLetter.equals('L')) {
            if (forwardMode) {
                intendMove = Direction.LEFT;
            } else {
                intendMove = Direction.RIGHT;
            }
        } else if (moveLetter.equals('R')) {
            if (forwardMode) {
                intendMove = Direction.RIGHT;
            } else {
                intendMove = Direction.LEFT;
            }
        }
        return intendMove;
    }
    
    /**
     * If the intended move succeeded, advances the iterator for the next call
     */
    @Override
	public void moveSucceeded() {
        lastMove = getMoveDirection();
        invokeIter(route);
    }

    /**
     * If the intended move failed, do nothing
     */
    @Override
    public Direction moveFailed() {
        return null;
    }
    
    /**
     * Traverses a StringCharacterIterator and preps the next character, switching directions
     * once either end of a route is reached
     * 
     * @param myIter - a StringCharacterIterator to be traversed
     */
    private void invokeIter(StringCharacterIterator myIter) {
        Integer currentIndex = myIter.getIndex();
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
    }

    /**********************************************************************************
     * SETTER
     **********************************************************************************/
    @Override
	public void updateLocation(Point newLoc) {
        location = newLoc;
    }
    
    @Override
	public void setPlayerLocation(Point playerLoc) {
    }
    
    /**********************************************************************************
     * GETTERS
     **********************************************************************************/
    @Override
    public Direction getLastMove() {
        return this.lastMove;
    }
    
    @Override
    public int getDetectionRadius() {
        return this.detectionRadius;
    }

    @Override
    public DetectionType getDetectionType() {
        return this.detectionType;
    }

    @Override
    public Point getLocation() {
        return (Point) this.location.clone();
    }



}
