package com.codingame.game;

import java.util.ArrayList;
import java.util.List;
import com.codingame.gameengine.core.AbstractMultiplayerPlayer;
import com.codingame.gameengine.module.entities.Text;

public class Player extends AbstractMultiplayerPlayer {
    private final List<Hero> heroes = new ArrayList<>();
    private Text scoreText;
    private Text partyText;

    @Override
    public int getExpectedOutputLines() {
        return Math.max(1, getAliveHeroes().size());
    }

    public List<Hero> getHeroes() {
        return heroes;
    }

    public List<Hero> getAliveHeroes() {
        List<Hero> alive = new ArrayList<>();
        for (Hero h : heroes) {
            if (h.isAlive()) alive.add(h);
        }
        return alive;
    }

    public void addHero(Hero h) {
        heroes.add(h);
    }

    public Text getScoreText() {
        return scoreText;
    }

    public void setScoreText(Text scoreText) {
        this.scoreText = scoreText;
    }

    public Text getPartyText() {
        return partyText;
    }

    public void setPartyText(Text partyText) {
        this.partyText = partyText;
    }
}
