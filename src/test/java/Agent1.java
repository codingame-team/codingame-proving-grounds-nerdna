import java.util.*;

/**
 * Tactical RPG Party AI for Proving Grounds of Nerdna:
 * - Coordinates Fighter (frontline SLASH + GUARD), Mage (AoE FIREBALL / MAHALITO),
 *   and Cleric (HEAL / DIOS on injured party members)
 */
public class Agent1 {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int width = in.nextInt();
        int height = in.nextInt();
        int leagueLevel = in.nextInt();

        while (true) {
            String[] grid = new String[height];
            for (int y = 0; y < height; y++) grid[y] = in.next();

            int heroCount = in.nextInt();
            List<String[]> myHeroes = new ArrayList<>();
            List<String[]> enemies = new ArrayList<>();
            for (int i = 0; i < heroCount; i++) {
                String[] h = {
                    in.next(), in.next(), in.next(),
                    in.next(), in.next(), in.next(), in.next()
                };
                if ("0".equals(h[1])) myHeroes.add(h);
                else enemies.add(h);
            }

            int relicCount = in.nextInt();
            List<int[]> relics = new ArrayList<>();
            for (int i = 0; i < relicCount; i++) {
                relics.add(new int[]{in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()});
            }

            Set<Integer> assignedRelics = new HashSet<>();
            for (String[] h : myHeroes) {
                int hid = Integer.parseInt(h[0]);
                String role = h[2];
                int hx = Integer.parseInt(h[3]);
                int hy = Integer.parseInt(h[4]);
                int hp = Integer.parseInt(h[5]);
                int cd = Integer.parseInt(h[6]);

                // Choose target relic or closest enemy
                int tx = width / 2, ty = height / 2;
                int bestD = 9999;
                int chosenR = -1;
                for (int ri = 0; ri < relics.size(); ri++) {
                    int[] r = relics.get(ri);
                    int d = Math.abs(hx - r[1]) + Math.abs(hy - r[2]) + (assignedRelics.contains(ri) ? 6 : 0);
                    if (d < bestD) {
                        bestD = d;
                        tx = r[1];
                        ty = r[2];
                        chosenR = ri;
                    }
                }
                if (chosenR != -1) {
                    assignedRelics.add(chosenR);
                } else if (!enemies.isEmpty()) {
                    String[] e = enemies.get(0);
                    tx = Integer.parseInt(e[3]);
                    ty = Integer.parseInt(e[4]);
                }

                String action = "";
                if ("MAGE".equals(role) && cd == 0 && leagueLevel >= 2) {
                    for (String[] e : enemies) {
                        int ex = Integer.parseInt(e[3]), ey = Integer.parseInt(e[4]);
                        if (Math.abs(hx - ex) + Math.abs(hy - ey) <= 3) {
                            action = " FIREBALL " + ex + " " + ey;
                            break;
                        }
                    }
                } else if ("CLERIC".equals(role) && cd == 0 && leagueLevel >= 2) {
                    for (String[] ally : myHeroes) {
                        int ax = Integer.parseInt(ally[3]), ay = Integer.parseInt(ally[4]);
                        int ahp = Integer.parseInt(ally[5]);
                        if (ahp <= 20 && Math.abs(hx - ax) + Math.abs(hy - ay) <= 3) {
                            action = " HEAL " + ally[0];
                            break;
                        }
                    }
                }

                if (action.isEmpty()) {
                    for (String[] e : enemies) {
                        int ex = Integer.parseInt(e[3]), ey = Integer.parseInt(e[4]);
                        if (Math.abs(hx - ex) + Math.abs(hy - ey) <= 1) {
                            action = " SLASH " + e[0];
                            break;
                        }
                    }
                }

                if (action.isEmpty() && "FIGHTER".equals(role) && leagueLevel >= 3) {
                    for (String[] ally : myHeroes) {
                        if (!ally[0].equals(h[0])) {
                            int ax = Integer.parseInt(ally[3]), ay = Integer.parseInt(ally[4]);
                            if (Math.abs(hx - ax) + Math.abs(hy - ay) <= 1) {
                                action = " GUARD " + ally[0];
                                break;
                            }
                        }
                    }
                }

                System.out.println("MOVE " + tx + " " + ty + action);
            }
        }
    }
}
