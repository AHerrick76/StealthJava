package stealth_game;

/**
 * CIS 120 Game HW
 * (c) University of Pennsylvania
 * 
 * @version 2.1, Apr 2017
 */

import java.awt.Graphics;

/**
 * An object in the game. Master class for Circle/Square
 */
public abstract class GameObj {
    /*
     * Current position of the object (in terms of graphics coordinates)
     */
    private int px;
    private int py;

    /* Size of object, in pixels. */
    private int width;
    private int height;

    /**
     * Constructor
     */
    public GameObj(
            int px, int py, int width, int height
    ) {
        this.px = px;
        this.py = py;
        this.width = width;
        this.height = height;
    }

    /***
     * GETTERS
     **********************************************************************************/
    public int getPx() {
        return this.px;
    }

    public int getPy() {
        return this.py;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    /**************************************************************************
     * SETTERS
     **************************************************************************/
    public void setPx(int px) {
        this.px = px;
    }

    public void setPy(int py) {
        this.py = py;
    }

    /**************************************************************************
     * UPDATES AND OTHER METHODS
     **************************************************************************/
    /**
     * Default draw method that provides how the object should be drawn in the
     * GUI. This method does not draw anything. Subclass should override this
     * method based on how their object should appear.
     *
     * @param g The <code>Graphics</code> context used for drawing the object.
     *          Remember graphics contexts that we used in OCaml, it gives the
     *          context in which the object should be drawn (a canvas, a frame,
     *          etc.)
     */
    public abstract void draw(Graphics g);
}