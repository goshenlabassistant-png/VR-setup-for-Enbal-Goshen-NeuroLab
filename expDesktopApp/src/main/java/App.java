import java.io.File;
import java.util.ArrayList;

import classes.ExperimentData;
import classes.RewardStationDef;
import mainFlow.ExperimentFlow;

public class App {
    public static void main(String[] args) throws Exception {
        ExperimentData expData = new ExperimentData("GCamp_Gq19", "M1_trainig", "C:\\Users\\owner\\Desktop\\Shay");
        ExperimentFlow exp = new ExperimentFlow(35, expData);
        ArrayList<RewardStationDef> rewards = new ArrayList<>();
        rewards.add(new RewardStationDef(5, 1, 1, 0));
        rewards.add(new RewardStationDef(5, 1, 1, 0));
        rewards.add(new RewardStationDef(5, 1, 1, 0));
        exp.startExp(new File("C:\\Users\\owner\\Desktop\\new_bhv\\main_maze\\env _A_csv_random_rew_new_small_treadmill.blend.exe"), rewards);
    }
}
