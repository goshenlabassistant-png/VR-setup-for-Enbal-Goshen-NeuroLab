package classes;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.jfree.data.json.impl.JSONArray;

import consts.Defs;

/**
 * Created by Moriah Eldar, based on Netai Benaim
 * for G-Lab, Hebrew University of Jerusalem
 * contact at: netai.benaim@mail.huji.ac.il
 * version:
 * <p>
 * this is RewardStation in experiments
 * created on 29/01/2025
 */

public class RewardStationDef implements Serializable {
    // number of rewards in a lap
    private int numInLap;
    // num of laps for this defenition. If -1 then it's infinite
    private int numLaps;
    // zones to put the rewards in. From 1 - REWARD_ZONES_NUMBER(5). Should be as the numInLap. If numInLap > 5, just puts rewards int the middle.
    private int[] zones;
    // for the sake of changing the rewards. From -1 - REWARDS_SPOTS_OPTIONS, where -1 is random, 0 is in the middle.
    private int[] placeInZone;
    // probability list of rewards
    private double[] probability;
    
    public RewardStationDef(int numRewardsInTheLap, int numLaps) {
        this.numInLap = numRewardsInTheLap;
        this.numLaps = numLaps;
        if (numRewardsInTheLap <= Defs.REWARD_ZONES_NUMBER) {
            this.zones = Defs.DefaultZones.get(numRewardsInTheLap);
            this.placeInZone = new int[numInLap];
            this.probability = new double[numInLap];
            for (int i = 0; i < numLaps; i++) {
                placeInZone[i] = 0;
                probability[i] = 1.0;
            }
        }
    }

    public RewardStationDef(int numRewardsInTheLap, int numLaps, int[] zones) {
        this.numInLap = numRewardsInTheLap;
        this.numLaps = numLaps;
        if (numRewardsInTheLap <= Defs.REWARD_ZONES_NUMBER) {
            this.zones = zones;
            this.placeInZone = new int[numInLap];
            this.probability = new double[numInLap];
            for (int i = 0; i < numLaps; i++) {
                placeInZone[i] = 0;
                probability[i] = 1.0;
            }
        }
        else {
            System.out.println("Error: numRewardsInTheLap is greater than " + Defs.REWARD_ZONES_NUMBER);
        }
    }

    public RewardStationDef(int numRewardsInTheLap, int numLaps, int[] zones, int[] placeInZone, double[] probability) {
        this.numInLap = numRewardsInTheLap;
        this.numLaps = numLaps;
        this.zones = zones;
        this.probability = probability;
        this.placeInZone = placeInZone;
    }

    // Get and set to all the fields
    public int getNumInLap() {
        if (numInLap <= 0) {
            throw new IllegalArgumentException("numInLap must be positive");
        }
        return numInLap;
    }

    public void setNumInLap(int numInLap) {
        this.numInLap = numInLap;
    }

    public int getNumLaps() {
        return numLaps;
    }

    public void setNumLaps(int numLaps) {
        if (numLaps < -1 || numLaps == 0) {
            throw new IllegalArgumentException("numLaps must be between positive or -1 for infinite");
        }
        this.numLaps = numLaps;
    }

    /**
     * calculates rewards psitions in a circle.
     * @return an array with a list of reward positions.
     */
    public ArrayList<Double> calculateRewardsPosition() {
        ArrayList<Double> positions = new ArrayList<Double>();
        double blendMazeCircumference = Math.PI * 2;
        
        if (numInLap > Defs.REWARD_ZONES_NUMBER) {
            // divide the maze into equal sections for the rewards to be in
            double sectionLength = blendMazeCircumference / numInLap;
            for (int j = 0; j < numInLap; j++) {
                // calculate the reward position
                positions.add(sectionLength * j + sectionLength / 2);
            }
        }
        else if (numInLap > 0) {
            double blendMazePartsSize = blendMazeCircumference / Defs.REWARD_ZONES_NUMBER;
            for (int j = 0; j < numInLap; j++) {
                if (Math.random() < probability[j]) {
                    double rewardPosition = blendMazePartsSize * (zones[j] - 1);
                    if (placeInZone[j] == -1) {
                        rewardPosition += Math.random() * blendMazePartsSize;
                    } 
                    else {
                        if (placeInZone[j] == 0) {
                            rewardPosition += blendMazePartsSize / 2;
                        } 
                        else {
                            double sectionZones = blendMazePartsSize / Defs.REWARDS_SPOTS_AMOUNT;
                            rewardPosition += sectionZones * (placeInZone[j] - 1) + sectionZones / 2;
                        }
                    }
                    positions.add(rewardPosition);
                }
            }
        }

        return positions;
    }

    
}
