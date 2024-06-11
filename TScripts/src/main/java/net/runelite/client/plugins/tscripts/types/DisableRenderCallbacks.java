package net.runelite.client.plugins.tscripts.types;

import net.runelite.api.*;
import net.runelite.api.hooks.DrawCallbacks;

public class DisableRenderCallbacks implements DrawCallbacks {

    @Override
    public void draw(Projection projection, Scene scene, Renderable renderable, int i, int i1, int i2, int i3, long l) {

    }

    @Override
    public void drawScenePaint(Scene scene, SceneTilePaint sceneTilePaint, int i, int i1, int i2) {

    }

    @Override
    public void drawSceneTileModel(Scene scene, SceneTileModel sceneTileModel, int i, int i1) {

    }

    @Override
    public void draw(int overlayColor) {

    }

    @Override
    public void drawScene(int cameraX, int cameraY, int cameraZ, int cameraPitch, int cameraYaw, int plane) {
    }

    @Override
    public void postDrawScene() {

    }

    @Override
    public void animate(Texture texture, int diff) {

    }

    @Override
    public void loadScene(Scene scene) {

    }

    @Override
    public void swapScene(Scene scene) {

    }
}