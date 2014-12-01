package outpost.group4;

import java.util.*;

public class AdvancedStrategy implements Strategy {

    ArrayList<Post> posts;
    ;
    ArrayList<Post> offense;
    Strategy defenseStrategy;
    Strategy shellStrategy;
    Strategy offenseStrategy;
    int DEFENSE_SIZE = 8;
    int SHELL_SIZE = 3;
    int turn;

    public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
        if (defenseStrategy == null && offenseStrategy == null) {
          defenseStrategy = new UtilityMaxStrategy();
          shellStrategy = new ShellStrategy();
          offenseStrategy = new SabotageStrategy(Player.knownID);
        }

        turn += 1;
        this.posts = posts;

        ArrayList<Post> defense = new ArrayList<Post>();
        ArrayList<Post> shell = new ArrayList<Post>();
        ArrayList<Post> offense = new ArrayList<Post>();

        for (int i = 0; i < posts.size(); i++) {
          Post p = posts.get(i);

          if (i < DEFENSE_SIZE) {
            defense.add(p);
          }
          else if (i < DEFENSE_SIZE + SHELL_SIZE) {
            shell.add(p);
          }
          else {
            offense.add(p);
          }
        }

        ArrayList<Post> newPosts = new ArrayList<Post>();
        ArrayList<Post> newDefense = defenseStrategy.move(otherPlayerPosts, defense, newSeason);
        ArrayList<Post> newShell = shellStrategy.move(otherPlayerPosts, shell, newSeason);
        ArrayList<Post> newOffense = offenseStrategy.move(otherPlayerPosts, offense, newSeason);

        for (Post p : newDefense)
          newPosts.add(p);
        for (Post p : newShell)
          newPosts.add(p);
        for (Post p : newOffense)
          newPosts.add(p);

        return newPosts;
    }
}
