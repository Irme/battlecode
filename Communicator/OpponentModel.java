package Communicator;

import battlecode.common.GameObject;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class OpponentModel {

	/**
	 * A variable describing the opponent.
	 * 1: Aggressive, 0 defensive.
	 * On start of the game it is set 0.5
	 */
	private static double opponent = 0.5;

	double health = 100;
	double newhealth;
	double phealth = 100;
	double pnewhealth;


	public void scoutingData(RobotController rc){
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		//System.out.println(enemyRobots.length);
		if(enemyRobots.length == 0){
			//Do nothing? Yield?
		} else {
			newhealth = rc.getHealth();
			if(newhealth < health){
				setOpponent(opponent+ 0.001);
			}
			else if((newhealth <= health) ){
				setOpponent(opponent - 0.001);
			}


			health = newhealth;
			//System.out.println(getOpponent());
		}
	}

	public void pastureData(RobotController rc){
		pnewhealth = rc.getHealth();
		if(pnewhealth < phealth){
			setOpponent(opponent+ 0.01);
		}
		//TODO: Implement something if health didn't decrease and enemy seen.
		else if(pnewhealth <= health){
			setOpponent(opponent - 0.01);
		}


		phealth = pnewhealth;

	}


	public double getOpponent() {
		return opponent;
	}

	public void setOpponent(double opponent) {
		OpponentModel.opponent = opponent;
		//System.out.println(OpponentModel.opponent);
	}



}
