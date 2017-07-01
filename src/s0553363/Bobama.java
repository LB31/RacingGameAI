package s0553363;

/**
 * Selten dämliches Auto. Jedoch findet es sein Ziel. Auch wenn später als andere.
 * Gute Eltern lieben all ihre Kinder..
 * 
 * @author Leonid Barsht s0553363 und Eric Wagner s0554195
 * @version 2017.05.16
 */

import java.awt.Polygon;

import org.lwjgl.opengl.GL11;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;

public class Bobama extends AI {
	private float targetX;
	private float targetY;
	private float carX;
	private float carY;
	private float carAngle;
	private float wishSpinSpeed;
	private float wishSpeed;
	private float acceleration;
	private float speed = 1;
	private float tolerance = 0.01f;

	private Vector2D vectorLeft;
	private Vector2D vectorMiddle;
	private Vector2D vectorRight;

	private float distanceObsticleLeft;
	private float distanceObsticleMiddle;
	private float distanceObsticleRight;
	private int lengthFor;
	private int seperations = 10;

	private Polygon[] obstacles = info.getTrack().getObstacles();

	public Bobama(Info info) {
		super(info);

		// Add own obsticles
		for (int i = 0; i < obstacles.length; i++)// durch obstacles iterieren
		{
			for (int j = 0; j < obstacles[i].xpoints.length; j++)// durch
																	// x-Koordinaten
																	// iterieren

			{

				if (j == 0)
					lengthFor = obstacles[i].xpoints.length;
				if (j < lengthFor - 1) {

					for (int k = 0; k < seperations; k++) {
						obstacles[i].addPoint(
								(int) ((obstacles[i].xpoints[j + 1] - obstacles[i].xpoints[j]) / seperations * (k + 1))
										+ obstacles[i].xpoints[j],
								(int) ((obstacles[i].ypoints[j + 1] - obstacles[i].ypoints[j]) / seperations * (k + 1))
										+ obstacles[i].ypoints[j]);
						if (j == lengthFor - 2) {
							int indexJ = j + 1;
							obstacles[i].addPoint(

									(int) ((obstacles[i].xpoints[0] - obstacles[i].xpoints[indexJ]) / seperations
											* (k + 1)) + obstacles[i].xpoints[indexJ],
									(int) ((obstacles[i].ypoints[0] - obstacles[i].ypoints[indexJ]) / seperations
											* (k + 1)) + obstacles[i].ypoints[indexJ]);

						}
					}

				}
			}
		}

	}

	@Override
	public DriverAction update(boolean wasResetAfterCollision) {

		// Delegieren an Align
		carX = info.getX();
		carY = info.getY();
		targetX = (float) info.getCurrentCheckpoint().getX();
		targetY = (float) info.getCurrentCheckpoint().getY();
		// targetX = 450;
		// targetY = 800;

		carAngle = info.getOrientation();
		wishSpinSpeed = delegateAlign(targetX, targetY);

		// Angle between vectors
		float angleBetween = angleBetweenVectors();

		// Verhaltensbaustein: Align
		if (angleBetween < (Math.PI / 2f)) {
			wishSpinSpeed *= info.getMaxAngularAcceleration() / (Math.PI / 2f);
		} else {
			wishSpinSpeed = info.getMaxAngularAcceleration();
		}

		// Angle Clipping
		if (wishSpinSpeed > Math.PI) {
			wishSpinSpeed -= Math.PI * 2;
		} else if (wishSpinSpeed < -Math.PI) {
			wishSpinSpeed += Math.PI * 2;
		}

		// Arrive
		if (getDistance() < 100 && delegateAlign(targetX, targetY) > 0.15) {
			speed = -1;
		} else {
			speed = 1;
		}

		// float abbremsRadius = 15;
		// if(getDistance()< abbremsRadius){
		// wishSpeed = (float) ((getDistance()*1f/10000f) *
		// info.getMaxAcceleration() / Math.toRadians(abbremsRadius));
		//
		// }else{
		// wishSpeed = info.getMaxAcceleration();
		// }
		//
		// speed = wishSpeed - 1;
		//
		// if(speed > 1) speed = 1;

		// if(angleBetween < tolerance){
		// acceleration = 0;
		// }

		acceleration = wishSpinSpeed - info.getAngularVelocity();

		for (int i = 0; i < obstacles.length; i++)// durch obstacles iterieren
		{
			float obstacleX;
			float obstacleY;
			float distanceObstacle;
			float distanceX;
			float distanceY;
			float distanceLeftX;
			float distanceLeftY;
			float distanceRightX;
			float distanceRightY;
			float distanceLeft;
			float distanceRight;

			float distanceMyObstacles;

			// Richtungsvektoren zum Auto berechnen
			vectorMiddle = new Vector2D(25, 0);
			vectorMiddle.rotate(carAngle);

			vectorLeft = new Vector2D(15, 0);
			vectorLeft.rotate(carAngle + Math.toRadians(45));

			vectorRight = new Vector2D(15, 0);
			vectorRight.rotate(carAngle + Math.toRadians(-45));

			for (int j = 0; j < obstacles[i].xpoints.length; j++)// durch
																	// x-Koordinaten
																	// iterieren
			{

				// Koordinaten des Hindernisses holen.
				obstacleX = obstacles[i].xpoints[j];
				obstacleY = obstacles[i].ypoints[j];

				distanceObstacle = getObstacleDistance(vectorMiddle, obstacleX, obstacleY);

				distanceLeft = getObstacleDistance(vectorLeft, obstacleX, obstacleY);

				distanceRight = getObstacleDistance(vectorRight, obstacleX, obstacleY);

				// Radius vom Hindernis
				if (distanceObstacle < 30) {

					if (distanceLeft > distanceRight) {
						acceleration = 1;
					} else {
						acceleration = -1;
					}

				}

			}
		}

		return new DriverAction(speed, acceleration);
	}

