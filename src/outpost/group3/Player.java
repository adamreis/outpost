package outpost.group3;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;
import outpost.group3.Board;
import outpost.group3.Outpost;

public class Player extends outpost.sim.Player {
	static int size = 100;
	static Random random = new Random();

	private boolean isInitialized = false;
	private Board board;

	ArrayList<Outpost> outposts;
	int nextOutpostId;

	public Player(int id) {
		super(id);
	}

    private ArrayList<Outpost> getOutpostsWithStrategy(String strategyName) {
    	ArrayList<Outpost> outpostList = new ArrayList<Outpost>();
    	
    	for (Outpost outpost : outposts)
    		if (outpost.getStrategy() == strategyName)
    			outpostList.add(outpost);
    	
    	return outpostList;
    }
    
    private void assignStrategySame(ArrayList<Outpost> outpostsForStrategy, String strategyName, int max) {
    	if (outpostsForStrategy.size() >= max)
    		return;
    	
    	for (Outpost outpost : outposts) {
    		if (!outpost.isUpdated() && outpost.getStrategy() == strategyName) {
          		if (outpostsForStrategy.size() == max) {
    				outpost.setStrategy(null);
    				outpost.setTargetLoc(null);
    				outpost.memory.clear();
          		} else {
        			outpostsForStrategy.add(outpost);
        			outpost.setUpdated(true);
          		}
    		}
    	}
    }

    private void assignStrategyUnassigned(ArrayList<Outpost> outpostsForStrategy, String strategyName, int max) {
		if (outpostsForStrategy.size() >= max)
			return;
		
		for (Outpost outpost : outposts) {
			if (!outpost.isUpdated() && outpost.getStrategy() == null && outpostsForStrategy.size() < max) {
				outpostsForStrategy.add(outpost);
				outpost.setStrategy(strategyName);
				outpost.setUpdated(true);
		
				if (outpostsForStrategy.size() >= max)
					break;
			}
		}
	}
  
    private void assignStrategySteal(ArrayList<Outpost> outpostsForStrategy, String strategyName, int max) {
    	if (outpostsForStrategy.size() >= max)
    		return;
    	
    	for (Outpost outpost : outposts) {
    		if (!outpost.isUpdated() && outpostsForStrategy.size() < max && !outpost.getStrategy().equals("consumer")) {
    			outpostsForStrategy.add(outpost);
    			outpost.setStrategy(strategyName);
				outpost.setTargetLoc(null);
    			outpost.memory.clear();
    			outpost.setUpdated(true);
    			
    	    	if (outpostsForStrategy.size() >= max)
    	    		break;
    		}
    	}
	}

	private void assignStrategy(ArrayList<Outpost> outpostsForStrategy, String strategyName, int max) {
		assignStrategySame(outpostsForStrategy, strategyName, max);
		assignStrategyUnassigned(outpostsForStrategy, strategyName, max);
		assignStrategySteal(outpostsForStrategy, strategyName, max);
	}
	
    private void markStrategyDone(ArrayList<Outpost> outpostsForStrategy) {
    	for (Outpost outpost : outpostsForStrategy) {
    		if (outpost.getStrategy() == null) {
    			outpost.memory.clear();
    			outpost.setTargetLoc(null);
    			outpost.setUpdated(false);
    		}
    	}
    }
    
    // type 0 = stay the same; type 1 = move left; type 2 = move up; type 3 = move up/left whichever is greater; type 4 = move right; type 5 = move down; 
    private Board getBoardAfterOpponentMove(Board board, int type) {
    	Board boardAfterOpponentMoves = new Board(board);
    	
    	ArrayList<ArrayList<Loc>> allOutposts = boardAfterOpponentMoves.allOutposts();
    	
    	for (int opponentId = 0; opponentId < Consts.numPlayers; opponentId++) {
    		if (opponentId == id)
    			continue;
    		
    		ArrayList<Loc> opponentOutposts = allOutposts.get(opponentId);
    		
    		for (int j = 0; j < opponentOutposts.size(); j++) {
    			Loc loc = opponentOutposts.get(j);
    			
    			if (type == 1 || (type == 3 && loc.x > loc.y))
    				loc.x = Math.max(0, loc.x - 1);
    			else if (type == 2 || (type == 3 && loc.x <= loc.y))
    				loc.y = Math.max(0, loc.y - 1);
    			else if (type == 4)
    				loc.x = Math.min(size - 1, loc.x + 1);
    			else if (type == 5)
    				loc.y = Math.min(size - 1, loc.y + 1);
    		}
    	}
    	
    	boardAfterOpponentMoves.update(allOutposts);
    	
    	return boardAfterOpponentMoves;
    }
    
