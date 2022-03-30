package stealth_game;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.json.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

/**
 * Game Main class that specifies the frame and widgets of the GUI
 */
public class RunStealthGame implements Runnable {
    
    // tracks list of possible levels
    private int currentLevel;
    private String fullRules;
    private String levelListTxt;
    private Boolean baseLevels;
    
    // tracks level information
    private HashMap<Integer, HashMap<String, String>> baseLevelDictionary;
    private HashMap<Integer, HashMap<String, String>> expansionLevelDictionary;
    
    private Boolean expansion;

    public RunStealthGame() {
    }

    @Override
    public void run() {
        
        // test json stuff
        parseLevelJson();
        expansion = false;
        currentLevel = 0;
        
        // retrieves list of valid levels
        retrieveRules();
        retrieveLevelList();
        currentLevel = 0;
        baseLevels = true;
        
        // Top-level frame in which game components live.
        final JFrame frame = new JFrame("StealthJava");
        frame.setLocation(300, 300);
        
        // Status panel
        final JPanel status_panel = new JPanel();
        frame.add(status_panel, BorderLayout.SOUTH);
        final JLabel status = new JLabel("Running...");
        status_panel.add(status);

        // Main playing area
        String filename = baseLevelDictionary.get(0).get("filename");
        final GamePanel gPanel = new GamePanel(filename, status);
        frame.add(gPanel, BorderLayout.CENTER);

        // Reset button
        final JPanel control_panel = new JPanel();
        frame.add(control_panel, BorderLayout.NORTH);

        // Note here that when we add an action listener to the reset button, we
        // define it as an anonymous inner class that is an instance of
        // ActionListener with its actionPerformed() method overridden. When the
        // button is pressed, actionPerformed() will be called.
        final JButton reset = new JButton("Reset");
        reset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gPanel.reset();
            }
        });
        
        // Creates a button that walks back to the previous level
        final JButton prevLevel = new JButton("Previous Level");
        prevLevel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                recedeLevel(gPanel);
            }
        });
        // Creates a button that advances to the next level
        final JButton nextLevel = new JButton("Next Level");
        nextLevel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                advanceLevel(gPanel);
            }
        });
        
        // Creates a button that displays the rules
        final JButton rules = new JButton("Rules");
        rules.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // display text in popup - technique taken from
                // https://stackoverflow.com/questions/7080205/popup-message-boxes
                JOptionPane.showMessageDialog(null, fullRules, "StealthJava Rules", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        // Creates a button that displays the level list
        final JButton levelList = new JButton("Level List");
        levelList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // display text in popup
                JOptionPane.showMessageDialog(null, levelListTxt, "StealthJava Level List", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        // Creates a button that swaps between expansions
        final JButton expansionButton = new JButton("Swap to Expansion");
        expansionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // swap level lists
                currentLevel = 0;
                if (expansion) {
                    expansion = false;
                    expansionButton.setText("Swap to Expansion");
                } else {
                    expansion = true;
                    expansionButton.setText("Swap to Base Game");
                }
                // reload level
                prepCurrentLevel(gPanel);
            }
        });
        
        // adds buttons to control panel
        control_panel.add(rules);
        control_panel.add(prevLevel);
        control_panel.add(reset);
        control_panel.add(nextLevel);
        control_panel.add(levelList);
        control_panel.add(expansionButton);

        // Put the frame on the screen
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Start game
        gPanel.reset();
    }
    
    /**
     * advances the level index if a next level exists. If one does, also refreshes the GUI board
     * 
     * @param gPanel - the game panel object
     */
    private void advanceLevel(GamePanel gPanel) {
        Integer levelCount = 0;
        if (expansion) {
            levelCount = Collections.max(expansionLevelDictionary.keySet());
        } else {
            levelCount = Collections.max(baseLevelDictionary.keySet());
        }
        System.out.print(levelCount);
                
        if (currentLevel < levelCount) {
            currentLevel += 1;
            prepCurrentLevel(gPanel);
        }
    }
    
    /**
     * decrements the level index if a previous level exists. If one does, also refreshes the 
     * GUI board
     * 
     * @param gPanel - the game panel object
     */
    private void recedeLevel(GamePanel gPanel) {
        if (currentLevel > 0) {
            currentLevel -= 1;
            prepCurrentLevel(gPanel);
        }
    }
    
    /**
     * Loads the Rules.txt file and parses to a string to be displayed
     */
    private void retrieveRules() {
        // load contents from Rules.txt, catching any exceptions
        try {
            FileReader fileReader = new FileReader("files/Rules.txt");
            BufferedReader reader = new BufferedReader(fileReader);
            fullRules = "";
            String currentLine;
            try {
                while ((currentLine = reader.readLine()) != null) {
                    fullRules += currentLine;
                    fullRules += "\n";
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    }
    
    /**
     * Loads the LevelList.txt file and parses to a string to be displayed
     */
    private void retrieveLevelList() {
        // load contents from Rules.txt, catching any exceptions
        try {
            FileReader fileReader = new FileReader("files/LevelList.txt");
            BufferedReader reader = new BufferedReader(fileReader);
            levelListTxt = "";
            String currentLine;
            try {
                while ((currentLine = reader.readLine()) != null) {
                    levelListTxt += currentLine;
                    levelListTxt += "\n";
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    }
    
    /**
     * Parses levelParser.json to determine characteristics of levels
     */
    @SuppressWarnings("unchecked")
    private void parseLevelJson() {
        
        // initialize vars
        JSONParser parser = new JSONParser();
        baseLevelDictionary = new HashMap<Integer, HashMap<String, String>>();
        expansionLevelDictionary = new HashMap<Integer, HashMap<String, String>>();
        
        // construct java parser
        try {
            Object obj = parser.parse(new FileReader("files/levelParser.json"));
            JSONObject jsonObject = (JSONObject) obj;
            
            // iterate through level keys and construct dictionary structure
            Set<String> allLevels = jsonObject.keySet();
            Iterator<String> levelIter = allLevels.iterator();
            while (levelIter.hasNext()) {
                String levelName = levelIter.next();
                JSONObject levelInstance = (JSONObject) jsonObject.get(levelName);
                
                // fetch all attributes and add to levelMap
                String filename = (String) levelInstance.get("filename");
                String tileset = String.valueOf(levelInstance.get("tileset"));
                String darkLevel = String.valueOf(levelInstance.get("darkLevel"));
                String visionRadius = String.valueOf(levelInstance.get("visionRadius"));
                Boolean expansion = (Boolean) levelInstance.get("expansion");
                Integer levelIndex = Integer.valueOf(String.valueOf(levelInstance.get("levelIndex")));
                
                HashMap<String, String> levelMap = new HashMap<String, String>();
                levelMap.put("filename", filename);
                levelMap.put("tileset", tileset);
                levelMap.put("darkLevel", darkLevel);
                levelMap.put("visionRadius", visionRadius);
                levelMap.put("levelName", levelName);
                
                if (expansion) {
                    expansionLevelDictionary.put(levelIndex, levelMap);
                } else {
                    baseLevelDictionary.put(levelIndex, levelMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Retrieves all information necessary to call setCurrentLevel in the GamePanel
     */
    private void prepCurrentLevel(GamePanel gPanel) {
        
        // retrieve levelMap
        HashMap<String, String> currentLevelEntry = new HashMap<String, String>();
        if (expansion) {
            currentLevelEntry = expansionLevelDictionary.get(currentLevel);
        } else {
            currentLevelEntry = baseLevelDictionary.get(currentLevel);
        }
        
        String filename = currentLevelEntry.get("filename");
        Boolean darkLevel = Boolean.parseBoolean(currentLevelEntry.get("darkLevel"));
        Integer visionRadius = Integer.valueOf(currentLevelEntry.get("visionRadius"));
        Integer tileset = Integer.valueOf(currentLevelEntry.get("tileset"));
        String levelName = currentLevelEntry.get("levelName");
        
        // set current level using retrieved info
        gPanel.setCurrentLevel(filename, currentLevel, darkLevel, visionRadius, tileset);
        
        // update displayed levelName (TODO)
    }

}
