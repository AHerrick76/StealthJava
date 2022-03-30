package stealth_game;

/**
 * CIS 120 Game HW
 * (c) University of Pennsylvania
 * 
 * @version 2.1, Apr 2017
 */

import java.awt.*;

/**
 * A basic game object of the game panel. It
 * is displayed as a square of a specified color.
 */
public class Square extends GameObj {

    private Color color;

    /**
     * Note that, because we don't need to do anything special when constructing
     * a Square, we simply use the superclass constructor called with the
     * correct parameters.
     */
    public Square(int posX, int posY, int size, Color color) {
        super(posX, posY, size, size);

        this.color = color;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(this.color);
        g.fillRect(this.getPx(), this.getPy(), this.getWidth(), this.getHeight());
    }
}