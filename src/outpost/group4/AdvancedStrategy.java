package outpost.group4;

import java.util.*;

public class AdvancedStrategy implements Strategy {

    Strategy defenseStrategy;
    Strategy shellStrategy;
    Strategy offenseStrategy;

    HashSet<Post> previousTurnDefense;
    HashSet<Post> previousTurnShell;
    HashSet<Post> previousTurnOffense;

    HashSet<Location> controlledLand;
    HashSet<Location> controlledWater;

    static final int RESOURCE_SIZE = 8;
    static final int BASE_DEFENSE_SIZE = 2;

    int turn;

    public AdvancedStrategy() {
        turn = 0;

        previousTurnDefense = new HashSet<Post>();
        previousTurnShell = new HashSet<Post>();
        previousTurnOffense = new HashSet<Post>();
    }

    public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
        if (defenseStrategy == null && offenseStrategy == null) {
          defenseStrategy = new UtilityMaxStrategy();
          shellStrategy = new ShellStrategy();
          offenseStrategy = new SabotageStrategy(Player.knownID);
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
          else {
            unassigned.add(p);
          }
        }

        //System.out.printf("turn %d unassigned size %d\n", turn, unassigned.size());

        // deal with unassigned posts
        for (Post p : unassigned) {
          if (defense.size() < RESOURCE_SIZE) {
            defense.add(p);
          }
          else if (shell.size() < BASE_DEFENSE_SIZE) {
            shell.add(p);
          }
          else {
            offense.add(p);
          }
        }

        //System.out.printf("turn %d defense %d shell %d offense %d\n", turn, defense.size(), shell.size(), offense.size());

        ArrayList<Post> newPosts = new ArrayList<Post>();
        ArrayList<Post> newDefense = defenseStrategy.move(otherPlayerPosts, defense, newSeason);
        ArrayList<Post> newShell = shellStrategy.move(otherPlayerPosts, shell, newSeason);
        ArrayList<Post> newOffense = offenseStrategy.move(otherPlayerPosts, offense, newSeason);

        previousTurnDefense.clear();
        previousTurnShell.clear();
        previousTurnOffense.clear();

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
