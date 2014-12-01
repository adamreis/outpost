package outpost.group4;

import java.util.*;

public class AdvancedStrategy implements Strategy {

    ArrayList<Post> posts;
    ArrayList<Post> defense;
    ArrayList<Post> offense;
    Strategy defenseStrategy;
    Strategy offenseStrategy;
    int DEFENSE_SIZE = 3;
    int turn;
  
    public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
        if (defense == null && offense == null && defenseStrategy == null && offenseStrategy == null) {
          defense = new ArrayList<Post>();
          offense = new ArrayList<Post>();
          defenseStrategy = new UtilityMaxStrategy();
          offenseStrategy = new SabotageStrategy(Player.knownID);
        }
        turn += 1;
        this.posts = posts;

        for (int i = 0; i < posts.size(); i++) {
          if (i < DEFENSE_SIZE) {
            defense.add(posts.get(i));
          } else {
            offense.add(posts.get(i));
          }
        }

        ArrayList<Post> newPosts = new ArrayList<Post>();
        ArrayList<Post> newDefense = defenseStrategy.move(otherPlayerPosts, defense, newSeason);
        ArrayList<Post> newOffense = offenseStrategy.move(otherPlayerPosts, offense, newSeason);

        for (Post p : newDefense)
          newPosts.add(p); 
        for (Post p : newOffense)
          newPosts.add(p);
    
        return newPosts;
    }
}
        
