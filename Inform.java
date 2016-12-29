package winterbreak;

import battlecode.common.*;
import java.util.*;
//universal information
//possible method instead of just relying on communication?
public class Inform
{
	public static Team us;
	public static Team them;
	public static HashMap<Integer, Integer> enemyArchonLoc;
	public static HashMap<Integer, Integer> allyArchonLoc;
	//call for help system?
	public static PriorityQueue<String> callForHelpLoc; //letter of priority + loc
	//strategy booleans
	//ex. boolean retreat = false; last ditch effort archon red alert?
	//boolean antiEnemyStrat = false; general - if enough info gathered to identify enemy strat, start antistrat
}