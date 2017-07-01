package s0553363;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Leonid Barsht
 *
 */
public class WeightedGraph {
	private float[][] adjacencyMatrix; // [row][col]

	ArrayList<Vector2D> vertices = new ArrayList<>();
	boolean programStart = true;
	int numberVertices;

	public ArrayList<Vector2D> getVertices() {
		return vertices;
	}

	public float[][] getAdjacencyMatrix() {
		return adjacencyMatrix;
	}

	public WeightedGraph() {
		// openList.put(0, 25f);
		// openList.put(1, 5f);
		// openList.put(2, 4f);
		// openList.put(3, 8f);
		// temp = (getKeyByValue(4f));
		// printVertices();
	}

	// A * Algorithm Fields
	private HashMap<Integer, Float> openList = new HashMap<Integer, Float>();
	private List<Integer> closedList = new ArrayList<Integer>();
	private HashMap<Integer, Integer> cameFrom = new HashMap<Integer, Integer>();
	private HashMap<Integer, Float> fScore = new HashMap<Integer, Float>() {
		@Override
		public Float get(Object key) {
			if (!containsKey(key))
				return Float.MAX_VALUE;
			return super.get(key);
		}
	};
	private HashMap<Integer, Float> gScore = new HashMap<Integer, Float>() {
		@Override
		public Float get(Object key) {
			if (!containsKey(key))
				return Float.MAX_VALUE;
			return super.get(key);
		}
	};

	// A * Algorithm
	public ArrayList<Vector2D> aStar(int start, int end) {
		openList = new HashMap<Integer, Float>();
		closedList = new ArrayList<Integer>();
		cameFrom = new HashMap<Integer, Integer>();
		fScore = new HashMap<Integer, Float>() {
			@Override
			public Float get(Object key) {
				if (!containsKey(key))
					return Float.MAX_VALUE;
				return super.get(key);
			}
		};
		gScore = new HashMap<Integer, Float>() {
			@Override
			public Float get(Object key) {
				if (!containsKey(key))
					return Float.MAX_VALUE;
				return super.get(key);
			}
		};

		gScore.put(start, 0f);
		fScore.put(start, calcHCost(start, end)); // TODO
		openList.put(start, calcFCost(start, start, end)); // TODO

		while (openList.size() > 0) {
			int current = getKeyByValue(Collections.min(openList.values()));
			if (current == end) {
				return reconstructPath(cameFrom, current);
			}
			openList.remove(current);
			closedList.add(current);

			for (Integer neighbour : getNeighbours(current)) {
				if (closedList.contains(neighbour)) {
					continue;
				}

				if (!openList.containsKey(neighbour)) {
					openList.put(neighbour, calcFCost(neighbour, start, end));
				}

				float tempGScore = gScore.get(current) + calcDistance(vertices.get(current), vertices.get(neighbour));
				if (tempGScore >= gScore.get(neighbour)) {
					continue;
				}
				cameFrom.put(neighbour, current);
				gScore.put(neighbour, tempGScore);
				fScore.put(neighbour, gScore.get(neighbour) + calcHCost(neighbour, end));

			}

		}
		return null;
	}

	public ArrayList<Vector2D> reconstructPath(HashMap<Integer, Integer> cameFrom, int current) {
		ArrayList<Vector2D> totalPath = new ArrayList<>();
		totalPath.add(new Vector2D(vertices.get(current).getX(), vertices.get(current).getY()));

		while (cameFrom.keySet().contains(current)) {
			current = cameFrom.get(current);
			// totalPath.add(current);
			totalPath.add(new Vector2D(vertices.get(current).getX(), vertices.get(current).getY()));

		}

		return totalPath;
	}

	public int getKeyByValue(float value) {
		int backKey = 0;
		for (int key : openList.keySet()) {

			if (openList.get(key) == value) {
				backKey = key;
			}

		}

		return backKey;
	}

	// Abstand vom Startpunkt
	public float calcGCost(int currentPoint, int startPoint) {

		float weight = adjacencyMatrix[startPoint][currentPoint];
		

		return weight;
	}

	// Abstand vom Ziel
	public float calcHCost(int currentPoint, int destinationPoint) {
		float distanceTarget = calcDistance(vertices.get(currentPoint), vertices.get(destinationPoint));
		return distanceTarget;
	}

	public float calcFCost(int currentPoint, int startPoint, int destinationPoint) {
		return calcGCost(currentPoint, startPoint) + calcHCost(currentPoint, destinationPoint);

	}

