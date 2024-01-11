package ghost.asteroid.game.objects;

import java.awt.Color;
import java.util.ArrayList;

import ghost.asteroid.game.objects.components.Point;
import ghost.asteroid.game.objects.components.Polygon;


public class Asteriod extends GameObject {

	private Color color;
	
	private Double rotationSpeed;
	private Double angle;
	private Double scale;
	private static final Integer NUMOFCHILDREN = 2;
	private static final Double SPEED = 2.0;
	
	public ArrayList<Asteriod> childrens = new ArrayList<>();

	
	public Asteriod(Point pos, Double scale) {
		this.color = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
		poly = createShape();

		angle = Math.random() * 360;
		rotationSpeed = Math.random();
		poly.scale(scale);
		this.scale = scale;

		poly.translate(pos.x, pos.y);
	}

	
	private Polygon createShape() {
		Polygon poly = new Polygon(new Point(0.0, 0.0), color);
		poly.add(new Point(0.0, 0.0));
		poly.add(new Point(5.0, 1.0));
		poly.add(new Point(5.0, 1.0));
		poly.add(new Point(6.0, 5.0));
		poly.add(new Point(6.0, 5.0));
		poly.add(new Point(-1.0, 10.0));
		poly.add(new Point(-1.0, 10.0));
		poly.add(new Point(0.0, 0.0));
		return poly;
	}

	
	public void tick() {
		super.tick();
		poly.rotate(rotationSpeed);
		
		Point point = new Point(0, -1);
		point.rotate(angle);
		point.scale(SPEED);
		
		poly.translate(point.x, point.y);
	}

	
	public void dies() {
		
		
		if (this.scale / 2 > 0.5) {
			for (int i = 0; i < NUMOFCHILDREN; i++) {
				Asteriod asteriod = new Asteriod(new Point(poly.pos.x, poly.pos.y), this.scale / 2);
				childrens.add(asteriod);
			}
		}

	}
}
