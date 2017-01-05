package winterbreak;

import battlecode.common.*;

public class archon {
	static void run(RobotController rc) throws Exception{
		//really basic archon code, just build stuff
		RobotType[] buildlist={RobotType.SOLDIER,RobotType.SOLDIER,RobotType.TURRET,RobotType.SCOUT,
				RobotType.VIPER,RobotType.SOLDIER,RobotType.GUARD,RobotType.SOLDIER,RobotType.SOLDIER};
		//RobotType[] buildlist={RobotType.SOLDIER};
		int nextBuild=0;
		Direction[] dirs={Direction.NORTH,Direction.NORTH_EAST,Direction.EAST,Direction.SOUTH_EAST,Direction.SOUTH,Direction.SOUTH_WEST,Direction.WEST,Direction.NORTH_WEST};
		while (true) {
			RobotType nxt=buildlist[nextBuild];
			MapLocation here=rc.getLocation();
			//Get all robots within the radius of the archon, find the one with the lowest health, heal it
			RobotInfo[] everyone = rc.senseNearbyRobots(24);
			double low = 9000.0;
			MapLocation loc = null;
			for(int i = 0; i < everyone.length; i++) {
				if(rc.getTeam() == everyone[i].team) 
					if(low > everyone[i].health && everyone[i].type != RobotType.ARCHON) {
						low = everyone[i].health;
						loc = everyone[i].location; 
					}
			}
			if(loc!=null)
				rc.repair(loc);
			//End healing code
			
			//Builds the next robot in line and puts it in the first possible place, if able to
			if (rc.isCoreReady()&&rc.getTeamParts()>=nxt.partCost) {
				for (int i=7; i>=0; i--) {
					if (rc.canBuild(dirs[i],nxt)) {
						rc.build(dirs[i],nxt);
						nextBuild=(nextBuild+1)%buildlist.length;
						break;
					}
					
				}
			}
			//End building code
			
			Clock.yield();
		}
	}
	
}