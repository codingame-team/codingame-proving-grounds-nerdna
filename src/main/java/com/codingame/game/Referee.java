package com.codingame.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.endscreen.EndScreenModule;
import com.codingame.gameengine.module.entities.Circle;
import com.codingame.gameengine.module.entities.Curve;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Group;
import com.codingame.gameengine.module.entities.Polygon;
import com.codingame.gameengine.module.entities.Rectangle;
import com.codingame.gameengine.module.entities.Shape;
import com.codingame.gameengine.module.entities.Sprite;
import com.codingame.gameengine.module.entities.Text;
import com.codingame.gameengine.module.toggle.ToggleModule;
import com.codingame.gameengine.module.tooltip.TooltipModule;
import com.google.inject.Inject;

public class Referee extends AbstractReferee {
    public static final int WIDTH = 11;
    public static final int HEIGHT = 11;
    public static final int MAX_TURNS = 100;
    private static final int CELL_SIZE = 76;
    private static final int ORIGIN_X = (1920 - WIDTH * CELL_SIZE) / 2;
    private static final int ORIGIN_Y = (1080 - HEIGHT * CELL_SIZE) / 2 + 25;

    private static final int[] DX = {0, 1, 0, -1};
    private static final int[] DY = {-1, 0, 1, 0};

    @Inject private MultiplayerGameManager<Player> gameManager;
    @Inject private GraphicEntityModule graphicEntityModule;
    @Inject private TooltipModule tooltipModule;
    @Inject private EndScreenModule endScreenModule;
    @Inject private ToggleModule toggleModule;

    private final char[][] grid = new char[HEIGHT][WIDTH];
    private final List<Hero> allHeroes = new ArrayList<>();
    private final Map<Integer, Hero> heroById = new HashMap<>();
    private final List<Relic> relics = new ArrayList<>();
    private Text turnText;
    private Sprite fxFireball;
    private Sprite fxHeal;
    private Sprite fxSlash;
    private int leagueLevel;

    private int cellX(int x) {
        return ORIGIN_X + x * CELL_SIZE + CELL_SIZE / 2;
    }

    private int cellY(int y) {
        return ORIGIN_Y + y * CELL_SIZE + CELL_SIZE / 2;
    }

    @Override
    public void init() {
        Random rng = gameManager.getRandom();
        leagueLevel = Math.max(1, gameManager.getLeagueLevel());

        gameManager.setMaxTurns(MAX_TURNS);
        gameManager.setFirstTurnMaxTime(1000);
        gameManager.setTurnMaxTime(50);
        gameManager.setFrameDuration(550);

        generateDungeon(rng);
        spawnParties();
        initGraphics();

        for (Player player : gameManager.getActivePlayers()) {
            player.sendInputLine(String.format("%d %d %d", WIDTH, HEIGHT, leagueLevel));
        }
    }

