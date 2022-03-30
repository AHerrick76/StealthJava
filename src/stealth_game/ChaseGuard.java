package stealth_game;

import java.awt.Point;
import java.util.HashSet;

/**
 * @author Austin Herrick
 * 
 * Implements Guard for guards which chase the player. Tracks how these guards detect
 * players, their current location, and how new directions of movement are generated.
 * 
 * Chase guards move directly toward the player, minimizing X or Y distance, depending
 * on which is larger. If no legal move can bring the guard closer to the player, they don't
 * move.
 *
 */
public class ChaseGuard implements Guard {

    // Core information
    private Point location;
    private int detectionRadius;
    private DetectionType detectionType;
    
    // Directional information
    private Direction lastMove;
    private Direction attemptedDirection;
    private HashSet<Direction> hDirections;
    private Point playerLocation;
    private int failCount;
    
    /**********************************************************************************
     * Constructor and Setup Methods
     **********************************************************************************/

    /**
     * Sets initial location, parses detection string to find radius/type, initalizes valid 
     * directions, and initializes lastMove to UP (used for Breath detection to 
     * find "facing")
     */
    public ChaseGuard(Point locationInitial, String guardBuilder) {
        location = locationInitial;
        parseDetection(guardBuilder);
        lastMove = Direction.UP;
        failCount = 0;
        playerLocation = new Point(0, 0);
        
        // define horizontal direction category
        hDirections = new HashSet<Direction>();
        hDirections.add(Direction.LEFT);
        hDirections.add(Direction.RIGHT);
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
    @Override
	public Direction getMoveDirection() {
        
        // find relative x/y distances between Player/Guard
        int xDistance = (int) (location.getX() - playerLocation.getX());
        int yDistance = (int) (location.getY() - playerLocation.getY());
        
        // if previous attempt failed, reset initial attempted axis to 0
        if (failCount == 1) {
            if (hDirections.contains(attemptedDirection)) {
                xDistance = 0;
            } else {
                yDistance = 0;
            }
            
            // if both axes are zero (b/c guard was aligned on one-axis and unable to move
            // along the other), return null direction to skip
            if ((xDistance == 0) & (yDistance == 0)) {
//                failCount = 0;
                return null;
            }
        }
       
        
        if (Math.abs(xDistance) > Math.abs(yDistance)) {
            if (xDistance >= 0) {
                attemptedDirection = Direction.LEFT;
            } else {
                attemptedDirection = Direction.RIGHT;
            }
        } else {
            if (yDistance >= 0) {
                attemptedDirection = Direction.UP;
            } else {
                attemptedDirection = Direction.DOWN;
            }
        }
        
        return attemptedDirection;
    }
    
    /**
     * If move succeeded, set lastMove to direction of attempted movement and reset validDirections
     */
    @Override
	public void moveSucceeded() {
        lastMove = attemptedDirection;
        failCount = 0;
    }
    
    /**
     * If move failed, remove attempted direction from set and try again (provided set is
     * non-empty)
     */
    @Override
    public Direction moveFailed() {
        failCount += 1;
        if (failCount < 2) {
            getMoveDirection();
        } else {
            failCount = 0;
            return null;
        }
        return attemptedDirection;

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
        playerLocation = playerLoc;
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
