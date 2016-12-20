package winterbreak;

import battlecode.common.*;

public class soilder {
	/*Soldier priorities
	 * if canAttack:
	 *    Shoot the closest thing that can attack
	 * if enemies and zombies: 
	 *    stay out of range of everything
	 * if zombies:
	 *    move out of range of zombies
	 * if enemies:
	 *    move in as long as they can't shoot you
	 *    if 3x move visible friends than enemies blip to rush
	 *    if you recieve blip rush
	 * if nothing:
	 *    id%2==0: 
	 *       nav to scout location
	 *    id%2==1:
	 *       stay near archons
	 *    
	 */
	static void run(RobotController rc)throws Exception {
		while (true) {
			RobotInfo[] info=rc.senseNearbyRobots();
			RobotInfo[] friend=new RobotInfo[info.length];
			RobotInfo[] enemy=new RobotInfo[info.length];
			RobotInfo[] zomb=new RobotInfo[info.length];
			RobotInfo[] den=new RobotInfo[info.length];
			RobotInfo[] neut=new RobotInfo[info.length];
			int enemies=0;
			int friends=0;
			int zombies=0;
			int dens=0;
			int neutrals=0; 
			for (int i=info.length-1;i>=0; i--) {
				switch (info[i].team) {
				case A:
					if (rc.getTeam()==Team.A) {
						friend[friends++]=info[i];
					} else {
						enemy[enemies++]=info[i];
					}
					break;
				case B:
					if (rc.getTeam()==Team.B) {
						friend[friends++]=info[i];
					} else {
						enemy[enemies++]=info[i];
					}
					break;
				case NEUTRAL:
					neut[neutrals++]=info[i];
					break;
				case ZOMBIE:
					if (info[i].type==RobotType.ZOMBIEDEN) {
						den[dens++]=info[i];
					} else {
						zomb[zombies++]=info[i];
					}
				}
			}
			if (rc.isWeaponReady()) {
				int maxPri=-10000;
				/*Priority System:
			CAN KILL +=5000
			0:unit who will die of infection
			1:den
			10-1200 can't hit us, 1200-remaining health
			1300-1600 can hit us, 1500-remaining health
			unit addon: Turret 40, soldier 10, viper 40, scout 2, guard 2, archon 0
			For zombies:
            can hit us:
			Ranged:1800-health
			Fast:1740-health
			Normal:1600-health
			Big:1700-health
			Can't:
			Big:1702-health-5*distance
			Ranged:1200-health-5*distance
			Fast: 1000-health-5*distance
			Normal: 800-health-10*distance
				 */
				RobotInfo target=null;
				for (int i=zombies-1; i>=0; i--) {
					RobotInfo rinf=zomb[i];
					int dist=rinf.location.distanceSquaredTo(rc.getLocation());
					if (dist<=13) {
						int rpri=0;
						switch (rinf.type) {
						case BIGZOMBIE:
							if (dist<=2) {
								rpri=(int)(1700-rinf.health);
							} else {
								rpri=(int)(1702-rinf.health-5*dist);
							}
							break;
						case FASTZOMBIE:
							if (dist<=2) {
								rpri=(int)(1740-rinf.health);
							} else {
								rpri=(int)(1000-rinf.health-5*dist);
							}
							break;
						case RANGEDZOMBIE:
							if (dist<=13) {
								rpri=(int)(1800-rinf.health);
							} else {
								rpri=(int)(1200-rinf.health-5*dist);
							}
							break;
						case STANDARDZOMBIE:
							if (dist<=2) {
								rpri=(int)(1600-rinf.health);
							} else {
								rpri=(int)(800-rinf.health-10*dist);
							}
						}
						if (rinf.health<=4) rpri+=5000;
						if (rpri>maxPri) {
							maxPri=rpri;
							target=rinf;
						}
					}
				}
				for (int i=enemies-1; i>=0; i--) {
					RobotInfo rinf=enemy[i];
					int rpri=0;
					int dist=rinf.location.distanceSquaredTo(rc.getLocation());
					if (dist<=13) {
						int sumhealth=(int)(rinf.health-2*rinf.viperInfectedTurns);
						switch (rinf.type) {
						case SOLDIER:
							if (dist<=13) {
								rpri=(1510-sumhealth);
							} else {
								rpri=(1210-sumhealth);
							}
							break;
						case GUARD:
							if (dist<=13) {
								rpri=(1500-sumhealth);
							} else {
								rpri=(1200-sumhealth);
							}
							break;
						case TURRET:
							if (dist>=10) {
								rpri=(1540-sumhealth);
							} else {
								rpri=(1440-sumhealth);
							}
							break;
						case ARCHON:
							rpri=(1100-sumhealth);
							break;
						case TTM:
							rpri=(1400-sumhealth);
							break;
						case VIPER:
							if (dist<=20) {
								rpri=(1540-sumhealth);
							} else {
								rpri=(1240-sumhealth);
							}
							break;
						case SCOUT:
							rpri=(1150-sumhealth);
						}
						if (sumhealth<=0) rpri=0;
						else if (sumhealth<=4) rpri+=5000;
						if (rpri>maxPri) {
							maxPri=rpri;
							target=rinf;
						}
					}
				}
				for (int i=dens-1; i>=0; i--) {
					RobotInfo rinf=den[i];
					if (rinf.location.distanceSquaredTo(rc.getLocation())<=13) {
						if (rinf.health<=4) {
							target=rinf;
							maxPri=10000;
						} else {
							if (maxPri<-rinf.health) {
								maxPri=-(int)rinf.health;
								target=rinf;
							}
						}
					}
				}
				if (target!=null) {
					rc.attackLocation(target.location);
				}
			}

		}
	}

}