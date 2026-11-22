package com.codingame.game;

import com.codingame.gameengine.module.entities.Group;
import com.codingame.gameengine.module.entities.Sprite;

public class Relic {
    private final int id;
    private final int x;
    private final int y;
    private final int value;
    private int guardianHp;
    private boolean collected;

    private Group rootGroup;
    private Sprite chestSprite;
    private Sprite guardianSprite;

    public Relic(int id, int x, int y, int value, int guardianHp) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.value = value;
        this.guardianHp = guardianHp;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getValue() {
        return value;
    }

    public int getGuardianHp() {
        return guardianHp;
    }

    public void damageGuardian(int dmg) {
        this.guardianHp = Math.max(0, this.guardianHp - dmg);
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    public Group getRootGroup() {
        return rootGroup;
    }

    public void setRootGroup(Group rootGroup) {
        this.rootGroup = rootGroup;
    }

    public Sprite getChestSprite() {
        return chestSprite;
    }

    public void setChestSprite(Sprite chestSprite) {
        this.chestSprite = chestSprite;
    }

    public Sprite getGuardianSprite() {
        return guardianSprite;
    }

    public void setGuardianSprite(Sprite guardianSprite) {
        this.guardianSprite = guardianSprite;
    }
}
