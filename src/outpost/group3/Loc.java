package outpost.group3;

public class Loc {
	public int x;
	public int y;
	
	Loc() {
		x = 0;
		y = 0;
	}
	
	Loc(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	Loc(Loc l) {
		x = l.x;
		y = l.y;
	}
	
	static public int mDistance(int x1, int y1, int x2, int y2) {
		return Math.abs(x1 - x2) + Math.abs(y1 - y2);
	}
	
	static public int mDistance(int x, int y, Loc l) {
		return mDistance(x, y, l.x, l.y);
	}
	
	static public int mDistance(Loc l, int x, int y) {
		return mDistance(l.x, l.y, x, y);
	}
	
	static public int mDistance(Loc l1, Loc l2) {
		return mDistance(l1.x, l1.y, l2.x, l2.y);
	}
	
	static public double distanceSquared(int x1, int y1, int x2, int y2) {
    	return (x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2);
    }
    
    static public double distance(int x1, int y1, int x2, int y2) {
    	return Math.sqrt(distanceSquared(x1, y1, x2, y2));
    }
    
    public double distanceSquared(Loc comparison) {
    	return distanceSquared(this, comparison);
    }
    
    public double distance(Loc comparison) {
    	return distance(this, comparison);
    }
    
    static public double distanceSquared(Loc l1, Loc l2) {
    	return distanceSquared(l1.x, l1.y, l2.x, l2.y);
    }
    
    static public double distance(int x, int y, Loc l) {
    	return distance(x, y, l.x, l.y);
    }
    
    static public double distanceSquared(int x, int y, Loc l) {
    	return distanceSquared(x, y, l.x, l.y);
    }
    
    static public double distance(Loc l1, Loc l2) {
    	return distance(l1.x, l1.y, l2.x, l2.y);
    }
    
    static public boolean equals(Loc l1, Loc l2) {
    	return l1.x == l2.x && l1.y == l2.y;
    }
    
    @Override
    public String toString() {
    	return "(" + x + ", " + y + ")";
    }
    
    public boolean equals(Object obj) {
    	if (!(obj instanceof Loc))
    		return false;
    	
    	Loc l = (Loc) obj;
    	
    	return x == l.x && y == l.y;
    }
}
