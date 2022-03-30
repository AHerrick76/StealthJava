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
 * is displayed as a circle of a specified color.
 */
public class Circle extends GameObj {

    private Color color;

    public Circle(int posX, int posY, int size, Color color) {
        super(posX, posY, size, size);
        this.color = color;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(this.color);
        g.fillOval(this.getPx(), this.getPy(), this.getWidth(), this.getHeight());
    }
}