package outpost.group3;

import java.util.ArrayList;

import outpost.group3.Outpost;

public class ConsumerStrategy extends outpost.group3.Strategy {

  private int r;
  private int size = 100;
  enum State { ASSIGN, BUILD, ATTACK };
  static ArrayList<State> states = new ArrayList<State>();
  static boolean unassigned = true;

  //center of our consumer formation
  static ArrayList<Loc> consCenters = new ArrayList<Loc>(); 
  static ArrayList<Loc> targets = new ArrayList<Loc>();

  //These are the 4 outposts composing a consumer
  final int NORTH = 0;
  final int EAST = 1;
  final int SOUTH = 2;
  final int WEST = 3;
  final int CENTER = 4;


  ConsumerStrategy() {}

  ConsumerStrategy(int radius){
    this.r = radius;
    states.add(State.ASSIGN);
    consCenters.add(new Loc());
    targets.add(new Loc());
  }

  public void run(Board board, ArrayList<Outpost> outposts) {


    ArrayList<Outpost> consumer = new ArrayList<Outpost>(4);
    if (outposts.size() >= 5){
      if (unassigned){

        //Do this the first time we run consumer strategy on a move;
        unassigned = false;

        for (int i = 0; i < outposts.size() / 5; i++){
          for (int j = 5*i; j < 5 + 5*i; j++){
            consumer.add(outposts.get(j));
          }
          runConsumer(i,board, consumer);
          consumer.clear();
        }
      }
      else{
        for (int i = 0; i < outposts.size() / 5; i++){
          //Ensure the same outposts stay in a formation if they all still exist
          for (Outpost outpost : outposts){
            if (outpost.memory.get("formation") == i)
              consumer.add(outpost);
          }
          if (consumer.size() < 5){
            //Need to dole execess outposts to next consumer
            int components = consumer.size();
            for (Outpost outpost : outposts){
              if (outpost.memory.get("formation") == null){
                consumer.add(outpost);
                components++;
              }
              if (components == 5)
                break;
            }
          }
          if (consumer.size() < 5){
            //If formation still isn't complete, we don't have enough outposts
            for (Outpost outpost : consumer)
              outpost.setStrategy(null);
            consumer.clear();
          }
          else{
            runConsumer(i,board,consumer);
            consumer.clear();
          }
        }
      }


      for (Outpost outpost : outposts)
        if (!outpost.memory.containsKey("formation"))
          outpost.setStrategy(null);
    }
    else{
      //Less than 4 outposts, so don't make a consumer
      for (Outpost outpost : outposts)
        outpost.setStrategy(null);
    }
  }

  private void runConsumer(int formationNum, Board board, ArrayList<Outpost> outposts){

    //Check if any outposts haven't beenassigned a role
    ArrayList<String> filledRoles = new ArrayList<String>();
    for (Outpost outpost : outposts){
      if (outpost.memory.containsKey("role"))
        filledRoles.add((String)outpost.memory.get("role"));
    }
    if (filledRoles.size() < 5){
      //Go back to original assignment point to reform the formation
      states.set(formationNum,State.ASSIGN);
      for (Outpost outpost : outposts){
        if (outpost.memory.containsKey("role"))
          filledRoles.remove((String)outpost.memory.get("role"));
      }
      //Assign unassigned roles to new outposts
      for (Outpost outpost : outposts){
        if(!outpost.memory.containsKey("role") && !filledRoles.isEmpty()){
          outpost.memory.put("formation",formationNum);
          outpost.memory.put("role",filledRoles.remove(0));
        }
      }
    }

    switch (states.get(formationNum)){

      case ASSIGN:

        int numOutposts = outposts.size();

        Loc corner = new Loc(0,0);
        double minDistance = 1000000000.0;

        //Pick a location where we'll form the consumer that's close to our corner
        for (int i = r; i < size - r; i++){
          for (int j = r; j < size - r; j++){
            if (!board.getCell(i,j).isWater()){
              if(!board.getCell(i-r,j).isWater() &&
                  !board.getCell(i,j-r).isWater() &&
                  !board.getCell(i+r,j).isWater() &&
                  !board.getCell(i,j+r).isWater()){
                Loc temp = new Loc(i,j);
                if (corner.distance(temp) < minDistance){
                  minDistance = consCenters.get(formationNum).distance(temp);
                  consCenters.set(formationNum,temp);
                }
              }
            }
          }
        } 

        //Put first four outposts in first outpost
        for (int i = 0; i < 5; i ++)
          outposts.get(i).memory.put("formation",formationNum);

        //Assign each outpost its position
        outposts.get(NORTH).memory.put("role","north");
        outposts.get(SOUTH).memory.put("role","south");
        outposts.get(WEST).memory.put("role","west");
        outposts.get(EAST).memory.put("role","east");
        outposts.get(CENTER).memory.put("role","center");

        //Set state to BUILD and fall through
        states.set(formationNum,State.BUILD);

      case BUILD: 
        buildMove(formationNum,outposts,board);
        break;

      case ATTACK: 
        attackMove(formationNum,outposts,board);
        break;

    }

  }

