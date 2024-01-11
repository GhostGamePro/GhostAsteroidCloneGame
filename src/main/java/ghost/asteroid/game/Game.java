package ghost.asteroid.game;

 

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;

import ghost.asteroid.game.listeners.KeyboardListener; 
import ghost.asteroid.game.objects.Asteriod;
import ghost.asteroid.game.objects.Bullet;
import ghost.asteroid.game.objects.GameObject;
import ghost.asteroid.game.objects.Player;
import ghost.asteroid.game.objects.components.Point;

public class Game extends JComponent {

	private static final long serialVersionUID = 1L;

	public Dimension screenSize;

	private Player player;
	private List<Asteriod> asteroids = Collections.synchronizedList(new ArrayList<Asteriod>());
	private List<Bullet> bullets = Collections.synchronizedList(new ArrayList<Bullet>());
	private Integer score = 0;
	private static Integer highscore = 0;

	public Boolean isGameOver = false;
	public KeyboardListener keyboard;

	public Game(Dimension screenSize) {
		this.screenSize = screenSize;
		this.setPreferredSize(screenSize);
		GameObject.screenSize = screenSize;

	
		keyboard = new KeyboardListener();
		player = new Player(new Point(screenSize.getWidth() / 2.0, screenSize.getHeight() / 2.0), keyboard);
		this.addKeyListener(keyboard);

		asteroids.add(new Asteriod(new Point(50.0, 50.0), 10.0));
		asteroids.add(new Asteriod(new Point(180.0, 150.0), 10.0));
	}


	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.setColor(Color.BLACK);
		g.fillRect(1, 1, (int) Math.floor(this.getWidth()) - 2, (int) Math.floor(this.getHeight()) - 2);

		player.draw(g);

		
		synchronized (bullets) {
			for (GameObject bullet : bullets) {
				bullet.draw(g);
			}
		}
		
		synchronized (asteroids) {
			for (GameObject asteriod : asteroids) {
				asteriod.draw(g);
			}
		}
		
		g.setColor(Color.WHITE);
		g.drawString("HighScore: " + highscore, this.getWidth() - 100, 30);
		g.drawString("Score: " + score, 20, 30);

		if (isGameOver) {
			
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.PLAIN, 24));
			g.drawString("GAME OVER", this.getWidth() / 2 - 80, this.getHeight() / 2);

			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.PLAIN, 12));
			g.drawString("(Press Space to reset)", this.getWidth() / 2 - 70, this.getHeight() / 2 + 20);
		}
	}

	
	public void tick() {

		if (!isGameOver) {
			player.tick();
		}
		if (score > highscore) {
			highscore = score;
		}

		ArrayList<Bullet> newBullets = player.bullets;
		for (Bullet bullet : newBullets) {
			bullets.add(bullet);
		}
		player.bullets = new ArrayList<Bullet>();

		ArrayList<Bullet> oldBullets = new ArrayList<>();
		ArrayList<Asteriod> destroyedAsteroid = new ArrayList<>();

		
		for (Bullet bullet : bullets) {
			for (Asteriod asteroid : asteroids) {
				Boolean result = bullet.isColliding(asteroid);
				if (result) {
					destroyedAsteroid.add(asteroid);
					oldBullets.add(bullet);
				}
			}
			bullet.tick();
			if (bullet.duration <= 0) {
				oldBullets.add(bullet);
			}
		}

		
		for (Bullet bullet : oldBullets) {
			bullets.remove(bullet);
		}

		ArrayList<Asteriod> newAsteroids = new ArrayList<>();

		
		for (Asteriod asteroid : destroyedAsteroid) {
			score += 25;
			asteroid.dies();
			newAsteroids.addAll(asteroid.childrens);
			asteroids.remove(asteroid);
		}
		asteroids.addAll(newAsteroids);

		
		for (GameObject asteriod : asteroids) {
			asteriod.tick();
			if (asteriod.isColliding(player)) {
				isGameOver = true;
				player.poly.points = new ArrayList<>();
			}
		}
	}
}



