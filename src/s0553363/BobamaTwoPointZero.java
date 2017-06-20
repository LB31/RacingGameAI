package s0553363;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.Point;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;

public class BobamaTwoPointZero extends AI {
	private Polygon[] obstacles = info.getTrack().getObstacles();
	private ArrayList<ArrayList<Integer>> xpoints = new ArrayList<>();
	private ArrayList<ArrayList<Integer>> ypoints = new ArrayList<>();
	private boolean trackInitReady = false;
	
	private float targetX = 0;
	private float targetY = 0;
	private float carX;
	private float carY;

	private WeightedGraph graph = new WeightedGraph();

	public BobamaTwoPointZero(Info info) {
		super(info);

		// Add the obstacles coordinates to ArrayLists for a better handling
		for (int i = 0; i < obstacles.length; i++) {
			xpoints.add(new ArrayList<Integer>());
			ypoints.add(new ArrayList<Integer>());
			for (int j = 0; j < obstacles[i].xpoints.length; j++) {
				xpoints.get(i).add(obstacles[i].xpoints[j]);
				ypoints.get(i).add(obstacles[i].ypoints[j]);
			}
		}

		createGraph();
		findRoutes();
		graph.addVertex(info.getX(), info.getY());
		graph.addVertex((float)info.getCurrentCheckpoint().getX(), (float)info.getCurrentCheckpoint().getY());
		trackInitReady = true;
		
	}

	@Override
	public DriverAction update(boolean wasResetAfterCollision) {

		carX = info.getX();
		carY = info.getY();
		if(info.getCurrentCheckpoint().getX() != targetX && info.getCurrentCheckpoint().getY() != targetY){
		targetX = (float) info.getCurrentCheckpoint().getX();
		targetY = (float) info.getCurrentCheckpoint().getY();
		graph.removeVertex(graph.getVertices().size()-1);
		graph.removeVertex(graph.getVertices().size()-1);
		graph.clearCarTargetEdges();
		graph.addVertex(carX, carY);
		graph.addVertex(targetX, targetY);
		findRoutes();
		graph.printAdjacencyMatrix();
		}
		
		return new DriverAction(0, 0.01f);

	}

	@Override
	public void doDebugStuff() {
		// TODO Auto-generated method stub
		super.doDebugStuff();

		GL11.glPointSize(20);
		GL11.glColor3d(1, 0, 0);

		GL11.glBegin(GL11.GL_POINTS);

		for (int i = 0; i < graph.getVertices().size(); i++) {
			float x = graph.getVertices().get(i).getX();
			float y = graph.getVertices().get(i).getY();
			GL11.glVertex2d(x, y);

		}
		GL11.glEnd();
		int matrixLength = graph.getAdjacencyMatrix().length;
		for (int i = 0; i < matrixLength; i++) {
			for (int j = 0; j < matrixLength; j++) {
				float[] edges = graph.getEdgePairs(i, j);

				GL11.glBegin(GL11.GL_LINES);
				GL11.glColor3d(0, 1, 0);
				GL11.glVertex2d(edges[0], edges[1]);
				GL11.glVertex2d(edges[2], edges[3]);
				GL11.glEnd();

			}
		}

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Bobama 2.0";
	}

	@Override
	public String getTextureResourceName() {
		// TODO Auto-generated method stub
		return "/s0553363/custom1.png";
	}

	@Override
	public boolean isEnabledForRacing() {
		// TODO Auto-generated method stub
		return super.isEnabledForRacing();
	}


	
	
	// Checks if a given point c is on the right side from a and b
	// https://stackoverflow.com/a/3461533
	public boolean isRight(float ax, float bx, float cx, float ay, float by, float cy) {
		return ((bx - ax) * (cy - ay) - (by - ay) * (cx - ax)) <= 0;
	}

