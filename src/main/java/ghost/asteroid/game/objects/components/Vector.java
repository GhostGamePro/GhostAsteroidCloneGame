package ghost.asteroid.game.objects.components;

public class Vector {

	private Point pt;
	
	public Vector(Double x, Double y) {
		pt = new Point(x, y);
	}
	
	
	public Vector(Point pt) {
		this.pt = pt;
	}
	
	
	public void add(Vector v2) {
		pt.x += v2.pt.x;
		pt.y += v2.pt.y;
	}
	
	
	public void rotate(Double angle) {
		pt.rotate(angle);
	}	
	
	
	public void scale(Double scale) {
		pt.scale(scale);
	}
	
	
	public Double getX() {
		return pt.x;
	}
	
	
	public Double getY() {
		return pt.y;
	}
	
	
	public void setX(Double x) {
		pt.x = x;
	}
	
	
	public void setY(Double y) {
		pt.y = y;
	}		
	
	
	public Vector clone() {
		return new Vector(this.pt.clone());
	}
}
