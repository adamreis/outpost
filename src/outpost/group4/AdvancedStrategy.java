package outpost.group4;

import java.util.*;

public class AdvancedStrategy implements Strategy {

    Strategy defenseStrategy;
    Strategy shellStrategy;
    Strategy offenseStrategy;
    Strategy resourceStrategy;

    HashSet<Post> previousTurnDefense;
    HashSet<Post> previousTurnShell;
    HashSet<Post> previousTurnOffense;
    HashSet<Post> previousTurnResource;

    HashSet<Location> controlledLand;
    HashSet<Location> controlledWater;

    static final int WATER_COLLECTOR_MIN_SIZE = 3;
    static final int BASE_DEFENSE_MIN_SIZE = 2;

    static final double WATER_COLLECTOR_RATIO = 0.2;
    static final double EARLY_OFFENSE_RATIO = 0.8;
    static final double LATE_OFFENSE_RATIO = 0.35;

    int turn;

    public AdvancedStrategy() {
        turn = 0;

        previousTurnDefense = new HashSet<Post>();
        previousTurnShell = new HashSet<Post>();
        previousTurnOffense = new HashSet<Post>();
        previousTurnResource = new HashSet<Post>();
    }

    public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
        if (defenseStrategy == null && offenseStrategy == null) {
          defenseStrategy = new UtilityMaxStrategy(false);
          shellStrategy = new ShellStrategy();
          offenseStrategy = new SabotageStrategy(Player.knownID);
          resourceStrategy = new UtilityMaxStrategy(true);
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
          double offenseRatio = numOffense > 40? EARLY_OFFENSE_RATIO : LATE_OFFENSE_RATIO;

          if (numWater / total < WATER_COLLECTOR_RATIO) {
            defense.add(p);
          }
          else if (numOffense / total < offenseRatio) {
            offense.add(p);
          }
          else {
            resource.add(p);
          }
        }

        //System.out.printf("turn %d defense %d shell %d offense %d\n", turn, defense.size(), shell.size(), offense.size());

        ArrayList<Post> newPosts = new ArrayList<Post>();
        ArrayList<Post> newDefense = defenseStrategy.move(otherPlayerPosts, defense, newSeason);
        ArrayList<Post> newShell = shellStrategy.move(otherPlayerPosts, shell, newSeason);
        ArrayList<Post> newOffense = offenseStrategy.move(otherPlayerPosts, offense, newSeason);
        ArrayList<Post> newResource = resourceStrategy.move(otherPlayerPosts, resource, newSeason);

        previousTurnDefense.clear();
        previousTurnShell.clear();
        previousTurnOffense.clear();
        previousTurnResource.clear();

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
