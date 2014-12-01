package outpost.group3;

import java.util.*;

import outpost.group3.Loc;

/* Implementation of jump point search as described in http://users.cecs.anu.edu.au/~dharabor/data/papers/harabor-grastien-aaai11.pdf
 * Ported from https://github.com/qiao/PathFinding.js/
 * All credit goes to the original authors
 */

public class JPS {	
	private class Node {
		int x;
		int y;
		boolean allowed;
		boolean opened;
		boolean closed;
		int g;
		int f;
		int h;
		Node parent;
		
		Node (int x, int y, boolean allowed) {
			this.x = x;
			this.y = y;
			this.allowed = allowed;
			this.opened = false;
			this.closed = false;
			this.g = 0;
			this.f = 0;
			this.h = 0;
			this.parent = null;
		}	
	}
	
	private class NodeComparator implements Comparator<Node> {
		@Override
		public int compare(Node n1, Node n2) {
			return (int) (n1.f - n2.f);
		}
	}
	
	private boolean rawGrid[][];
	private Node grid[][];
	private int width;
	private int height;
	private Node startNode;
	private Node endNode;
	
	private PriorityQueue<Node> openList;
	
	JPS (boolean rawGrid[][], int width, int height) {
		this.rawGrid = rawGrid;
		this.width = width;
		this.height = height;
	}
	
	public ArrayList<Loc> findPath(Loc startLoc, Loc endLoc) {
		Comparator<Node> comparator = new NodeComparator();
		openList = new PriorityQueue<Node>(100, comparator);
		
		// Set up the grid of nodes
		grid = new Node[width][height];
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				grid[x][y] = new Node(x, y, rawGrid[x][y]);

		startNode = grid[startLoc.x][startLoc.y];
		endNode = grid[endLoc.x][endLoc.y];
		
		openList.add(startNode);
		startNode.opened = true;
		
		while (!openList.isEmpty()) {
			Node node = openList.poll();
			node.closed = true;
			
			if (node.x == endNode.x && node.y == endNode.y) {
				return expand(backtrace(endNode));
			}
			
			identifySuccessors(node);
		}
		
		return new ArrayList<Loc>();
	}
	
	private ArrayList<Loc> backtrace(Node node) {
		ArrayList<Loc> locs = new ArrayList<Loc>();
		locs.add(new Loc(node.x, node.y));
		
		while (node.parent != null) {
			node = node.parent;
			locs.add(new Loc(node.x, node.y));
		}
		
		Collections.reverse(locs);
		return locs;
	}
	
	private ArrayList<Loc> interpolate(int x0, int y0, int x1, int y1) {
		ArrayList<Loc> line = new ArrayList<Loc>();
		
		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);
		
		int sx = (x0 < x1) ? 1 : -1;
		int sy = (y0 < y1) ? 1 : -1;
		
		int err = dx - dy;
		
		while (true) {
			line.add(new Loc(x0, y0));
			
			if (x0 == x1 && y0 == y1)
				break;
			
			int err2 = 2 * err;
			
			if (err2 > -dy) {
				err = err - dy;
				x0 = x0 + sx;
			}
			
			if (err2 < dx) {
				err = err + dx;
				y0 = y0 + sy;
			}
		}
		
