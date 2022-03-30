package stealth_game;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

/**
 * GamePanel
 *
 * This class holds the primary game logic for how different objects interact
 * with the GUI. It creates calls to the underlying GameBoard to manipulate all
 * objects as necessary
 */
@SuppressWarnings("serial")
public class GamePanel extends JPanel {
    
    // Game Board information
    private GameBoard gb;
    private Player player;
    
    // Game Status information
    private Integer currentTurn;
    private JLabel status; // Current status text, i.e. "Running..."
    private Boolean playing; // whether the game is running
    
    // framing/size information  (board size scaled to level size
    private String currentLevel;
    private int boardWidth;
    private int boardHeight;
    
    // drawable collections
    private Sprite playerGraphic;
    private Sprite victorySquare;
    private HashSet<Sprite> wallSprites;
    private HashSet<Sprite> guardSprites;
    private HashSet<Circle> detectionCircles;
    private HashSet<Sprite> floor;
    
    // drawable keys/gates
    private HashSet<Sprite> keySprites;
    private HashSet<Square> gateSquares;
    
    // define wall/floor images
    private String wallImage;
    private String floorImage;
    
    // define darkness
    private HashSet<Square> darknessSquares;
    private Boolean darkLevel;
    private int visionRadius;
    
    // this is a weird thing to track
    private String pastKeys;

