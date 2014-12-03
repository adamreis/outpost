package outpost.group4;

import java.util.*;

public class AdvancedStrategy implements Strategy {

    Strategy defenseStrategy;
    Strategy shellStrategy;
    Strategy offenseStrategy;

    ArrayList<Post> previousTurnDefense;
    ArrayList<Post> previousTurnShell;
    ArrayList<Post> previousTurnOffense;

    int DEFENSE_SIZE = 3;
    int SHELL_SIZE = 2;

    int turn;

    public AdvancedStrategy() {
        turn = 0;

        previousTurnDefense = new ArrayList<Post>();
        previousTurnShell = new ArrayList<Post>();
        previousTurnOffense = new ArrayList<Post>();
    }

    public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
        if (defenseStrategy == null && offenseStrategy == null) {
          defenseStrategy = new UtilityMaxStrategy();
          shellStrategy = new ShellStrategy();
          offenseStrategy = new SabotageStrategy(Player.knownID);
        }
        
        HashMap<Location, ArrayList<Integer>> idmap = new HashMap<Location, ArrayList<Integer>>();
        for (int i = 0; i < posts.size(); i++) {
        	Post p = posts.get(i);
        	if (idmap.get(p) == null) idmap.put(p, new ArrayList<Integer>());
        	ArrayList<Integer> ids = idmap.get(p);
        	ids.add(i);
        }

        turn += 1;

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
//        	System.out.println("unassigned: " + p);
            unassigned.add(p);
          }
        }

        //System.out.printf("turn %d unassigned size %d\n", turn, unassigned.size());

        // deal with unassigned posts
        for (Post p : unassigned) {
          if (defense.size() < DEFENSE_SIZE) {
            defense.add(p);
          }
          else if (shell.size() < SHELL_SIZE) {
            shell.add(p);
          }
          else {
            offense.add(p);
          }
        }

        //System.out.printf("turn %d defense %d shell %d offense %d\n", turn, defense.size(), shell.size(), offense.size());

//        System.out.println("Offense we are giving:");
//        for (Post p : offense) System.out.println(p);
        
        ArrayList<Post> newPosts = new ArrayList<Post>();
        ArrayList<Post> newDefense = defenseStrategy.move(otherPlayerPosts, defense, newSeason);
        ArrayList<Post> newShell = shellStrategy.move(otherPlayerPosts, shell, newSeason);
        ArrayList<Post> newOffense = offenseStrategy.move(otherPlayerPosts, offense, newSeason);

        previousTurnDefense = new ArrayList<Post>();
        previousTurnShell = new ArrayList<Post>();
        previousTurnOffense = new ArrayList<Post>();

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
        
//        System.out.println("Offense we are returning:");
//        for (Post p : previousTurnOffense) System.out.println(p);

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
