package stealth_game;

import java.awt.Point;

/**
 * @author Austin Herrick
 * Interface for guard objects from the gameboard. All types of guards implement this interface, 
 * which is used as the entry point for assigned moves, detection zones, etc.
 */
public interface Guard extends Movable {
    public int getDetectionRadius();
    public DetectionType getDetectionType();
    public Direction getMoveDirection();
    public Direction getLastMove();
    public Direction moveFailed();
    public void moveSucceeded();
    public void setPlayerLocation(Point playerLoc);
}