    private void generateDungeon(Random rng) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                grid[y][x] = '.';
            }
        }
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x <= WIDTH / 2; x++) {
                if (x == WIDTH / 2 && y > HEIGHT / 2) continue;
                if (rng.nextDouble() < 0.16) {
                    grid[y][x] = '#';
                    grid[HEIGHT - 1 - y][WIDTH - 1 - x] = '#';
                }
            }
        }
        // Carve guaranteed dungeon corridors
        int mid = WIDTH / 2;
        for (int i = 1; i < WIDTH - 1; i++) {
            grid[mid][i] = '.';
            grid[2][i] = '.';
            grid[HEIGHT - 3][i] = '.';
            grid[i][mid] = '.';
            grid[i][2] = '.';
            grid[i][WIDTH - 3] = '.';
        }
        // Clear spawn rooms
        for (int y = 3; y <= 7; y++) {
            grid[y][1] = '.';
            grid[y][WIDTH - 2] = '.';
        }

        // Place 5 symmetric Relics (with Undead Guardians in League 3)
        relics.clear();
        int rid = 0;
        int guardHp = (leagueLevel >= 3) ? 14 : 0;
        relics.add(new Relic(rid++, mid, mid, 25, guardHp));
        int[][] pairs = {{mid, 2}, {3, 2}};
        for (int[] p : pairs) {
            int rx = p[0], ry = p[1];
            grid[ry][rx] = '.';
            grid[HEIGHT - 1 - ry][WIDTH - 1 - rx] = '.';
            relics.add(new Relic(rid++, rx, ry, 15, guardHp));
            relics.add(new Relic(rid++, WIDTH - 1 - rx, HEIGHT - 1 - ry, 15, guardHp));
        }
    }

    private void spawnParties() {
        Player p0 = gameManager.getPlayer(0);
        Player p1 = gameManager.getPlayer(1);
        Hero.Role[] roles = (leagueLevel == 1)
            ? new Hero.Role[]{Hero.Role.FIGHTER, Hero.Role.FIGHTER}
            : new Hero.Role[]{Hero.Role.FIGHTER, Hero.Role.MAGE, Hero.Role.CLERIC};
        int[][] spawns = {{1, 5}, {1, 3}, {1, 7}};

        int hid = 0;
        for (int i = 0; i < roles.length; i++) {
            Hero h0 = new Hero(hid++, p0, roles[i], spawns[i][0], spawns[i][1]);
            p0.addHero(h0);
            allHeroes.add(h0);
            heroById.put(h0.getId(), h0);
        }
        for (int i = 0; i < roles.length; i++) {
            int sx = WIDTH - 1 - spawns[i][0];
            int sy = HEIGHT - 1 - spawns[i][1];
            Hero h1 = new Hero(hid++, p1, roles[i], sx, sy);
            p1.addHero(h1);
            allHeroes.add(h1);
            heroById.put(h1.getId(), h1);
        }
    }

    private void initGraphics() {
        graphicEntityModule.createSprite()
            .setImage("background.jpg")
            .setBaseWidth(1920)
            .setBaseHeight(1080)
            .setZIndex(-20);

        graphicEntityModule.createRectangle()
            .setX(ORIGIN_X - 14)
            .setY(ORIGIN_Y - 14)
            .setWidth(WIDTH * CELL_SIZE + 28)
            .setHeight(HEIGHT * CELL_SIZE + 28)
            .setFillColor(0x0c101a)
            .setAlpha(0.88)
            .setLineWidth(4)
            .setLineColor(0x4a5568)
            .setZIndex(-15);

        graphicEntityModule.createText("PROVING GROUNDS OF WERDNA")
            .setX(960)
            .setY(40)
            .setAnchor(0.5)
            .setFontFamily("Lato")
            .setFontSize(42)
            .setFillColor(0xffffff)
            .setStrokeThickness(4)
            .setStrokeColor(0x000000)
            .setZIndex(30);

        turnText = graphicEntityModule.createText("TURN 0 / " + MAX_TURNS)
            .setX(960)
            .setY(92)
            .setAnchor(0.5)
            .setFontFamily("Lato")
            .setFontSize(36)
            .setFillColor(0xf6e05e)
            .setZIndex(30);

        // Player HUD Panels
        for (Player p : gameManager.getPlayers()) {
            int idx = p.getIndex();
            int hudX = idx == 0 ? 235 : 1685;

            graphicEntityModule.createRectangle()
                .setX(hudX - 195)
                .setY(180)
                .setWidth(390)
                .setHeight(330)
                .setFillColor(0x161c28)
                .setAlpha(0.90)
                .setLineWidth(4)
                .setLineColor(p.getColorToken())
                .setZIndex(20);

            graphicEntityModule.createSprite()
                .setImage(p.getAvatarToken())
                .setX(hudX)
                .setY(250)
                .setAnchor(0.5)
                .setBaseWidth(86)
                .setBaseHeight(86)
                .setZIndex(22);

            graphicEntityModule.createText(p.getNicknameToken())
                .setX(hudX)
                .setY(325)
                .setAnchor(0.5)
                .setFontFamily("Lato")
                .setFontSize(36)
                .setFillColor(p.getColorToken())
                .setZIndex(22);

            Text scoreTxt = graphicEntityModule.createText("SCORE: 0")
                .setX(hudX)
                .setY(390)
                .setAnchor(0.5)
                .setFontFamily("Lato")
                .setFontSize(40)
                .setFillColor(0xf6e05e)
                .setZIndex(22);
            p.setScoreText(scoreTxt);

            Text partyTxt = graphicEntityModule.createText("HEROES: " + p.getAliveHeroes().size())
                .setX(hudX)
                .setY(448)
                .setAnchor(0.5)
                .setFontFamily("Lato")
                .setFontSize(36)
                .setFillColor(0x90cdf4)
                .setZIndex(22);
            p.setPartyText(partyTxt);
        }

        // Dungeon Tiles (Sprited + Geometric Debug Outline)
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int cx = cellX(x);
                int cy = cellY(y);
                boolean isWall = grid[y][x] == '#';

                String img = isWall ? "pillar.png" : (((x + y) % 2 == 0) ? "floor1.png" : "floor2.png");
                Sprite tileSprite = graphicEntityModule.createSprite()
                    .setImage(img)
                    .setX(cx)
                    .setY(cy)
                    .setAnchor(0.5)
                    .setBaseWidth(CELL_SIZE - 2)
                    .setBaseHeight(CELL_SIZE - 2)
                    .setZIndex(1);
                toggleModule.displayOnToggleState(tileSprite, "renderMode", true);

                Rectangle debugTile = graphicEntityModule.createRectangle()
                    .setX(cx - CELL_SIZE / 2 + 2)
                    .setY(cy - CELL_SIZE / 2 + 2)
                    .setWidth(CELL_SIZE - 4)
                    .setHeight(CELL_SIZE - 4)
                    .setFillColor(isWall ? 0x3b4252 : 0x181e29)
                    .setLineWidth(2)
                    .setLineColor(isWall ? 0x81a1c1 : 0x2e3440)
                    .setZIndex(1);
                toggleModule.displayOnToggleState(debugTile, "renderMode", false);
            }
        }

        // Relics & Undead Guardians (Dual-Mode: RPG Sprites vs Geometric Diamond/Hexagon)
        for (Relic r : relics) {
            Sprite chest = graphicEntityModule.createSprite()
                .setImage("chest_closed.png")
                .setAnchor(0.5)
                .setBaseWidth(50)
                .setBaseHeight(50);

            Sprite guard = graphicEntityModule.createSprite()
                .setImage("monster_skeleton.png")
                .setY(-14)
                .setAnchor(0.5)
                .setBaseWidth(54)
                .setBaseHeight(54)
                .setVisible(r.getGuardianHp() > 0);
            toggleModule.displayOnToggleState(chest, "renderMode", true);
            toggleModule.displayOnToggleState(guard, "renderMode", true);

            Polygon debugDiamond = graphicEntityModule.createPolygon()
                .addPoint(0, -24)
                .addPoint(24, 0)
                .addPoint(0, 24)
                .addPoint(-24, 0)
                .setFillColor(0xf6e05e)
                .setLineWidth(3)
                .setLineColor(0xffffff);
            toggleModule.displayOnToggleState(debugDiamond, "renderMode", false);

            Group rg = graphicEntityModule.createGroup(chest, guard, debugDiamond)
                .setX(cellX(r.getX()))
                .setY(cellY(r.getY()))
                .setZIndex(5);

            r.setChestSprite(chest);
            r.setGuardianSprite(guard);
            r.setRootGroup(rg);
            updateRelicTooltip(r);
        }

        // Heroes (Dual-Mode: Layered RPG Sprites with Aura & HP Bar vs Class-Distinct Geometric Shapes)
        for (Hero h : allHeroes) {
            int pColor = h.getOwner().getColorToken();

            // 1. RPG Sprite Mode View
            Circle aura = graphicEntityModule.createCircle()
                .setRadius(28)
                .setFillColor(pColor)
                .setFillAlpha(0.32)
                .setLineWidth(3)
                .setLineColor(pColor);

            Sprite charSprite = graphicEntityModule.createSprite()
                .setImage(h.getRole().spriteAsset)
                .setAnchor(0.5)
                .setBaseWidth(58)
                .setBaseHeight(58);

            Rectangle hpBarFill = graphicEntityModule.createRectangle()
                .setX(-24)
                .setY(-36)
                .setWidth(48)
                .setHeight(7)
                .setFillColor(0x48bb78);
            h.setHpBarFill(hpBarFill);

            Group spriteView = graphicEntityModule.createGroup(aura, charSprite, hpBarFill);
            toggleModule.displayOnToggleState(spriteView, "renderMode", true);
            h.setSpriteView(spriteView);

            // 2. Geometric Debug Mode View (Square=Fighter, Diamond=Mage, Triangle=Cleric)
            Shape<?> geoShape;
            if (h.getRole() == Hero.Role.FIGHTER) {
                geoShape = graphicEntityModule.createRectangle()
                    .setX(-22)
                    .setY(-22)
                    .setWidth(44)
                    .setHeight(44)
                    .setFillColor(pColor)
                    .setLineWidth(3)
                    .setLineColor(0xffffff);
            } else if (h.getRole() == Hero.Role.MAGE) {
                geoShape = graphicEntityModule.createPolygon()
                    .addPoint(0, -26)
                    .addPoint(26, 0)
                    .addPoint(0, 26)
                    .addPoint(-26, 0)
                    .setFillColor(pColor)
                    .setLineWidth(3)
                    .setLineColor(0xffffff);
            } else {
                geoShape = graphicEntityModule.createPolygon()
                    .addPoint(0, -25)
                    .addPoint(24, 20)
                    .addPoint(-24, 20)
                    .setFillColor(pColor)
                    .setLineWidth(3)
                    .setLineColor(0xffffff);
            }

            Text debugLbl = graphicEntityModule.createText(h.getRole().shortCode + h.getId())
                .setAnchor(0.5)
                .setFontFamily("Lato")
                .setFontSize(20)
                .setFillColor(0xffffff)
                .setStrokeThickness(2)
                .setStrokeColor(0x000000);
            h.setDebugLabel(debugLbl);

            Group debugView = graphicEntityModule.createGroup(geoShape, debugLbl);
            toggleModule.displayOnToggleState(debugView, "renderMode", false);
            h.setDebugView(debugView);

            Group root = graphicEntityModule.createGroup(spriteView, debugView)
                .setX(cellX(h.getX()))
                .setY(cellY(h.getY()))
                .setZIndex(10);
            h.setRootGroup(root);
            updateHeroTooltip(h);
        }

        // Reusable spell/combat FX sprites
        fxFireball = graphicEntityModule.createSprite()
            .setImage("fx_fireball.png")
            .setAnchor(0.5)
            .setBaseWidth(84)
            .setBaseHeight(84)
            .setVisible(false)
            .setZIndex(18);
        fxHeal = graphicEntityModule.createSprite()
            .setImage("fx_heal.png")
            .setAnchor(0.5)
            .setBaseWidth(74)
            .setBaseHeight(74)
            .setVisible(false)
            .setZIndex(18);
        fxSlash = graphicEntityModule.createSprite()
            .setImage("fx_slash.png")
            .setAnchor(0.5)
            .setBaseWidth(68)
            .setBaseHeight(68)
            .setVisible(false)
            .setZIndex(18);
    }

    private void updateRelicTooltip(Relic r) {
        tooltipModule.setTooltipText(r.getRootGroup(),
            String.format("Relic Chest %d (%d, %d)\nValue: %d pts | Guardian HP: %d",
                r.getId(), r.getX(), r.getY(), r.getValue(), r.getGuardianHp()));
    }

    private void updateHeroTooltip(Hero h) {
        tooltipModule.setTooltipText(h.getRootGroup(),
            String.format("Hero %d (%s - Player %d)\nPos: (%d, %d) | HP: %d/%d | CD: %d",
                h.getId(), h.getRole().name(), h.getOwner().getIndex(),
                h.getX(), h.getY(), h.getHp(), h.getRole().maxHp, h.getCooldown()));
    }

    @Override
    public void gameTurn(int turn) {
        turnText.setText("TURN " + turn + " / " + MAX_TURNS);
        fxFireball.setVisible(false);
        fxHeal.setVisible(false);
        fxSlash.setVisible(false);

        for (Hero h : allHeroes) {
            if (h.isAlive()) {
                h.setCooldown(h.getCooldown() - 1);
                h.resetTurnCommand();
            }
        }

        for (Player player : gameManager.getActivePlayers()) {
            sendTurnInput(player);
            player.execute();
        }

        for (Player player : gameManager.getActivePlayers()) {
            try {
                List<String> outputs = player.getOutputs();
                parseOutputs(player, outputs);
            } catch (TimeoutException e) {
                disqualify(player, "timed out!");
            }
        }

        if (gameManager.getActivePlayers().size() < 2) {
            gameManager.endGame();
            return;
        }

        resolveMovement();
        resolveActions();
        resolveRelicsAndScoring();
        refreshGraphics();
        checkEndConditions();
    }

    private void sendTurnInput(Player player) {
        int myIdx = player.getIndex();
        for (int y = 0; y < HEIGHT; y++) {
            player.sendInputLine(new String(grid[y]));
        }

        List<Hero> ordered = new ArrayList<>();
        for (Hero h : allHeroes) {
            if (h.isAlive() && h.getOwner().getIndex() == myIdx) ordered.add(h);
        }
        for (Hero h : allHeroes) {
            if (h.isAlive() && h.getOwner().getIndex() != myIdx) ordered.add(h);
        }

        player.sendInputLine(String.valueOf(ordered.size()));
        for (Hero h : ordered) {
            int relOwner = (h.getOwner().getIndex() == myIdx) ? 0 : 1;
            player.sendInputLine(String.format("%d %d %s %d %d %d %d",
                h.getId(), relOwner, h.getRole().name(), h.getX(), h.getY(), h.getHp(), h.getCooldown()));
        }

        List<Relic> activeRelics = new ArrayList<>();
        for (Relic r : relics) {
            if (!r.isCollected()) activeRelics.add(r);
        }
        player.sendInputLine(String.valueOf(activeRelics.size()));
        for (Relic r : activeRelics) {
            player.sendInputLine(String.format("%d %d %d %d %d",
                r.getId(), r.getX(), r.getY(), r.getValue(), r.getGuardianHp()));
        }
    }

    private void parseOutputs(Player player, List<String> outputs) {
        List<Hero> myAlive = player.getAliveHeroes();
        for (int i = 0; i < myAlive.size() && i < outputs.size(); i++) {
            Hero h = myAlive.get(i);
            String line = outputs.get(i).trim();
            if (line.isEmpty()) {
                disqualify(player, "sent an empty line!");
                return;
            }
            String[] tokens = line.split("\\s+");
            int c = 0;
            try {
                String cmd = tokens[c++].toUpperCase();
                if ("MOVE".equals(cmd)) {
                    int mx = Integer.parseInt(tokens[c++]);
                    int my = Integer.parseInt(tokens[c++]);
                    if (mx >= 0 && mx < WIDTH && my >= 0 && my < HEIGHT) {
                        h.setMoveTarget(mx, my);
                    }
                } else if (!"WAIT".equals(cmd)) {
                    disqualify(player, "unrecognized command '" + cmd + "'");
                    return;
                }

                if (c < tokens.length) {
                    String act = tokens[c++].toUpperCase();
                    if ("SLASH".equals(act)) {
                        int tid = Integer.parseInt(tokens[c++]);
                        h.setAction(Hero.ActionType.SLASH, tid, -1);
                    } else if ("FIREBALL".equals(act)) {
                        int tx = Integer.parseInt(tokens[c++]);
                        int ty = Integer.parseInt(tokens[c++]);
                        if (leagueLevel >= 2 && h.getRole() == Hero.Role.MAGE) {
                            h.setAction(Hero.ActionType.FIREBALL, tx, ty);
                        }
                    } else if ("HEAL".equals(act)) {
                        int aid = Integer.parseInt(tokens[c++]);
                        if (leagueLevel >= 2 && h.getRole() == Hero.Role.CLERIC) {
                            h.setAction(Hero.ActionType.HEAL, aid, -1);
                        }
                    } else if ("GUARD".equals(act)) {
                        int aid = Integer.parseInt(tokens[c++]);
                        if (leagueLevel >= 3 && h.getRole() == Hero.Role.FIGHTER) {
                            h.setAction(Hero.ActionType.GUARD, aid, -1);
                        }
                    }
                }
            } catch (Exception e) {
                disqualify(player, "malformed command '" + line + "'");
                return;
            }
        }
    }

    private void disqualify(Player player, String reason) {
        player.deactivate(player.getNicknameToken() + " " + reason);
        player.setScore(-1);
        gameManager.addToGameSummary(GameManager.formatErrorMessage(player.getNicknameToken() + " " + reason));
    }

    private int[] nextStep(int sx, int sy, int tx, int ty) {
        if (sx == tx && sy == ty) return new int[]{sx, sy};
        int[][] dist = new int[HEIGHT][WIDTH];
        int[][][] first = new int[HEIGHT][WIDTH][2];
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) dist[y][x] = -1;
        }
        Queue<int[]> q = new ArrayDeque<>();
        dist[sy][sx] = 0;
        q.add(new int[]{sx, sy});
        int[] best = {sx, sy};
        int bestM = Math.abs(sx - tx) + Math.abs(sy - ty);

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int cx = cur[0], cy = cur[1];
            if (cx == tx && cy == ty) return first[cy][cx];
            for (int d = 0; d < 4; d++) {
                int nx = cx + DX[d], ny = cy + DY[d];
                if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT && grid[ny][nx] == '.' && dist[ny][nx] == -1) {
                    dist[ny][nx] = dist[cy][cx] + 1;
                    first[ny][nx][0] = (cx == sx && cy == sy) ? nx : first[cy][cx][0];
                    first[ny][nx][1] = (cx == sx && cy == sy) ? ny : first[cy][cx][1];
                    q.add(new int[]{nx, ny});
                    int m = Math.abs(nx - tx) + Math.abs(ny - ty);
                    if (m < bestM) {
                        bestM = m;
                        best = first[ny][nx];
                    }
                }
            }
        }
        return best;
    }

    private void resolveMovement() {
        Map<Hero, int[]> desired = new HashMap<>();
        for (Hero h : allHeroes) {
            if (!h.isAlive()) continue;
            desired.put(h, nextStep(h.getX(), h.getY(), h.getMoveTargetX(), h.getMoveTargetY()));
        }

        boolean conflict = true;
        while (conflict) {
            conflict = false;
            for (int i = 0; i < allHeroes.size(); i++) {
                Hero h1 = allHeroes.get(i);
                if (!h1.isAlive()) continue;
                int[] p1 = desired.get(h1);
                for (int j = i + 1; j < allHeroes.size(); j++) {
                    Hero h2 = allHeroes.get(j);
                    if (!h2.isAlive()) continue;
                    int[] p2 = desired.get(h2);
                    if ((p1[0] == p2[0] && p1[1] == p2[1])
                        || (p1[0] == h2.getX() && p1[1] == h2.getY() && p2[0] == h1.getX() && p2[1] == h1.getY())) {
                        if (p1[0] != h1.getX() || p1[1] != h1.getY()) {
                            p1[0] = h1.getX();
                            p1[1] = h1.getY();
                            conflict = true;
                        }
                        if (p2[0] != h2.getX() || p2[1] != h2.getY()) {
                            p2[0] = h2.getX();
                            p2[1] = h2.getY();
                            conflict = true;
                        }
                    }
                }
            }
        }

        for (Hero h : allHeroes) {
            if (!h.isAlive()) continue;
            int[] dest = desired.get(h);
            h.setX(dest[0]);
            h.setY(dest[1]);
            h.getRootGroup().setX(cellX(h.getX()), Curve.EASE_IN_AND_OUT)
                            .setY(cellY(h.getY()), Curve.EASE_IN_AND_OUT);
        }
    }

    private void resolveActions() {
        // 1. Resolve GUARD stances first
        for (Hero h : allHeroes) {
            if (!h.isAlive() || h.getActionType() != Hero.ActionType.GUARD) continue;
            Hero ally = heroById.get(h.getActionParam1());
            if (ally != null && ally.isAlive() && ally.getOwner() == h.getOwner()
                && Math.abs(h.getX() - ally.getX()) + Math.abs(h.getY() - ally.getY()) <= 1) {
                h.setGuardedThisTurn(true);
                ally.setGuardedThisTurn(true);
            }
        }

        // 2. Resolve SLASH and FIREBALL attacks
        for (Hero h : allHeroes) {
            if (!h.isAlive()) continue;
            if (h.getActionType() == Hero.ActionType.SLASH) {
                Hero target = heroById.get(h.getActionParam1());
                if (target != null && target.isAlive() && target.getOwner() != h.getOwner()
                    && Math.abs(h.getX() - target.getX()) + Math.abs(h.getY() - target.getY()) <= 1) {
                    target.takeDamage(h.getRole().attackDamage);
                    h.getOwner().setScore(h.getOwner().getScore() + 3);
                    fxSlash.setX(cellX(target.getX())).setY(cellY(target.getY())).setVisible(true);
                }
            } else if (h.getActionType() == Hero.ActionType.FIREBALL && h.getCooldown() == 0) {
                int tx = h.getActionParam1(), ty = h.getActionParam2();
                if (Math.abs(h.getX() - tx) + Math.abs(h.getY() - ty) <= 3) {
                    h.setCooldown(2);
                    fxFireball.setX(cellX(tx)).setY(cellY(ty)).setVisible(true);
                    for (Hero enemy : allHeroes) {
                        if (enemy.isAlive() && enemy.getOwner() != h.getOwner()
                            && Math.abs(enemy.getX() - tx) + Math.abs(enemy.getY() - ty) <= 1) {
                            enemy.takeDamage(8);
                            h.getOwner().setScore(h.getOwner().getScore() + 4);
                        }
                    }
                    for (Relic r : relics) {
                        if (!r.isCollected() && r.getGuardianHp() > 0
                            && Math.abs(r.getX() - tx) + Math.abs(r.getY() - ty) <= 1) {
                            r.damageGuardian(8);
                        }
                    }
                }
            }
        }

        // 3. Resolve Cleric HEAL (DIOS)
        for (Hero h : allHeroes) {
            if (!h.isAlive() || h.getActionType() != Hero.ActionType.HEAL || h.getCooldown() > 0) continue;
            Hero ally = heroById.get(h.getActionParam1());
            if (ally != null && ally.isAlive() && ally.getOwner() == h.getOwner()
                && Math.abs(h.getX() - ally.getX()) + Math.abs(h.getY() - ally.getY()) <= 3) {
                h.setCooldown(2);
                ally.heal(10);
                h.getOwner().setScore(h.getOwner().getScore() + 2);
                fxHeal.setX(cellX(ally.getX())).setY(cellY(ally.getY())).setVisible(true);
            }
        }
    }

    private void resolveRelicsAndScoring() {
        for (Relic r : relics) {
            if (r.isCollected()) continue;
            for (Hero h : allHeroes) {
                if (!h.isAlive()) continue;
                int dist = Math.abs(h.getX() - r.getX()) + Math.abs(h.getY() - r.getY());
                if (r.getGuardianHp() > 0 && dist <= 1) {
                    r.damageGuardian(h.getRole().attackDamage);
                    if (r.getGuardianHp() == 0) {
                        r.getGuardianSprite().setVisible(false);
                        h.getOwner().setScore(h.getOwner().getScore() + 10);
                    }
                }
                if (r.getGuardianHp() == 0 && dist == 0) {
                    r.setCollected(true);
                    r.getChestSprite().setImage("chest_open.png");
                    r.getRootGroup().setAlpha(0.35);
                    h.getOwner().setScore(h.getOwner().getScore() + r.getValue());
                    break;
                }
            }
        }

        for (Hero h : allHeroes) {
            if (!h.isAlive()) {
                h.getRootGroup().setVisible(false);
            }
        }
    }

    private void refreshGraphics() {
        for (Player p : gameManager.getPlayers()) {
            p.getScoreText().setText("SCORE: " + p.getScore());
            p.getPartyText().setText("HEROES: " + p.getAliveHeroes().size());
        }
        for (Relic r : relics) {
            updateRelicTooltip(r);
        }
        for (Hero h : allHeroes) {
            if (h.isAlive()) {
                int barW = Math.max(4, (int) Math.round(48.0 * h.getHp() / h.getRole().maxHp));
                h.getHpBarFill().setWidth(barW);
                h.getDebugLabel().setText(h.getRole().shortCode + h.getId());
            }
            updateHeroTooltip(h);
        }
    }

    private void checkEndConditions() {
        Player p0 = gameManager.getPlayer(0);
        Player p1 = gameManager.getPlayer(1);
        int a0 = p0.getAliveHeroes().size();
        int a1 = p1.getAliveHeroes().size();
        if (a0 == 0 || a1 == 0) {
            p0.setScore(p0.getScore() + a0 * 20);
            p1.setScore(p1.getScore() + a1 * 20);
            refreshGraphics();
            gameManager.endGame();
            return;
        }
        boolean anyRelicLeft = false;
        for (Relic r : relics) {
            if (!r.isCollected()) anyRelicLeft = true;
        }
        if (!anyRelicLeft && Math.abs(p0.getScore() - p1.getScore()) > 30) {
            gameManager.endGame();
        }
    }

    @Override
    public void onEnd() {
        int[] scores = gameManager.getPlayers().stream().mapToInt(Player::getScore).toArray();
        String[] texts = gameManager.getPlayers().stream()
            .map(p -> p.getScore() + " pts (" + p.getAliveHeroes().size() + " heroes alive)")
            .toArray(String[]::new);
        endScreenModule.setScores(scores, texts);
    }
}
