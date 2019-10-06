package com.startwith.nothing;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import static com.startwith.nothing.Board.TILE_HEIGHT;
import static com.startwith.nothing.Board.TILE_WIDTH;

public class UpgradeTank extends Tank {
    Point owningLocation;
    public UpgradeTank(int x, int y, boolean enemy) {
        super(x, y, enemy);
        this.health = 1;
        this.damage = 0;
        this.speed = 0;
        this.name = "Tank Mk1";
        owningLocation = new Point(x, y);
    }

    int mark = 1;

    public void upgrade() {
        mark++;
        this.name = "Tank Mk" + mark;
        this.canAct = false;
        this.canMove = false;
        if (mark == 10) name += " (No more upgrades)";
    }

    @Override
    public void damage(int amt) {
        super.damage(amt);
        if (health <= 0) {
            if (enemy) {
                NothingGame.enemySpawn.add(owningLocation);
            } else {
                NothingGame.playerSpawn.add(owningLocation);
            }
        }
    }

    @Override
    public void render(SpriteBatch batch, int x, int y) {
        batch.draw(tankTex, x, y,
                enemy ? TILE_WIDTH : 0, 0, TILE_WIDTH, TILE_HEIGHT);
    }

    public String getHealthStr() {
        return "Health: " + health + ((mark < 10 && !enemy) ? " (press 'h' to upgrade)" : "");
    }

    public String getDamageStr() {
        return "Damage: " + damage + ((mark < 10 && !enemy) ? " (press 'd' to upgrade)" : "");
    }

    public String getSpeedStr() {
        return "Speed: " + speed + ((mark < 10 && !enemy) ? " (press 's' to upgrade)" : "");
    }

    public String getActionStr() {
        return canAct ? "Upgrading counts as an action!" : "";
    }
}
