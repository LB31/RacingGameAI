package s0553363;


import java.awt.Polygon;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;


public class DriveCommandFaster
{
	private Info info;
	
	private float acceleration = 1f; //0-1
	private float direction = 0;//1 links, -1 rechts	
	private float maxRotSpeed = 1;
	private float maxSpeed = 1;
	private float seekDirection = 0;//Gewichtung für seek
	private float a = 1;
	private float fleeDirection = 0;
	private float b = 0;//Gewichtung für flee
	
	
	//Koordinaten
	private float autoX;
	private float autoY;
	private float zielX;
	private float zielY;
	private float richtungX;
	private float richtungY;
	private float zielAbstand;
	

	
	//Winkel	
	private float zielAusrichtung;
	private float eigeneAusrichtung;
	private float deltaWinkelCheckPoint;
	
	//Debugzeuch
	private int timer = 0;
	private int counterFeinausrichtung;
	private boolean ausweichVerhaltenAktiv;
	
	//Knoten, die tatsächlich abgefahren werden sollen.
	ArrayList<Point2D.Float> readyPath = new ArrayList<Point2D.Float>();
	private Integer pos;
	

	public DriveCommandFaster(Info info) 
	{
		this.info = info;
	}


	public float[] drive(float carX, float carY, float targetX, float targetY) 
	{		
		//Standarwerte setzen
		acceleration = 1;
		a = 1;
		b = 0;
		//A* ausführen
		autoX = carX;
		autoY = carY;
		zielX = targetX;
		zielY = targetY;
		
		
		//Seekverhalten
		seekDirection = seek(info.getCurrentCheckpoint().x, info.getCurrentCheckpoint().y);
		

		//Winkelüberlauf korrigieren
		if(deltaWinkelCheckPoint > Math.PI)
		{
//			System.out.println("Korrigiere Winkelüberlauf.");
			deltaWinkelCheckPoint = (float) (deltaWinkelCheckPoint - 2 * Math.PI);
		}
		if(deltaWinkelCheckPoint < -Math.PI)
		{
//			System.out.println("Korrigiere Winkelüberlauf.");
			deltaWinkelCheckPoint = (float) (deltaWinkelCheckPoint + 2 * Math.PI);
		}
		
		//Wenn das Ziel hinter sich hinter dem Auto befindet, verringere die Geschwindigkeit.
		if((zielAusrichtung < eigeneAusrichtung - Math.PI/2) || (zielAusrichtung > eigeneAusrichtung + Math.PI/2))
		{
//			System.out.println("Das Ziel befindet sich hinter mir!");
			if((info.getVelocity().length() > 0.8))
			{
				acceleration = -0.25f;
			}		
			else
			{
				acceleration = 1;
			}
		}
		
		//Finale Richtung bestimmen.
		direction = (a * seekDirection + b * fleeDirection) / (a+b);
		
		float[] speedAcceleration = new float[2];
		speedAcceleration[0] = acceleration;
		speedAcceleration[1] = direction;

		return speedAcceleration;
		

	}	
	
//Funktionen
	//Seek
	public float seek(float X, float Y)
	{
		//Richtungsvektor zum Checkpoint berechnen.
		autoX = info.getX();
		autoY = info.getY();
		zielX = X;
		zielY = Y;
		richtungX = zielX - autoX;
		richtungY = zielY - autoY;
		
		
		//Ausrichtungen berechnen
		zielAusrichtung = (float) Math.atan2(richtungY, richtungX);
		eigeneAusrichtung = info.getOrientation();
		deltaWinkelCheckPoint = (zielAusrichtung - eigeneAusrichtung);
		
		//Abstände berechnen
		zielAbstand =  (float) Math.sqrt(Math.pow(richtungX, 2) + Math.pow(richtungY, 2));
		
		//Wenn der Checkpoint innerhalb des Toleranzbereich ist, gleiche die Lenkrichtung an.
		if(Math.abs(deltaWinkelCheckPoint) < 0.785)		
		{
			seekDirection = deltaWinkelCheckPoint * info.getMaxAngularAcceleration()/0.785f;
			
			if(Math.abs(deltaWinkelCheckPoint) < 0.33)
			{
				seekDirection = (float) (deltaWinkelCheckPoint * info.getMaxAngularAcceleration()/0.33);
				//Wenn man dem Checkpoint sehr nahe ist, aber nicht direkt darauf ausgerichtet soll abgebremst werden.
				if((zielAbstand < 125) && (Math.abs(deltaWinkelCheckPoint) > 0.2))
				{
//					System.out.println("Feinausrichtung!");
					if(info.getVelocity().length() > 0.6f)
					{
						acceleration = -0.25f;
					}
					else
					{
						acceleration = 1;
					}
				}
			}
		}
		else 
		{
			//Entscheiden ob nach links oder rechts gelenkt wird.
			if(zielAusrichtung > eigeneAusrichtung)
			{
				seekDirection = 1;
			}
			if(zielAusrichtung < eigeneAusrichtung)
			{
				seekDirection = -1;
			}			
		}
		
		//Finale Lenkrichtung
		return (float) ((seekDirection - info.getAngularVelocity()));
	}
	

	

}

