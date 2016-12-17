package winterbreak;

import battlecode.common.*;

import java.util.Random;

public class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws Exception{
        switch (rc.getType()) {
        case ARCHON:
        	archon.run(rc);
        	break;
        case SCOUT:
        	scout.run(rc);
        	break;
        case SOLDIER:
        	soilder.run(rc);
        	break;
        case TURRET:
        	turret.run(rc);
        	break;
        case VIPER:
        	viper.run(rc);
        	break;
        case GUARD:
        	guard.run(rc);
        	break;
        
        }
    }
}
