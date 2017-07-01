package s0553363;

public class Vector2D {
	private float x;

	public void setX(float x) {
		this.x = x;
	}

	private float y;

	public void setY(float y) {
		this.y = y;
	}

	public Vector2D(float x, float y) {
		this.x = x;
		this.y = y;

	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public void rotate(double degree) {
		// degree = Math.toRadians(degree);
		float oldX = x;
		x = (float) (x * Math.cos(degree) - y * Math.sin(degree));

		y = (float) (oldX * Math.sin(degree) + y * Math.cos(degree));

	}

	public void setXY(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Vector2D cloneVector(Vector2D other) {
		return new Vector2D(other.getX(), other.getY());
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
