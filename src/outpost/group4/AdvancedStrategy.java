package outpost.group4;

import java.util.*;

public class AdvancedStrategy implements Strategy {

    Strategy defenseStrategy;
    Strategy shellStrategy;
    Strategy offenseStrategy;
    Strategy resourceStrategy;

    ArrayList<Post> previousTurnDefense;
    ArrayList<Post> previousTurnShell;
    ArrayList<Post> previousTurnOffense;
    ArrayList<Post> previousTurnResource;

    HashSet<Location> controlledLand;
    HashSet<Location> controlledWater;

    static final int WATER_COLLECTOR_MIN_SIZE = 3;
    static final int BASE_DEFENSE_MIN_SIZE = 2;

    static final double WATER_COLLECTOR_RATIO = 0.25;
    static final double EARLY_OFFENSE_RATIO = 0.7;
    static final double LATE_OFFENSE_RATIO = 0.44;

    int turn;

    public AdvancedStrategy() {
        turn = 0;

        previousTurnDefense = new ArrayList<Post>();
        previousTurnShell = new ArrayList<Post>();
        previousTurnOffense = new ArrayList<Post>();
        previousTurnResource = new ArrayList<Post>();
    }

    public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
        if (defenseStrategy == null && offenseStrategy == null) {
          defenseStrategy = new UtilityMaxStrategy(false);
          shellStrategy = new ShellStrategy();
          offenseStrategy = new SabotageStrategy(Player.knownID);
          resourceStrategy = new DumbQuadrantStrategy();
        }

        HashMap<Location, ArrayList<Integer>> idmap = new HashMap<Location, ArrayList<Integer>>();
        for (int i = 0; i < posts.size(); i++) {
        	Post p = posts.get(i);
        	if (idmap.get(p) == null) idmap.put(p, new ArrayList<Integer>());
        	ArrayList<Integer> ids = idmap.get(p);
        	ids.add(i);
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
        int potentialOutposts = Player.parameters.outpostsSupportedWithResources(landCount, waterCount);

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

        //System.out.printf("turn %d unassigned size %d\n", turn, unassigned.size());

        // assign some unassigned posts to water and base
        ArrayList<Post> veryUnassigned = new ArrayList<Post>();
        for (Post p : unassigned) {
          if (defense.size() < WATER_COLLECTOR_MIN_SIZE) {
            defense.add(p);
          }
          else if (shell.size() < BASE_DEFENSE_MIN_SIZE) {
            shell.add(p);
          }
          else {
            veryUnassigned.add(p);
          }
        }

        // assign rest based on ratios
        for (Post p : veryUnassigned) {
          double numWater = defense.size();
          double numOffense = offense.size();
          double numResource = resource.size();
          double total = posts.size() - BASE_DEFENSE_MIN_SIZE;
          double offenseRatio = numOffense < 40? EARLY_OFFENSE_RATIO : LATE_OFFENSE_RATIO;

          if (numWater / total <= WATER_COLLECTOR_RATIO) {
            defense.add(p);
          }
          else if (numOffense / total <= offenseRatio) {
            offense.add(p);
          }
          else {
            resource.add(p);
          }

          System.out.printf("water %f offense %f resource %f total %f\n", numWater, numOffense, numResource, total);
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
      // delete a shell if possible
      for (int i = 0; i < posts.size(); i++) {
        Post p = posts.get(i);
        if (previousTurnShell.contains(p)) {
            return i;
        }
      }

      // delete an offense if possible
      for (int i = 0; i < posts.size(); i++) {
        Post p = posts.get(i);
        if (previousTurnOffense.contains(p)) {
          return i;
        }
      }

      // at this point anything is bad
      return Player.random.nextInt(posts.size());
    }
}
