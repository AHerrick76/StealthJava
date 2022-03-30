package stealth_game;

import java.awt.Point;
import java.util.*;

/**
 * @author Austin Herrick
 * 
 * Implements Guard for guards which move at random each turn. Tracks how these guards detect
 * players, their current location, and how new directions of movement are generated.
 * 
 * Random Guards sometimes attempt movement several times (due to illegal selected moves), and
 * therefore store information about directions of movement not yet attempted.
 *
 */
public class RandomGuard implements Guard {
    
    // Core information
    private Point location;
    private int detectionRadius;
    private DetectionType detectionType;
    
    // Directional information
    private Direction lastMove;
    private Direction attemptedDirection;
    private HashSet<Direction> validDirections;

    /**********************************************************************************
     * Constructor and Setup Methods
     **********************************************************************************/

    /**
     * Sets initial location, parses detection string to find radius/type, resets valid direction
     * list, and initializes lastMove to UP (used for Breath detection to find "facing")
     */
    public RandomGuard(Point locationInitial, String guardBuilder) {
        location = locationInitial;
        parseDetection(guardBuilder);
        lastMove = Direction.UP;
        resetValidDirections();
    }
    
    /**
     * Parses the detection radius and type of the guard, which is stored after the first
     * semicolon
     */
    private void parseDetection(String guardBuilder) {
        String[] segments = guardBuilder.split(";");
        String detectInfo = segments[segments.length - 1];
        detectionRadius = Character.getNumericValue(detectInfo.charAt(1));
        Character detectType = detectInfo.charAt(0);
        if (detectType.equals('C')) {
            detectionType = DetectionType.CIRCLE;
        } else if (detectType.equals('B')) {
            detectionType = DetectionType.BREATH;
        }
    }
    
    /**
     * Refreshes validDirections to include all cardinal directions again
     */
    private void resetValidDirections() {
        validDirections = new HashSet<Direction>();
        for (Direction d: Direction.values()) {
            validDirections.add(d);
        }
    }

    /**********************************************************************************
     * Movement Handling
     **********************************************************************************/
    /**
     * Finds the intended move direction of a guard, generated at random
     * Help for random assignment from:
     * https://stackoverflow.com/questions/124671/picking-a-random-element-from-a-set
     * 
     * attemptedDirection is stored a class attribute in case lastMove needs to be updated.
     * Note that the class doesn't know if it should be yet, because move validity is handled
     * by the GameBoard.
     *
     * @return intended direction of movement
     */
    public Direction getMoveDirection() {
        
        int index = new Random().nextInt(validDirections.size());
        Iterator<Direction> iter = validDirections.iterator();
        for (int i = 0; i < index; i++) {
            iter.next();
        }
        attemptedDirection = iter.next();
        return attemptedDirection;
    }
    
    /**
     * If move succeeded, set lastMove to direction of attempted movement and reset validDirections
     */
    public void moveSucceeded() {
        lastMove = attemptedDirection;
        resetValidDirections();
    }
    
    /**
     * If move failed, remove attempted direction from set and try again (provided set is
     * non-empty)
     */
    @Override
    public Direction moveFailed() {
        validDirections.remove(attemptedDirection);
        if (validDirections.size() > 0) {
            getMoveDirection();
        } else {
            validDirections = new HashSet<Direction>();
            for (Direction d: Direction.values()) {
                validDirections.add(d);
            }
            return null;
        }
        return attemptedDirection;

    }
    
    /**********************************************************************************
     * SETTER
     **********************************************************************************/
    public void updateLocation(Point newLoc) {
        location = newLoc;
    }
    
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
