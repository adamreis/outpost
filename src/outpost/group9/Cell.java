package outpost.group9;

import outpost.sim.Point;

public class Cell {
	
	Point cell;
	public int land, water;
	public int score;
	public Cell()
	{
		cell=new Point();
		cell.x = 0;
		cell.y = 0;
		land = 0;
		water = 0;
		score=0;
	}
	public Cell(Point k)
	{
		cell=new Point();
		cell.x = k.x;
		cell.y = k.y;
		land = 0;
		water = 0;
	}
}
