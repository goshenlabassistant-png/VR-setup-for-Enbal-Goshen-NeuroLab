package mainFlow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import classes.ExperimentData;
import classes.RewardStationDef;
import consts.Defs;
import java.awt.Robot;

public class ExperimentFlow {
    private BlenderConnection blender;
    private ArduinoConnection arduino;
    private FileSystem fileSystem;
    private boolean licked = true;
    private boolean onReward = false;
    private int lapNumber = 1;
    private String mazeLocation = "0";
    private int ttlNumber = 0;
    private long lastUpdateTime = 0;
    
    public ExperimentFlow(float radius, ExperimentData exp) {
        this.blender = new BlenderConnection(this, radius);
        this.arduino = new ArduinoConnection(this);
        this.fileSystem = new FileSystem(this, exp);
    }

    public void startExp(File maze, ArrayList<RewardStationDef> rewards) {
        try {
            new Thread(() -> this.preventScreenSaver()).start();
            blender.startGame(maze, rewards);
            arduino.connectArduino();
            fileSystem.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void updateMazeArgs(int lapNumber, boolean onReward, String mazeLocation) {
        this.lapNumber = lapNumber;
        this.mazeLocation = mazeLocation;

        if (onReward) { //! check if before was not reward?
            triggerGotToReward();
            this.onReward = true;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > Defs.UPDATE_TIME) {
            fileSystem.updateBehavioralFile();
            lastUpdateTime = currentTime;
        }
    }

    public void triggerGotToReward() {
        try {
            arduino.sendGotToReward();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void handleArduinoNumber(int number) {
        if (number == 100 || number == -100) { //! this is good only of for each touch there is an up and down signal. like a ttl.
            if (number == 100) {
                licked = true;
            }
            return; //! cant take down the 100 because then is moves
        }
        else if (number == 0) {
            this.ttlNumber += 1;
            fileSystem.updateFileOnTtl(Integer.toString(this.ttlNumber));
            return;
        }
        else {
            blender.move(number);
        }
    }

    public void finishRun() {
        try {
            fileSystem.updateBehavioralFile();
            arduino.disconnectArduino();
            blender.closeSocketConnection();
            fileSystem.stopLogging();
            fileSystem.makeOutputFiles();
            //! finish robot
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean fileSystemIsOnReward() {
        if (onReward) {
            onReward = false;
            return true;
        }
        return false;
    }
    
    public boolean fileSystemLick() {
        if (licked) {
            licked = false;
            return true;
        }

        return false;
    }

    public int getLapNumber() {
        return this.lapNumber;
    }

    public String getMazeLocation() {
        return this.mazeLocation;
    }

    public float getRadius() {
        return this.blender.getRadius();
    }

    public boolean isTtlOn() {
        return this.ttlNumber > 0;
    }

    public int getTtlNumber() {
        return this.ttlNumber;
    }

    public void preventScreenSaver() {
        try {
            Robot robot = new Robot();
            robot.setAutoDelay(60000); // Every 60 seconds
            
            // This will move the mouse slightly to prevent screen timeout
            robot.mouseMove(robot.getAutoDelay(), robot.getAutoDelay()); //! check that it works
        } catch (java.awt.AWTException e) {
            e.printStackTrace();
        }
    }
}