  //Build a formation centered at consCenter
  private void buildMove(int formationNum, ArrayList<Outpost> outposts, Board board){
    int numInPosition = 0;

    //Set largest spacing between components that leads to a water-less consumer
    /*
       Commented out for submission

       for (int i = 0; i < r; i ++){
       Loc spot = consCenters.get(formationNum);
       if (board.getCell(new Loc(spot.x + i, spot.y)).isWater()
       || board.getCell(new Loc(spot.x - i, spot.y)).isWater()
       || board.getCell(new Loc(spot.x, spot.y + i)).isWater()
       || board.getCell(new Loc(spot.x, spot.y - i)).isWater()){
       System.out.println("Building with " + i + " space for formation " + formationNum);
       setFormationLocs(outposts,formationNum,"",board,i,false);
       break;
       }
       else if ( i == r-1)
       setFormationLocs(outposts,formationNum,"",board,r,false);

     */

    //set the target locations
    setFormationLocs(outposts,consCenters.get(formationNum),board);

    //Are we in formation?
    for (Outpost outpost : outposts){
      if (outpost.getCurrentLoc().equals(outpost.getTargetLoc()))
        numInPosition++;
    }

    if(numInPosition == 5){
      //We have fully formed a consumer, let's attack!
      states.set(formationNum,State.ATTACK);
      for (Outpost outpost : outposts)
        outpost.memory.put("expectedSpot",outpost.getExpectedLoc());
      attackMove(formationNum,outposts,board);
    }
    //set the target locations

    /*
    //Are we in formation?
    for (Outpost outpost : outposts){
    if (outpost.getCurrentLoc().equals(outpost.getTargetLoc()))
    numInPosition++;
    }

    if(numInPosition >= 4){
    //We have fully formed a consumer, let's attack!
    states.set(formationNum,State.ATTACK);
    for (Outpost outpost : outposts)
    outpost.memory.put("expectedSpot",outpost.getExpectedLoc());
    attackMove(formationNum,outposts,board);
    }
    }
     */
  }

