package outpost.group6;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

import java.lang.management.*;
 
public class Player extends outpost.sim.Player {
    static int size =100;
    static Point[] grid = new Point[size*size];
    int r;
    int L;
    int W;
    Pair[] home = new Pair[4];
    boolean [][] opponentOutpost = new boolean[size][size];
    Random random;

    int outpostCounter = 0;
    int my_id;
    //For use in floodfill
    int [][] tempGrid = new int[size][size];
    boolean[][] vst = new boolean[size][size];
    int[] cx = {0, 0, 1, -1};
    int[] cy = {1, -1, 0, 0};

    int sx, sy;
    int[] mx = {1, 0, 1};
    int[] my = {0, 1, 1};
    int mSize = 3;

    ArrayList<Cell> homeCells;
    ArrayList<Cell> allCells;
    ArrayList<Post> ourPosts;
    Pair[] region = new Pair[2];
    int[] rx = {10, -10, -10, 10};
    int[] ry = {10, 10, -10, -10};
    int[] startx = {0, size - 1, size -1, 0};
    int[] starty = {0, 0, size -1, size -1};
    int moveCount = 0;
    int resizeCount = 0;

    boolean initDone = false;

    long totalCpuTime = 0;


    public Player(int id_in) {
        super(id_in);

        my_id = id_in;
    }

