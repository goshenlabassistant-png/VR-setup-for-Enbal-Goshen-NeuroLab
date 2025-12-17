package mainFlow;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import classes.RewardStationDef;
import consts.Defs;

public class BlenderConnection {
    private float radius; //! raduis is or default or set with the maze. it is nonsece it's not together
    private Socket socket;
    private DataOutputStream out = null;
    private ExperimentFlow exp = null;
    private boolean isConnected = false;

    private static Properties properties = new Properties();

    static {
        try (InputStream input = BlenderConnection.class.getClassLoader().getResourceAsStream("blenderConnection.properties")) {
            if (input == null) {
                throw new IOException("Sorry, unable to find blenderConnection.properties");
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public BlenderConnection(ExperimentFlow exp, float radius) {
        this.exp = exp;
        this.radius = radius;
    }

    private void loadMaze(File maze, String rewardList) {
        try {
            // Create ProcessBuilder with command
            ProcessBuilder pb = new ProcessBuilder(
                maze.getAbsolutePath(),  // Path to the Blender executable
                "--",  // Optional arguments
                rewardList.replace("\"", "\\\"")
            );
            
            // Redirect output to console
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            // Start the process
            Process process = pb.start();
            
            // Wait for completion
            int exitCode = process.waitFor();
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startGame(File maze, ArrayList<RewardStationDef> rewards) {
        // connect the blender socket

        // Start Blender game with parameters
        String rewardList = calculateRewardList(rewards).toString();
        new Thread(() -> this.loadMaze(maze, rewardList)).start();
        new Thread(() -> this.configureSocket()).start();
        //! check if arduino is in a thread (should be)
        //! check not to stop the program

    }

    /**
     * Configure the socket connection to the blender
     */
    private void configureSocket() {
        while (true) {
            try {
                this.socket = new Socket(Defs.BLENDER_IP, Defs.BLENDER_PORT);
                isConnected = true;
                System.out.println("Connected!");
                break;
            } catch (IOException e) {
                System.out.println("still trying");
                try {
                    Thread.sleep(1000); // wait 1 second before retrying
                } catch (InterruptedException ignored) {}
            }
        }

        // configure out socket
        //! TODO what to do if an error accurs here? still safe to do input? close connection?
        try {
            this.out = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // configure constantly listening to data
        new Thread(() -> {
            try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        JSONObject receivedJson = new JSONObject(line.trim());
                        int lapNumber = receivedJson.getInt(properties.getProperty("recieve.data.laps.name"));
                        boolean onReward = receivedJson.getBoolean(properties.getProperty("recieve.data.reward.name"));
                        String mazeLocation = receivedJson.get(properties.getProperty("recieve.data.location.name")).toString();
                        
                        exp.updateMazeArgs(lapNumber, onReward, mazeLocation);
                    } catch (Exception e) {
                        System.err.println("Failed to parse JSON: " + line);
                    }
                }

                // If we reach here, the connection was closed by Blender
                System.out.println("Connection closed by Blender.");
                exp.finishRun(); // finish the run and close all connections
            } catch (IOException e) {
                System.err.println("Blender socket closed or errored: " + e.getMessage());
                exp.finishRun();
            }
        }).start();
    }

    /**
     * Move on the blender file
     * @param movment = -1/1/0
     */
    public void move(int movement) {
        // send to blender the movment that should have
        if(movement != 0 && out != null) {
            movement = movement * -1;
            try {
                out.write(String.valueOf(movement + "\n").getBytes(StandardCharsets.UTF_8)); // Send message to servers
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // public JSONArray calculateRewardList(ArrayList<RewardStationDef> rewardsDefs) {
    //     // save for if number is more then 5.
    //     // calculate the rewards list
    //     JSONArray rewardsLists = new JSONArray();
    //     for (RewardStationDef rewardDef : rewardsDefs) {
    //         double blendMazeCircumference = Math.PI * 2;
    //         for (int i = 0; i < rewardDef.getNumLaps(); i++) { // TODO short this when prob = 1 and not random
    //             // divide the maze into equal sections for the rewards to be in
    //             JSONArray rewards = new JSONArray();
    //             double sectionLength = blendMazeCircumference / rewardDef.getNumInLap();
    //             for (int j = 0; j < rewardDef.getNumInLap(); j++) {
    //                 if (Math.random() < rewardDef.getProbability()) {
    //                     // calculate the reward position
    //                     double rewardPosition = sectionLength * j;
    //                     int placeInZone = rewardDef.getPlaceInZone();
    //                     if (placeInZone == -1) {
    //                         rewardPosition += Math.random() * sectionLength;
    //                     } 
    //                     else {
    //                         if (placeInZone == 0) {
    //                             rewardPosition += sectionLength / 2;
    //                         } 
    //                         else {
    //                             double sectionZones = sectionLength / Defs.REWARDS_SPOTS_AMOUNT;
    //                             rewardPosition += sectionZones * (placeInZone - 1) + sectionZones / 2;
    //                         }
    //                     }

    //                     // calculate the reward position in the maze
    //                     //? maze is oppisite. Should I move this code to the maze?
    //                     double x = Math.cos(-rewardPosition) * this.radius;
    //                     double y = Math.sin(-rewardPosition) * this.radius;

    //                     // add the reward to the list of rewards for this lap
    //                     rewards.put(new JSONArray(new Double[] { x, y }));
    //                 }
    //             }
    //             // add the list of rewards for this lap to the list of rewards for all laps
    //             rewardsLists.put(rewards);
    //         }
    //     }
    //     return rewardsLists;
    // }

    public JSONArray calculateRewardList(ArrayList<RewardStationDef> rewardsDefs) {
        // calculate the rewards list
        JSONArray rewardsLists = new JSONArray();
        for (RewardStationDef rewardDef : rewardsDefs) {
            for (int i = 0; i < rewardDef.getNumLaps(); i++) {
                JSONArray rewards = new JSONArray();
                ArrayList<Double> rewardPositions = rewardsDefs.get(i).calculateRewardsPosition();
                for (double rewardPosition : rewardPositions) {
                    // calculate the reward position in the maze
                    //? maze is oppisite. Should I move this code to the maze?
                    double x = Math.cos(-rewardPosition) * this.radius;
                    double y = Math.sin(-rewardPosition) * this.radius;

                    // add the reward to the list of rewards for this lap
                    rewards.put(new JSONArray(new Double[] { x, y }));
                }
                // add the list of rewards for this lap to the list of rewards for all laps
                rewardsLists.put(rewards);
            }
        }
        return rewardsLists;
    }

    public void closeSocketConnection() {
        try {
            if(this.socket != null) {
                this.socket.close();
            }
            this.out = null;
            this.isConnected = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public float getRadius() {
        return this.radius; // return the radius as an int
    }
}
