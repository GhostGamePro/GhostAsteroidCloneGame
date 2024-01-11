package ghost.asteroid.game.objects;

import java.awt.Color;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import ghost.asteroid.game.objects.components.Point;
import ghost.asteroid.game.objects.components.Polygon;
import ghost.asteroid.game.objects.components.Vector;

public class Bullet extends GameObject {

	Color color;
	public static final Double SPEED = 2.0;
	public Integer duration = 150;
	private Clip laserClip;

	public Bullet() {
		this.color = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
		poly = createShape();
	}

	private Polygon createShape() {
		Polygon poly = new Polygon(new Point(0.0, 0.0), color);
		poly.add(new Point(0.0, 0.0));
		poly.add(new Point(0.0, 5.0));

		return poly;
	}

	public void tick() {
		super.tick();

		Vector direction = poly.getDirection();
		direction.scale(SPEED);

		poly.translate(direction.getX(), direction.getY());
		duration -= 1;

		if (duration % 10 == 0) {
			playLaserSound();
		}
	}

	private void playLaserSound() {
		try {
			File soundFile = new File("laser.wav");
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
			laserClip = AudioSystem.getClip();
			laserClip.open(audioInputStream);
			laserClip.start();
		} catch (Exception e) {
			System.err.println("Error playing laser sound: " + e.getMessage());
		}
	}

	public void destroy() {
		super.destroy();
		if (laserClip != null) {
			laserClip.stop();
			laserClip.close();
		}
	}
}
