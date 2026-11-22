import com.codingame.gameengine.runner.MultiplayerGameRunner;
import com.codingame.gameengine.runner.simulate.GameResult;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && "--simulate".equals(args[0])) {
            runBatchSimulation();
            return;
        }
        if (!ensureSystemClasspath(args)) {
            return;
        }

        MultiplayerGameRunner gameRunner = new MultiplayerGameRunner();
        gameRunner.setLeagueLevel(3);
        gameRunner.setSeed(42L);
        gameRunner.addAgent(Agent1.class, "WerdnaPartyAI");
        gameRunner.addAgent(Agent2.class, "DungeonBoss");

        gameRunner.start(8888);
    }

    /**
     * CodinGame's Renderer exports view/assets using ClassLoader.getSystemClassLoader().getResources("view").
     * When launched via `mvn exec:java`, Maven uses an isolated URLClassLoader and the system classloader
     * only contains plexus-classworlds.jar. Re-launching in a child JVM with the full URLClassLoader classpath
     * on the system classpath ensures view/assets are always exported properly.
     */
    private static boolean ensureSystemClasspath(String[] args) {
        if (ClassLoader.getSystemClassLoader().getResource("view") != null) {
            return true;
        }
        ClassLoader cl = Main.class.getClassLoader();
        if (cl instanceof URLClassLoader) {
            try {
                URL[] urls = ((URLClassLoader) cl).getURLs();
                StringBuilder cp = new StringBuilder();
                for (URL url : urls) {
                    if (cp.length() > 0) cp.append(File.pathSeparator);
                    cp.append(new File(url.toURI()).getAbsolutePath());
                }
                String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                List<String> cmd = new ArrayList<>();
                cmd.add(javaBin);
                cmd.add("-cp");
                cmd.add(cp.toString());
                cmd.add(Main.class.getName());
                cmd.addAll(Arrays.asList(args));
                Process p = new ProcessBuilder(cmd).inheritIO().start();
                Runtime.getRuntime().addShutdownHook(new Thread(p::destroy));
                p.waitFor();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private static void runBatchSimulation() {
        for (int league = 1; league <= 3; league++) {
            int p0Wins = 0, p1Wins = 0, draws = 0;
            for (int i = 1; i <= 5; i++) {
                MultiplayerGameRunner runner = new MultiplayerGameRunner();
                runner.setLeagueLevel(league);
                runner.setSeed(1000L * league + i);
                runner.addAgent(Agent1.class, "WerdnaPartyAI");
                runner.addAgent(Agent2.class, "DungeonBoss");

                GameResult result = runner.simulate();
                int s0 = result.scores.get(0);
                int s1 = result.scores.get(1);
                if (s0 > s1) p0Wins++;
                else if (s1 > s0) p1Wins++;
                else draws++;

                System.out.printf("[League %d | Seed %d] WerdnaPartyAI=%d vs DungeonBoss=%d (Frames=%d)%n",
                    league, 1000L * league + i, s0, s1, result.views.size());
            }
            System.out.printf("==> League %d Summary: WerdnaPartyAI Wins=%d, DungeonBoss Wins=%d, Draws=%d%n%n",
                league, p0Wins, p1Wins, draws);
        }
    }
}
