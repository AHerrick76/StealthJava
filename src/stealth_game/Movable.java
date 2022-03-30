package stealth_game;

import java.awt.Point;

/**
 * @author Austin Herrick
 * Interface for movable objects from the gameboard. Both Player and Guard implement this
 * interface, which is used as the entry point for moving objects, checking certain collisions,
 * etc.
 */
public interface Movable {
    public Point getLocation();
    public void updateLocation(Point newLoc);
}
