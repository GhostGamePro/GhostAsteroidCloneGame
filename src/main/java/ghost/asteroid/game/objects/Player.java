package ghost.asteroid.game.objects;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Set;

import ghost.asteroid.game.controls.Action;
import ghost.asteroid.game.listeners.KeyboardListener;
import ghost.asteroid.game.objects.components.Point;
import ghost.asteroid.game.objects.components.Polygon;
import ghost.asteroid.game.objects.components.Vector;


public class Player extends GameObject {

	private static final Double ROTATION_SPEED = 2.0;
	private static final Double MAXSPEED = 3.0;
	private static final Double FRICTION = 0.005;
	private static final Vector THRUST = new Vector(0.0, 0.02);	
	
	private static Integer SHOOTING_DELAY = 30;
	
	private Color color;

	public ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	public KeyboardListener controls;
	
	private Vector movement = new Vector(0.0, 0.0);
	private Integer shootingCounter = SHOOTING_DELAY;


	public Player(Point pos, KeyboardListener controls) {
		this.color = Color.WHITE;
		poly = createShip();
		poly.translate(pos.x, pos.y);
		this.controls = controls;
	}

	public void tick() {
		super.tick();
		
		movement.setY(applySpeedCalc(movement.getY()));
		movement.setX(applySpeedCalc(movement.getX()));

		poly.translate(movement.getX()  , movement.getY());
		
		manageControls(controls.pressedKeys);
	}

	private void manageControls(Set<Action> pressedKeys) {
		if (shootingCounter > 0) {
			shootingCounter -= 1;
		}

		for (Action code : pressedKeys) {
			if (code == Action.left) {
				this.rotateLeft();
			} else if (code == Action.right) {
				this.rotateRight();
			}

			if (code == Action.up) {
				this.moveUp();
			} else if (code == Action.down) {
				this.moveDown();
			}

			if (code == Action.decrease) {
				SHOOTING_DELAY -= 1;
			} 
			
			if (code == Action.increase) {
				SHOOTING_DELAY += 1;
			}
			
			if (shootingCounter <= 0) {
				if (code == Action.space) {
					shootingCounter = SHOOTING_DELAY;
					if (pressedKeys.contains(Action.space))
						this.shoot();
				}
			}
		}
	}

	
	private Double applySpeedCalc(Double value) {
		Double newValue = value;
	
		//Apply friction
		if (Math.ceil(value) > 0) {
			newValue = value - FRICTION;
		} else if (Math.floor(value) < 0) {
			newValue = value + FRICTION;
		} else {
			newValue = 0.0;
		}
		
		//Apply MAXSPEED
		if (Math.ceil(value) > MAXSPEED) {
			newValue = MAXSPEED;
		}
		
		if (Math.floor(value) < -MAXSPEED) {
			newValue = -MAXSPEED;
		}
		
		return newValue;
	}
	
	public Polygon createShip() {

		Polygon poly = new Polygon(new Point(0, 0), color);
		poly.add(new Point(0, 0));
		poly.add(new Point(15, 35));
		poly.add(new Point(13, 30));
		poly.add(new Point(-13, 30));
		poly.add(new Point(-15, 35));
		poly.add(new Point(0, 0));

		return poly;
	}


	public void moveUp() {
		Vector thr = THRUST.clone();
		thr.setY(thr.getY() * -1);
		thr.rotate(poly.angle);
		movement.add(thr);
	}

	public void moveDown() {
		Vector thr = THRUST.clone();
		thr.rotate(poly.angle);
		movement.add(thr);
	}

	public void rotateLeft() {
		poly.rotate(ROTATION_SPEED);
	}

	public void rotateRight() {
		poly.rotate(-ROTATION_SPEED);
	}

	public void shoot() {
		Bullet bullet = new Bullet();
		bullet.poly.rotate(poly.angle);
		bullet.poly.translate(this.poly.pos.x, this.poly.pos.y);

		bullets.add(bullet);
	}

}