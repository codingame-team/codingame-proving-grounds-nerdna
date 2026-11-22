package com.codingame.game;

import com.codingame.gameengine.module.entities.Group;
import com.codingame.gameengine.module.entities.Rectangle;
import com.codingame.gameengine.module.entities.Text;

public class Hero {
    public enum Role {
        FIGHTER(36, 9, "hero_fighter.png", "F"),
        MAGE(22, 4, "hero_mage.png", "M"),
        CLERIC(28, 6, "hero_cleric.png", "C");

        public final int maxHp;
        public final int attackDamage;
        public final String spriteAsset;
        public final String shortCode;

        Role(int maxHp, int attackDamage, String spriteAsset, String shortCode) {
            this.maxHp = maxHp;
            this.attackDamage = attackDamage;
            this.spriteAsset = spriteAsset;
            this.shortCode = shortCode;
        }
    }

    public enum ActionType {
        NONE, SLASH, FIREBALL, HEAL, GUARD
    }

    private final int id;
    private final Player owner;
    private final Role role;
    private int x;
    private int y;
    private int hp;
    private int cooldown;
    private boolean guardedThisTurn;

    // Turn commands
    private int moveTargetX;
    private int moveTargetY;
    private ActionType actionType = ActionType.NONE;
    private int actionParam1;
    private int actionParam2;

    // Dual-Mode Graphics (Sprite View vs Geometric Debug View)
    private Group rootGroup;
    private Group spriteView;
    private Group debugView;
    private Rectangle hpBarFill;
    private Text debugLabel;

    public Hero(int id, Player owner, Role role, int x, int y) {
        this.id = id;
        this.owner = owner;
        this.role = role;
        this.x = x;
        this.y = y;
        this.hp = role.maxHp;
        this.moveTargetX = x;
        this.moveTargetY = y;
    }

    public void resetTurnCommand() {
        this.moveTargetX = this.x;
        this.moveTargetY = this.y;
        this.actionType = ActionType.NONE;
        this.actionParam1 = -1;
        this.actionParam2 = -1;
        this.guardedThisTurn = false;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void takeDamage(int dmg) {
        int actual = guardedThisTurn ? Math.max(1, dmg - 3) : dmg;
        this.hp = Math.max(0, this.hp - actual);
    }

    public void heal(int amount) {
        if (isAlive()) {
            this.hp = Math.min(role.maxHp, this.hp + amount);
        }
    }

    public int getId() {
        return id;
    }

    public Player getOwner() {
        return owner;
    }

    public Role getRole() {
        return role;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getHp() {
        return hp;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = Math.max(0, cooldown);
    }

    public boolean isGuardedThisTurn() {
        return guardedThisTurn;
    }

    public void setGuardedThisTurn(boolean guardedThisTurn) {
        this.guardedThisTurn = guardedThisTurn;
    }

    public int getMoveTargetX() {
        return moveTargetX;
    }

    public int getMoveTargetY() {
        return moveTargetY;
    }

    public void setMoveTarget(int tx, int ty) {
        this.moveTargetX = tx;
        this.moveTargetY = ty;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public int getActionParam1() {
        return actionParam1;
    }

    public int getActionParam2() {
        return actionParam2;
    }

    public void setAction(ActionType type, int p1, int p2) {
        this.actionType = type;
        this.actionParam1 = p1;
        this.actionParam2 = p2;
    }

    public Group getRootGroup() {
        return rootGroup;
    }

    public void setRootGroup(Group rootGroup) {
        this.rootGroup = rootGroup;
    }

    public Group getSpriteView() {
        return spriteView;
    }

    public void setSpriteView(Group spriteView) {
        this.spriteView = spriteView;
    }

    public Group getDebugView() {
        return debugView;
    }

    public void setDebugView(Group debugView) {
        this.debugView = debugView;
    }

    public Rectangle getHpBarFill() {
        return hpBarFill;
    }

    public void setHpBarFill(Rectangle hpBarFill) {
        this.hpBarFill = hpBarFill;
    }

    public Text getDebugLabel() {
        return debugLabel;
    }

    public void setDebugLabel(Text debugLabel) {
        this.debugLabel = debugLabel;
    }
}
