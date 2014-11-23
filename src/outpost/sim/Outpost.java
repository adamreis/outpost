package outpost.sim;

// general utilities
import java.io.*;
import java.util.List;
import java.util.*;

import javax.tools.*;

import java.util.concurrent.*;
import java.net.URL;

// gui utility
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;

import outpost.sim.Pair;
import outpost.sim.Player;
import outpost.sim.Point;

public class Outpost
{
	static String ROOT_DIR = "outpost";

	// recompile .class file?
	static boolean recompile = true;

	// print more details?
	static boolean verbose = true;

	// Step by step trace
	static boolean trace = true;

	// enable gui
	static boolean gui = true;

	static int size =100;
	static private Point[] grid = new Point[size*size];

	static int r_distance;

	static boolean reach = false;
	static Player[] players = new Player[4];

	static int L; //land cells number to afford outpost
	static int W; 

	static int MAX_TICKS = 10000;
	static ArrayList<Pair> searchlist = new ArrayList();

	static double[] water = new double[4];
	static double[] soil = new double[4];
	static int[] noutpost = new int[4];
	static int nrounds;

	static String group0 = null;
	static String group1 = null;
	static String group2 = null;
	static String group3 = null;
	
	static ArrayList<ArrayList<Pair>> king_outpostlist = new ArrayList<ArrayList<Pair>>();

	// list files below a certain directory
	// can filter those having a specific extension constraint
	//
	static List <File> directoryFiles(String path, String extension) {
		List <File> allFiles = new ArrayList <File> ();
		allFiles.add(new File(path));
		int index = 0;
		while (index != allFiles.size()) {
			File currentFile = allFiles.get(index);
			if (currentFile.isDirectory()) {
				allFiles.remove(index);
				for (File newFile : currentFile.listFiles())
					allFiles.add(newFile);
			} else if (!currentFile.getPath().endsWith(extension))
				allFiles.remove(index);
			else index++;
		}
		return allFiles;
	}

	// compile and load players dynamically
	//
	static Player loadPlayer(String group, int id) {
		try {
			// get tools
			URL url = Outpost.class.getProtectionDomain().getCodeSource().getLocation();
			// use the customized reloader, ensure clearing all static information
			ClassLoader loader = new ClassReloader(url, Outpost.class.getClassLoader());
			if (loader == null) throw new Exception("Cannot load class loader");
			JavaCompiler compiler = null;
			StandardJavaFileManager fileManager = null;
			// get separator
			String sep = File.separator;
			// load players
			// search for compiled files
			File classFile = new File(ROOT_DIR + sep + group + sep + "Player.class");
			System.err.println(classFile.getAbsolutePath());
			if (!classFile.exists() || recompile) {
				// delete all class files
				List <File> classFiles = directoryFiles(ROOT_DIR + sep + group, ".class");
				System.err.print("Deleting " + classFiles.size() + " class files...   ");
				for (File file : classFiles)
					file.delete();
				System.err.println("OK");
				if (compiler == null) compiler = ToolProvider.getSystemJavaCompiler();
				if (compiler == null) throw new Exception("Cannot load compiler");
				if (fileManager == null) fileManager = compiler.getStandardFileManager(null, null, null);
				if (fileManager == null) throw new Exception("Cannot load file manager");
				// compile all files
				List <File> javaFiles = directoryFiles(ROOT_DIR + sep + group, ".java");
				System.err.print("Compiling " + javaFiles.size() + " source files...   ");
				Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(javaFiles);
				boolean ok = compiler.getTask(null, fileManager, null, null, null, units).call();
				if (!ok) throw new Exception("Compile error");
				System.err.println("OK");
			}
			// load class
			System.err.print("Loading player class...   ");
			Class playerClass = loader.loadClass(ROOT_DIR + "." + group + ".Player");
			System.err.println("OK");
			// set name of player and append on list
			//Player player = (Player) playerClass.newInstance(id);
			Class[] cArg = new Class[1]; //Our constructor has 3 arguments
			//cArg[0] = Pair.class; //First argument is of *object* type Long
			cArg[0] = int.class; //Second argument is of *object* type String
			Player player = (Player) playerClass.getDeclaredConstructor(cArg).newInstance(id);
			if (player == null)
				throw new Exception("Load error");
			else
				return player;

		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}

	}



