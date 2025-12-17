import java.io.File;
import java.nio.file.FileSystem;
import java.util.ArrayList;

import classes.ExperimentData;
import classes.RewardStationDef;
import mainFlow.ExperimentFlow;

public class App {
    public static void main(String[] args) throws Exception {
        ExperimentData expData = new ExperimentData("GCamp_Gq20", "M1_trainig", "C:\\Users\\owner\\Desktop\\Shay");
        ExperimentFlow exp = new ExperimentFlow(35, expData);
        ArrayList<RewardStationDef> rewards = new ArrayList<>();

        rewards.add(new RewardStationDef(5, 2));
        // rewards.add(new RewardStationDef(4, 2, 
        //     new Integer[] {1,2,4,5}, // zones
        //     new Integer[] {0,0,1,-1}, // placeInZone
        //     new Double[] {1.0,0.9,0.7,1.0})); // probability

        exp.startExp(new File("C:\\Users\\owner\\Desktop\\new_bhv\\main_maze\\env _A_csv_random_rew_new_small_treadmill.blend.exe"), rewards);
    }
}