	public ArrayList<Integer> getNeighbours(int root) {
		ArrayList<Integer> neighbours = new ArrayList<>();
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			if (adjacencyMatrix[root][i] != -1f) {
				neighbours.add(i);
			}
		}
		return neighbours;
	}

	// Abstand zwischen Knoten berechnen
	public float calcDistance(Vector2D start, Vector2D end) {
		float differenceX = end.getX() - start.getX();
		float differenceY = end.getY() - start.getY();
		float distance = (float) Math.sqrt(Math.pow(differenceX, 2) + Math.pow(differenceY, 2));
		return distance;
	}

	public void printVertices() {
		String x = "The x are: ";
		String y = "The y are: ";
		for (int i = 0; i < vertices.size(); i++) {
			x += "'" + vertices.get(i).getX() + "' ";
			y += "'" + vertices.get(i).getY() + "' ";

		}
		System.out.println(x);
		System.out.println(y);

	}

	// Breadth-first search
	// public String getShortestPath(int startVertex, int endVertex) {
	//
	// int start = startVertex;
	// int end = endVertex;
	//
	//
	// Queue<Integer> queue = new LinkedList<Integer>();
	// ArrayList<Integer> directions = new ArrayList<>();
	// boolean[] visited = new boolean[adjacencyMatrix.length];
	//
	// int node;
	//
	// queue.add(start);
	// while (!queue.isEmpty()) {
	// node = queue.poll();
	// if (node == end) {
	// return arrayToString(directions);
	// }
	// ArrayList<Integer> neighbours = getNeighbours(node);
	// visited[node] = true;
	// for (int child : neighbours) {
	// if (visited[child] == false) {
	// queue.add(child);
	// visited[child] = true;
	// directions.add(node);
	//
	// }
	// }
	//
	// }
	//
	// return "There wasn't a path";
	//
	// }

	// private String arrayToString(ArrayList<Integer> directions) {
	// String all = "The path was: ";
	// for (int i = 0; i < directions.size(); i++) {
	// all += directions.get(i) + " ";
	// }
	// return all;
	// }

	// Dijkstra algorithm
	// public void getCheapestPath(int graph[][], int src, int aim) {
	//
	// int dist[] = new int[numberVertices];
	// Boolean sptSet[] = new Boolean[numberVertices];
	//
	// for (int i = 0; i < numberVertices; i++) {
	// dist[i] = Integer.MAX_VALUE;
	// sptSet[i] = false;
	// }
	//
	// dist[src] = 0;
	//
	// for (int count = 0; count < numberVertices - 1; count++) {
	//
	// int u = minDistance(dist, sptSet);
	//
	// sptSet[u] = true;
	//
	// for (int v = 0; v < numberVertices; v++)
	//
	// if (!sptSet[v] && graph[u][v] != 0 && dist[u] != Integer.MAX_VALUE &&
	// dist[u] + graph[u][v] < dist[v])
	// dist[v] = dist[u] + graph[u][v];
	// }
	//
	// printSolution(dist, numberVertices);
	//
	// System.out.println("The cheapest path between the cities '" +
	// indexToString(aim) + "' and '"
	// + indexToString(src) + "' is " + dist[aim]);
	//
	// }

	public void printSolution(int dist[], int n) {
		System.out.println("Vertex Distance from Source");
		for (int i = 0; i < numberVertices; i++)
			System.out.println(i + " \t\t " + dist[i]);
	}

	public int minDistance(int dist[], Boolean sptSet[]) {
		// Initialize min value
		int min = Integer.MAX_VALUE, min_index = -1;

		for (int v = 0; v < numberVertices; v++)
			if (sptSet[v] == false && dist[v] <= min) {
				min = dist[v];
				min_index = v;
			}

		return min_index;
	}

	public void printAdjacencyMatrix() {
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			System.out.print(i + " ||| ");
			for (int j = 0; j < adjacencyMatrix[i].length; j++) {

				System.out.print(adjacencyMatrix[i][j] + " | ");
			}
			System.out.println();
		}
	}

	// public void weightBetween(int start, int end) {
	// int weight = adjacencyMatrix[start][end];
	// String city1 = vertices[start];
	// String city2 = vertices[end];
	// if (weight == 0)
	// System.out.println("These cities aren't connected");
	// else
	// System.out.println("The distance between the cities '" + city1 + "' and
	// '" + city2 + "' is " + weight);
	//
	// }

	public void addEdge(int sourceVertex, int targetVertex, float weight) {
		if (programStart) {
			numberVertices = vertices.size() + 2;
			adjacencyMatrix = new float[numberVertices][numberVertices];
			for (float[] floatArray : adjacencyMatrix) {
				Arrays.fill(floatArray, -1f);
			}
			programStart = false;
		}
		adjacencyMatrix[sourceVertex][targetVertex] = weight;
		adjacencyMatrix[targetVertex][sourceVertex] = weight;
	}

	// Entfernt die letzte und vorletzte Kante in der Matrix
	public void clearCarTargetEdges() {

		for (int i = 0; i < adjacencyMatrix.length; i++) {
//			 adjacencyMatrix[i][vertices.size() - 1] = -1f;
//			 adjacencyMatrix[vertices.size() - 1][i] = -1f;
//			 adjacencyMatrix[i][vertices.size() - 2] = -1f;
//			 adjacencyMatrix[vertices.size() - 2][i] = -1f;
			
			for (float[] floatArray : adjacencyMatrix) {
				Arrays.fill(floatArray, -1f);
			}

		}

	}

	public void removeEdge(int sourceVertex, int targetVertex) {
		adjacencyMatrix[sourceVertex][targetVertex] = -1f;
		adjacencyMatrix[targetVertex][sourceVertex] = -1f;
	}

	public void addVertex(float vertexX, float VertexY) {
		vertices.add(new Vector2D(vertexX, VertexY));
	}

	public void removeVertex(int position) {
		vertices.remove(position);
	}

	public float[] getEdgePairs(int i, int j) {
		float[] pairReturn = new float[4];

		if (adjacencyMatrix[i][j] != -1.0) {
			pairReturn[0] = vertices.get(i).getX();
			pairReturn[1] = vertices.get(i).getY();
			pairReturn[2] = vertices.get(j).getX();
			pairReturn[3] = vertices.get(j).getY();

		}
		return pairReturn;
	}

	//
	// public boolean containsEdge(Edge e) {
	// // TODO Auto-generated method stub
	// return false;
	// }
	//
	//
	// public boolean containsVertex(Vertex v) {
	// // TODO Auto-generated method stub
	// return false;
	// }
	//
	//
	// public Edge getEdge(Vertex sourceVertex, Vertex targetVertex) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	//
	// public double getWeight(Edge e) {
	// // TODO Auto-generated method stub
	// return 0;
	// }

}
