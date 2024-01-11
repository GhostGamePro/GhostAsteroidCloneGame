package ghost.asteroid.game.objects;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import ghost.asteroid.game.objects.components.Line;
import ghost.asteroid.game.objects.components.Point;
import ghost.asteroid.game.objects.components.Polygon;

public abstract class GameObject {

	public Polygon poly;
	public ArrayList<KeyListener> listeners = new ArrayList<KeyListener>();
	public static Dimension screenSize;
	public boolean collided = false;
	
	
	public void draw(Graphics g) {
		poly.draw(g);
	}
	
	
	public void tick() {
		outOfBoundHandler();
	} 
	
	
	
	public void outOfBoundHandler() {
		Point pos = poly.pos;
		
		if (pos.x > screenSize.width) {
			poly.translate(-pos.x, 0.0);
		} else if (pos.x < 0) {
			poly.translate(screenSize.width + 0.0, 0.0);
		}
		
		if (pos.y > screenSize.height) {
			poly.translate(0.0, -pos.y);
		} else if (pos.y < 0) {
			poly.translate(0.0, screenSize.height + 0.0);
		}	
	}	
	
	
	public Boolean isColliding(GameObject object) {
		for (int i = 0; i < this.poly.points.size(); i = i + 2) {
			Point p1 = this.poly.points.get(i);
			Point p2 = this.poly.points.get(i + 1);
			Line line1 = new Line(p1, p2);
			
			for (int x = 0; x < object.poly.points.size(); x = x + 2) {
				Point a1 = object.poly.points.get(x);
				Point a2 = object.poly.points.get(x + 1);	
				Line line2 = new Line(a1, a2);
				if (line1.isIntersecting(line2)) {
					if (object.collided == false) {
						object.collided = true;
						return true;
					} else {
						return false;
					}
					
				}
			}
		}
		return false;
	}

    protected void destroy() {
    }
}
