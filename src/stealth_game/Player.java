package stealth_game;

import java.awt.Point;

/**
 * @author Austin Herrick
 * Class which controls attributes & characteristics of the Player object. Implements the Movable
 * interface
 */
public class Player implements Movable {
    
    private Point locaction;

    public Player(Point locationInitial) {
        locaction = locationInitial;
    }

    /**
     * returns current location of player
     * 
     * @return Point - current location of player
     */
    public Point getLocation() {
        return (Point) this.locaction.clone();
    }
    
    /**
     * Updates current location of player.
     * 
     * @param newLoc - new location to assign Player object
     */
    public void updateLocation(Point newLoc) {
        locaction = newLoc;
    }

}