  //Set the next move for our consumer formation to attack the closest enemy
  /*
     private void attackMove(int formationNum, ArrayList<Outpost> outposts, Board board){

     double enemyDist = 100;

     Loc centerTarget = new Loc();
     ArrayList<Loc> enemyOutposts = new ArrayList<Loc>();
     ArrayList<Outpost> outOfFormation = new ArrayList<Outpost>();
     Loc oldCenter = consCenters.get(formationNum);
     System.out.println("Original center is " + consCenters.get(formationNum));

     Original solution to water traversal

     for (Outpost outpost : outposts){
     Loc spot = (Loc)outpost.memory.get("expectedSpot");
     Loc realSpot = outpost.getExpectedLoc();
     if(spot.x != realSpot.x && spot.y != realSpot.y){
     outOfFormation.add(outpost);
     }
     }

     if (!outOfFormation.isEmpty()){
     System.out.println("Moving to build");
  //If both coordinates of an outpost's location are incorrect
  //with respect to its formation assignment, we need to rebuild
  states.set(formationNum,State.BUILD);
  buildMove(formationNum,outposts,board);
  }

//else{

//Find closest enemy
String closest = "";
for (int i = 0; i < 4; i ++){
enemyOutposts = board.theirOutposts(i);
if (enemyOutposts == board.ourOutposts())
continue;
for (Loc enemy : enemyOutposts){
for (Outpost outpost : outposts){
double tempDist = outpost.getCurrentLoc().distance(enemy);
if (tempDist < enemyDist){
enemyDist = tempDist;
centerTarget = enemy;
closest = (String) outpost.memory.get("role");
}
}
}
}


System.out.println("Ideally want to move " + closest + " towards enemy " + centerTarget);

//Update expected positions for next turn and move the center of our formation

//TODO: Check if the next move for any component (based on findPath)
//is not in the direction we desire. If only one component moves in a "wrong" direction,
//move all components in that direction.
//int newCenterx = consCenters.get(formationNum).x;
//int newCentery = consCenters.get(formationNum).y;
//if(closest.equals("north")){
//  newCentery++;
//  updateExpectations(outposts,"y",1);
//}
//else if(closest.equals("south")){
//  newCentery--;
//  updateExpectations(outposts,"y",-1);
//}
//else if(closest.equals("east")){
//  newCenterx++;
//  updateExpectations(outposts,"x",1);
//}
//else if(closest.equals("west")){
//  newCenterx--;
//  updateExpectations(outposts,"x",-1);
//}

//Set new formation center
//consCenters.set(formationNum,new Loc(newCenterx, newCentery));
//System.out.println("Set center to " + consCenters.get(formationNum));

setFormationLocs(outposts,formationNum,closest,board,r,false);

boolean stationary = false;
int wrongMove = 0;
String wrongOutpost = "";
for (Outpost outpost : outposts) {
  Loc currentLoc = outpost.getCurrentLoc();
  Loc targetLoc = outpost.getTargetLoc();

  if (targetLoc == null)
    targetLoc = new Loc(currentLoc);

  ArrayList<Loc> path = board.findPath(currentLoc, targetLoc);

  Loc loc = new Loc();
  if (path == null || path.size() == 0 || path.size() == 1) {
    loc.x = currentLoc.x;
    loc.y = currentLoc.y;
    System.out.println("Issue with path for " + (String) outpost.memory.get("role"));
  } else {
    loc.x = path.get(1).x;
    loc.y = path.get(1).y;
  }
  //Check if next move would lead to any outposts in formation being stationary 
  if (loc.equals(currentLoc)){
    stationary = true;
    System.out.println("Outpost " + (String) outpost.memory.get("role") + " would be stationary " );
  }
  //Check if any outposts will move to unusual position
  else if (!loc.equals(outpost.memory.get("expectedSpot"))){
    wrongMove++;
    wrongOutpost = (String) outpost.memory.get("role");
    System.out.println("Outpost " + wrongOutpost + " was going to move to " + loc + " but expected to move " + outpost.memory.get("expectedSpot"));
  }
}

//If one outpost makes "wrong" move, move in that direction
//TODO: Fix bugs, yo
if (wrongMove >= 1){
  System.out.println("Wrong formation");

  //Manually set each outpost to move in "wrong" direction
  setFormationLocs(outposts,formationNum,wrongOutpost,board,r,true);

  stationary = false;
  //Check if things would be stationary again
  for (Outpost outpost : outposts) {
    Loc currentLoc = outpost.getCurrentLoc();
    Loc targetLoc = outpost.getTargetLoc();

    if (targetLoc == null)
      targetLoc = new Loc(currentLoc);

    ArrayList<Loc> path = board.findPath(currentLoc, targetLoc);

    Loc loc = new Loc();
    if (path == null || path.size() == 0 || path.size() == 1) {
      loc.x = currentLoc.x;
      loc.y = currentLoc.y;
    } else {
      loc.x = path.get(1).x;
      loc.y = path.get(1).y;
    }
    //Check if next move would lead to any outposts in formation being stationary 
    if (loc.equals(currentLoc))
      stationary = true;
  }
  //If any outposts would be stationary, contract formation
  if (stationary){
    System.out.println("Reset formation");
    setFormationLocs(outposts,formationNum,"",board,r--,true);
  }

}
//If any outposts would be stationary or multiple outposts would make a wrong move, contract formation
else if (stationary){
  System.out.println("Reset formation");
  consCenters.set(formationNum,oldCenter);
  setFormationLocs(outposts,formationNum,"",board,r--,true);
}
//}
System.out.println("Final center is " + consCenters.get(formationNum));
}

//Given the center of a formation, assign the consumer outposts to their associated positions
private void setFormationLocs(ArrayList<Outpost> outposts, int formationNum, String direction , Board board, int dist, boolean stationary){
  Loc oldCenter = consCenters.get(formationNum);
  int space = 0;
  for (Outpost outpost : outposts){
    Loc center = outpost.getCurrentLoc();
    if (stationary && states.get(formationNum) != State.BUILD){
      //Contract
      if(outpost.memory.get("role").equals("north")){
        outpost.setTargetLoc(board.nearestLand(new Loc(center.x,center.y - 1)));
      }
      else if(outpost.memory.get("role").equals("south")){
        outpost.setTargetLoc(board.nearestLand(new Loc(center.x,center.y + 1)));
      }
      else if(outpost.memory.get("role").equals("east")){
        outpost.setTargetLoc(board.nearestLand(new Loc(center.x - 1,center.y)));
      }
      else if(outpost.memory.get("role").equals("west")){
        outpost.setTargetLoc(board.nearestLand(new Loc(center.x + 1,center.y)));
      }
    }
    else if (states.get(formationNum) == State.BUILD){
      if(outpost.memory.get("role").equals("north")){
        outpost.setTargetLoc(board.nearestLand(new Loc(oldCenter.x,oldCenter.y + dist)));
      }
      else if(outpost.memory.get("role").equals("south")){
        outpost.setTargetLoc(board.nearestLand(new Loc(oldCenter.x,oldCenter.y - dist)));
      }
      else if(outpost.memory.get("role").equals("east")){
        outpost.setTargetLoc(board.nearestLand(new Loc(oldCenter.x + dist,oldCenter.y)));
      }
      else if(outpost.memory.get("role").equals("west")){
        outpost.setTargetLoc(board.nearestLand(new Loc(oldCenter.x - dist,oldCenter.y)));
      }
    }
    else{
      if(direction.equals("north")){
        outpost.setTargetLoc(board.nearestLand(new Loc(center.x,center.y + 1)));
        consCenters.set(formationNum, new Loc(oldCenter.x,oldCenter.y + 1));
      }
      else if(direction.equals("south")){
        outpost.setTargetLoc(board.nearestLand(new Loc(center.x,center.y - 1)));
        consCenters.set(formationNum, new Loc(oldCenter.x,oldCenter.y - 1));
      }
      else if(direction.equals("east")){
        outpost.setTargetLoc(board.nearestLand(new Loc(center.x + 1,center.y)));
        consCenters.set(formationNum, new Loc(oldCenter.x + 1,oldCenter.y));
      }
      else if(direction.equals("west")){
        outpost.setTargetLoc(board.nearestLand(new Loc(center.x - 1,center.y)));
        consCenters.set(formationNum, new Loc(oldCenter.x - 1,oldCenter.y));
      }
    }
    outpost.memory.put("expectedSpot",outpost.getTargetLoc());
    System.out.println("Oupost " + outpost.memory.get("role") + " is moving to " + outpost.getTargetLoc());
  }

}

*/
//Update each outpost's memory of the expected location for the next turn
private void updateExpectations(ArrayList<Outpost> outposts, String coord, int change){
  for (Outpost outpost : outposts){
    Loc newspot = new Loc(outpost.getCurrentLoc());
    if (coord.equals("y"))
      newspot.y += change;
    else
      newspot.x += change;
    outpost.memory.put("expectedSpot",newspot);
  }
}


