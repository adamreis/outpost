package outpost.group4;

import java.util.*;

public class AdvancedStrategy implements Strategy {

    Strategy defenseStrategy;
    Strategy shellStrategy;
    SabotageStrategy offenseStrategy;
    Strategy resourceStrategy;

    ArrayList<Post> previousTurnDefense;
    ArrayList<Post> previousTurnShell;
    ArrayList<Post> previousTurnOffense;
    ArrayList<Post> previousTurnResource;
    ArrayList<Post> trash;

    HashSet<Location> controlledLand;
    HashSet<Location> controlledWater;

    static final int WATER_COLLECTOR_MIN_SIZE = 3;
    static final int WATER_COLLECTOR_MAX_SIZE = 13;
    static final int OFFENSE_FIRST_PAIR_MIN_SIZE = 0;
    static final int BASE_DEFENSE_MIN_SIZE = 2;

    static final double WATER_COLLECTOR_RATIO = 0.26;
    static final double EARLY_OFFENSE_RATIO = 0.69;
    static final double LATE_OFFENSE_RATIO = 0.48;

    double waterRatioHelper;

    int turn;

    public AdvancedStrategy() {
        turn = 0;

        previousTurnDefense = new ArrayList<Post>();
        previousTurnShell = new ArrayList<Post>();
        previousTurnOffense = new ArrayList<Post>();
        previousTurnResource = new ArrayList<Post>();
        trash = new ArrayList<Post>();

        waterRatioHelper = -100;
    }

    public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
        if (defenseStrategy == null && offenseStrategy == null) {
          defenseStrategy = new UtilityMaxStrategy(false);
          shellStrategy = new AdamShellStrategy(Player.knownID);
          offenseStrategy = new SabotageStrategy(Player.knownID);
          resourceStrategy = new DumbQuadrantStrategy();
        }

        if (waterRatioHelper <= -1) {
          if (Player.parameters.requiredLand > 20) {
            waterRatioHelper = -0.08;
          }
          else {
            waterRatioHelper = 0.0;
          }
        }
        
        if (offenseStrategy.weWonBro()) {
        	return posts;
        }

        turn += 1;

        controlledLand = new HashSet<Location>();
        controlledWater = new HashSet<Location>();
        for (Post p : posts) {
            ArrayList<GridSquare> controlledSquares = Player.board.squaresWithinRadius(p);
            for (GridSquare square : controlledSquares) {
                if (square.water) controlledWater.add(square);
                else controlledLand.add(square);
            }
        }

        int landCount = controlledLand.size();
        int waterCount = controlledWater.size();
        //int potentialOutposts = Player.parameters.outpostsSupportedWithResources(landCount, waterCount);

        ArrayList<Post> defense = new ArrayList<Post>();
        ArrayList<Post> shell = new ArrayList<Post>();
        ArrayList<Post> offense = new ArrayList<Post>();
        ArrayList<Post> resource = new ArrayList<Post>();
        ArrayList<Post> unassigned = new ArrayList<Post>();

        // assign what we know from last turn
        // we remove things from the set after counting them in cast multiple outposts are in the same locationc
        for (Post p : posts) {
          if (previousTurnDefense.contains(p)) {
            defense.add(p);
            previousTurnDefense.remove(p);
          }
          else if (previousTurnShell.contains(p)) {
            shell.add(p);
            previousTurnShell.remove(p);
          }
          else if (previousTurnOffense.contains(p)) {
            offense.add(p);
            previousTurnOffense.remove(p);
          }
          else if (previousTurnResource.contains(p)) {
            resource.add(p);
            previousTurnResource.remove(p);
          }
          else {
//        	System.out.println("unassigned: " + p);
            unassigned.add(p);
          }
        }
        
        int waterCollectorMinimum = Player.parameters.requiredLand > 20 ? WATER_COLLECTOR_MIN_SIZE : WATER_COLLECTOR_MIN_SIZE - 2;
        int landCollectorMinimum = Player.parameters.requiredLand > 20 ? 2 : 0;
        int shellMinimum = Player.parameters.requiredLand > 20 ? 0 : BASE_DEFENSE_MIN_SIZE;
        
        while (defense.size() < waterCollectorMinimum && shell.size() > 0) {
        	Post p = shell.get(0);
        	shell.remove(p);
        	defense.add(p);
        } 
        while (defense.size() < waterCollectorMinimum && resource.size() > 0) {
        	Post p = resource.get(0);
        	resource.remove(p);
        	defense.add(p);
        }
        while (defense.size() < waterCollectorMinimum && offense.size() > 0) {
        	Post p = offense.get(0);
        	offense.remove(p);
        	defense.add(p);
        }
        
