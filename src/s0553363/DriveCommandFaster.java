package s0553363;

import java.awt.Polygon;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;

import java.awt.geom.Point2D;

import org.lwjgl.opengl.GL11;

public class DriveCommandFaster {

	private float acceleration = 1f; // 0-1
	private float direction = 0;// 1 links, -1 rechts
	private float seekDirection = 0;// Gewichtung für seek
	private float a = 1;
	private float fleeDirection = 0;
	private float b = 0;// Gewichtung für flee

	// Koordinaten
	private float autoX;
	private float autoY;
	private float zielX;
	private float zielY;
	private float richtungX;
	private float richtungY;
	private float zielAbstand;

	// Hindernisse

	float kleinsterAbstand = 100;
	float abstandZumHindernis;
	float richtungsVektorHindernisX;
	float richtungsVektorHindernisY;
	float aktuellesHindernisX = 100;
	float aktuellesHindernisY = 100;
	float deltaWinkelHindernis = 1;
	float hindernisAusrichtung = 1;

	// Winkel
	private float zielAusrichtung;
	private float eigeneAusrichtung;
	private float deltaWinkelCheckPoint;

	// Debugzeuch
	private int timer = 0;

	private Info info;

	private Polygon[] obstacles;
	private int lengthFor;
	private int seperations = 10;
	private Vector2D vectorLeft;
	private Vector2D vectorMiddle;
	private Vector2D vectorRight;

	public DriveCommandFaster(Info info) {
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

	public float[] drive(float cX, float cY, float tX, float tY) {

		autoX = cX;
		autoY = cY;
		zielX = tX;
		zielY = tY;

		// Standarwerte setzen
		acceleration = 1;
		a = 1;
		b = 0;

		// Seekverhalten
		seekDirection = seek(zielX, zielY);

		// Wenn das Ziel hinter sich hinter dem Auto befindet, verringere die
		// Geschwindigkeit.
		if ((zielAusrichtung < eigeneAusrichtung - Math.PI / 2)
				|| (zielAusrichtung > eigeneAusrichtung + Math.PI / 2)) {
			// System.out.println("Das Ziel befindet sich hinter mir!");
			if ((info.getVelocity().length() > 15)) {
				acceleration = -0.25f;
			} else {
				acceleration = 1;
			}
		}

		// Finale Richtung bestimmen.
		direction = (a * seekDirection + b * fleeDirection) / (a + b);

		float[] backCar = new float[2];
		// Ausweichen; optional
		doAvoidingStuff();
		backCar[0] = acceleration;
		backCar[1] = direction;
		return backCar;

	}

	// Funktionen
	// Seek
	public float seek(float X, float Y) {
		// Richtungsvektor zum Checkpoint berechnen.
		autoX = info.getX();
		autoY = info.getY();
		zielX = X;
		zielY = Y;
		richtungX = zielX - autoX;
		richtungY = zielY - autoY;

		// Ausrichtungen berechnen
		zielAusrichtung = (float) Math.atan2(richtungY, richtungX);
		eigeneAusrichtung = info.getOrientation();
		deltaWinkelCheckPoint = (zielAusrichtung - eigeneAusrichtung);

		// Winkelüberlauf korrigieren
		if (deltaWinkelCheckPoint > Math.PI) {
			// System.out.println("Korrigiere Winkelüberlauf.");
			deltaWinkelCheckPoint = (float) (deltaWinkelCheckPoint - 2 * Math.PI);
		}
		if (deltaWinkelCheckPoint < -Math.PI) {
			// System.out.println("Korrigiere Winkelüberlauf.");
			deltaWinkelCheckPoint = (float) (deltaWinkelCheckPoint + 2 * Math.PI);
		}

		// Abstände berechnen
		zielAbstand = (float) Math.sqrt(Math.pow(richtungX, 2) + Math.pow(richtungY, 2));

		// Wenn der Checkpoint innerhalb des Toleranzbereich ist, gleiche die
		// Lenkrichtung an.
		if (Math.abs(deltaWinkelCheckPoint) < 0.785) {
			deltaWinkelCheckPoint = deltaWinkelCheckPoint * info.getMaxAngularAcceleration() / 0.785f;

			if (Math.abs(deltaWinkelCheckPoint) < 0.33) {
				deltaWinkelCheckPoint = (float) (deltaWinkelCheckPoint * info.getMaxAngularAcceleration() / 0.33);
				// Wenn man dem Checkpoint sehr nahe ist, aber nicht direkt
				// darauf ausgerichtet soll abgebremst werden.
				if ((zielAbstand < 125) && (Math.abs(deltaWinkelCheckPoint) > 0.2)) {
					// System.out.println("Feinausrichtung!");
					if (info.getVelocity().length() > 3f) {
						acceleration = -0.25f;
					} else {
						acceleration = 1;
					}
				}
			}
		} else {
			// Entscheiden ob nach links oder rechts gelenkt wird.
			if (zielAusrichtung > eigeneAusrichtung) {
				deltaWinkelCheckPoint = 1;
			}
			if (zielAusrichtung < eigeneAusrichtung) {
				deltaWinkelCheckPoint = -1;
			}
		}

		// Finale Lenkrichtung
		return (float) ((deltaWinkelCheckPoint - info.getAngularVelocity()));
	}

	public float getObstacleDistance(Vector2D vector, float obstacleX, float obstacleY) {
		float distanceObstacle;
		float distanceX;
		float distanceY;
		distanceX = obstacleX - (vector.getX() + autoX);
		distanceY = obstacleY - (vector.getY() + autoY);

		distanceObstacle = (float) Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
		return distanceObstacle;
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
			vectorMiddle.rotate(eigeneAusrichtung);

			vectorLeft = new Vector2D(15, 0);
			vectorLeft.rotate(eigeneAusrichtung + Math.toRadians(45));

			vectorRight = new Vector2D(15, 0);
			vectorRight.rotate(eigeneAusrichtung + Math.toRadians(-45));

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
				if (distanceObstacle < 12) {

					if (distanceLeft > distanceRight) {
						direction = 1;

					} else {
						direction = -1;

					}

				}

			}
		}
	}

}
