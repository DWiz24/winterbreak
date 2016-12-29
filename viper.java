package winterbreak;

import battlecode.common.*;
import java.util.Random;

public class viper extends Inform {
	/*
	 * Priority: enemy is not infected:
	 *	Infect enemies with most health first - avoid turning into zombies
	 *	AVOID scouts + archons with low health [no big or fast zombies]
	 *If about to die & more enemies close
	 *	attack self
	 *	when nearest robot is enemy
	 *		disintegrate [suicide]
	 *If infected
	 *	commonFunc
	 */
	private static MapLocation attackTarget = null;
	
	private static RobotType type = RobotType.VIPER;
	
	private static int lastKnownArchonId = -1;
	private static MapLocation lastKnownArchonLocation = null;
	
	public static RobotController me;
	public static MapLocation here;
	
	static void run(RobotController rc) throws Exception
	{
		me = rc;
		here = rc.getLocation();
		Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
		Random rand = new Random(rc.getID());
		while (true) 
		{
			try
			{
				attack();
				
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
			Clock.yield();
		}
	}
	private static void attack()
	{
		if (me.isWeaponReady() && me.getCoreDelay() >= type.cooldownDelay) 
		{
			RobotInfo[] enemies = me.senseNearbyRobots(here, type.attackRadiusSquared, them);
			if (enemies.length > 0)
			{
				RobotInfo best = null;
				for( RobotInfo pos : enemies )
				{
					if( pickTarget(pos, best) )
						best = pos;
				}
				me.attackLocation(best.location);
			}
		}
	}
	//update overall goal based on new info
	private static void updateTarget()
	{
		
	}
	//If a is betterTarget than b return true
	//Improve
	private static boolean pickTarget(RobotInfo a, RobotInfo b) {
		if (a.type == RobotType.SCOUT)
			return false; //never infect scouts
		if (a.type == RobotType.ARCHON && a.health < 500)
			return false; // big zombies are too dangerous
		if (b == null)
			return true;
		if (b.type.isZombie) 
		{
			if (a.type.isZombie)
				return a.health < b.health;
			return true;
		}
		// b is not a zombie
		if (a.type.isZombie)
			return false;
		// neither a nor b are zombies
		if (a.viperInfectedTurns != b.viperInfectedTurns) 
		{
			return a.viperInfectedTurns < b.viperInfectedTurns;	
			//add conditions based on health
		}
		// a and b are infected for the same number of turns
		return score(a.type, a.health) > score(b.type, b.health);
	}

	private static double score(RobotType type, double health) //arbitrary values right now
	{
		switch(type) 
		{
		case ARCHON:
			return -1;
		case ZOMBIEDEN:
			return -2;	
		case TTM:
			return RobotType.TURRET.attackPower / (health * RobotType.TURRET.attackDelay);
		case TURRET:
			return type.attackPower / (health * type.attackDelay);
		case VIPER:
			return 10 / (health);
		default:
			return type.attackPower / (health * type.attackDelay);
		}
	}
}