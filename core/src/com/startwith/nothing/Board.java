package com.startwith.nothing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.io.BufferedReader;
import java.io.FileReader;

public class Board {
    public Tile[][] tiles;
    public Tank[][] tanks;
    public int width;
    public int height;

    public static int TILE_WIDTH = 32;
    public static int TILE_HEIGHT = 32;

    static Texture boardTex = new Texture("tiles.png");

    public Board(String file) {
        try {
            FileHandle f = Gdx.files.internal(file);
            BufferedReader br = f.reader(100);

            String line = br.readLine();
            String[] nums = line.split(" ");
            width = Integer.parseInt(nums[0]);
            height = Integer.parseInt(nums[1]);
            tiles = new Tile[width][height];
            tanks = new Tank[width][height];

            int j = height - 1;
            while ((line = br.readLine()) != null) {
                nums = line.split(" ");
                for (int i = 0; i < width; i++) {
                    tiles[i][j] = new Tile(i, j, Tile.Type.types[Integer.parseInt(nums[i])]);
                    if (tiles[i][j].type == Tile.Type.PCITY) {
                        UpgradeTank ut = new UpgradeTank(i, j, false);
                        NothingGame.playerTanks.add(ut);
                        tanks[i][j] = ut;
                    } else if (tiles[i][j].type == Tile.Type.ECITY) {
                        UpgradeTank ut = new UpgradeTank(i, j, true);
                        NothingGame.enemyTanks.add(ut);
                        tanks[i][j] = ut;
                    }
                }
                j--;
            }
            assert(j == 0);

            br.close();
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void render(SpriteBatch batch) {
        for(int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                batch.draw(boardTex, i * TILE_WIDTH, j * TILE_HEIGHT,
                        tiles[i][j].type.tx * TILE_WIDTH, tiles[i][j].type.ty * TILE_HEIGHT,
                        TILE_WIDTH, TILE_HEIGHT);
            }
        }
    }

    public void highlight(ShapeRenderer renderer, int x, int y, Color c) {
        renderer.setColor(c);
        renderer.rect(x * TILE_WIDTH, y * TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
    }

    public static class Tile {
        int x, y;
        Type type;

        public Tile(int x, int y, Type type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }

        public enum Type{
            PLAINS(0, 1, 0, 0),
            MOUNTAIN(3, 4, 2, 0),
            FOREST(1, 2, 1, 0),
            NCITY(2, 1, 0, 1),
            PCITY(2, 1, 1, 1),
            ECITY(2, 1, 2, 1);

            public static Type[] types = {PLAINS, MOUNTAIN, FOREST, NCITY, PCITY, ECITY};

            int defense, cost, tx, ty;

            Type(int defense, int cost, int tx, int ty) {
                this.defense = defense;
                this.cost = cost;
                this.tx = tx;
                this.ty = ty;
            }
        }
    }
}