    /** Get CPU time in nanoseconds. */
    public long getCpuTime( ) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
        return bean.isCurrentThreadCpuTimeSupported( ) ?
            bean.getCurrentThreadCpuTime( ) : 0L;
    }


    public void init() {
        home[0] = new Pair(0,0);
        home[1] = new Pair(size-1, 0);
        home[2] = new Pair(size-1, size-1);
        home[3] = new Pair(0,size-1);

        random = new Random();
        for(int id = 0; id < 4; id++)
        {
            if (id == my_id)
                continue;
            int dx = home[id].x - home[my_id].x;
            int dy = home[id].y - home[my_id].y;

            if(dx != 0)
            {
                sx = dx/Math.abs(dx);
            }
            if(dy != 0)
            {
                sy = dy/Math.abs(dy);
            }
        }

        for(int i = 0; i < mSize; i++)
        {
            mx[i] *= sx;
            my[i] *= sy;
        }
        System.out.printf("[Group6][INIT] sx=%d, sy=%d\n", sx, sy);
        System.out.printf("[Group6][INIT] mx=[%d, %d, %d]\n", mx[0], mx[1], mx[2]);
        System.out.printf("[Group6][INIT] my=[%d, %d, %d]\n", my[0], my[1], my[2]);

    }
    
    static double distance(Point a, Point b) {
        return Math.sqrt((a.x-b.x) * (a.x-b.x) +
                         (a.y-b.y) * (a.y-b.y));
    }

    static int manDistance(Pair a, Pair b) {
        return Math.abs(a.x-b.x) + Math.abs(a.y-b.y);
    }

    static boolean isInBounds(Pair a) {
        return !(a.x < 0 || a.x >= size || a.y < 0 || a.y >= size);
    }

    static boolean isInRegion(Pair p, Pair a, Pair b) {
        int left, right, top, bottom;

        if (a.x < b.x) {
            left = a.x;
            right = b.x;
        } else {
            left = b.x;
            right = a.x;
        }

        if (a.y < b.y) {
            bottom = a.y;
            top = b.y;
        } else {
            bottom = b.y;
            top = a.y;
        }

        return !(p.x < left || p.x > right || p.y < bottom || p.y > top);
    }
    
    public int delete(ArrayList<ArrayList<Pair>> king_outpostlist, Point[] gridin) {
        System.out.printf("haha, we are trying to delete a outpost for player %d\n", this.id);
        int del = king_outpostlist.get(my_id).size() - 1;
        ourPosts.remove(ourPosts.size() - 1);
        return del;
    }

    public Pair findNextMovePos(Pair current, Pair target)
    {
        System.out.printf("[Group6][LOG] Finding path (%d, %d) -> (%d, %d)\n", current.x, current.y, target.x, target.y);
        for (int i = 0; i < size; ++i)
        {
            for (int j = 0; j < size; ++j)
            {
                vst[i][j] = false;
                tempGrid[i][j] = 4*size;
            }
        }

        vst[target.x][target.y] = true;
        tempGrid[target.x][target.y] = 0;
       
        Queue<Pair> q = new LinkedList<Pair>();
        q.add(target);
       
        while (!q.isEmpty()) {
            Pair p = q.poll();
            // if (p.equals(current))
            //     break;
            int d = tempGrid[p.x][p.y];
            for (int i = 0; i < 4; ++i) {
                int x = p.x + cx[i], y = p.y + cy[i];
                if (x < 0 || x >= size || y < 0 || y >= size || vst[x][y]) continue;
                if (x == current.x && y == current.y)
                {
                    System.out.printf("[Group6][LOG] Path Found\n");
                    return new Pair(p);
                }
                Pair pr = new Pair(x, y);
                Point pt = PairtoPoint(pr);
                if (!pt.water /*&& (pt.ownerlist.size() == 0 || (pt.ownerlist.size() == 1 && pt.ownerlist.get(0).x == my_id)) /*&& (!opponentPresence(p))*/) {
                    vst[x][y] = true;
                    tempGrid[x][y] = Math.min(tempGrid[x][y], d+1);
                    q.add(pr);
                }
            }
        }

        // int min = tempGrid[current.x][current.y];
        // Pair move = new Pair(current);
        // for (int i = 0; i < 4; ++i) 
        // {
        //     int x = current.x + cx[i], y = current.y + cy[i];
        //     if (x < 0 || x >= size || y < 0 || y >= size) continue;
        //     if(min > tempGrid[x][y])
        //     {
        //         min = tempGrid[x][y];
        //         move.x = x;
        //         move.y = y;
        //     }
        // }

        System.out.printf("[Group6][LOG] Path Found\n");
        return new Pair(current);

    }

    //Check for opponents presence in nearby area (8 surrounding cells)
    public boolean opponentPresence(Pair p)
    {
        int[] cx = {0,0,0,0,1,-1,2,-2};
        int[] cy = {1,-1,2,-2,0,0,0,0};
        
        for (int i = 0; i < 8; ++i) {
            int x = p.x + cx[i], y = p.y + cy[i];
            if (x < 0 || x >= size || y < 0 || y >= size) continue;
            Pair p_new = new Pair(x,y);
            Point pt = PairtoPoint(p_new);
            if (pt.ownerlist.size() == 0 || (pt.ownerlist.size() == 1 && pt.ownerlist.get(0).x == my_id))
                continue;
            else
                return true;             
        }
        return false;
    }
  
    public ArrayList<movePair> move(ArrayList<ArrayList<Pair>> king_outpostlist, Point[] gridin, int r, int L, int W, int t){
        moveCount++;
        this.grid = gridin;

        if (!initDone) {
            this.r = r;
            this.L = L;
            this.W = W;
            initCells();
            calcCellValues(); //sort every cell on the board by their "water value", descending
            ourPosts = new ArrayList<Post>(); // a list of our Outposts that persists through "move" calls
            region[0] = new Pair(startx[my_id], starty[my_id]);
            region[1] = new Pair (50 , 50   );


            initDone = true;
        }

        refreshPosts(king_outpostlist); //allign our outpost list with the one passed in from the simulator
                                        //also sets targets for any newly created outposts, stored in Post.target
        refreshTargets(region[0], region[1]); 
        
        // if (moveCount % 300 == 0) {
        //     region[1].x += rx[my_id];
        //     region[1].y += ry[my_id];
        
        // }

        ArrayList<movePair> nextlist = new ArrayList<movePair>();

        for (Post post : ourPosts) {
            Pair next = findNextMovePos(post.current, post.target);

            System.out.println("[GROUP 6][LOG] " + post + " Next: " + next.x + "," + next.y);
            
            nextlist.add(new movePair(post.id, next));
        }

        return nextlist;
    
    }

    /*allign our outpost list with the one passed in from the simulator
    also sets targets for any newly created outposts, stored in Post.target */
    void refreshPosts(ArrayList<ArrayList<Pair>> king_outpostlist) {
        ArrayList<Pair> ourKingList = king_outpostlist.get(my_id);
             
        ourPosts.clear();

        for (int i = 0; i < ourKingList.size(); i++) {
                Post post = new Post(i);
                post.current = ourKingList.get(i);
                ourPosts.add(post);                
            
        }
    }

    
    /* finds a suitable target cell in the region specified by Pair a, and Pair b. We can define rectangular regions this way
    with two pairs making up two opposite corners of the rectangle */
    Pair targetInRegion(Pair a, Pair b) {
        for (Cell c : allCells) {
            if (!isInRegion(c.location, a, b))
                continue;

            int postCount = 0;
            for (Post post : ourPosts) {
                if (manDistance(post.target, c.location) > 2*r) {
                    postCount++;
                } else {
                    break;
                }
            }

            if (postCount == ourPosts.size()) {
                return c.location;
            }
        }

        int c = moveCount % 4;
        return new Pair(startx[c], starty[c]);
    }

    void refreshTargets(Pair a, Pair b) {
        
        ArrayList<Post> ourPostsCopy = new ArrayList<Post>();
        //ourPostsCopy.addAll(ourPosts);
        //Make copy of all outposts except home base protecting outposts
        //And assign home cells to certain outposts manually
        int outpostCount = 0;
        int homeCellCount = 0;
        for(Post p: ourPosts)
        {
        	if((outpostCount >9 && outpostCount < 13))
        	{
        		p.target= homeCells.get(homeCellCount).location;
        		p.targetSet=true;
        		homeCellCount++;
        	}
        	else
        	{
        		ourPostsCopy.add(p);
        	}
        	outpostCount++;
        }

        for (Post post : ourPostsCopy) {
            post.targetSet = false;
        }

        for (Cell c : allCells) {
            if (!isInRegion(c.location, a, b))
                continue;

            int postCount = 0;
            for (Post post : ourPosts) {
                if (post.targetSet == false || manDistance(post.target, c.location) > 2*r) {
                    postCount++;
                } else {
                    break;
                }
            }

            if (postCount != ourPosts.size()) {
                continue;
            }

            int bestDist = 1000;
            Post closestPost = null;
            for (Post post : ourPostsCopy) {
                int d = manDistance(post.current, c.location);
                if (d < bestDist) {
                    bestDist = d;
                    closestPost = post;
                }
            }
            closestPost.target = c.location;
            closestPost.l_value = c.l_value;
            closestPost.w_value = c.w_value;
            closestPost.r_value = c.r_value;
            closestPost.targetSet = true;
            ourPostsCopy.remove(closestPost);

            if (ourPostsCopy.size() == 0) {
                break;
            }      
        }

        if (ourPostsCopy.size() > 0) {
           
            if (resizeCount < 2) {
                region[1].x += rx[my_id];
                region[1].y += ry[my_id];
                refreshTargets(region[0], region[1]);
                resizeCount++;
            } else {
                int count = 0;

                for (Post post : ourPostsCopy) {
                    count = count % 4;
                    post.target = new Pair(startx[count], starty[count]);
                    post.targetSet = true;
                    count++;
                }
            }

        }
    }

    /* Calculate the water value and land value for every cell */
    void calcCellValues() {

        for (Cell cell : allCells) {
            Pair orig = cell.location;

            if (PairtoPoint(orig).water) {
                    cell.w_value = -1;
                    cell.l_value = -1;
                    cell.r_value = -1;
                    continue;
            }

            ArrayList<Pair> diamond = diamondFromPair(orig, r);
            for (Pair p : diamond) {

                if (PairtoPoint(p).water) {
                    cell.w_value += L; 
                } else {
                    cell.l_value += W;
                }      
            }
        
            cell.r_value = Math.min(cell.w_value, cell.l_value);
        }
        Collections.sort(allCells);
    }

    void initCells() {
        allCells = new ArrayList<Cell>();
        homeCells = new ArrayList<Cell>(3);
        for (int i = 0; i < grid.length; i++) {
            allCells.add(new Cell(PointtoPair(grid[i])));
            
          //Add home cells
            Cell c = new Cell(PointtoPair(grid[i]));
            if(manDistance(c.location, home[my_id]) <=1)
            	homeCells.add(c);
        }
    }

    ArrayList<Pair> diamondFromPair(Pair a, int r) {
        ArrayList<Pair> diamond = new ArrayList<Pair>();

        for (int i = -r; i <= r; i++) {
            for (int j = -r; j <= r; j++) {
                if (Math.abs(i) + Math.abs(j) > r) continue;
                Pair tmp = new Pair(a.x + i, a.y + j);
                if (isInBounds(tmp)) diamond.add(tmp);
            }
        }

        return diamond;
    }


    
    static Point PairtoPoint(Pair pr) {
        return grid[pr.x*size+pr.y];
    }
    static Pair PointtoPair(Point pt) {
        return new Pair(pt.x, pt.y);
    }

    static String stringifyPair(Pair pr) {
        return String.format("%d,%d", pr.x, pr.y);
    }
}
