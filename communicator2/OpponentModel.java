package communicator2;

import battlecode.common.Robot;
import battlecode.common.RobotController;

public class OpponentModel {

	/**
	 * A variable describing the opponent.
	 * 1: Aggressive, 0 defensive.
	 * On start of the game it is set 0.5
	 */

	double health = 100;
	double newhealth;
	double phealth = 100;
	double pnewhealth;


	public void scoutingData(RobotController rc){
		if(HQBehavior.opponent < 1 && HQBehavior.opponent > 0){
			Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
			if(enemyRobots.length != 0){
				newhealth = rc.getHealth();
				if(newhealth < health){
					HQBehavior.opponent = (HQBehavior.opponent+ 0.005);
				}
				else if((newhealth >= health) ){
					HQBehavior.opponent = (HQBehavior.opponent - 0.005);
				}


				health = newhealth;
				System.out.println("Opponent " + HQBehavior.opponent);

				//System.out.println(getOpponent());

			}
		}
	}

	public void pastureData(RobotController rc){

		pnewhealth = rc.getHealth();
		if(pnewhealth < phealth){
			HQBehavior.opponent = ( HQBehavior.opponent+ 0.05);
		}
		//TODO: Implement something if health didn't decrease and enemy seen.
		else if(pnewhealth >= phealth){
			HQBehavior.opponent = (HQBehavior.opponent - 0.05);
		}
		phealth = pnewhealth;
	}






}
