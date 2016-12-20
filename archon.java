package winterbreak;

import battlecode.common.*;

public class archon {
	static void run(RobotController rc) throws Exception{
		//really basic archon code, just build stuff
		//RobotType[] buildlist={RobotType.SOLDIER,RobotType.SOLDIER,RobotType.TURRET,RobotType.SCOUT,
		//		RobotType.VIPER,RobotType.SOLDIER,RobotType.GUARD,RobotType.SOLDIER,RobotType.SOLDIER};
		RobotType[] buildlist={RobotType.SOLDIER};
		int nextBuild=0;
		Direction[] dirs={Direction.NORTH,Direction.NORTH_EAST,Direction.EAST,Direction.SOUTH_EAST,Direction.SOUTH,Direction.SOUTH_WEST,Direction.WEST,Direction.NORTH_WEST};
		while (true) {
			RobotType nxt=buildlist[nextBuild];
			MapLocation here=rc.getLocation();
			if (rc.isCoreReady()&&rc.getTeamParts()>=nxt.partCost) {
				for (int i=7; i>=0; i--) {
					if (rc.canBuild(dirs[i],nxt)) {
						rc.build(dirs[i],nxt);
						nextBuild=(nextBuild+1)%buildlist.length;
						break;
					}
					
				}
			}
			Clock.yield();
		}
	}
	
}