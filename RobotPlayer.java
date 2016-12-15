package winterbreak;

import battlecode.common.*;

import java.util.Random;

public class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) {
        switch (rc.getType()) {
        case RobotType.ARCHON:
        	archon.run(rc);
        	break;
        case RobotType.SCOUT:
        	scout.run(rc);
        	break;
        case RobotType.SOILDER:
        	soilder.run(rc);
        	break;
        case RobotType.TURRET:
        	turret.run(rc);
        	break;
        case RobotType.VIPER:
        	viper.run(rc);
        	break;
        case RobotType.GUARD:
        	guard.run(rc);
        	break;
        }
        }
    }
}
