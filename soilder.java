package winterbreak;

import battlecode.common.*;
import java.util.Random;

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
		Direction[] dirs={Direction.NORTH,Direction.NORTH_EAST,Direction.EAST,Direction.SOUTH_EAST,Direction.SOUTH,Direction.SOUTH_WEST,Direction.WEST,Direction.NORTH_WEST};
		Random rng=new Random(rc.getID());
		MapLocation dest=rc.getLocation().add(dirs[rng.nextInt(8)],10);
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
			if (rc.isCoreReady()) {
				if (zombies!=0) {
					Direction[] possMoves=new Direction[8];
					int poss=0;
					for (int i=7; i>=0; i--) {
						if (rc.canMove(dirs[i])) {
							possMoves[poss++]=dirs[i];
						}
					}
					
					int minTaken=0;
					boolean canHit=false;
					double minClosest=9999;
					MapLocation currloc=rc.getLocation();
					for (int k=zombies-1; k>=0; k--) {
						int dist=currloc.distanceSquaredTo(zomb[k].location);
						canHit=canHit||dist<=13;
						minClosest=Math.min(minClosest,dist);
						if (dist<=zomb[k].type.attackRadiusSquared) {
							minTaken+=zomb[k].attackPower;
						}
					}
					for (int k=enemies-1; k>=0; k--) {
						int dist=currloc.distanceSquaredTo(enemy[k].location);
						canHit=canHit||dist<=13;
						minClosest=Math.min(minClosest,dist);
						if (dist<=enemy[k].type.attackRadiusSquared) {
							minTaken+=enemy[k].attackPower;
						}
					}
					Direction best=Direction.NONE;
					for (int i=poss-1; i>=0; i--) {
						boolean hitSomething=false;
						int damage=0;
						double closest=999;
						MapLocation nloc=rc.getLocation().add(possMoves[i]);
						for (int k=zombies-1; k>=0; k--) {
							int dist=nloc.distanceSquaredTo(zomb[k].location);
							hitSomething=hitSomething||dist<=13;
							closest=Math.min(closest,dist);
							if (dist<=zomb[k].type.attackRadiusSquared) {
								damage+=zomb[k].attackPower;
							}
						}
						for (int k=enemies-1; k>=0; k--) {
							int dist=nloc.distanceSquaredTo(enemy[k].location);
							hitSomething=hitSomething||dist<=13;
							closest=Math.min(closest,dist);
							if (dist<=enemy[k].type.attackRadiusSquared) {
								damage+=enemy[k].attackPower;
							}
						}
						if (!possMoves[i].isDiagonal()) closest*=1.5;
						if ((hitSomething&&!canHit)||(damage<minTaken||(damage==minTaken&&closest<minClosest))) {
							canHit=hitSomething;
							minTaken=damage;
							minClosest=closest;
							best=possMoves[i];
						}
					}
					if (best!=Direction.NONE)
					rc.move(best);
				}
				//end of zombie and/or enemy move
				else if (enemies!=0) {
					Direction[] possMoves=new Direction[8];
					int poss=0;
					for (int i=7; i>=0; i--) {
						if (rc.canMove(dirs[i])) {
							possMoves[poss++]=dirs[i];
						}
					}
					
					int minTaken=0;
					boolean canHit=false;
					MapLocation currloc=rc.getLocation();
					for (int k=enemies-1; k>=0; k--) {
						int dist=currloc.distanceSquaredTo(enemy[k].location);
						canHit=canHit||dist<=13;
						if (dist<=enemy[k].type.attackRadiusSquared) {
							minTaken+=enemy[k].attackPower;
						}
					}
					Direction best=Direction.NONE;
					for (int i=poss-1; i>=0; i--) {
						boolean hitSomething=false;
						int damage=0;
						MapLocation nloc=rc.getLocation().add(possMoves[i]);
						for (int k=enemies-1; k>=0; k--) {
							int dist=nloc.distanceSquaredTo(enemy[k].location);
							hitSomething=hitSomething||dist<=13;
							if (dist<=enemy[k].type.attackRadiusSquared) {
								damage+=enemy[k].attackPower;
							}
						}
						if (damage<minTaken||(damage==minTaken&&hitSomething&&!canHit)) {
							canHit=hitSomething;
							minTaken=damage;
							best=possMoves[i];
						}
					}
					if (best!=Direction.NONE)
					rc.move(best);
				}
				//end of enemy only code
				else if (dens!=0){
					double minHP=9999;
					RobotInfo target=null;
					for (int i=dens-1; i>=0; i--) {
						if (den[i].health<minHP) {
							target=den[i];
							minHP=target.health;
						}
					}
					if (rc.getLocation().distanceSquaredTo(target.location)>13) {
						tryToMove(rc.getLocation().directionTo(target.location),rc);
					}
				//end of den attack code
				} else {
					Direction toDest=rc.getLocation().directionTo(dest);
					if (rc.getLocation().distanceSquaredTo(dest)<=10 ||!rc.onTheMap(rc.getLocation().add(toDest))) {
						dest=rc.getLocation().add(dirs[rng.nextInt(8)],10);
					}
					tryToMove(toDest,rc);
				}
				//end of no hostile code
			}
			//end of move
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
			//end of attack
			Clock.yield();
		}
	}
	static void tryToMove(Direction d, RobotController rc) throws Exception {
		if (rc.canMove(d)) {
			rc.move(d);
		} else if (rc.canMove(d.rotateLeft())) {
			rc.move(d.rotateLeft());
		} else if (rc.canMove(d.rotateRight())) {
			rc.move(d.rotateRight());
		} else {
			MapLocation lm=rc.getLocation().add(d.rotateLeft());
			MapLocation rm=rc.getLocation().add(d.rotateRight());
			MapLocation fm=rc.getLocation().add(d);
			boolean lvalid=rc.isLocationOccupied(lm)||!rc.onTheMap(lm);
			boolean rvalid=rc.isLocationOccupied(rm)||!rc.onTheMap(rm);
			boolean fvalid=rc.isLocationOccupied(fm)||!rc.onTheMap(fm);
			double lrub=rc.senseRubble(lm);
			double rrub=rc.senseRubble(rm);
			double frub=rc.senseRubble(fm);
			if (!fvalid&&(lvalid||lrub>=frub)&&(rvalid||rrub>=frub)) {
				rc.clearRubble(d);
			} else if (!lvalid&&(rvalid||rrub>=lrub)) {
				rc.clearRubble(d.rotateLeft());
			} else if (!rvalid) {
				rc.clearRubble(d.rotateRight());
			}
		}
	}
}