/**
 * 
 */
package stealth_game;

import java.awt.Point;
import java.util.*;

/**
 * @author Austin Herrick
 * 
 * GameBoard is the class that handles the primary game logic. An array, parsed from a file by
 * LevelReader, holds the location of all objects of interest, and is used to initialize the 
 * player, the goal, and all walls, guards, and "danger zones" of detection. When the player
 * successfully moves, GameBoard increments an internal turn timer and handles all turn transitions
 * including victory/defeat checking, guard movement, and the moving detection zone.
 *
 */
public class GameBoard {
    
    // Define board parameters
    private int width;
    private int height;
    private String[][] boardArray;
    
    // define Board characteristics
    private TreeSet<String> legalDestinations;
    private int turn;
    private Boolean victory;
    private Boolean defeat;
    
    // define Board objects
    private Player player;
    private Point victorySquare;
    private HashSet<Point> walls;
    private HashSet<Guard> guards;
    private HashSet<Point> detectionZone;
    private Boolean guardMovement;
    
    // define doors/keys
    private HashMap<Point, Integer> gateDoors;
    private HashMap<Point, Integer> gateKeys;
    
    // define vision
    private Boolean darkLevel;
    private int visionRadius;
    private HashSet<Point> fullZone;
    private HashSet<Point> darknessZone;
    
    /**********************************************************************************
     * Constructor and Setup Methods
     * 
     * GameBoard, parseBoard, and parseGuard are run once upon initializing a GameBoard
     **********************************************************************************/

    /**
     * Initializes a GameBoard containing the location of all objects, along with the "goal" for 
     * the player to reach and all obstacles. Also contains the location of the player and all 
     * guards
     * 
     * @param filePath - the path to the CSV file used to initialize the board state
     */
    public GameBoard(String filePath, Boolean isDark, Integer vision) {
        
        // assign initial state vars
        turn = 0;
        victory = false;
        defeat = false;
        walls = new HashSet<Point>();
        gateDoors = new HashMap<Point, Integer>();
        gateKeys = new HashMap<Point, Integer>();
        guards = new HashSet<Guard>();
        detectionZone = new HashSet<Point>();
        guardMovement = true;
        
        // define vision area vars
        fullZone = new HashSet<Point>();
        darknessZone = new HashSet<Point>();
        darkLevel = isDark;
        visionRadius = vision;
        
        // parse level contents using LevelReader, while initializing Players & Guards
        LevelReader lr = new LevelReader(filePath);
        LinkedList<String[]> parsedFile = lr.getBoardList();
        width = lr.getWidth();
        height = lr.getHeight();
        parseBoard(parsedFile);
        
        // populate legalDestinations set (used to assess whether attempted moves are legal
        legalDestinations = new TreeSet<String>();
        legalDestinations.add("E"); // legal to move onto empty space
        legalDestinations.add("G"); // legal to move onto the goal
        legalDestinations.add("K"); // legal to move onto any key
    }

    /**
     * Parses the LinkedList passed by LevelReader into the actual game board. Initializes player
     * and all guards as they are encountered
     * 
     * @param parsedFile -> the LinkedList produced by the LevelReader from the level csv
     */
    private void parseBoard(LinkedList<String[]> parsedFile) {
        // initialize board, and iterate through parsedFile to populate
        boardArray = new String[height][width];
        int currentRow = 0;
        Iterator<String[]> iter = parsedFile.iterator();
        while (iter.hasNext()) {
            String[] levelRow = iter.next();
            for (int col = 0; col < width; col++) {
                boardArray[currentRow][col] = levelRow[col];
                
                // append to fullZone of points
                fullZone.add(new Point(col, currentRow));
                
                // if cell contains a feature of note, initialize object
                if (levelRow[col].equals("P")) {
                    Point playerLoc = new Point(col, currentRow);
                    player = new Player(playerLoc);
                } else if (levelRow[col].equals("G")) {
                    victorySquare = new Point(col, currentRow);
                } else if (levelRow[col].equals("W")) {
                    walls.add(new Point(col, currentRow));
                // if an Guard/Mob is detected, parse string and abbreviate array storage
                } else if (levelRow[col].contains("M")) {
                    Point guardLoc = new Point(col, currentRow);
                    parseGuard(levelRow[col], guardLoc);
                    boardArray[currentRow][col] = "M";
                 // if a Key/Door is detected, assign as appropriate
                } else if ((levelRow[col].startsWith("D")) || (levelRow[col].startsWith("K"))) {
                    Point keyDoorLoc = new Point(col, currentRow);
                    parseKeyDoor(levelRow[col], keyDoorLoc);
                    if (levelRow[col].startsWith("D")) {
                        boardArray[currentRow][col] = "D";
                    } else {
                        boardArray[currentRow][col] = "K";
                    }
                }
            }
            currentRow += 1;
        }
        
        // if necessary, create vision area
        if (darkLevel) {
            updateVision();
        }
    }
    