    /**********************************************************************************
     * Constructor and Setup/Logic Methods
     * 
     * GamePanel, setCurrentLevel, reset, and clientSideTurnIncrementor initialize the GUI board, 
     * reset when requested, handle transitioning to a different level when requested, and
     * handle inter-turn transitions on the display side (game logic is handled by GameBoard)
     **********************************************************************************/
    public GamePanel(String filePath, JLabel status) {
        
        // initialize pastKeys set
        pastKeys = "";
        darkLevel = false;
        visionRadius = 6;
        
        // creates border around the court area, JComponent method
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        // Enable keyboard focus on the court area. When this component has the
        // keyboard focus, key events are handled by its key listener.
        setFocusable(true);
        
        // loads requested starter level
        currentLevel = filePath;
        this.status = status;
        setCurrentLevel(currentLevel, 0, darkLevel, visionRadius, 0);
        
        // This key listener detects intended player movement by registering keyboard inputs
        addKeyListener(new KeyAdapter() {
            @Override
			public void keyPressed(KeyEvent e) {
                if (playing) {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        gb.moveObject(player, Direction.LEFT);
                        pastKeys += "L";
                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        gb.moveObject(player, Direction.RIGHT);
                        pastKeys += "R";
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        pastKeys += "D";
                        gb.moveObject(player, Direction.DOWN);
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        pastKeys += "U";
                        gb.moveObject(player, Direction.UP);
                    } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        gb.incrementTurn();
                        pastKeys += " ";
                    } else if (e.getKeyCode() == KeyEvent.VK_A) {
                        pastKeys += "A";
                    } else if (e.getKeyCode() == KeyEvent.VK_B) {
                        pastKeys += "B";
                    } else {
                        pastKeys += " ";
                    }
                    // update GUI, check for victory/defeat, etc
                    clientSideTurnIncrementor();
                }
            }
        });
    }
    
    /**
     * Sets the current level to the filepath of choice, then resets the stage.
     * Also initializes first time setup -- victory square assignment, loading the walls, etc.
     */
    public void setCurrentLevel(String filePath, int levelCount, 
            Boolean isDark, Integer vision, Integer tileset) {
        
        currentLevel = filePath;
        darkLevel = isDark;
        visionRadius = vision;
        
        // set wall/floor image based on levelCount
        updateTileset(tileset);
        
        // first time setup
        gb = new GameBoard(currentLevel, darkLevel, visionRadius);
        
        // find board dimensions
        boardWidth = gb.getWidth() * 30;
        boardHeight = gb.getHeight() * 30;
        
         // translate all walls into shapes (only done once, since walls cannot be changed)
        HashSet<Point> wallCoords = gb.getWalls();
        wallSprites = new HashSet<Sprite>();
        Iterator<Point> wallIter = wallCoords.iterator();
        while (wallIter.hasNext()) {
            Point nextWall = wallIter.next();
            Sprite wall = createSprite(nextWall, 30, wallImage);
            wallSprites.add(wall);
        }
        
        // store floor tiles for every square (drawn underneath all objects)
        HashSet<Point> fullGrid = new HashSet<Point>();
        for (int row = 0; row < gb.getHeight(); row++) {
            for (int col = 0; col < gb.getWidth(); col++) {
                fullGrid.add(new Point(col, row));
            }
        }
        floor = new HashSet<Sprite>();
        Iterator<Point> floorIter = fullGrid.iterator();
        while (floorIter.hasNext()) {
            Point nextFloor = floorIter.next();
            Sprite floorTile = createSprite(nextFloor, 30, floorImage);
            floor.add(floorTile);
        }
        
        // create a Square to store the victory square (only done once, since victorySquare
        // doesn't move
        Point victoryPoint = gb.getVictorySquare();
        victorySquare = createSprite(victoryPoint, 25, "files/assets/altar.png");
        
        // resets the level
        reset();
    }
    
    /**
     * (Re-)set the game to its initial state.
     * 
     * Note that string formatting help was found via StackOverflow here: 
     * https://stackoverflow.com/questions/6431933/how-to-format-strings-in-java
     */
    public void reset() {
        gb = new GameBoard(currentLevel, darkLevel, visionRadius);
        player = gb.getPlayer();
        currentTurn = gb.getTurn();
        status.setText(String.format("It's Stealth Time! Currently turn: %d", currentTurn));
        playing = true;
        
        // recreate all movable shapes & repaint
        recreateMovables();
        repaint();

        // Make sure that this component has the keyboard focus
        requestFocusInWindow();
    }
    
    /**
     * After the player attempts a move, update client side information.
     * This is the GUI equivalent of GameBoard's incrementTurn method
     */
    private void clientSideTurnIncrementor() {
      
        // recreate all movable shapes
        recreateMovables();
      
        // update turn display
        currentTurn = gb.getTurn();
        status.setText(String.format("It's Stealth Time! Currently turn: %d", currentTurn));
        repaint();
      
        // check if player has won or lost
        if (gb.isVictory()) {
            status.setText(String.format("Level Complete after %d turns!", currentTurn));
            playing = false;
        } else if (gb.isDefeat()) {
            status.setText(String.format("You were caught after %d turns!", currentTurn));
            playing = false;
        } 
      
        // konami
        if (pastKeys.contains("UUDDLRLRBA")) {
            gb.disableGuardMovement();
        }
        // clear buffer every 1000 keys
        if (pastKeys.length() > 1000) {
            pastKeys = "";
        }
    }
    
    /**********************************************************************************
     * Mutable Display Methods
     * 
     * recreateMovables, createSquare, createCircle, coordinateToPixels handle drawing of moving
     * objects
     **********************************************************************************/
    /**
     * Called after incrementing the turn or resetting the level. Recreates shapes for all
     * non-static display elements -- the player, all guards, and the detection zone
     */
    private void recreateMovables() {
      
        // updates player location graphically
        Point playerLoc = player.getLocation();
        playerGraphic = createSprite(playerLoc, 30, "files/assets/donald.png");
      
        // create guard squares
        HashSet<Guard> guards = gb.getGuards();
        Iterator<Guard> guardIter = guards.iterator();
        guardSprites = new HashSet<Sprite>();
        while (guardIter.hasNext()) {
            Guard guard = guardIter.next();
            Point guardLoc = guard.getLocation();
          
            // clarify guard type via sprite
            String imageFile = "files/assets/chaos_spawn.png";
            if (guard instanceof RouteGuard) {
                imageFile = "files/assets/paladin.png";
            } else if (guard instanceof ChaseGuard) {
                imageFile = "files/assets/centaur.png";
            }
            Sprite guardSprite = createSprite(guardLoc, 30, imageFile);
            guardSprites.add(guardSprite);
        }
      
        // create detectionZone circles
        HashSet<Point> dZoneCoords = gb.getDetectionZone();
        detectionCircles = new HashSet<Circle>();
        Iterator<Point> dZoneIter = dZoneCoords.iterator();
        while (dZoneIter.hasNext()) {
            Point nextDetection = dZoneIter.next();
            Circle detectionCircle = createCircle(nextDetection, 20, Color.YELLOW);
            detectionCircles.add(detectionCircle);
        }
        
        // create darkness squares, if needed
        Boolean isDark = gb.getDarkness();
        darknessSquares = new HashSet<Square>();
        if (isDark) {
            HashSet<Point> darknessZone = gb.getDarknessZone();
            Iterator<Point> darkIter = darknessZone.iterator();
            while (darkIter.hasNext()) {
                Point nextDark = darkIter.next();
                Square darkSquare = createSquare(nextDark, 30, Color.BLACK);
                darknessSquares.add(darkSquare);
            }
        }
        
        // create Keys & Gates
        keySprites = new HashSet<Sprite>();
        gateSquares = new HashSet<Square>();
        HashMap<Point, Integer> keys = gb.getKeys();
        HashMap<Point, Integer> doors = gb.getDoors();
        Iterator<Point> doorIter = doors.keySet().iterator();
        while (doorIter.hasNext()) {
            Point nextDoor = doorIter.next();
            int keyCode = doors.get(nextDoor);
            // assign door color based on int
            Color doorColor = Color.GREEN;
            if (keyCode == 2) {
                doorColor = Color.BLUE;
            } else if (keyCode == 3) {
                doorColor = Color.MAGENTA;
            } else if (keyCode == 4) {
                doorColor = Color.RED;
            }
            Square doorSquare = createSquare(nextDoor, 30, doorColor);
            gateSquares.add(doorSquare);
        }
        Iterator<Point> keyIter = keys.keySet().iterator();
        while (keyIter.hasNext()) {
            Point nextKey = keyIter.next();
            int keyCode = keys.get(nextKey);
            // assign door color based on int
            String fileName = "files/assets/stone2_green.png";
            if (keyCode == 2) {
                fileName = "files/assets/stone2_blue.png";
            } else if (keyCode == 3) {
                fileName = "files/assets/eye_magenta.png";
            } else if (keyCode == 4) {
                fileName = "files/assets/ring_red.png";
            }
            Sprite keyAmulet = createSprite(nextKey, 30, fileName);
            keySprites.add(keyAmulet);
        }
        
    }
    
    /**
     * Given a starting point, a color, and a size, handles all steps necessary to construct a
     * square on the GUI (representing different objects)
     * 
     * @param p -> the point, in grid coordinates
     * @param size -> and int specifying dimensions
     * @param color -> the color of the rectangle
     * @return Square -> the constructed rectangle object
     */
    private Square createSquare(Point p, int size, Color color) {
        Point pixelLoc = coordinateToPixels(p, size);
        int xCoord = (int) pixelLoc.getX();
        int yCoord = (int) pixelLoc.getY();
        Square mySquare = new Square(xCoord, yCoord, size, color);
        return mySquare;
    }
    
    /**
     * Given a starting point, a color, and a size, handles all steps necessary to construct a
     * circle on the GUI (representing detection zones)
     * 
     * @param p -> the point, in grid coordinates
     * @param size -> and int specifying dimensions
     * @param color -> the color of the rectangle
     * @return Square -> the constructed rectangle object
     */
    private Circle createCircle(Point p, int size, Color color) {
        Point pixelLoc = coordinateToPixels(p, size);
        int xCoord = (int) pixelLoc.getX();
        int yCoord = (int) pixelLoc.getY();
        Circle myCircle = new Circle(xCoord, yCoord, size, color);
        return myCircle;
    }
    
    /**
     * Constructs altar
     */
    private Sprite createSprite(Point p, int size, String filePath) {
        Point pixelLoc = coordinateToPixels(p, size);
        int xCoord = (int) pixelLoc.getX();
        int yCoord = (int) pixelLoc.getY();
        Sprite sprite = new Sprite(xCoord, yCoord, size, filePath);
        return sprite;
    }
    
    /**
     * Translates coordinates from their board array equivalents to pixel locations. Also centers
     * objects within their target cell, based on size of intended square
     * Used to translate the location of objects onto the GUI
     * 
     * @param p -> the point, in grid coordinates
     * @param size -> int size of the final square, used for centering
     * @return Point -> the same point, in pixel coordinates
     */
    private Point coordinateToPixels(Point p, int size) {
        if (p == null) {
            return null;
        }
        int xCoord = (int) p.getX();
        int yCoord = (int) p.getY();
        int offset = (30 - size) / 2;
        return new Point((xCoord * 30) + offset, (yCoord * 30) + offset);
    }
    
    /**********************************************************************************
     * Main Painting Methods
     * 
     * paintComponent, getPreferredSize -> Creates actual displays for visuals on screen,
     * sets initial window size
     **********************************************************************************/
    
    /**
     * Updates tileset according to request
     */
    private void updateTileset(Integer tileset) {
        
        // set wall/floor image based on requested tileset
        if (tileset == 0) {
            wallImage = "files/assets/stone_brick7.png";
            floorImage = "files/assets/grass_flowers_blue2.png";
        } else if (tileset == 1) {
            wallImage = "files/assets/brick_brown4.png";
            floorImage = "files/assets/dirt0.png";
        } else if (tileset == 2) {
            wallImage = "files/assets/brick_brown-vines2.png";
            floorImage = "files/assets/grass0-dirt-mix2.png";
        } else if (tileset == 3) {
            wallImage = "files/assets/crystal_wall00.png";
            floorImage = "files/assets/ice0.png";
        } else if (tileset == 4) {
            wallImage = "files/assets/cobalt_stone_1.png";
            floorImage = "files/assets/frozen_12.png";
        }
    }
    
    
    /**
     * Draws the game board and all components.
     * 
     * The array itself is drawn with 30x30 pixel squares, calculated using the height/width
     * of the GameBoard. Other objects are drawn after creation, handled above.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // draw floor (this MUST be drawn first)
        Iterator<Sprite> floorIter = floor.iterator();
        while (floorIter.hasNext()) {
            Sprite floorTile = floorIter.next();
            floorTile.draw(g);
        }
        
        // draw boundaries of grid
        for (int r = 0; r <= boardHeight; r += 30) {
            g.drawLine(0, r,  boardWidth,  r);
        }
        for (int c = 0; c <= boardWidth; c += 30) {
            g.drawLine(c, 0,  c,  boardHeight);
        }
        
        // draw all detectionZone circles
        // (This happens BEFORE wall-drawing, so that detection that overlaps walls is ignored)
        Iterator<Circle> detectObjIter = detectionCircles.iterator();
        while (detectObjIter.hasNext()) {
            Circle dCircle = detectObjIter.next();
            dCircle.draw(g);
        }
        
        // draw key objects
        victorySquare.draw(g);
        playerGraphic.draw(g);
        
        // draw all guards
        Iterator<Sprite> guardObjIter = guardSprites.iterator();
        while (guardObjIter.hasNext()) {
            Sprite guard = guardObjIter.next();
            guard.draw(g);
        }
            
        // draw all walls
        Iterator<Sprite> wallObjIter = wallSprites.iterator();
        while (wallObjIter.hasNext()) {
            Sprite wall = wallObjIter.next();
            wall.draw(g);
        }
        
        // draw all gates & keys
        Iterator<Sprite> keyIter = keySprites.iterator();
        while (keyIter.hasNext()) {
            Sprite key = keyIter.next();
            key.draw(g);
        } 
        Iterator<Square> gateIter = gateSquares.iterator();
        while (gateIter.hasNext()) {
            Square gate = gateIter.next();
            gate.draw(g);
        }   
        
        // draw darkness squares, if needed
        Boolean isDark = gb.getDarkness();
        if (isDark) {
            Iterator<Square> darkSquareIter = darknessSquares.iterator();
            while (darkSquareIter.hasNext()) {
                Square dSquare = darkSquareIter.next();
                dSquare.draw(g);
            }
        }
        
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(boardWidth, boardHeight);
    }

}
