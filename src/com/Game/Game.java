package com.Game;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.Random;

public class Game extends Canvas implements Runnable {

    public static final int WIDTH = 640, HEIGHT = WIDTH / 12 * 9;

    private Thread thread;
    private boolean running = false;

    private final Random r;
    private final Handler handler = new Handler();
    private final HUD hud = new HUD();
    private final Spawn spawner;
    private final Menu menu = new Menu(handler, hud);

    public enum STATE {
        MENU,
        HELP,
        GAME,
        END
    }

    public static STATE gameState = STATE.MENU;

    public Game() {

        this.addKeyListener(new KeyInput(handler));
        this.addMouseListener(menu);

        AudioPlayer.load();

        AudioPlayer.getMusic("music").loop();

        new Window(WIDTH, HEIGHT, "First Game", this);

        spawner = new Spawn(handler, hud);
        r = new Random();

        if (gameState == STATE.GAME) {
            handler.addObject(new Player(WIDTH / 2 - 32, HEIGHT / 2 - 32, ID.Player, handler));
            handler.addObject(new BasicEnemy(r.nextInt(Game.WIDTH - 50), r.nextInt(Game.HEIGHT - 50), ID.BasicEnemy, handler));
        } else {
            for (int i = 0; i < 20; i++) {
                handler.addObject(new MenuParticle(r.nextInt(WIDTH), r.nextInt(HEIGHT), ID.ManuParticle, handler));
            }
        }

    }

    public synchronized void start() {
        thread = new Thread(this);
        thread.start();
        running = true;
    }

    public synchronized void stop() {
        try {
            thread.join();
            running = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() { // GAME LOOP
        this.requestFocus();
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        // int frames = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while (delta >= 1) {
                tick();
                delta--;
            }

            if (running) {
                render();
            }

            // frames++;

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                // System.out.println("FPS : " + frames);
                // frames = 0;
            }
        }

        stop();
    }

    private void tick() {
        handler.tick();

        if (gameState == STATE.GAME) {
            hud.tick();
            spawner.tick();

            if (HUD.HEATLH <= 0) {
                HUD.HEATLH = 100;
                gameState = STATE.END;
                handler.clearEnemies();
                for (int i = 0; i < 20; i++) {
                    handler.addObject(new MenuParticle(r.nextInt(WIDTH), r.nextInt(HEIGHT), ID.ManuParticle, handler));
                }
            }

        } else if (gameState == STATE.MENU || gameState == STATE.END) {
            menu.tick();
        }
    }

    private void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        handler.render(g);

        if (gameState == STATE.GAME) {
            hud.render(g);
        } else if (gameState == STATE.MENU
                || gameState == STATE.HELP
                || gameState == STATE.END) {
            menu.render(g);
        }

        g.dispose();
        bs.show();
    }

    public static float clamp(float var, float min, float max) {
        if (var >= max) return max;
        else if (var <= min) return min;
        else {
            return var;
        }
    }

    public static void main(String[] args) {
        new Game();
    }
}