	// compute Euclidean distance between two points
	static double distance(Point a, Point b) {
		return Math.sqrt((a.x-b.x) * (a.x-b.x) +
				(a.y-b.y) * (a.y-b.y));
		//return Math.max(a.x-b.x, a.y-b.y);
	}
	static double M_distance(Point a, Point b) {
		//return Math.sqrt((a.x-b.x) * (a.x-b.x) +
			//	(a.y-b.y) * (a.y-b.y));
		return Math.abs(a.x-b.x)+Math.abs(a.y-b.y);
	}

	static double vectorLength(double ox, double oy) {
		return Math.sqrt(ox * ox + oy * oy);
	}

	void playgui() {
		SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				OutpostUI ui  = new OutpostUI();
				ui.createAndShowGUI();
				}
				});
	}


	class OutpostUI extends JPanel implements ActionListener {
		int FRAME_SIZE = 800;
		int FIELD_SIZE = 600;
		JFrame f;
		FieldPanel field;
		JButton next;
		JButton next10;
		JButton next50;
		JLabel label;
		JLabel label0;
		JLabel label1;
		JLabel label2;
		JLabel label3;

		public OutpostUI() {
			setPreferredSize(new Dimension(FRAME_SIZE, FRAME_SIZE));
			setOpaque(false);
		}

		public void init() {}

		private boolean performOnce() {
		/*	if (tick > MAX_TICKS) {
				label.setText("Time out!!!");
				label.setVisible(true);
				// print error message
				System.err.println("[ERROR] The player is time out!");
				next.setEnabled(false);
				return false;
			}
*/
			if (tick > nrounds) {
				calculateres();
				label.setText("End!  "+group0+" control water "+water[0]+", land "+soil[0]);
				label.setForeground(Color.BLACK);
				label.setVisible(true);
				label0.setText("End!  "+group1+" control water "+water[1]+", land "+soil[1]);
				label0.setForeground(Color.BLACK);
				label0.setVisible(true);
				label1.setText("End!  "+group2+" control water "+water[2]+", land "+soil[2]);
				label1.setForeground(Color.BLACK);
				label1.setVisible(true);
				label2.setText("End!  "+group3+" control water "+water[3]+", land "+soil[3]);
				label2.setForeground(Color.BLACK);
				label2.setVisible(true);
					//System.err.printf("Player %d control water %f, land %f\n", i, water[i], soil[i]);
				
				return false;
			}
			else {
				playStep();
				label3.setText("(water, land, outposts)      ticks: "+tick);
				label3.setVisible(true);
				label.setText(group0+" ("+Math.round(water[0])+", "+Math.round(soil[0])+", "+king_outpostlist.get(0).size()+")");
				label.setForeground(Color.BLACK);
				label.setVisible(true);
				label0.setText(group1+" ("+Math.round(water[1])+" ,"+Math.round(soil[1])+", "+king_outpostlist.get(1).size()+")");
				label0.setForeground(Color.BLACK);
				label0.setVisible(true);
				label1.setText(group2+" ("+Math.round(water[2])+" ,"+Math.round(soil[2])+", "+king_outpostlist.get(2).size()+")");
				label1.setForeground(Color.BLACK);
				label1.setVisible(true);
				label2.setText(group3+" ("+Math.round(water[3])+" ,"+Math.round(soil[3])+", "+king_outpostlist.get(3).size()+")");
				label2.setForeground(Color.BLACK);
				label2.setVisible(true);
				return true;
			}
		}

		public void actionPerformed(ActionEvent e) {
			int steps = 0;

			if (e.getSource() == next)
				steps = 1;
			else if (e.getSource() == next10)
				steps = 10;
			else if (e.getSource() == next50)
				steps = 50;

			for (int i = 0; i < steps; ++i) {
				if (!performOnce())
					break;
			}

			repaint();
		}


		public void createAndShowGUI()
		{
			this.setLayout(null);

			f = new JFrame("Outposts");
			field = new FieldPanel(1.0 * FIELD_SIZE / dimension);
			next = new JButton("Next"); 
			next.addActionListener(this);
			next.setBounds(0, 0, 100, 50);
			next10 = new JButton("Next10"); 
			next10.addActionListener(this);
			next10.setBounds(100, 0, 100, 50);
			next50 = new JButton("Next50"); 
			next50.addActionListener(this);
			next50.setBounds(200, 0, 100, 50);

			label = new JLabel();
			label.setVisible(false);
			label.setBounds(30, 90, 450, 15);
			label.setFont(new Font("Arial", Font.PLAIN, 15));
			label0 = new JLabel();
			label0.setVisible(false);
			//label0.setBounds(0, 65, 500, 15);
			label0.setBounds(600, 90, 450, 15);
			label0.setFont(new Font("Arial", Font.PLAIN, 15));
			label1 = new JLabel();
			label1.setVisible(false);
			//label1.setBounds(0, 80, 500, 15);
			//label1.setBounds(0, 700, 400, 15);
			label1.setBounds(600, 700, 450, 15);
			label1.setFont(new Font("Arial", Font.PLAIN, 15));
			label2 = new JLabel();
			label2.setVisible(false);
			//label2.setBounds(0, 95, 500, 15);
			//label2.setBounds(600, 700, 400, 15);
			label2.setBounds(30, 700, 450, 15);
			label2.setFont(new Font("Arial", Font.PLAIN, 15));
			field.setBounds(100, 100, FIELD_SIZE + 50, FIELD_SIZE + 50);
			label3 = new JLabel();
			label3.setVisible(false);
			label3.setBounds(30, 75, 450, 15);
			label3.setFont(new Font("Arial", Font.PLAIN, 15));

			this.add(next);
			this.add(next10);
			this.add(next50);
			this.add(label);
			this.add(label0);
			this.add(label1);
			this.add(label2);
			this.add(label3);
			this.add(field);

			f.add(this);

			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.pack();
			f.setVisible(true);
		}

		@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
			}

	}

	class FieldPanel extends JPanel {
		double PSIZE = 10;
		double s;
		BasicStroke stroke = new BasicStroke(2.0f);
		double ox = 10.0;
		double oy = 10.0;

		public FieldPanel(double scale) {
			setOpaque(false);
			s = scale;
		}

		@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);

				Graphics2D g2 = (Graphics2D) g;
				g2.setStroke(stroke);

				// draw 2D rectangle
				double x_in = (dimension*s-ox)/size;
				double y_in = (dimension*s-oy)/size;
				// g2.draw(new Rectangle2D.Double(ox,oy,ox+x_in,oy+y_in));
				for (int i=0; i<size; i++) {

					for (int j=0; j<size; j++) {
						if (grid[i*size+j].water) {
							g2.setPaint(Color.blue.brighter());
						}
						else {
							g2.setPaint(Color.orange);
						}
						g2.fill(new Rectangle2D.Double(ox+x_in*i,oy+y_in*j,x_in,y_in));
						if (grid[i*size+j].ownerlist.size()==1) {
							//	System.out.println("this is owned by player 0");
							if (grid[i*size+j].ownerlist.get(0).x==0)  {

								g2.setPaint(Color.white);
								g2.fill(new Rectangle2D.Double(ox+x_in/4+x_in*i,oy+x_in/4+y_in*j,x_in/2,y_in/2));
							}
							if (grid[i*size+j].ownerlist.get(0).x==1)  {

								g2.setPaint(Color.GREEN.darker());
								g2.fill(new Rectangle2D.Double(ox+x_in/4+x_in*i,oy+x_in/4+y_in*j,x_in/2,y_in/2));
							}
							if (grid[i*size+j].ownerlist.get(0).x==2)  {

								g2.setPaint(Color.BLACK);
								g2.fill(new Rectangle2D.Double(ox+x_in/4+x_in*i,oy+x_in/4+y_in*j,x_in/2,y_in/2));
							}
							if (grid[i*size+j].ownerlist.get(0).x==3)  {

								g2.setPaint(Color.RED);
								g2.fill(new Rectangle2D.Double(ox+x_in/4+x_in*i,oy+x_in/4+y_in*j,x_in/2,y_in/2));
							}
						} else if (grid[i*size+j].ownerlist.size()>1) {
							g2.setPaint(Color.GRAY);
							g2.fill(new Rectangle2D.Double(ox+x_in/4+x_in*i,oy+x_in/4+y_in*j,x_in/2,y_in/2));
						}
					}

				}

				for (int i = 0; i < king_outpostlist.size(); i++) {
					// drawPoint(g2, pointers[i]);
					for (int j=0; j<king_outpostlist.get(i).size(); j++) {
						//System.out.printf("(%d, %d)", i, j);
						drawPoint(g2, king_outpostlist.get(i).get(j), i);
					}
				}


			}

		public void drawPoint(Graphics2D g2, Pair pr, int id) {
			if (id == 0) 
				g2.setPaint(Color.WHITE);
			else if (id == 1)
				g2.setPaint(Color.GREEN.darker());
			else if (id == 2)
				g2.setPaint(Color.BLACK);
			else if (id == 3)
				g2.setPaint(Color.RED);

			double x_in = (dimension*s-ox)/size;
			double y_in = (dimension*s-oy)/size;

			Ellipse2D e = new Ellipse2D.Double(pr.x*x_in+10, pr.y*y_in+10, x_in, y_in);
			g2.setStroke(stroke);
			g2.draw(e);
			g2.fill(e);

		}



	}



	void updatePoint(Point pr) {
		pr.ownerlist.clear();
		double mindist = Math.sqrt(size*size);

		for (int j =0 ; j<king_outpostlist.size(); j++) {
			for (int f =0; f<king_outpostlist.get(j).size(); f++) {
				double d = M_distance(PairtoPoint(king_outpostlist.get(j).get(f)), pr);
				if (d <= mindist) {
					mindist = d;

				}
			}
		}
		if (mindist <= r_distance){
			for (int j =0 ; j<king_outpostlist.size(); j++) {
				for (int f =0; f<king_outpostlist.get(j).size(); f++) {
					double d = M_distance(PairtoPoint(king_outpostlist.get(j).get(f)), pr);
					if (d == mindist) {
						Pair tmp = new Pair(j, f);
						if (pr.ownerlist.size()==0) {
							pr.ownerlist.add(tmp);
						}
						else if (!thesameowner(pr, tmp)) {
							pr.ownerlist.add(tmp);
						}
					}
				}
			}
		}
	}

	boolean thesameowner(Point pt, Pair tmp) {
		for (int i=0; i<pt.ownerlist.size(); i++){
			if (pt.ownerlist.get(i).x==tmp.x)
				return true;
		}
		return false;
	}


	void calculateres() {
		//System.out.println("calculate resouce");
		for (int i=0; i<4; i++) {
			water[i] =0.0;
			soil[i] =0.0;
		}
		for (int i=0; i<size*size; i++) {
			if (grid[i].ownerlist.size() == 1) {
				if (grid[i].water) {
					water[grid[i].ownerlist.get(0).x]++;
				}
				else {
					soil[grid[i].ownerlist.get(0).x]++;
				}
			}
			else if (grid[i].ownerlist.size() > 1){
				for (int f=0; f<grid[i].ownerlist.size(); f++) {
					if (grid[i].water) {
						water[grid[i].ownerlist.get(f).x]=water[grid[i].ownerlist.get(f).x]+1/grid[i].ownerlist.size();
					}
					else {
						soil[grid[i].ownerlist.get(f).x]=soil[grid[i].ownerlist.get(f).x]+1/grid[i].ownerlist.size();
					}
				}

			}
		}
		for (int i=0; i<4; i++) {
			noutpost[i] = (int) Math.min(soil[i]/L, water[i]/W)+1;
			if (noutpost[i]>king_outpostlist.get(i).size()) {
				//System.out.printf("After the calculation, the number of outpost for %d king should increase", i);
				if (i==0) {
					king_outpostlist.get(i).add(new Pair(0,0));
				//	System.out.println("the first player got a new outpost");
				}
				if (i==1) {
					king_outpostlist.get(i).add(new Pair(size-1, 0));
					//System.out.println("the second player got a new outpost");
				}
				if (i==2) {
					king_outpostlist.get(i).add(new Pair(size-1, size-1));
					//System.out.println("the third player got a new outpost");
				}
				if (i==3) {
					king_outpostlist.get(i).add(new Pair(0,size-1));
					//System.out.println("the fourth player got a new outpost");
				}
			}

		}

	}

	void updatemap(ArrayList<movePair> nextlist, int id) {
		for (int i=0; i<nextlist.size(); i++) {
			movePair mpr = nextlist.get(i);
			
			if (mpr.id<king_outpostlist.get(id).size()){
				Pair current = king_outpostlist.get(id).get(mpr.id);
				Pair next = mpr.pr;

				for (int j=0; j<surroundpr(current).size(); j++) {
					if (surroundpr(current).get(j).equals(next)) {
						if (!PairtoPoint(next).water) {
							Pair tmp = new Pair(next.x, next.y);
							king_outpostlist.get(id).set((mpr.id), tmp);
						}
					}
				}

			}
			
		}
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				updatePoint(grid[i*size+j]);
			}
		}
	}

	static Point PairtoPoint(Pair pr) {
		return grid[pr.x*size+pr.y];
	}
	static Pair PointtoPair(Point pt) {
		return new Pair(pt.x, pt.y);
	}

	ArrayList<Pair> validateMove(ArrayList<movePair> nextlist, int id) {
		ArrayList<Pair> toremove = new ArrayList<Pair>();
		//reach = false;
	/*	for (int i=0; i<nextlist.size(); i++) {
			movePair mpr = nextlist.get(i);
			if (mpr.id<king_outpostlist.get(id).size()){
				Pair current = king_outpostlist.get(id).get(mpr.id);
				Pair next = mpr.pr;

				for (int j=0; j<surroundpr(current).size(); j++) {
					if (surroundpr(current).get(j).equals(next)) {
						if (!PairtoPoint(next).water) {
							Pair tmp = new Pair(next.x, next.y);
							king_outpostlist.get(id).set((mpr.id), tmp);
						}
					}
				}

			}
		}*/

		Pair target = null;
		if (id ==0) {
			target = new Pair(0,0);
		}
		if (id == 1) {
			target = new Pair(size-1,0);
		}
		if (id ==2 ) {
			target = new Pair(size-1, size-1);
		}
		if (id ==3) {
			target = new Pair(0,size-1);
		}

		//System.out.printf("Player: %d\n", id);
		//for (int i = 0; i < king_outpostlist.get(id).size(); ++i) {
		//	System.out.printf("(%d, %d)\n", king_outpostlist.get(id).get(i).x, king_outpostlist.get(id).get(i).y);
		//}

		// floodfill from target
		int[] cx = {0, 0, 1, -1};
		int[] cy = {1, -1, 0, 0};
		boolean[][] vst = new boolean[size][size];
		for (int i = 0; i < size; ++i)
			for (int j = 0; j < size; ++j)
				vst[i][j] = false;
		vst[target.x][target.y] = true;
		Queue<Pair> q = new LinkedList<Pair>();
		q.add(target);
		while (!q.isEmpty()) {
			Pair p = q.poll();
			for (int i = 0; i < 4; ++i) {
				int x = p.x + cx[i], y = p.y + cy[i];
				if (x < 0 || x >= size || y < 0 || y >= size || vst[x][y]) continue;
				Point pt = PairtoPoint(p);
				if (!pt.water && (pt.ownerlist.size() == 0 || (pt.ownerlist.size() == 1 && pt.ownerlist.get(0).x == id))) {
					vst[x][y] = true;
					q.add(new Pair(x, y));
				}
			}
		}
		// remove outposts in unvisited cells
		for (int i = king_outpostlist.get(id).size() - 1; i >= 0; --i) {
			Pair p = king_outpostlist.get(id).get(i);
			if (!vst[p.x][p.y]) {
				//System.out.printf("player %d's outpost (%d, %d) cannot find a supplyline back \n", id, p.x, p.y);
				//System.out.print(group0);
				if (id==0) {
					System.err.print(group0);
					System.err.printf(" outpost (%d, %d) cannot find a supplyline back \n", id, p.x, p.y);
					
				}
				if (id==1) {
					System.err.print(group1);
					System.err.printf(" outpost (%d, %d) cannot find a supplyline back \n", id, p.x, p.y);
					
				}
				if (id==2) {
					System.err.print(group2);
					System.err.printf(" outpost (%d, %d) cannot find a supplyline back \n", id, p.x, p.y);
					
				}
				if (id==3) {
					System.err.print(group3);
					System.err.printf(" outpost (%d, %d) cannot find a supplyline back \n", id, p.x, p.y);
					
				}
				toremove.add(new Pair(id, i));
				//king_outpostlist.get(id).remove(i);
			}
		}
		return toremove;
	}

	static boolean hascontained(ArrayList<Pair> list, Pair pr) {
		for (int j=0; j<list.size(); j++){

			if (list.get(j).equals(pr)) {
				return true;
			}
		}
		return false;
	}

	static void supplyline (Pair target, int id) {
		//System.out.printf("target is (%d, %d)\n", target.x, target.y);
		boolean stop = true;
		int player;
		player = id;
		Point source = new Point();
		ArrayList<Pair> surlist = new ArrayList<Pair>();

		for (int j=0; j<king_outpostlist.get(id).size(); j++){
			if (!hascontained(searchlist, king_outpostlist.get(id).get(j))) {
				stop = false;
				break;
			}
		}
		if (!stop & !hascontained(searchlist, target)) {
			//search_depth++;
			source = grid[target.x*size+target.y];
			searchlist.add(target);
			surlist = surround(target);
			for (int i=0; i<surlist.size(); i++) {
				if (!PairtoPoint(surlist.get(i)).water && (PairtoPoint(surlist.get(i)).ownerlist.size() ==0 || (PairtoPoint(surlist.get(i)).ownerlist.size()== 1 && PairtoPoint(surlist.get(i)).ownerlist.get(0).x==player))){
					Pair pt = new Pair(surlist.get(i).x, surlist.get(i).y);
					boolean has = false;
					if (!grid[pt.x*size+pt.y].water) {
						for (int j=0; j<searchlist.size(); j++) {
							if (searchlist.get(j).equals(pt)) {
								has = true;
							}
						}
						if (!has) {
							supplyline(pt, id);
						}
					}	
				}
			}
		}
	}



	static ArrayList<Pair> surround(Pair start) {
		// 	System.out.printf("start is (%d, %d)", start.x, start.y);
		ArrayList<Pair> prlist = new ArrayList<Pair>();
		for (int i=0; i<4; i++) {
			Pair tmp0 = new Pair(start);
			Pair tmp;
			if (i==0) {
				if (start.x>0) {
					tmp = new Pair(tmp0.x-1,tmp0.y);
					prlist.add(tmp);
				}
			}
			if (i==1) {
				if (start.x<size-1) {
					tmp = new Pair(tmp0.x+1,tmp0.y);
					prlist.add(tmp);
				}
			}
			if (i==2) {
				if (start.y>0) {
					tmp = new Pair(tmp0.x, tmp0.y-1);
					prlist.add(tmp);
				}
			}
			if (i==3) {
				if (start.y<size-1) {
					tmp = new Pair(tmp0.x, tmp0.y+1);
					prlist.add(tmp);
				}
			}

		}

		return prlist;
	}

	static ArrayList<Pair> surroundpr(Pair start) {
		// 	System.out.printf("start is (%d, %d)", start.x, start.y);
		ArrayList<Pair> prlist = new ArrayList();
		for (int i=0; i<4; i++) {
			Pair tmp0 = new Pair(start);
			//Pair tmp = new Pair();
			if (i==0) {
				if (start.x>0) {
					Pair tmp = new Pair(start.x-1, start.y);
					prlist.add(tmp);
				}
			}
			if (i==1) {
				if (start.x<size-1) {
					Pair tmp = new Pair(start.x+1, start.y);
					prlist.add(tmp);
				}
			}
			if (i==2) {
				if (start.y>0) {
					Pair tmp = new Pair(start.x, start.y-1);
					prlist.add(tmp);
				}
			}
			if (i==3) {
				if (start.y<size-1 ) {
					Pair tmp = new Pair(start.x, start.y+1);
					prlist.add(tmp);
				}
			}

		}
		//for (int j=0; j<prlist.size(); j++) {
		//System.out.printf("surround is (%d, %d)", prlist.get(j).x, prlist.get(j).y);
		//}
		return prlist;
	}
	


	void playStep() {
		tick++;        

		// move the player dogs
		if (tick % 10 == 0 && tick !=0){
			calculateres();
			//updatemap();
		}
		ArrayList<movePair> nextlist0 = new ArrayList<movePair>();
		ArrayList<movePair> nextlist1 = new ArrayList<movePair>();
		ArrayList<movePair> nextlist2 = new ArrayList<movePair>();
		ArrayList<movePair> nextlist3 = new ArrayList<movePair>();
		for (int d=0; d<4; d++) {
			try {
				//ArrayList<movePair> nextlist = new ArrayList<movePair>();
				if (king_outpostlist.get(d).size()>noutpost[d]) {
					int removedid = players[d].delete(king_outpostlist, grid);
					king_outpostlist.get(d).remove(removedid);
					System.err.printf("player %d delete outpost %d\n", d, removedid);
				}
				if (d==0)
					nextlist0 = players[d].move(king_outpostlist, grid, r_distance, L, W, MAX_TICKS);
				if (d==1)
					nextlist1 = players[d].move(king_outpostlist, grid, r_distance, L, W, MAX_TICKS);
				if (d==2)
					nextlist2 = players[d].move(king_outpostlist, grid, r_distance, L, W, MAX_TICKS);
				if (d==3)
					nextlist3 = players[d].move(king_outpostlist, grid, r_distance, L, W, MAX_TICKS);
				/*for (int i=0; i<nextlist.size(); i++) {
					//movePair next = new movePair();
					//next = nextlist.get(i);
					
					validateMove(nextlist, d);
					
					updatemap();
					
				}*/

			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("[ERROR] Player throws exception!!!!");
				//next[d] = pipers[d]; // let the dog stay
			}
		}
		updatemap(nextlist0, 0);
		updatemap(nextlist1, 1);
		updatemap(nextlist2, 2);
		updatemap(nextlist3, 3);
		ArrayList<ArrayList<Pair>> toremove = new ArrayList<ArrayList<Pair>>();
		for (int d=0; d<4; d++) {
			if (d==0){
			//for (int i=0; i<nextlist0.size(); i++) {
				//movePair next = new movePair();
				//next = nextlist.get(i);
				
				toremove.add(validateMove(nextlist0, d));
				
				//updatemap();
				
			//}
			}
			if (d==1) {
				//for (int i=0; i<nextlist1.size(); i++) {
					//movePair next = new movePair();
					//next = nextlist.get(i);
					
					toremove.add(validateMove(nextlist1, d));
					
					//updatemap();
					
				//}
			}
			if (d==2) {
			//for (int i=0; i<nextlist2.size(); i++) {
				//movePair next = new movePair();
				//next = nextlist.get(i);
				
				toremove.add(validateMove(nextlist2, d));
				
				//updatemap();
				
			//}
			}
			if (d==3) {
				//for (int i=0; i<nextlist3.size(); i++) {
					//movePair next = new movePair();
					//next = nextlist.get(i);
					
					toremove.add(validateMove(nextlist3, d));
					
					//updatemap();
					
				//}
			}
			}
		//System.out.println(toremove);
		for (int i=0; i<toremove.size(); i++) {
			for (int j=0; j<toremove.get(i).size(); j++) {
				king_outpostlist.get(toremove.get(i).get(j).x).remove(toremove.get(i).get(j).y);
				System.out.printf("(%d, %d)",toremove.get(i).get(j).x, toremove.get(i).get(j).y);
			}
			
		}
		/*for (int i=0; i<4; i++) {
			for (int j=0; j<king_outpostlist.get(i).size(); j++) {
				System.err.printf("(%d, %d)",king_outpostlist.get(i).get(j).x, king_outpostlist.get(i).get(j).y);
			}
			System.err.println();
		}*/
		updatemap(nextlist0, 0);
		updatemap(nextlist1, 1);
		updatemap(nextlist2, 2);
		updatemap(nextlist3, 3);
	/*	if (tick == nrounds) {
			calculateres();
			for (int i=0; i<4; i++) {
				
				System.err.printf("Player %d control water %f, land %f\n", i, water[i], soil[i]);
			}
		}*/
	}

	void play() {
	}

	void init() {

		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				grid[i*size+j] = new Point(i, j, false);
			}
		}
		for (int i=0; i<4; i++) {
			ArrayList<Pair> tmp_arr = new ArrayList<Pair>();
			if (i==0)
				tmp_arr.add(new Pair(0,0));
			if (i==1)
				tmp_arr.add(new Pair(size-1, 0));
			if (i==2)
				tmp_arr.add(new Pair(size-1, size-1));
			if (i==3)
				tmp_arr.add(new Pair(0,size-1));
			king_outpostlist.add(tmp_arr);
		}
		for (int i=0; i<4; i++) {
			noutpost[i] = 1;
		}

	}


	Outpost()
	{
		this.players = players;
	}

	public void read(String map) {
		List<Pair> list = new ArrayList<Pair>();
		File file = new File(map);
		BufferedReader reader = null;
		int counter = 0;
		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;

			while ((text = reader.readLine()) != null) {
				java.util.List<String> item2 = new ArrayList();
				item2 = Arrays.asList(text
						.split(" "));
				ArrayList array_tmp0 = new ArrayList();
				Pair pr = new Pair();
				pr.x = Integer.parseInt(item2.get(0));
				pr.y = Integer.parseInt(item2.get(1));
				list.add(pr);
				counter = counter +1;
				/*grid[pr.x*size+pr.y].water = true;
				grid[(size-pr.y)*size+pr.x].water = true;
				grid[(100-pr.x)*size+(100-pr.y)].water = true;
				grid[pr.y*size+100-pr.x].water = true;*/
				
				grid[pr.x*size+pr.y].water = true;
				grid[(size-pr.y-1)*size+pr.x].water = true;
				grid[(size-pr.x-1)*size+(size-pr.y-1)].water = true;
				grid[pr.y*size+size-pr.x-1].water = true;
				
				//grid[(100-pr.x)*size+pr.y].water = true;
				//grid[(100-pr.x)*size+100-pr.y].water = true;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}
		//System.out.println(counter);
	}
	public static void main(String[] args) throws Exception
	{
		// game parameters
		String map = null;


		if (args.length > 0)
			map = args[0];
		if (args.length > 1)
			r_distance = Integer.parseInt(args[1]);
		if (args.length > 2)
			L = Integer.parseInt(args[2]);
		if (args.length > 3)
			W = Integer.parseInt(args[3]);
		if (args.length > 4)
			gui = Boolean.parseBoolean(args[4]);
		if (args.length >5) 
			group0 = args[5];
		if (args.length>6)
			group1 = args[6];
		if (args.length>7)
			group2 = args[7];
		if (args.length>8)
			group3 = args[8];
		if (args.length>9)
			nrounds = Integer.parseInt(args[9]);
		// load players

		players[0] = loadPlayer(group0, 0);
		players[0].init();
		players[1] = loadPlayer(group1, 1);
		players[1].init();
		players[2] = loadPlayer(group2, 2);
		players[2].init();
		players[3] = loadPlayer(group3, 3);
		players[3].init();

		// create game
		Outpost game = new Outpost();
		// init game
		game.init();
		game.read(map);

		game.playgui();

	}        

	int tick = 0;

	static double dimension = 100.0; // dimension of the map
	static Random random = new Random();
}