	public void createGraph() {

		for (int i = 0; i < xpoints.size(); i++)// durch obstacles iterieren
		{

			for (int j = 0; j < xpoints.get(i).size(); j++) {

				boolean goodPoint = false;
				float obstacleX = xpoints.get(i).get(j);
				float obstacleY = ypoints.get(i).get(j);
				float previousObstacleX;
				float previousObstacleY;
				float nextObstacleX;
				float nextObstacleY;

				// Beim ersten Punkt den letzten aus der Liste als Vorg�nger
				// ansprechen
				if (j == 0) {
					previousObstacleX = xpoints.get(i).get(xpoints.get(i).size() - 1);
					previousObstacleY = ypoints.get(i).get(ypoints.get(i).size() - 1);

				} else {
					previousObstacleX = xpoints.get(i).get(j - 1);
					previousObstacleY = ypoints.get(i).get(j - 1);
				}

				// Beim letzten Punkt den ersten aus der Liste als Nachfolger
				// ansprechen
				if (j == (xpoints.get(i).size() - 1)) {
					nextObstacleX = xpoints.get(i).get(0);
					nextObstacleY = ypoints.get(i).get(0);

				} else {
					nextObstacleX = xpoints.get(i).get(j + 1);
					nextObstacleY = ypoints.get(i).get(j + 1);
				}

			

				goodPoint = isRight(previousObstacleX, nextObstacleX, obstacleX, previousObstacleY, nextObstacleY,
						obstacleY);
				if (goodPoint) {
			
					float[] offSet = calcOffset(previousObstacleX, nextObstacleX, previousObstacleY, nextObstacleY);
					// check, if the new offset point is outside the track
					if (info.getTrack().getWidth() <= (obstacleX + offSet[0]) || (obstacleX + offSet[0]) < 0) {
						offSet[0] *= -1f;
						offSet[1] *= -1f;
					}
					graph.addVertex(obstacleX + offSet[0], obstacleY + offSet[1]);
					// graph.addVertex(obstacleX, obstacleY);
				} else {

					System.out.println("Punkt:");
					System.out.println(xpoints.get(i).get(j));
					System.out.println(ypoints.get(i).get(j));

					xpoints.get(i).remove(j);
					ypoints.get(i).remove(j);
					j = j - 1;
				}

			} // for j end
		} // for i end

	} // createGraph end

	// Abstand der Knoten zum urspr�nglichen Hindernis berechnen
	public float[] calcOffset(float ax, float bx, float ay, float by) {
		float[] backSet = new float[2];
		float offSet = 50;
		float newVectorX = bx - ax;
		float newVectorY = by - ay;
		float storer = newVectorX;
		newVectorX = newVectorY;
		newVectorY = storer * -1;
		float betrag = (float) Math.sqrt(Math.pow(newVectorX, 2) + Math.pow(newVectorY, 2));
		float kehrwert = 1 / betrag;
		newVectorX *= kehrwert * offSet;
		newVectorY *= kehrwert * offSet;


		backSet[0] = newVectorX;
		backSet[1] = newVectorY;
		return backSet;
	}

	// Polygon zwischen 2 Punkten bilden
	public Polygon createPolygon(Vector2D startPunkt, Vector2D endPunkt) {
		Polygon linePolygon = new Polygon();

		linePolygon.addPoint((int) (startPunkt.getX()), (int) (startPunkt.getY()));
		linePolygon.addPoint((int) (startPunkt.getX() + 1), (int) (startPunkt.getY()));
		linePolygon.addPoint((int) (endPunkt.getX()), (int) (endPunkt.getY()));
		linePolygon.addPoint((int) (endPunkt.getX()), (int) (endPunkt.getY() + 1));

		return linePolygon;
	}

	// Returns true, if there is a collision
	public boolean testIntersection(Polygon polygonA, Polygon polygonB) {
		Area areaA = new Area(polygonA);
		areaA.intersect(new Area(polygonB));
		return !areaA.isEmpty();
	}



	// Alle Knoten durchlaufen und diejenigen suchen, die sich sehen k�nnen
	public void findRoutes() {
		ArrayList<Vector2D> buffer = graph.getVertices();
		Polygon tempPoly;
		int jCompare;

		for (int i = 0; i < buffer.size(); i++) {
			if(trackInitReady){
				jCompare = buffer.size()-2;
			} else{
				jCompare = i + 1;
			}
			for (int j = jCompare; j < buffer.size(); j++) {
				tempPoly = createPolygon(buffer.get(i), buffer.get(j));
				boolean freeRoad = true;
				for (int k = 0; k < obstacles.length; k++) {
					boolean crash = testIntersection(tempPoly, obstacles[k]);
					if (crash) {
						freeRoad = false;
					}
				} // end for obstacles.length
				if (freeRoad) {
					float distancePoints = graph.calcDistance(buffer.get(i), buffer.get(j));
					graph.addEdge(i, j, distancePoints);
				} // if freeRoad end
			} // end for second buffer.size
		} // end for first buffere.size
	}

}