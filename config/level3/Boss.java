import java.util.*;

class Player {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int width = in.nextInt();
        int height = in.nextInt();
        int leagueLevel = in.nextInt();

        while (true) {
            for (int y = 0; y < height; y++) in.next();
            int heroCount = in.nextInt();
            List<String[]> myHeroes = new ArrayList<>();
            List<String[]> oppHeroes = new ArrayList<>();
            for (int i = 0; i < heroCount; i++) {
                String[] h = {in.next(), in.next(), in.next(), in.next(), in.next(), in.next(), in.next()};
                if ("0".equals(h[1])) myHeroes.add(h);
                else oppHeroes.add(h);
            }
            int relicCount = in.nextInt();
            List<int[]> relics = new ArrayList<>();
            for (int i = 0; i < relicCount; i++) {
                relics.add(new int[]{in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()});
            }

            for (int idx = 0; idx < myHeroes.size(); idx++) {
                String[] h = myHeroes.get(idx);
                int hid = Integer.parseInt(h[0]);
                String role = h[2];
                int hx = Integer.parseInt(h[3]);
                int hy = Integer.parseInt(h[4]);
                int hp = Integer.parseInt(h[5]);
                int cd = Integer.parseInt(h[6]);

                // Choose movement target: nearest Relic (preferring unguarded or low-guardian in L3), else nearest enemy
                int tx = width / 2, ty = height / 2;
                int bestScore = 100000;
                for (int[] r : relics) {
                    int d = Math.abs(r[1] - hx) + Math.abs(r[2] - hy);
                    int penalty = (3 >= 3 && r[4] > 0 && !"FIGHTER".equals(role)) ? 4 : 0;
                    int sc = d * 3 + penalty - r[3] / 8 + (idx * 2 % 3);
                    if (sc < bestScore) {
                        bestScore = sc;
                        tx = r[1];
                        ty = r[2];
                    }
                }
                if (relics.isEmpty() && !oppHeroes.isEmpty()) {
                    String[] e = oppHeroes.get(0);
                    tx = Integer.parseInt(e[3]);
                    ty = Integer.parseInt(e[4]);
                }

                String action = "";
                if ("FIGHTER".equals(role) && cd == 0) {
                    // Guard if wounded and enemy is nearby, otherwise Slash adjacent enemy
                    String[] adjEnemy = null;
                    for (String[] e : oppHeroes) {
                        int ex = Integer.parseInt(e[3]), ey = Integer.parseInt(e[4]);
                        if (Math.abs(ex - hx) + Math.abs(ey - hy) == 1) {
                            adjEnemy = e;
                            break;
                        }
                    }
                    if (hp <= 14 && adjEnemy != null) {
                        action = " GUARD " + hid;
                    } else if (adjEnemy != null) {
                        action = " SLASH " + adjEnemy[0];
                    }
                } else if (3 >= 2 && "MAGE".equals(role) && cd == 0) {
                    // Cast FIREBALL at enemy hero within range <= 3 (or guarded relic in L3)
                    int fx = -1, fy = -1;
                    for (String[] e : oppHeroes) {
                        int ex = Integer.parseInt(e[3]), ey = Integer.parseInt(e[4]);
                        if (Math.abs(ex - hx) + Math.abs(ey - hy) <= 3) {
                            fx = ex; fy = ey;
                            break;
                        }
                    }
                    if (fx == -1 && 3 >= 3) {
                        for (int[] r : relics) {
                            if (r[4] > 0 && Math.abs(r[1] - hx) + Math.abs(r[2] - hy) <= 3) {
                                fx = r[1]; fy = r[2];
                                break;
                            }
                        }
                    }
                    if (fx != -1) {
                        action = " FIREBALL " + fx + " " + fy;
                    }
                } else if (3 >= 2 && "CLERIC".equals(role) && cd == 0) {
                    // Cast HEAL on most wounded ally within range <= 3
                    String healTarget = null;
                    int lowestHp = 999;
                    for (String[] a : myHeroes) {
                        int ax = Integer.parseInt(a[3]), ay = Integer.parseInt(a[4]), ahp = Integer.parseInt(a[5]);
                        int maxHp = "FIGHTER".equals(a[2]) ? 36 : ("MAGE".equals(a[2]) ? 22 : 28);
                        if (ahp < maxHp - 4 && Math.abs(ax - hx) + Math.abs(ay - hy) <= 3 && ahp < lowestHp) {
                            lowestHp = ahp;
                            healTarget = a[0];
                        }
                    }
                    if (healTarget != null) {
                        action = " HEAL " + healTarget;
                    }
                }

                System.out.println("MOVE " + tx + " " + ty + action);
            }
        }
    }
}
