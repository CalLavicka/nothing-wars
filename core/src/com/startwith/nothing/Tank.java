package com.startwith.nothing;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.startwith.nothing.Board.TILE_HEIGHT;
import static com.startwith.nothing.Board.TILE_WIDTH;

public class Tank {
    int x, y;
    int health, damage, speed;
    boolean enemy;

    String name = "Commander (Lose\nif Destroyed)";

    boolean canMove = true;
    boolean canAct = true;

    static Texture tankTex = new Texture("tank.png");

    private static Point[] dirs = {new Point(-1, 0), new Point(1, 0),
            new Point(0, 1), new Point(0, -1)};

    public Tank(int x, int y, boolean enemy) {
        this.x = x;
        this.y = y;
        this.health = 10;
        this.damage = 1;
        this.speed = 4;
        this.enemy = enemy;
    }

    public Set<Point> getMovement() {
        Board b = NothingGame.board;
        Map<Point, Integer> visited = new HashMap<>();
        Map<Point, Integer> visiting = new HashMap<>();
        visiting.put(new Point(x, y), this.speed);
        Set<Point> result = new HashSet<>();
        while (!visiting.isEmpty()) {
            Point p = visiting.keySet().iterator().next();
            int left = visiting.get(p);
            visiting.remove(p);

            if (b.tanks[p.x][p.y] == null || (p.x == x && p.y == y))
                result.add(p);
            else if (b.tanks[p.x][p.y].enemy != this.enemy) {
                continue;
            }

            for (Point dir : dirs) {
                Point newp = new Point(p.x + dir.x, p.y + dir.y);
                if (newp.x >= 0 && newp.x < b.width && newp.y >= 0 && newp.y < b.height) {
                    int newleft = left - b.tiles[newp.x][newp.y].type.cost;
                    if (newleft >= 0 && (!visited.containsKey(newp) || visited.get(newp) < left)) {
                        visiting.put(newp, newleft);
                        visited.put(newp, newleft);
                    }
                }
            }
        }
        return result;
    }

    public void move(int newX, int newY) {
        NothingGame.board.tanks[x][y] = null;
        NothingGame.board.tanks[newX][newY] = this;
        x = newX;
        y = newY;
        canMove = false;
    }

    public void attack(int ax, int ay) {
        Tank target = NothingGame.board.tanks[ax][ay];
        target.damage(this.damage);
        canMove = false;
        canAct = false;
    }

    public void damage(int amt) {
        amt = Math.max(amt - NothingGame.board.tiles[x][y].type.defense, 1);
        this.health -= amt;
        if (this.health <= 0) {
            NothingGame.board.tanks[x][y] = null;
            if (enemy) {
                NothingGame.enemyTanks.remove(this);
            } else {
                NothingGame.playerTanks.remove(this);
            }

            if (!(this instanceof UpgradeTank)) {
                if (enemy) {
                    NothingGame.win();
                } else {
                    NothingGame.lose();
                }
            }
        }
    }

    public Set<Point> getAttack() {
        Board b = NothingGame.board;
        Set<Point> result = new HashSet<>();
        if (damage == 0) {
            return result;
        }

        for (Point dir : dirs) {
            Point newp = new Point(x + dir.x, y + dir.y);
            if (newp.x >= 0 && newp.x < b.width && newp.y >= 0 && newp.y < b.height) {
                if (b.tanks[newp.x][newp.y] != null && b.tanks[newp.x][newp.y].enemy != enemy) {
                    result.add(newp);
                }
            }
        }
        return result;
    }

    public void render(SpriteBatch batch) {
        render(batch, x * TILE_WIDTH, y * TILE_HEIGHT);
    }

    public void render(SpriteBatch batch, int x, int y) {
        batch.draw(tankTex, x, y,
                enemy ? TILE_WIDTH : 0, TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
    }

    public String getHealthStr() {
        return "Health: " + health;
    }

    public String getDamageStr() {
        return "Damage: " + damage;
    }

    public String getSpeedStr() {
        return "Speed: " + speed;
    }

    public String getActionStr() {
        return (NothingGame.board.tiles[x][y].type == Board.Tile.Type.NCITY ||
                NothingGame.board.tiles[x][y].type == Board.Tile.Type.ECITY) && canAct ?
                "Press 'c' to capture city" : "Move over a city to capture it!";
    }
}
