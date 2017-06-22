package s0553363;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Vector;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.Point;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.track.Track;

public class DriveCommand {
	private float targetX;
	private float targetY;
	private float carX;
	private float carY;
	private float carAngle;
	private float wishSpinSpeed;
	private float acceleration;
	private float speed = 1;
	
	
	private Polygon[] obstacles;
	private int lengthFor;
	private int seperations = 10;
	private Vector2D vectorLeft;
	private Vector2D vectorMiddle;
	private Vector2D vectorRight;

	private Info info;

	public DriveCommand(Info info) {
		this.info = info;
		obstacles = info.getTrack().getObstacles();
		
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

	public float[] seek(float carX, float carY, float targetX, float targetY) {
		
		this.carX = carX;
		this.carY = carY;
		this.targetX = targetX;
		this.targetY = targetY;
		
		
		float[] speedAcceleration = new float[2];

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
		
		
		// Ausweichen; optional
		doAvoidingStuff();

		speedAcceleration[0] = speed;
		speedAcceleration[1] = acceleration;

		return speedAcceleration;
	}

	public void doAvoidingStuff() {
		for (int i = 0; i < obstacles.length; i++)// durch obstacles iterieren
		{
			float obstacleX;
			float obstacleY;
			float distanceObstacle;

			float distanceLeft;
			float distanceRight;


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
				if (distanceObstacle < 10) {

					if (distanceLeft > distanceRight) {
						acceleration = 1;
					} else {
						acceleration = -1;
					}

				}

			}
		}
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
		// targetX = 1000f;
		// targetY = 300f;
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

}
