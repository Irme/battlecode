package communicator2;

public class StaticVariables {

	/** The channel on which the the ID is delivered to the soldiers. **/
	public static final int ROBOT_ID_CHANNEL = 10500;
	/** The starting index of the command channels which are indexed by the unique ID of each soldier.
	 *  So the command-channel-index of a soldier sol is ROBOT_COMMAND_CHANNEL_START+(sol.iD-1);
	**/
	public static final int ROBOT_COMMAND_CHANNEL_START = 11000;
	/** The starting index of the feedback channels which are indexed by the unique ID of each soldier.
	 *  So the feedback-channel-index of a soldier sol is ROBOT_FEEDBACK_CHANNEL_START+(sol.iD-1);**/
	public static final int ROBOT_FEEDBACK_CHANNEL_START = 12000;
	
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
	
	/** this number indicates how many soldiers have to be considered, also indicates the maximum amount of spawn soldiers **/
	public static final int MAX_ROBOTS_SPAWN = 20;
	
	/** deprecated, will be changed in the near future**/
	public static final int HQ_FIELD_WIDTH = 7;
	/** deprecated, will be changed in the near future**/
	public static final int HQ_FIELD_HEIGHT = 7;
	
	
	public static final double VERSION_NUMBER = 1.005d;
}