  //Set the next move for our consumer formation to attack the closest enemy
  private void attackMove(int formationNum, ArrayList<Outpost> outposts, Board board){

    double enemyDist = 100;

    ArrayList<Loc> enemyOutposts = new ArrayList<Loc>();
    ArrayList<Outpost> outOfFormation = new ArrayList<Outpost>();

    for (Outpost outpost : outposts){
      Loc spot = (Loc)outpost.memory.get("expectedSpot");
      Loc realSpot = outpost.getExpectedLoc();
      if(spot.x != realSpot.x && spot.y != realSpot.y){
        outOfFormation.add(outpost);
      }
    }

    if (!outOfFormation.isEmpty()){
      //If both coordinates of an outpost's location are incorrect
      //with respect to its formation assignment, we need to rebuild
      states.set(formationNum,State.BUILD);
      buildMove(formationNum,outposts,board);
    }
    else{
      //Find closest enemy
      for (int i = 0; i < 4; i ++){
        enemyOutposts = board.theirOutposts(i);
        if (enemyOutposts == board.ourOutposts())
          continue;
        for (Loc outpost : enemyOutposts){
          double tempDist = outpost.distance(consCenters.get(formationNum));
          if (tempDist < enemyDist && !targets.contains(outpost)){
            enemyDist = tempDist;
            targets.set(formationNum,outpost);
          }
        }
      }

      //Determine which member of our ideal formation is closest to the target
      String closest = "";
      enemyDist = 100;
      for (Outpost outpost : outposts){
        double tempDist = outpost.getCurrentLoc().distance(targets.get(formationNum));
        if (tempDist < enemyDist){
          closest = (String) outpost.memory.get("role");
          enemyDist = tempDist;
        }
      }

      //Update expected positions for next turn and move the center of our formation
      int newCenterx = consCenters.get(formationNum).x;
      int newCentery = consCenters.get(formationNum).y;
      if(closest.equals("north")){
        newCentery++;
        updateExpectations(outposts,"y",1);
      }
      else if(closest.equals("south")){
        newCentery--;
        updateExpectations(outposts,"y",-1);
      }
      else if(closest.equals("east")){
        newCenterx++;
        updateExpectations(outposts,"x",1);
      }
      else if(closest.equals("west")){
        newCenterx--;
        updateExpectations(outposts,"x",-1);
      }

      //Set new formation center
      consCenters.set(formationNum, new Loc(newCenterx, newCentery));

      setFormationLocs(outposts,consCenters.get(formationNum),board);
    }
  }

  //Given the center of a formation, assign the consumer outposts to their associated positions
  private void setFormationLocs(ArrayList<Outpost> outposts, Loc formationCenter, Board board){
    for (Outpost outpost : outposts){
      if(outpost.memory.get("role").equals("north"))
        outpost.setTargetLoc(board.nearestLand(new Loc(formationCenter.x,formationCenter.y + 1)));
      else if(outpost.memory.get("role").equals("south"))
        outpost.setTargetLoc(board.nearestLand(new Loc(formationCenter.x,formationCenter.y - 1)));
      else if(outpost.memory.get("role").equals("east"))
        outpost.setTargetLoc(board.nearestLand(new Loc(formationCenter.x + 1,formationCenter.y)));
      else if(outpost.memory.get("role").equals("west"))
        outpost.setTargetLoc(board.nearestLand(new Loc(formationCenter.x - 1,formationCenter.y)));
      else if(outpost.memory.get("role").equals("center"))
        outpost.setTargetLoc(board.nearestLand(new Loc(formationCenter.x,formationCenter.y)));
    }
  }


}