    /**
     * Parses cells with keys/doors
     */   
    private void parseKeyDoor(String keyDoorString, Point keyDoorLoc) {
        int lockNum = Character.getNumericValue(keyDoorString.charAt(1));
        if (keyDoorString.startsWith("D")) {
            gateDoors.put(keyDoorLoc, lockNum);
        } else {
            gateKeys.put(keyDoorLoc,  lockNum);
        }
    }
    
    /**
     * Initializes a new guard by parsing the guardString, which contains all information
     * on type
     * 
     * @param guardString -> string contents to be parsed
     * @param guardLoc -> initial location of guard
     */   
    private void parseGuard(String guardString, Point guardLoc) {
        // identify type of guard
        if (guardString.contains("P")) {
            RouteGuard guard = new RouteGuard(guardLoc, guardString);
            guards.add(guard);
            updateDetection(guard);
        } else if (guardString.contains("R")) {
            RandomGuard guard = new RandomGuard(guardLoc, guardString);
            guards.add(guard);
            updateDetection(guard);
        } else if (guardString.contains("H")) {
            ChaseGuard guard = new ChaseGuard(guardLoc, guardString);
            guards.add(guard);
            updateDetection(guard);
        }
    }
    
    /**********************************************************************************
     * Movement Methods
     * 
     * moveObject, checkMoveLegality, inBounds, getDestination; each used when attempting to 
     * relocate a movable
     **********************************************************************************/
  