		return line;
	}
	
	private ArrayList<Loc> expand(ArrayList<Loc> path) {
		ArrayList<Loc> expanded = new ArrayList<Loc>();

		if (path.size() < 2)
		    return expanded;
		
		for (int i = 0; i < path.size() - 1; i++) {
			Loc l0 = path.get(i);
			Loc l1 = path.get(i + 1);
		
			ArrayList<Loc> interpolated = interpolate(l0.x, l0.y, l1.x, l1.y);
			
			for (int j = 0; j < interpolated.size() - 1; j++)
		        expanded.add(interpolated.get(j));
		}
		
		expanded.add(path.get(path.size() - 1));
		
		return expanded;
	}
	
	private boolean isInside(int x, int y) {
		return (x >= 0 && x < width) && (y >= 0 && y < height);
	}
	
	private boolean isAllowed(int x, int y) {
		return isInside(x, y) && grid[x][y].allowed;
	}
	
	private ArrayList<Loc> orthogonalNodes(Node node) {
		ArrayList<Loc> locs = new ArrayList<Loc>();
		
		if (node.x > 0)
			locs.add(new Loc(node.x - 1, node.y));
		
		if (node.y > 0)
			locs.add(new Loc(node.x, node.y - 1));
		
		if (node.x < width - 1)
			locs.add(new Loc(node.x + 1, node.y));
		
		if (node.y < height - 1)
			locs.add(new Loc(node.x, node.y + 1));
		
		return locs;
	}
	
	private ArrayList<Loc> findNeighbors(Node node) {
		if (node.parent != null) {
			ArrayList<Loc> neighbors = new ArrayList<Loc>();			
			
			int x = node.x;
			int y = node.y;
			
			int dx = (x - node.parent.x) / Math.max(Math.abs(x - node.parent.x), 1);
			int dy = (y - node.parent.y) / Math.max(Math.abs(y - node.parent.y), 1);
			
			if (dx != 0) {
				if (isAllowed(x, y - 1))
					neighbors.add(new Loc(x, y - 1));
				
				if (isAllowed(x, y + 1))
					neighbors.add(new Loc(x, y + 1));
				
				if (isAllowed(x + dx, y))
					neighbors.add(new Loc(x + dx, y));
			} else if (dy != 0) {
				if (isAllowed(x - 1, y))
					neighbors.add(new Loc(x - 1, y));
				
				if (isAllowed(x + 1, y))
					neighbors.add(new Loc(x + 1, y));
				
				if (isAllowed(x, y + dy))
					neighbors.add(new Loc(x, y + dy));
			}
			
			return neighbors;
		} else {
			return orthogonalNodes(node);
		}
	}
	
	private Loc jump(int x, int y, int px, int py) {
		int dx = x - px;
		int dy = y - py;
		
		if (!isAllowed(x, y))
			return null;
		
		if (x == endNode.x && y == endNode.y)
			return new Loc(x, y);
		
		if (dx != 0) {
			if ((isAllowed(x, y - 1) && !isAllowed(x - dx, y - 1)) ||
				(isAllowed(x, y + 1) && !isAllowed(x - dx,y + 1)))
					return new Loc (x, y);
		} else if (dy != 0) {
			if ((isAllowed(x - 1, y) && !isAllowed(x - 1, y - dy)) ||
				(isAllowed(x + 1, y) && !isAllowed(x + 1, y - dy)))
					return new Loc (x, y);
	        
			if (jump(x + 1, y, x, y) != null || jump(x - 1, y, x, y) != null)
				return new Loc (x, y);
		}
		
		return jump(x + dx, y + dy, x, y);
	}
	
	private void identifySuccessors(Node node) {
        ArrayList<Loc> neighbors = findNeighbors(node);
        
        int x = node.x;
        int y = node.y;
        
        for (Loc neighbor : neighbors) {
        	Loc jumpPoint = jump(neighbor.x, neighbor.y, x, y);
        	if (jumpPoint != null) {
        		int jx = jumpPoint.x;
        		int jy = jumpPoint.y;
        		
        		Node jumpNode = grid[jx][jy];
        		
        		if (jumpNode.closed)
        			continue;
        		
        		// Manhattan distance
        		int d = Math.abs(jx - x) + Math.abs(jy - y);
        		int ng = node.g + d;
        		
        		if (!jumpNode.opened || ng < jumpNode.g) {
        			jumpNode.g = ng;
        			jumpNode.h = jumpNode.h == 0 ? jumpNode.h : (Math.abs(jx - x) + Math.abs(jy - y));
        			jumpNode.f = jumpNode.g - jumpNode.h;
        			jumpNode.parent = node;
        			
        			if (!jumpNode.opened) {
        				openList.add(jumpNode);
        				jumpNode.opened = true;
        			} else {
        				openList.remove(jumpNode);
        				openList.add(jumpNode);
        			}
        		}
        	}
        }
	}
}
