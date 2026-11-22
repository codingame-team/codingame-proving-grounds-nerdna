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

            for (String[] h : myHeroes) {
                int hx = Integer.parseInt(h[3]), hy = Integer.parseInt(h[4]);
                int tx = width / 2, ty = height / 2;
                if (!relics.isEmpty()) {
                    tx = relics.get(0)[1];
                    ty = relics.get(0)[2];
                } else if (!enemies.isEmpty()) {
                    tx = Integer.parseInt(enemies.get(0)[3]);
                    ty = Integer.parseInt(enemies.get(0)[4]);
                }
                String act = "";
                for (String[] e : enemies) {
                    int ex = Integer.parseInt(e[3]), ey = Integer.parseInt(e[4]);
                    if (Math.abs(hx - ex) + Math.abs(hy - ey) <= 1) {
                        act = " SLASH " + e[0];
                        break;
                    }
                }
                System.out.println("MOVE " + tx + " " + ty + act);
            }
        }
    }
}
