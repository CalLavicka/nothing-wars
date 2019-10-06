package com.startwith.nothing.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.startwith.nothing.NothingGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Nothing Wars";
		config.width = 640;
		config.height = 480;
		new LwjglApplication(new NothingGame(), config);
	}
}