    private ArrayList<Integer> outpostsToAdjustTactically() {
    	// First, construct a new board as if we played all our moves
    	Board boardAfterOurMoves = new Board(board);
    	
    	ArrayList<ArrayList<Loc>> allOutposts = boardAfterOurMoves.allOutposts();
    	
    	for (Outpost outpost : outposts) {
			Loc currentLoc = outpost.getCurrentLoc();
			Loc targetLoc = outpost.getTargetLoc();
			
			if (targetLoc == null)
				targetLoc = new Loc(currentLoc);
			
    		ArrayList<Loc> path = board.findPath(currentLoc, targetLoc);
    		
    		if (path == null || path.size() == 0 || path.size() == 1) {
    			Loc loc = allOutposts.get(id).get(outpost.getSimIndex());
    			loc.x = currentLoc.x;
    			loc.y = currentLoc.y;
    		} else {
    			Loc loc = allOutposts.get(id).get(outpost.getSimIndex());
    			loc.x = path.get(1).x;
    			loc.y = path.get(1).y;
    		}
    	}
    	
    	boardAfterOurMoves.update(allOutposts);
    	
    	// Consider six cases of how enemies might move: stay the same, move up, move left, or move whichever is more distant, move down, move right
    	ArrayList<Integer> disbandedOutposts = new ArrayList<Integer>();	// List of simIds of outposts that will be disbanded.  May contain duplicates, but that will not matter
    	
    	for (int i = 0; i < 6; i++) {
    		Board boardAfterOpponentMove = getBoardAfterOpponentMove(boardAfterOurMoves, i);
        	disbandedOutposts.addAll(boardAfterOpponentMove.outpostsToDisband(id));
    	}
    	
    	return disbandedOutposts;
    }

	public void init() {}

	public int delete(ArrayList<ArrayList<Pair>> king_outpostlist, Point[] gridin) {
		//System.out.printf("haha, we are trying to delete a outpost for player %d\n", this.id);
		int del = random.nextInt(king_outpostlist.get(id).size());
		return del;
	}

