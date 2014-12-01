package outpost.group2;
import java.util.ArrayList;
import java.util.Scanner;

import outpost.sim.Pair;

import java.awt.Point;
import java.awt.Polygon;

public class CalConvexHull {
	
	

	// be careful, the arraylist of points might be changed
	public ArrayList<Pair> getConvexHull(ArrayList<Pair> outposts, int player_id, int size)
	{
		ArrayList<Pair> myConvexHull = new ArrayList<Pair>();
		ArrayList<Pair> points = new ArrayList<Pair>();
		points = (ArrayList) outposts.clone();
		if(points.size() < 3)
		{
			myConvexHull = points;
			return myConvexHull;
		}
		
		int minPoint = -1;
		int maxPoint = -1;
		
		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		
		for(int i = 0; i < points.size(); i++)
		{
			if(points.get(i).x < minX)
			{
				minX = points.get(i).x;
			}
			
			if(points.get(i).x > maxX)
			{
				maxX = points.get(i).x;
			}
			
			if(points.get(i).y < minY)
			{
				minY = points.get(i).y;
			}
			
			if(points.get(i).y > maxY)
			{
				maxY = points.get(i).y;
			}
		}
		Pair A = new Pair();
		Pair B = new Pair();
		
		if(player_id == 0)
		{
			A = new Pair(maxX, 0);
			B = new Pair(0, maxY);
			points.add(new Pair(0, 0));
			//myConvexHull.add(new Pair(0, 0));
		}
		
		if(player_id == 1) // we are at the top-left corner
		{
			A = new Pair(minX, 0);
			B = new Pair(size-1, maxY);	
			points.add(new Pair(size-1,0));
			//myConvexHull.add(new Pair(size-1, 0));
		}
		if(player_id == 3) // we are at the top-right corner
		{
			A = new Pair(maxX, size-1);
			B = new Pair(0, minY);
			points.add(new Pair(0, size-1));
		}
		if(player_id == 2)
		{
			A = new Pair(minX, size-1);
			B = new Pair(size-1, minY);
			points.add(new Pair(size-1, size-1));
			//myConvexHull.add(new Pair(size-1, size-1));
		}
		points.add(A);
		points.add(B);
		
	/*	minX = Integer.MAX_VALUE;
		maxX = Integer.MIN_VALUE;
		
		for(int i = 0; i < points.size(); i++)
		{
			if(points.get(i).x < minX)
			{
				minX = points.get(i).x;
				minPoint = i;
			}
			
			if(points.get(i).x > maxX)
			{
				maxX = points.get(i).x;
				maxPoint = i;
			}
			
		}
		
		if(maxX < 50)
		{
			A = new Pair(50, 0);
			B = new Pair(0, 50);	
		}
		else
		{
			if(maxX + 20 < 100)
			{
				A = new Pair(maxX + 20, 0);
				B = new Pair(maxX + 20, 50);	
			}
			else
			{
				A = new Pair(maxX, 0);
				B = new Pair(maxX, 50);
			}
		}	*/
		myConvexHull.add(A);
		myConvexHull.add(B);

	//	System.out.println("minPoint x: " + points.get(minPoint).x + ", y: " + points.get(minPoint).y);
	//	System.out.println("maxPoint x: " + points.get(maxPoint).x + ", y: " + points.get(maxPoint).y);
		
		//points.remove(points.get(minPoint));
		//points.remove(points.get(maxPoint));

		
		/*Pair A = points.get(minPoint);
		Pair B = points.get(maxPoint);
		myConvexHull.add(A);
		myConvexHull.add(B);
		
		points.remove(A);
		points.remove(B);*/
		
		ArrayList<Pair> leftSet = new ArrayList<Pair>();
		ArrayList<Pair> rightSet = new ArrayList<Pair>();
		

		
		for(int i = 0; i < points.size(); i++)
		{
			Pair p = points.get(i);
			if(pointLocation(A, B, p) == -1)
				leftSet.add(p);
			else
				rightSet.add(p);
		}
		
		hullSet(A, B, rightSet, myConvexHull);
		hullSet(B, A, leftSet, myConvexHull);	
		
		/*System.out.println("---------print convexhull---------");
		for(int i = 0; i < myConvexHull.size(); i++)
		{
			System.out.println("x: " + myConvexHull.get(i).x + ", y: " + myConvexHull.get(i).y);
		}*/
		
		return myConvexHull;
	}
	
	public int distance(Pair A, Pair B, Pair C) {
		int ABx = B.x - A.x;
		int ABy = B.y - A.y;
		int num = ABx * (A.y - C.y) - ABy * (A.x - C.x);
		if (num < 0)
			num = -num;
		return num;
	}
	
	public void hullSet(Pair A, Pair B, ArrayList<Pair> set,
            ArrayList<Pair> hull)
    {
        int insertPosition = hull.indexOf(B);
        if (set.size() == 0)
            return;
        if (set.size() == 1)
        {
            Pair p = set.get(0);
            set.remove(p);
            hull.add(insertPosition, p);
            return;
        }
        int dist = Integer.MIN_VALUE;
        int furthestPoint = -1;
        for (int i = 0; i < set.size(); i++)
        {
            Pair p = set.get(i);
            int distance = distance(A, B, p);
            if (distance > dist)
            {
                dist = distance;
                furthestPoint = i;
            }
        }
        Pair P = set.get(furthestPoint);
        set.remove(furthestPoint);
        hull.add(insertPosition, P);
 
        // Determine who's to the left of AP
        ArrayList<Pair> leftSetAP = new ArrayList<Pair>();
        for (int i = 0; i < set.size(); i++)
        {
            Pair M = set.get(i);
            if (pointLocation(A, P, M) == 1)
            {
                leftSetAP.add(M);
            }
        }
 
        // Determine who's to the left of PB
        ArrayList<Pair> leftSetPB = new ArrayList<Pair>();
        for (int i = 0; i < set.size(); i++)
        {
            Pair M = set.get(i);
            if (pointLocation(P, B, M) == 1)
            {
                leftSetPB.add(M);
            }
        }
        hullSet(A, P, leftSetAP, hull);
        hullSet(P, B, leftSetPB, hull);
 
    }
	
	public int pointLocation(Pair A, Pair B, Pair P)
    {
        int cp1 = (B.x - A.x) * (P.y - A.y) - (B.y - A.y) * (P.x - A.x);
        return (cp1 > 0) ? 1 : -1;
    }
	
	public ArrayList<Pair> findIntruder(ArrayList<Pair> myConvexHull, ArrayList<ArrayList<Pair>> king_outpostlist, int playerID)
	{
		Polygon poly = new Polygon();
		ArrayList<Pair> intruderList = new ArrayList<Pair>();
		
		for(int i = 0; i < myConvexHull.size(); i++)
			poly.addPoint(myConvexHull.get(i).x, myConvexHull.get(i).y);
		
		for(int i = 0; i < king_outpostlist.size(); i++)
		{
			if(i != playerID)
			{
				for(int j = 0; j < king_outpostlist.get(i).size(); j++)
				{
					if(poly.contains(new Point(king_outpostlist.get(i).get(j).x, king_outpostlist.get(i).get(j).y)))
					{
						intruderList.add(king_outpostlist.get(i).get(j));
					}
				}
			}
			
		}
		return intruderList;
	}
}
