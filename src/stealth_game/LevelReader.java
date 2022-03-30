/**
 * 
 */
package stealth_game;
import java.util.*;
import java.io.*;

/**
 * @author Austin Herrick
 * Class to read level CSV files. The reader tracks the intended height/width of the board, along
 * with the contents of each row. Each row is stored as a List array, columns are stored via a
 * LinkedList of rows. Later, GameBoard will parse these LinkedLists into an array, which will
 * be manipulated during gameplay
 */
public class LevelReader {

    private BufferedReader reader;
    private int height;
    private int width;
    private LinkedList<String[]> contents;
    
    /**
     * Loads a passed csv file containing game state information.
     * 
     * @param filePath - the path to the CSV file used to initialize the board state
     */
    public LevelReader(String filePath) throws IllegalArgumentException {
        try {
            FileReader fileReader = new FileReader(filePath);
            reader = new BufferedReader(fileReader);
            
        // if file cannot be loaded, catch exception and warn reader
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Cannot load file - please provide valid filepath");
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(
                    "File is null or absent - please provide valid filepath"
            );
        }
        
        // parse file
        parseFile();
    }
    
    /**
     * Parses the passed CSV file, assigning all elements to a series of LinkedLists
     *
     * @return a boolean indicating whether the FileLineIterator can produce
     *         another line from the file
     */   
    private void parseFile() throws IllegalArgumentException {
        
        // initialize width/height counters & LinkedList tracker
        height = 0;
        width = 0;
        contents = new LinkedList<String[]>();
        
        // iterate through passed csv
        String currentLine;
        try {
            while ((currentLine = reader.readLine()) != null) {
                // split currentLine by commas and append row
                String[] split = currentLine.split(",");
                contents.add(split);
                
                // increment height
                height += 1;
                
                // update width, or ensure csv is constant width
                if (width == 0) {
                    width = split.length;
                } else {
                    if (width != split.length) {
                        throw new IllegalArgumentException("Level is not square! Ensure csv file"
                                + "has equal width in all rows");
                    }
                }
            }
        
        // catch IOExceptions if they arise
        } catch (IOException e) {
            e.printStackTrace();
            
        // close file when fully parsed
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // accessor for all attributes
    public int getHeight() {
        return this.height;
    }
    
    public int getWidth() {
        return this.width;
    }
    
    // access board contents via a shallow copy, to ensure encapsulation
    @SuppressWarnings("unchecked")
    public LinkedList<String[]> getBoardList() {
        return (LinkedList<String[]>) this.contents.clone();
    }
}