    public ArrayList<movePair> move(ArrayList<ArrayList<Pair>> simOutpostList, Point[] simGrid, int r, int L, int W, int T) {
    	if (!isInitialized) {
    		board = new Board(id, simGrid, r, L, W, T);
    		outposts = new ArrayList<Outpost>();
    		nextOutpostId = 0;
    		isInitialized = true;
    	}
    	
    	long startTime = System.currentTimeMillis();
    	
    	// For each of our outposts in the list, find and update it in our persistent list, or add it if not
    	for (Outpost outpost : outposts)
    		outpost.setUpdated(false);
    	    	
    	for (int i = 0; i < simOutpostList.get(id).size(); i++) {
    		Pair pair = simOutpostList.get(id).get(i);
			Loc loc = new Loc(pair.x, pair.y);
			board.simFlip(loc);
			
			boolean existing = false;
			for (Outpost outpost : outposts) {
				if (!outpost.isUpdated() && Loc.equals(outpost.getExpectedLoc(), loc)) {
					outpost.setCurrentLoc(loc);
					outpost.setUpdated(true);
					outpost.setSimIndex(i);
					existing = true;
					break;
				}
			}
			
			if (!existing) {
				outposts.add(new Outpost(nextOutpostId, loc, i));
				nextOutpostId++;
			}
		}
    	
    	for (int i = outposts.size() - 1; i >= 0; i--) {
    		Outpost outpost = outposts.get(i);
    		
    		if (!outpost.isUpdated())
    			outposts.remove(i);
    	}
    	
    	// Update the board object
    	board.updateFromSim(simOutpostList);
    	    	
    	// Assign and run strategies on each outpost; use updated to indicate whether a strategy has been run on an outpost
    	for (Outpost outpost : outposts)
    		outpost.setUpdated(false);

    	ArrayList<Outpost> outpostsForStrategy;
    	int targetNum;
    	
    	// Run resources gatherers
    	int minTarget = 5;
    	targetNum = Math.max(minTarget, (int) ((board.ourOutposts().size() + 1) / board.avgSupportPerCell()));
    	
       	outpostsForStrategy = new ArrayList<Outpost>();
    	assignStrategy(outpostsForStrategy, "resourceGatherer", targetNum);
    	Strategy getResources = new GetResources();
    	getResources.run(board, outpostsForStrategy);
    	markStrategyDone(outpostsForStrategy);
    	
    	// Run protect home strategy (It requires at the least 3 outposts to be generated on the board)
    	targetNum = 3;
    	outpostsForStrategy = new ArrayList<Outpost>();
    	assignStrategy(outpostsForStrategy, "protectHome", targetNum);
    	Strategy protectHome = new ProtectHome();
    	protectHome.run(board, outpostsForStrategy);
    	markStrategyDone(outpostsForStrategy);
    	
    	// Run consumer
        targetNum = 15;		// Just working with one consumer
        outpostsForStrategy = new ArrayList<Outpost>();
        assignStrategy(outpostsForStrategy, "consumer", targetNum);
        Strategy ConsumerStrategy = new ConsumerStrategy(r);
        ConsumerStrategy.run(board, outpostsForStrategy);
        markStrategyDone(outpostsForStrategy);
    	
    	// Run attack Enemy strategy
    	targetNum = 3;
    	outpostsForStrategy = new ArrayList<Outpost>();
    	assignStrategy(outpostsForStrategy, "attackEnemy", targetNum);
    	Strategy attackEnemy = new AttackEnemy();
    	attackEnemy.run(board, outpostsForStrategy);
    	markStrategyDone(outpostsForStrategy);
    	
        // Set any remaining unassigned to gatherers (no stealing)
        targetNum = outposts.size();
       	outpostsForStrategy = new ArrayList<Outpost>();
    	assignStrategyUnassigned(outpostsForStrategy, "resourceGatherer", targetNum);
    	Strategy getResourcesFill = new GetResources();
    	getResourcesFill.run(board, outpostsForStrategy);
    	markStrategyDone(outpostsForStrategy);
    	
    	/*
    	// Run diagonal strategy
    	targetNum = outposts.size();		// Temporary hack to just assign the rest
    	outpostsForStrategy = new ArrayList<Outpost>();
    	assignStrategy(outpostsForStrategy, "diagonalWall", targetNum);
    	Strategy DiagonalStrategy = new DiagonalStrategy();
    	DiagonalStrategy.run(board, outpostsForStrategy);
    	markStrategyDone(outpostsForStrategy);
    	*/
    	
    	// Adjust moves to avoid fruitless, suicidal moves
    	ArrayList<Integer> outpostsToAdjust = outpostsToAdjustTactically();
    	
    	// Pass back to the simulator where we want our outposts to go
    	ArrayList<movePair> moves = new ArrayList<movePair>();
    	Loc homeCell = board.getHomeCell(id);
    	
    	for (Outpost outpost : outposts) {
			Loc currentLoc = new Loc(outpost.getCurrentLoc());
			Loc targetLoc = outpost.getTargetLoc();
			
			if (targetLoc == null)
				targetLoc = new Loc(currentLoc);
			
			ArrayList<Loc> path;
			
	    	// Tactical adjustment
    		if (!outpost.memory.containsKey("role") && outpostsToAdjust.contains(new Integer(outpost.getSimIndex()))) {
    			targetLoc = new Loc(homeCell);
    			path = board.findPathPassable(currentLoc, targetLoc);
    		} else {
    			targetLoc = new Loc(targetLoc);
        		path = board.findPath(currentLoc, targetLoc);
    		}
    		
    		if (path == null || path.size() == 0 || path.size() == 1) {
    			outpost.setExpectedLoc(new Loc(currentLoc));
    			board.simFlip(currentLoc);
    			moves.add(new movePair(outpost.getSimIndex(), new Pair(currentLoc.x, currentLoc.y)));
    		} else {
    			outpost.setExpectedLoc(new Loc(path.get(1)));
    			Loc expectedLoc = path.get(1);
    			board.simFlip(expectedLoc);
    			moves.add(new movePair(outpost.getSimIndex(), new Pair(expectedLoc.x, expectedLoc.y)));
    		}
    	}
    	
    	System.out.printf("[GROUP3][LOG] Elapsed: %d\n", System.currentTimeMillis() - startTime);
    	
    	return moves;
    }
}
