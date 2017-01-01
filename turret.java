package winterbreak;

import battlecode.common.*;

public class turret  {
   /*
   Turret(+TTM) code:
   
   Upon creation:
   -Find a location to move to
   -If in turret mode, pack
   
   Every game turn:
   -If mobile: (TTM)
      -If enemies or zombies are in range:
         -If there are a sufficient ammount of allies: forget about destination; set up here (change destination to current location)
         -Else: run away! (pick a new destination 3 tiles away from the enemy)
      -Attempt to move towards destination
      -If reached destination, unpack
   -Else: (in turret form)
      TBD (shoot stuff, receive signals, pack and move if needed)
   */
   static void run(RobotController rc)throws Exception {
   	//everything written here happens once upon the creation of the turret
      Random rng = new Random(rc.getID());
      Team myTeam = rc.getTeam();
      Team enemyTeam = myTeam.opponent();
      MapLocation dest = getInitialLocation(rc, rng);
      if(rc.getType() == RobotType.TURRET)
         rc.pack();
      while(true){
         //everything written here happens every game turn
         MapLocation myLocation = rc.getLocation();
         
         //updating info about robots around me
         RobotInfo[] allRobots = rc.senseNearbyRobots();
         RobotInfo[] friendlyRobots = new RobotInfo[allRobots.length];
         RobotInfo[] enemyRobots = new RobotInfo[allRobots.length];
         RobotInfo[] zombieRobots = new RobotInfo[allRobots.length];
         RobotInfo[] neutralRobots = new RobotInfo[allRobots.length];
         int friendlyCount = 0, enemyCount = 0, zombieCount = 0, neutralCount = 0;
         for(RobotInfo info : allRobots){
            Team infoTeam = info.team;
            if(infoTeam == myTeam){
               friendlyRobots[friendlyCount] = info;
               friendlyCount++;
            }
            else if(infoTeam == enemyTeam){
               enemyRobots[enemyCount] = info;
               enemyCount++;
            }
            else if(infoTeam == Team.ZOMBIE){
               zombieRobots[zombieCount] = info;
               zombieCount++;
            }
            else if(infoTeam == Team.NEUTRAL){
               neutralRobots[neutralCount] = info;
               neutralCount++;
            }
         }
         int hostileCount = enemyCount + zombieCount;
         
         if(rc.getType() == RobotType.TTM){
            if(hostileCount > 0){   //hostiles sensed!
               if(hostileCount > friendlyCount)   //enough allies present               
                  dest = rc.getLocation();
               else{
                  //compute approx location of all hostiles
                  double sumX = 0, sumY = 0;
                  for(RobotInfo enemyInfo : enemyRobots){
                     if(enemyInfo == null)
                        break;
                     sumX += enemyInfo.location.x;
                     sumY += enemyInfo.location.y;
                  }
                  for(RobotInfo zombieInfo : zombieRobots){
                     if(zombieInfo == null)
                        break;
                     sumX += zombieInfo.location.x;
                     sumY += zombieInfo.location.y;
                  }
                  int averageX = (int)Math.round(sumX / hostileCount);
                  int averageY = (int)Math.round(sumY / hostileCount);
                  MapLocation averageLocation = myLocation.add(averageX - myLocation.x, averageY - myLocation.y);
                  dest = rc.getLocation().add(averageLocation.directionTo(myLocation), 3);
               }
            }
            //move towards dest if possible (more or less like a zombie)
            //unpack when reached destination
            if(rc.isCoreReady() && moveToDest(rc, dest))
               rc.unpack();
         }
         else{
            //TBD
         }
         Clock.yield(); //end of while loop; yield to end turn
      }
   }
   private static MapLocation getInitialLocation(RobotController rc, Random rng){
      //lame way of winding a 'frontier' - a most likely line of collision of the two teams, used to choose an initial position to deploy at:
      //1. take the average location of each of the teams' initial archon positions
      MapLocation[] teamALocs = rc.getInitialArchonLocations(Team.A);
      MapLocation[] teamBLocs = rc.getInitialArchonLocations(Team.B);
      double teamAX = 0, teamAY = 0, teamBX = 0, teamBY = 0;
      double minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
      for(MapLocation loc : teamALocs){
         teamAX += loc.x;
         teamAY += loc.y;
         if(loc.x < minX)
            minX = loc.x;
         if(loc.x > maxX)
            maxX = loc.x;
         if(loc.y < minY)
            minY = loc.y;
         if(loc.y > maxY)
            maxY = loc.y;
      }
      for(MapLocation loc : teamBLocs){
         teamBX += loc.x;
         teamBY += loc.y;
         if(loc.x < minX)
            minX = loc.x;
         if(loc.x > maxX)
            maxX = loc.x;
         if(loc.y < minY)
            minY = loc.y;
         if(loc.y > maxY)
            maxY = loc.y;
      }
      teamAX /= teamALocs.length; teamAY /= teamALocs.length; teamBX /= teamBLocs.length; teamBY /= teamBLocs.length;
      //2. draw an imaginary line between the two locations and find the middle, offset towards my team by 5 units (slightly above the turret's view range, the middle of its attack range)
      //   then, create an imaginary line (frontier) that it perpendicular to the previous, that intersects the other at the offset center
      double centerX = (teamAX + teamBX) / 2;
      double centerY = (teamAY + teamBY) / 2;
      double theta = Math.atan2(teamBY-teamAY, teamBX-teamAX); //2-argument tan^-1: opposite, adjacent (range: [-pi, pi])
      if(theta < 0)
         theta += Math.PI; //range: [0, pi]
      //subtract if my team's center is more to the left; add otherwise
      if((rc.getTeam() == Team.A && (teamAY < teamBY || (teamAY == teamBY && teamAX < teamBX))) || (rc.getTeam() == Team.B && (teamBY < teamAY || (teamBY == teamAY && teamBX < teamAX)))){
         centerX -= 5*Math.cos(theta);
         centerY -= 5*Math.sin(theta);
      }
      else{
         centerX += 5*Math.cos(theta);
         centerY += 5*Math.sin(theta);
      }
      //angle of perpendicular line (rotated by 90 degrees)
      double thetaPerp = theta + Math.PI/2;
      if(thetaPerp >= Math.PI)
         thetaPerp -= Math.PI;   //range: [0, pi)
      //System.out.print(thetaPerp + " ");
      
      //3. pick a random location along the frontier to go to. As there is no way to know the bounds of the map upon creation, use the next safest bet - min and max values of initial archon locations (+my location)
      MapLocation myLoc = rc.getLocation();
      if(myLoc.x < minX)
         minX = myLoc.x;
      if(myLoc.x > maxX)
         maxX = myLoc.x;
      if(myLoc.y < minY)
         minY = myLoc.y;
      if(myLoc.y > maxY)
         maxY = myLoc.y;
      //determine which parameter (x or y) to determine the location by. This will be the axis that has more in its domain within the frame (for example, a vertical frontier will cause the position
      //to be determined by its y-value, as there is only one x-value, not representative of a single location)
      boolean byX = true;
      if(thetaPerp > Math.PI/4 && thetaPerp < Math.PI/4*3)
         byX = false;
      //constrict the frame domain to account for crossing borders
      int range;
      if(byX && thetaPerp != 0.0){
         double minDeltay, maxDeltay;
         if(thetaPerp <= Math.PI/4){
            minDeltay = centerY - minY;
            maxDeltay = maxY - centerY;
         }
         else{
            minDeltay = maxY - centerY;
            maxDeltay = centerY - minY;
         }
         double tanTemp = Math.abs(Math.tan(thetaPerp));
         minX = Math.max(minX, Math.ceil(centerX - minDeltay / tanTemp));
         maxX = Math.min(maxX, Math.floor(centerX + maxDeltay / tanTemp));
         
      }
      else if(!byX && thetaPerp != Math.PI / 2){
         double minDeltax, maxDeltax;
         if(thetaPerp < Math.PI/2){
            minDeltax = centerX - minX;
            maxDeltax = maxX - centerX;
         }
         else{
            minDeltax = maxX - centerX;
            maxDeltax = centerX - minX;
         }
         double tanTemp = Math.abs(Math.tan(thetaPerp));
         minY = Math.max(minY, Math.ceil(centerY - minDeltax * tanTemp));
         maxY = Math.min(maxY, Math.floor(centerY + maxDeltax * tanTemp));
      }
      if(byX)
         range = (int)(maxX - minX);
      else
         range = (int)(maxY - minY);
      int finalX, finalY;
      if(byX){
         int rngX = rng.nextInt(range+1) - (int)Math.round(centerX - minX);
         finalX = (int)(centerX) + rngX;
         finalY = (int)(centerY) + (int)Math.round(Math.tan(thetaPerp) * rngX);
      }
      else{
         int rngY = rng.nextInt(range+1) - (int)Math.round(centerY - minY);
         finalX = (int)(centerX) + (int)Math.round(rngY / Math.tan(thetaPerp));
         finalY = (int)(centerY) + rngY;
      }
      //let's express the final location as an actual MapLocation. Pick a random location (my location), find translation values, and use rc.add()
      MapLocation reference = rc.getLocation();
      int deltaX = finalX - reference.x;
      int deltaY = finalY - reference.y;
      return reference.add(deltaX, deltaY);
   }
   private static boolean moveToDest(RobotController rc, MapLocation dest) throws Exception{   //returns true if further movement not required (reached location or can not move further)
      MapLocation myLocation = rc.getLocation();
      Direction d = myLocation.directionTo(dest);
      if(d == Direction.OMNI || myLocation.add(d).equals(dest))
         return true;
      if(rc.canMove(d)) rc.move(d);
      else if(rc.canMove(d.rotateLeft())) rc.move(d.rotateLeft());
      else if(rc.canMove(d.rotateRight())) rc.move(d.rotateRight());
      else{/*
         //will now measure degrees of impassability, where infinity is impassable, otherwise it represents the amount of rubble
         MapLocation myLocation = rc.getLocation();
         MapLocation locationForward = myLocation.add(d);
         MapLocation locationLeft = myLocation.add(d.rotateLeft());
         MapLocation locationRight = myLocation.add(d.rotateRight());
         double minImpassibility = Double.MAX_VALUE; //minDirection: -1 is left, 0 is forward, 1 is right
         Direction minDirection = d;
         if(rc.onTheMap(locationForward) && !rc.isLocationOccupied(locationForward))
            minImpassibility = rc.senseRubble(locationForward);
         if(rc.onTheMap(locationLeft) && !rc.isLocationOccupied(locationLeft)){
            double rubble = rc.senseRubble(locationLeft);
            if(rubble < minImpassibility){
               minImpassibility = rubble;
               minDirection = d.rotateLeft();
            }
         }
         if(rc.onTheMap(locationRight) && !rc.isLocationOccupied(locationRight)){
            double rubble = rc.senseRubble(locationRight);
            if(rubble < minImpassibility){
               minImpassibility = rubble;
               minDirection = d.rotateRight();
            }
         }
         if(minImpassibility <= 300)  //if sufficient rubble to clear out (300 rubble will take 10 turns to get to ~99.37)
            rc.clearRubble(minDirection);
         else*/   //side note: forgot TTMs can't clear rubble
         return true;
      }
      return false;
   }
}
