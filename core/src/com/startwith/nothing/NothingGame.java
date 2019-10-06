package com.startwith.nothing;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

import java.util.*;

public class NothingGame extends ApplicationAdapter {
	SpriteBatch batch;
	ShapeRenderer shapes;
	BitmapFont font;
	public static Random rand = new Random();

	public static Board board;

	public static OrthographicCamera camera;

	public static List<Tank> playerTanks = new ArrayList<>();
	public static List<Tank> enemyTanks = new ArrayList<>();
	public static List<Point> playerSpawn = new ArrayList<>();
	public static List<Point> enemySpawn = new ArrayList<>();

	public static boolean playerTurn = true;
	public static int turnCount = 1;

	static String message = null;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		shapes = new ShapeRenderer();
		font = new BitmapFont();
		board = new Board("level1.txt");
		board.tanks[0][0] = new Tank(0, 0, false);
		board.tanks[board.width - 2][board.height - 3] = new Tank(board.width - 2, board.height - 3, true);
		playerTanks.add(board.tanks[0][0]);
		enemyTanks.add(board.tanks[board.width - 2][board.height - 3]);

		camera = new OrthographicCamera(640, 480);
		camera.translate(320 - board.width * Board.TILE_WIDTH / 2, board.height * Board.TILE_HEIGHT / 2);
		camera.update();
	}

	Set<Point> movement = new HashSet<>();
	Set<Point> attack = new HashSet<>();
	Tank selected = null;

	public void nextTurn() {
		playerTurn = !playerTurn;
		if (playerTurn) {
			turnCount++;
			List<Point> deadSpawns = new ArrayList<>();
			for (Point p : playerSpawn) {
				if (board.tanks[p.x][p.y] == null) {
					board.tanks[p.x][p.y] = new UpgradeTank(p.x, p.y, false);
					playerTanks.add(board.tanks[p.x][p.y]);
					deadSpawns.add(p);
				}
			}
			playerSpawn.removeAll(deadSpawns);
			for (Tank t : playerTanks) {
				t.canAct = true;
				t.canMove = true;
			}
		} else {
			List<Point> deadSpawns = new ArrayList<>();
			for (Point p : enemySpawn) {
				if (board.tanks[p.x][p.y] == null) {
					board.tanks[p.x][p.y] = new UpgradeTank(p.x, p.y, true);
					enemyTanks.add(board.tanks[p.x][p.y]);
					deadSpawns.add(p);
				}
			}
			enemySpawn.removeAll(deadSpawns);
			for (Tank t : enemyTanks) {
				t.canAct = true;
				t.canMove = true;
			}
			aiIndex = 0;
			aiTime = 0;
		}
		selected = null;
		movement.clear();
		attack.clear();

	}

	public static int aiIndex;
	public static float aiTime;

	public void stepAI() {
		if (aiIndex >= enemyTanks.size()) {
			nextTurn();
		} else {
			if (selected != enemyTanks.get(aiIndex)) {
				selected = enemyTanks.get(aiIndex);
				movement = selected.getMovement();
			} else if (selected.canMove) {
				Set<Point> possible = selected.getMovement();
				Set<Point> candidates = new HashSet<>();
				for (Point p : possible) {
					selected.move(p.x, p.y);
					Set<Point> offensive = selected.getAttack();
					if (!offensive.isEmpty() && selected instanceof UpgradeTank) {
						candidates.add(p);
					}
					Board.Tile t = board.tiles[p.x][p.y];
					if ((t.type == Board.Tile.Type.NCITY || t.type == Board.Tile.Type.PCITY)
							&& !(selected instanceof UpgradeTank)) {
						candidates.add(p);
					}
				}
				if (!candidates.isEmpty()) {
					List<Point> pts = new ArrayList<>(candidates);
					Point p = pts.get(rand.nextInt(pts.size()));
					selected.move(p.x, p.y);
				} else {
					List<Point> pts = new ArrayList<>(possible);
					Point p = pts.get(rand.nextInt(pts.size()));
					selected.move(p.x, p.y);
				}
				movement.clear();
				attack = selected.getAttack();
			} else if (selected.canAct) {
				Set<Point> attacking = selected.getAttack();
				if (!(selected instanceof UpgradeTank) && (board.tiles[selected.x][selected.y].type == Board.Tile.Type.NCITY
				|| board.tiles[selected.x][selected.y].type == Board.Tile.Type.PCITY)) {
					board.tiles[selected.x][selected.y].type = Board.Tile.Type.ECITY;
					enemySpawn.add(new Point(selected.x, selected.y));
					playerSpawn.remove(new Point(selected.x, selected.y));
					selected.canAct = false;
				} else if (!attacking.isEmpty()) {
					Point p = attacking.iterator().next();
					selected.attack(p.x, p.y);
				} else {
					if (selected instanceof UpgradeTank) {
						UpgradeTank ut = (UpgradeTank) selected;
						if (ut.mark < 10) {
							int random = rand.nextInt(3);
							if (random == 0) {
								ut.damage++;
								ut.upgrade();
							} else if (random == 1) {
								ut.health++;
								ut.upgrade();
							} else {
								ut.speed++;
								ut.upgrade();
							}
						}
					}
					selected.canAct = false;
				}
				attack.clear();
			} else {
				aiIndex++;
			}
		}
	}

	@Override
	public void render () {
		if (message == null) {
			int mx = Gdx.input.getX();
			int my = Gdx.input.getY();
			Vector3 mouse = camera.unproject(new Vector3(mx, my, 0));
			mx = (int) mouse.x;
			my = (int) mouse.y;
			int bx = mx / Board.TILE_WIDTH;
			int by = my / Board.TILE_HEIGHT;

			if (playerTurn) {
				if (Gdx.input.justTouched()) {
					if (movement.contains(new Point(bx, by)) && (bx != selected.x || by != selected.y)) {
						selected.move(bx, by);
						movement.clear();
						attack = selected.getAttack();
					} else if (attack.contains(new Point(bx, by))) {
						selected.attack(bx, by);
						movement.clear();
						attack.clear();
						selected = null;
					} else {
						selected = null;
						movement.clear();
						attack.clear();
						if (bx >= 0 && by >= 0 && bx < board.width && by < board.height
								&& board.tanks[bx][by] != null) {
							selected = board.tanks[bx][by];
							if (selected.canMove && !selected.enemy) {
								movement = selected.getMovement();
							}
							if (selected.canAct && !selected.enemy) {
								attack = selected.getAttack();
							}
						}
					}
				}

				if (selected != null && selected instanceof UpgradeTank) {
					UpgradeTank ug = (UpgradeTank) selected;
					if (ug.mark < 10 && ug.canAct) {
						if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
							ug.health++;
							ug.upgrade();
							movement.clear();
							attack.clear();
							selected = null;
						} else if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
							ug.speed++;
							ug.upgrade();
							movement.clear();
							attack.clear();
							selected = null;
						} else if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
							ug.damage++;
							ug.upgrade();
							movement.clear();
							attack.clear();
							selected = null;
						}
					}
				} else if (selected != null) {
					if (selected.canAct) {
						Board.Tile t = board.tiles[selected.x][selected.y];
						if (Gdx.input.isKeyJustPressed(Input.Keys.C) &&
								(t.type == Board.Tile.Type.NCITY || t.type == Board.Tile.Type.ECITY)) {
							t.type = Board.Tile.Type.PCITY;
							playerSpawn.add(new Point(selected.x, selected.y));
							enemySpawn.remove(new Point(selected.x, selected.y));
							selected.canAct = false;
							movement.clear();
							attack.clear();
							selected = null;
						}
					}
				}

				if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
					nextTurn();
				}
			} else {
				aiTime -= Gdx.graphics.getDeltaTime();
				if (aiTime <= 0) {
					aiTime = 1;
					stepAI();
				}
			}
		}

		// RENDERING
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		board.render(batch);
		for (Tank tank : playerTanks) {
			tank.render(batch);
		}
		for (Tank tank : enemyTanks) {
			tank.render(batch);
		}

		if (selected != null) {
			selected.render(batch, board.width * Board.TILE_WIDTH + 40, board.height * Board.TILE_HEIGHT);
			font.draw(batch, selected.name, board.width * Board.TILE_WIDTH + 90, board.height * Board.TILE_HEIGHT + 14);

			font.draw(batch, selected.getHealthStr(), board.width * Board.TILE_WIDTH + 40,
					board.height * Board.TILE_HEIGHT - 50);
			font.draw(batch, selected.getSpeedStr(), board.width * Board.TILE_WIDTH + 40,
					board.height * Board.TILE_HEIGHT - 100);
			font.draw(batch, selected.getDamageStr(), board.width * Board.TILE_WIDTH + 40,
					board.height * Board.TILE_HEIGHT - 150);
			font.draw(batch, selected.getActionStr(), board.width * Board.TILE_WIDTH + 40,
					board.height * Board.TILE_HEIGHT - 200);
		}
		if (message != null) {
			font.draw(batch, message, 50, -50);
		} else if (playerTurn) {
			font.draw(batch, "Press 'space' to end turn", 50, -50);
		}
		batch.end();

		Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
		Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

		shapes.setProjectionMatrix(camera.combined);
		shapes.begin(ShapeRenderer.ShapeType.Filled);
		for (Point p : movement) {
			board.highlight(shapes, p.x, p.y, new Color(0, 0, 1, 0.3f));
		}

		for (Point p : attack) {
			board.highlight(shapes, p.x, p.y, new Color(1, 0, 0, 0.3f));
		}
		shapes.end();
		Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
	}

	public static void win() {
		message = "You win!";
	}

	public static void lose() {
		message = "You lose!";
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		shapes.dispose();
		Board.boardTex.dispose();
		font.dispose();
		Tank.tankTex.dispose();
	}
}
