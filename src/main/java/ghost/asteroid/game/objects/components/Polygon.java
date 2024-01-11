package ghost.asteroid.game.objects.components;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;


public class Polygon {

	private static final Double degreeToRad = Math.PI / 180;
	
	public Double angle = 0.0;
	public Point pos;
	public ArrayList<Point> points = new ArrayList<Point>();
	public Color color;

	
	public Polygon(Point pos, Color color) {
		this.pos = pos;
		this.color = color;
	}

	
	public void add(Point point) {
		points.add(point);
	}
	
	
	public Vector getDirection() {
		Vector direction = new Vector(new Point(0, -1));
		direction.rotate(angle);
		return direction;
	}

	
	public void draw(Graphics g) {
		for (int i = 0; i < points.size(); i = i + 2) {
			Point pt1 = points.get(i);
			Point pt2 = points.get(i + 1);

			g.setColor(color);
			g.drawLine((int) Math.floor(pt1.x), 
					(int) Math.floor(pt1.y), 
					(int) Math.floor(pt2.x), 
					(int) Math.floor(pt2.y));
		}
	}

	
	public void translate(Double xpos, Double ypos) {

		Double cos = Math.cos(0 * degreeToRad);
		Double sin = Math.sin(0 * degreeToRad);

		Double xPosIncrease = (xpos * cos) - (ypos * sin);
		Double yPosIncrease = (xpos * sin) + (ypos * cos);

		this.pos.x += xPosIncrease;
		this.pos.y += yPosIncrease;

		for (Point point : points) {
			point.translate(xPosIncrease, yPosIncrease);
		}
		
	}

	
	public void scale(Double scale) {
		for (Point point : points) {
			point.scale(scale);
		}
	}

	
	public void rotate(Double rot) {
		Point oldPos = this.pos.clone();

		translate(-oldPos.x, -oldPos.y);

		for (Point point : points) {
			point.rotate(rot);
		}

		translate(oldPos.x, oldPos.y);
		this.angle += rot;
	}
	
}