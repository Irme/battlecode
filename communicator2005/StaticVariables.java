package communicator2005;

import battlecode.common.Direction;

public class StaticVariables {

	/** The channel on which the the ID is delivered to the soldiers. **/
	public static final int ROBOT_ID_CHANNEL = 10500;
	/** The starting index of the command channels which are indexed by the unique ID of each soldier.
	 *  So the command-channel-index of a soldier sol is ROBOT_COMMAND_CHANNEL_START+(sol.iD-1);**/
	public static final int ROBOT_COMMAND_CHANNEL_START = 11000;
	/** The starting index of the feedback channels which are indexed by the unique ID of each soldier.
	 *  So the feedback-channel-index of a soldier sol is ROBOT_FEEDBACK_CHANNEL_START+(sol.iD-1);**/
	public static final int ROBOT_FEEDBACK_CHANNEL_START = 12000;
	/** The starting index of the channel responsible for group commands. there are 10 channels per group
	 * for a maximum of 10 groups. **/
	public static final int ROBOT_GROUP_COMMAND_CHANNEL_START = 13000;
	
	public static final int ROBOT_PATH_DISTRIBUTION_CHANNEL_START = 20000;
	
	/** the group channel length per group*/
	public static final int GROUP_CHANNEL_LENGTH_PER_GROUP = 10;
	
	/** This variable is a threshold. If the health of a soldier drops below this value, the soldier goes into the fleeing mode.**/
	public static final int ROBOT_FLEEING_HEALTH_THRESHOLD = 20;
	/** This variable is a threshold. If the health of a soldier exceeds this value, the soldier pursues the task he had before fleeing.**/
	public static final int ROBOT_RECOVERING_HEALTH_THRESHOLD = 40;
	/** This variable is a threshold. If the distance of an enemy is smaller then this value, the scout starts to flee **/
	public static final int ROBOT_SCOUTING_DISTANCE_THRESHOLD = 16;
	
	
	/** integer representation for a non-existing-command**/
	public static final int COMMAND_NOT_RECEIVED_YET = 0;
	/** integer representation for a pasture-building-command**/
	public static final int COMMAND_BUILD_PASTR = 1;
	/** integer representation for a assembling-command**/
	public static final int COMMAND_ASSEMBLE_AT_LOCATION = 2;
	/** integer representation for a attacking-command**/
	public static final int COMMAND_ATTACK_LOCATION = 3;
	/** integer representation for a scouting-command**/
	public static final int COMMAND_SCOUT_LOCATION = 4;
	/** integer representation for a group-movement-command**/
	public static final int COMMAND_GROUP_MOVE_TO_LOCATION = 5;
	/** integer representation for a farming-command*/
	public static final int COMMAND_FARMING_MODE = 6;
	/** integer representation for a formation-move-command */
	public static final int COMMAND_FORMATION_MOVE = 7;
	
	public static final int COMMAND_BUILD_NOISE_TOWER = 8;
	
	/** this number indicates how many soldiers have to be considered, also indicates the maximum amount of spawn soldiers **/
	public static final int MAX_ROBOTS_SPAWN = 20;
	/** number of possible soldier groups */
	public static final int MAX_POSSIBLE_GROUPS = 10;
	
	public static final float PROBABILITY_GROUP_MOVE_RANDOM_MOVE = 0.1f;
	
	public static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	
	public static final double VERSION_NUMBER = 2.005d;
}
