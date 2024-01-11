package ghost.asteroid.game.objects.components;

public class Point {

	public Double x;
	public Double y;
	private static final Double degreeToRad = Math.PI / 180;
	
	
	public Point(Double x, Double y) {
		this.x = x ;
		this.y = y;
	}
	
	
	public Point(Integer x, Integer y) {
		this.x = x + 0.0;
		this.y = y + 0.0;
	}
	
	
	public Point clone() {
		return new Point(x, y);
	}
	
	
	public String toString() {
		return "X: " + x + ", " + "Y: " + y;
	}
	
	
	public void rotate(Double rot) {
		Double cos = Math.cos(rot * degreeToRad);
		Double sin = Math.sin(rot * degreeToRad);

		Double newXpos = (x * cos) - (y * sin);
		Double newYpos = (x * sin) + (y * cos);			
		
		x = newXpos;
		y = newYpos;
	}
	
	
	public void translate(Double xPosIncrease, Double yPosIncrease) {
		x = (x + xPosIncrease);
		y = (y + yPosIncrease);
	}
	
	
	public void scale(Double scale) {
		x = x * scale;
		y = y * scale;
	}

}
