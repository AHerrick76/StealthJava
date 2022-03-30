package stealth_game;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

/**
 * A basic game object of the game panel. Loads an image from a specified image file.
 */
public class Sprite extends GameObj {
    
    private BufferedImage img;

    public Sprite(int posX, int posY, int size, String imgFile) {
        super(posX, posY, size, size);
        
        try {
            img = ImageIO.read(new File(imgFile));
        } catch (IOException e) {
            System.out.println("Internal Error:" + e.getMessage());
        }
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(img, this.getPx(), this.getPy(), this.getWidth(), this.getHeight(), null);
    }

}
