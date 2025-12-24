import java.io.File;
import java.nio.file.FileSystem;
import java.util.ArrayList;

import classes.ExperimentData;
import classes.RewardStationDef;
import mainFlow.ExperimentFlow;

public class App {
    public static void main(String[] args) throws Exception {
        ExperimentData expData = new ExperimentData("GCamp_Gq19 ", "M1_training", "C:\\Users\\owner\\Desktop\\Shay");
        ExperimentFlow exp = new ExperimentFlow(35, expData);
        ArrayList<RewardStationDef> rewards = new ArrayList<>();

        // rewards.add(new RewardStationDef(1, 2));
        rewards.add(new RewardStationDef(5, 2, 
            new int[] {1,2,3,4,5}, // zones
            new int[] {0,0,0,0,0}, // placeInZone
            new double[] {1.0,1.0,1.0,1.0,1.0})); // probability

        exp.startExp(new File("C:\\Users\\owner\\Desktop\\new_bhv\\main_maze\\env _A_csv_random_rew_new_small_treadmill.blend.exe"), rewards);
    }
}