        //System.out.printf("turn %d unassigned size %d\n", turn, unassigned.size());

        // assign some unassigned posts to water and base
        ArrayList<Post> veryUnassigned = new ArrayList<Post>();
        for (Post p : unassigned) {
          if (defense.size() < waterCollectorMinimum) {
            defense.add(p);
          }
          else if (resource.size() < landCollectorMinimum) {
        	resource.add(p);
          }
          else if (offense.size() < OFFENSE_FIRST_PAIR_MIN_SIZE) {
            offense.add(p);
          }
          else if (shell.size() < shellMinimum) {
            shell.add(p);
          }
          else {
            veryUnassigned.add(p);
          }
        }

        // assign rest based on ratios
        for (Post p : veryUnassigned) {
          double numWater = Math.max(defense.size() - waterCollectorMinimum, 0);
          double numOffense = offense.size();
          double numResource = resource.size();
          double total = posts.size() - shellMinimum - waterCollectorMinimum;
          
          double offenseRatio = numOffense < 14? EARLY_OFFENSE_RATIO : LATE_OFFENSE_RATIO;
          offenseRatio -= waterRatioHelper;
          double waterRatio = WATER_COLLECTOR_RATIO + waterRatioHelper;

          if (numWater > WATER_COLLECTOR_MAX_SIZE) {
            offenseRatio += waterRatio / 2;
            waterRatio = 0;
          }

          if (numWater / total <= waterRatio) {
            defense.add(p);
          }
          else if (numOffense / total <= offenseRatio) {
            offense.add(p);
          }
          else {
            resource.add(p);
          }

          //System.out.printf("water %f offense %f resource %f total %f\n", numWater, numOffense, numResource, total);
        }

        System.out.printf("turn %d defense %d shell %d offense %d\n", turn, defense.size(), shell.size(), offense.size());

//        System.out.println("Offense we are giving:");
//        for (Post p : offense) System.out.println(p);

        ArrayList<Post> newPosts = new ArrayList<Post>();
        ArrayList<Post> newDefense = defenseStrategy.move(otherPlayerPosts, defense, newSeason);
        ArrayList<Post> newShell = shellStrategy.move(otherPlayerPosts, shell, newSeason);
        ArrayList<Post> newOffense = offenseStrategy.move(otherPlayerPosts, offense, newSeason);
        ArrayList<Post> newResource = resourceStrategy.move(otherPlayerPosts, resource, newSeason);

        previousTurnDefense = new ArrayList<Post>();
        previousTurnShell = new ArrayList<Post>();
        previousTurnOffense = new ArrayList<Post>();
        previousTurnResource = new ArrayList<Post>();

        for (Post p : newDefense) {
          newPosts.add(p);
          previousTurnDefense.add(p);
        }
        for (Post p : newShell) {
          newPosts.add(p);
          previousTurnShell.add(p);
        }
        for (Post p : newOffense) {
          newPosts.add(p);
          previousTurnOffense.add(p);
        }
        for (Post p : newResource) {
          newPosts.add(p);
          previousTurnResource.add(p);
        }

        return newPosts;
    }

    public int delete(ArrayList<Post> posts) {
      // delete trash
      for (int i = 0; i < posts.size(); i++) {
    	  Post p = posts.get(i);
    	  if (p.x == Player.baseLoc.x && p.y == Player.baseLoc.y) {
    		  return i;
    	  }
      }
    	
      // delete a shell if possible
      for (int i = 0; i < posts.size(); i++) {
        Post p = posts.get(i);
        if (previousTurnShell.contains(p)) {
            return i;
        }
      }
      
      // delete an offense if possible
      int offDelIdx = offenseStrategy.delete(previousTurnOffense);
      if (offDelIdx >= 0) {
    	  Post p = previousTurnOffense.get(offDelIdx);
    	  int i = posts.indexOf(p);
    	  if (i >= 0) {
    		  return i;
    	  }
      }
      
      // delete a land resource if possible
      for (int i = 0; i < posts.size(); i++) {
          Post p = posts.get(i);
          if (previousTurnResource.contains(p)) {
              return i;
          }
        }

      // at this point anything is bad
      return Player.random.nextInt(posts.size());
    }
}