	public float getDistance() {

		float distanceX;
		float distanceY;
		distanceX = targetX - carX;
		distanceY = targetY - carY;

		return (float) Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
	}

	public float getObstacleDistance(Vector2D vector, float obstacleX, float obstacleY) {
		float distanceObstacle;
		float distanceX;
		float distanceY;
		distanceX = obstacleX - (vector.getX() + carX);
		distanceY = obstacleY - (vector.getY() + carY);

		distanceObstacle = (float) Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
		return distanceObstacle;
	}

	// Delegieren an Align
	public float delegateAlign(float targetX, float targetY) {
		targetX = 900f;
		targetY = 500f;
		float directionX = targetX - carX;
		float directionY = targetY - carY;
		float faceAlign = (float) Math.atan2(directionY, directionX);

		return faceAlign - carAngle;
	}

	// Angle between vectors
	public float angleBetweenVectors() {
		float skalar = carX * targetX + carY * targetY;
		float lengthVectorCar = (float) Math.sqrt(Math.pow(carX, 2) + Math.pow(carY, 2));
		float lengthVectorTarget = (float) Math.sqrt(Math.pow(targetX, 2) + Math.pow(targetY, 2));
		float angleBetween = (float) Math.acos(skalar / (lengthVectorCar * lengthVectorTarget));
		return angleBetween;
	}

	@Override
	public void doDebugStuff() {
		// TODO Auto-generated method stub
		super.doDebugStuff();

		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3d(1, 0, 0);
		GL11.glVertex2d(carX, carY);
		GL11.glVertex2d(vectorMiddle.getX() + carX, vectorMiddle.getY() + carY);
		//
		GL11.glColor3d(0, 1, 0);
		GL11.glVertex2d(carX, carY);
		GL11.glVertex2d(vectorLeft.getX() + carX, vectorLeft.getY() + carY);

		GL11.glColor3d(0, 0, 1);
		GL11.glVertex2d(carX, carY);
		GL11.glVertex2d(vectorRight.getX() + carX, vectorRight.getY() + carY);

		GL11.glEnd();

		// GL11.glPointSize(10);
		// GL11.glColor3d(1, 0, 0);
		//
		// GL11.glBegin(GL11.GL_POINTS);
		//
		// for (int i = 0; i < obstacles[2].xpoints.length; i++) {
		// GL11.glVertex2d(obstacles[2].xpoints[i], obstacles[2].ypoints[i]);
		// }
		//
		// for (int i = 0; i < obstacles[2].xpoints.length; i++) {
		// GL11.glVertex2d(obstacles[2].xpoints[i], obstacles[2].ypoints[i]);
		// }
		//
		// GL11.glEnd();

		// GL11.glPointSize(20);
		// GL11.glColor3d(1, 0, 0);
		//
		// GL11.glBegin(GL11.GL_POINTS);
		//
		// GL11.glVertex2d(960, 960);
		// GL11.glVertex2d(40, 40);
		// GL11.glVertex2d(40, 960);
		// GL11.glVertex2d(960, 40);
		// GL11.glVertex2d(500, 600);
		// GL11.glVertex2d(carX, carY);
		// System.out.println(carX);
		// System.out.println(carY);
		// GL11.glEnd();

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Bobama";
	}

	@Override
	public String getTextureResourceName() {
		// TODO Auto-generated method stub
		return "/s0553363/custom1.png";
	}

	@Override
	public boolean isEnabledForRacing() {
		// TODO Auto-generated method stub
		// return super.isEnabledForRacing();
		return true;
	}

}