    /**
     * Attempts to move an object (a player or guard) in a particular direction
     * 
     * @param character -> the object to be moved
     * @param direction -> the intended direction of movement
     * @param boolean -> returns whether the attempted move happened
     */   
    public Boolean moveObject(Movable character, Direction direction) {
        // if no direction is supplied, exit
        if (direction == null) {
            return false;
        }
        
        // get current location of object & find intended destination
        Point currentLocation = character.getLocation();
        Point destination = getDestination(currentLocation, direction);
        
        // check if intended destination is legal, and update location of movable if so
        if (checkMoveLegality(destination)) {
            character.updateLocation(destination);
            
            // clear previous location
            int prevX = (int) (currentLocation.getX());
            int prevY = (int) (currentLocation.getY());
            boardArray[prevY][prevX] = "E";
            
            // update location, and if movable is player, update the turn counter
            int newX = (int) (destination.getX());
            int newY = (int) (destination.getY());
            if (character instanceof Player) {
                boardArray[newY][newX] = "P";
                incrementTurn();
            } else {
                boardArray[newY][newX] = "M";
            }
            
            // if victory square is empty (because a movable moved off of it), update value
            int vX = (int) victorySquare.getX();
            int vY = (int) victorySquare.getY();
            if (boardArray[vY][vX].equals("E")) {
                boardArray[vY][vX] = "G";
            }
            
            // if a key square is empty, return to key
            Iterator<Point> keyIter = gateKeys.keySet().iterator();
            while (keyIter.hasNext()) {
                Point nextKey = keyIter.next();
                int kX = (int) nextKey.getX();
                int kY = (int) nextKey.getY();
                if (boardArray[kY][kX].equals("E")) {
                    boardArray[kY][kX] = "K";
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Given a current location and an intended direction, finds the intended destination
     * 
     * @param character -> the object to be moved
     * @param direction -> the intended direction of movement
     * @return Point -> the intended destination
     */
    private Point getDestination(Point currentLoc, Direction direction) {
        if (direction == null) {
            return null;
        }
        
        // get current location of object & find intended destination
        int currentX = (int) (currentLoc.getX());
        int currentY = (int) (currentLoc.getY());
        Point destination = new Point(0, 0);
        switch (direction) {
            case UP:
                destination.setLocation(currentX, currentY - 1);
                return destination;
            case DOWN:
                destination.setLocation(currentX, currentY + 1);
                return destination;
            case LEFT:
                destination.setLocation(currentX - 1, currentY);
                return destination;
            case RIGHT:
                destination.setLocation(currentX + 1, currentY);
                return destination;
            default:
                return destination;
        }
    }
    
    /**
     * Given a suggested destination point, checks if point represents a legal move. Points can
     * be illegal for two reasons:
     *    - Move is outside the bounds of the game board
     *    - Move is onto an illegal space (a Wall, another Guard/Player, etc)
     * 
     * @param destination -> the intended destination
     * @return Boolean -> whether the intended destination is legal
     */
    private Boolean checkMoveLegality(Point destination) {
        int xCoord = (int) (destination.getX());
        int yCoord = (int) (destination.getY());
        
        // check if point is outside bounds of array
        if (!inBounds(xCoord, yCoord)) {
            return false;
        }
        
        // check if point is a legal move destination
        String destinationContents = getElement(xCoord, yCoord);
        return legalDestinations.contains(destinationContents);
    }
    
    /**
     * Checks if x,y pair is within bounds of the array
     * 
     * @param xCoord -> x coordinate of point to check
     * @param yCoord -> y coordinate of point to check
     * @return Boolean -> whether the intended point is in bounds
     */
    private Boolean inBounds(int xCoord, int yCoord) {
        return ((xCoord >= 0) & (yCoord >= 0) & (xCoord < width) & (yCoord < height));
    }
    
    /**********************************************************************************
     * Turn Transition Methods
     * 
     * incrementTurn, updateDetection; are used to transition between turns. Handles end state
     * checking, moves all guards, and calculates the new detection zone
     **********************************************************************************/
    
    /**
     * Increments the turn counter. Whenever the turn counter increments, do each of the following:
     *    - Check if the player has won the game
     *    - Move all guards (and update their vision cones)
     *    - Check if the player has lost the game
     */   
    public void incrementTurn() {
        // increment actual turn
        turn += 1;
        
        // check if player has won the game
        Point playerLocation = player.getLocation();
        if (playerLocation.equals(victorySquare)) {
            victory = true;
        }
        
        // check if player received a key, and clear the relevant gates if so
        Iterator<Point> keyIter = gateKeys.keySet().iterator();
        while (keyIter.hasNext()) {
            Point nextKey = keyIter.next();
            if (playerLocation.equals(nextKey)) {
                int keyRef = gateKeys.get(nextKey);
                Iterator<Point> gateIter = gateDoors.keySet().iterator();
                
                // clear gates corresponding to found key
                while (gateIter.hasNext()) {
                    Point nextGate = gateIter.next();
                    int keyCode = gateDoors.get(nextGate);
                    if (keyCode == keyRef) {
                        int gX = (int) nextGate.getX();
                        int gY = (int) nextGate.getY();
                        boardArray[gY][gX] = "E";
                        gateIter.remove();
                    }
                }
                
                // remove key from keySet and set square to empty
                gateKeys.remove(nextKey);
                int kX = (int) nextKey.getX();
                int kY = (int) nextKey.getY();
                boardArray[kY][kX] = "E";
                
                // break, as player couldn't have stepped on a subsequent key as well
                break;
            }
        }
        
        // move all guards and update detection zone
        detectionZone = new HashSet<Point>();
        Iterator<Guard> iterGuard = guards.iterator();
        while ((iterGuard.hasNext()) & (guardMovement)) {
            Guard guard = iterGuard.next();
            guard.setPlayerLocation(playerLocation); // this only matters for chaseGuards
            Direction moveDir = guard.getMoveDirection();
            Boolean didMove = moveObject(guard, moveDir);
            
            // hand move outcome according to guard type
            if (didMove) {
                guard.moveSucceeded();
            // random guards continue to attempt moves until all options are exhausted or
            // a successful move is found
            } else {
                Direction nextDir = guard.moveFailed();
                Boolean backupMove = false;
                while ((!(nextDir == null)) & (!(backupMove))) {
                    backupMove = moveObject(guard, nextDir);
                    if (backupMove) {
                        guard.moveSucceeded();
                    } else {
                        nextDir = guard.moveFailed();
                    }
                }
            }
            
            // update detectionZone
            updateDetection(guard);
        }
        
        // check if player has lost the game
        if (detectionZone.contains(playerLocation)) {
            defeat = true;
        }
        
        // update vision area, if necessary
        if (darkLevel) {
            updateVision();
        }
    }
    
    /**
     * Update set of detected squares for a current guard's position, detection radius, and
     * detection type
     * 
     * @param guard -> the guard in question
     */  
    private void updateDetection(Guard guard) {
        // retrieve necessary info
        Point location = guard.getLocation();
        int xCoord = (int) location.getX();
        int yCoord = (int) location.getY();
        int radius = guard.getDetectionRadius();
        DetectionType type = guard.getDetectionType();
        
        if (type.equals(DetectionType.CIRCLE)) {
            // find all points surrounding guard, and add to detection zone within taxicab distance
            for (Integer col = xCoord - radius; col <= xCoord + radius; col++) {
                for (Integer row = yCoord - radius; row <= yCoord + radius; row++) {
                    // check that point is in bounds and not the guard's current location
                    if ((!(col.equals(xCoord) & (row.equals(yCoord)))) & (inBounds(col, row))) {
                        // check that point is within taxicab distance of radius
                        int distance = Math.abs(col - xCoord) + Math.abs(row - yCoord);
                        if (distance <= radius) {
                            detectionZone.add(new Point(col, row));
                        }
                    }
                }
            }
        } else if (type.equals(DetectionType.BREATH)) {
            Direction lastMove = guard.getLastMove();
            // use last direction, and spread out detection
            if (lastMove.equals(Direction.UP)) {
                for (int i = 0; i < radius; i++) {
                    for (int offset = -i; offset <= i; offset++) {
                        if (inBounds(xCoord + offset, yCoord - (i + 1))) {
                            detectionZone.add(new Point(xCoord + offset, yCoord - (i + 1)));
                        }
                    }
                }
            } else if (lastMove.equals(Direction.DOWN)) {
                for (int i = 0; i < radius; i++) {
                    for (int offset = -i; offset <= i; offset++) {
                        if (inBounds(xCoord + offset, yCoord + (i + 1))) {
                            detectionZone.add(new Point(xCoord + offset, yCoord + (i + 1)));
                        }
                    }
                }
            } else if (lastMove.equals(Direction.LEFT)) {
                for (int i = 0; i < radius; i++) {
                    for (int offset = -i; offset <= i; offset++) {
                        if (inBounds(xCoord - (i + 1), yCoord + offset)) {
                            detectionZone.add(new Point(xCoord - (i + 1), yCoord + offset));
                        }
                    }
                }
            } else if (lastMove.equals(Direction.RIGHT)) {
                for (int i = 0; i < radius; i++) {
                    for (int offset = -i; offset <= i; offset++) {
                        if (inBounds(xCoord + (i + 1), yCoord + offset)) {
                            detectionZone.add(new Point(xCoord + (i + 1), yCoord + offset));
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Updates the vision radius, for dark levels
     */  
    @SuppressWarnings("unchecked")
    private void updateVision() {
        
        // reset darkness squares to all squares
        darknessZone = (HashSet<Point>) fullZone.clone();
        
        // get playerLoc
        Point playerLoc = player.getLocation();
        int xCoord = (int) playerLoc.getX();
        int yCoord = (int) playerLoc.getY();
        
        // find all points surrounding player, and remove from darkness zone
        for (Integer col = xCoord - visionRadius; col <= xCoord + visionRadius; col++) {
            for (Integer row = yCoord - visionRadius; row <= yCoord + visionRadius; row++) {
                // check that point is in bounds and within taxicab distance
                int distance = Math.abs(col - xCoord) + Math.abs(row - yCoord);
                if ((distance <= visionRadius) & (inBounds(col, row))) {
                    darknessZone.remove(new Point(col, row));
                }
            }
         }
    }

    
    /**********************************************************************************
     * GETTERS
     * 
     * Note that, when possible, shallow copies of objects are returned to ensure encapsulation
     **********************************************************************************/
    public Boolean isVictory() {
        return this.victory;
    }
    
    public Boolean isDefeat() {
        return this.defeat;
    }
    
    public Point getVictorySquare() {
        return (Point) this.victorySquare.clone();
    }  
    
    @SuppressWarnings("unchecked")
    public HashSet<Point> getWalls() {
        return (HashSet<Point>) this.walls.clone();
    }
    
    @SuppressWarnings("unchecked")
    public HashSet<Point> getDetectionZone() {
        return (HashSet<Point>) this.detectionZone.clone();
    }
    
    @SuppressWarnings("unchecked")
    public HashSet<Guard> getGuards() {
        return (HashSet<Guard>) this.guards.clone();
    }
    
    @SuppressWarnings("unchecked")
    public HashMap<Point, Integer> getDoors() {
        return (HashMap<Point, Integer>) this.gateDoors.clone();
    }
    
    @SuppressWarnings("unchecked")
    public HashMap<Point, Integer> getKeys() {
        return (HashMap<Point, Integer>) this.gateKeys.clone();
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public Point getPlayerLocation() {
        return this.player.getLocation();
    }
    
    public Integer getHeight() {
        return this.height;
    }
    
    public Integer getWidth() {
        return this.width;
    }
    
    public Integer getTurn() {
        return this.turn;
    }
    
    // Checks the written value of a BoardArray cell -- used for testing
    public String getElement(int xCoord, int yCoord) {
        // check if point is within array
        if (inBounds(xCoord, yCoord)) {
            return boardArray[yCoord][xCoord];
        } else {
            return null;
        }
    }
    
    // What an odd method
    public void disableGuardMovement() {
        guardMovement = false;
    }

    @SuppressWarnings("unchecked")
    public HashSet<Point> getDarknessZone() {
        return (HashSet<Point>) this.darknessZone.clone();
    }
    
    // get dark status
    public Boolean getDarkness() {
        return this.darkLevel;
    }
}
