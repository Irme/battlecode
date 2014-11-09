package communicator;

public class StaticVariables {

	public static final int ROBOT_ID_CHANNEL = 10500;
	public static final int ROBOT_COMMAND_CHANNEL_START = 11000;
	public static final int ROBOT_FEEDBACK_CHANNEL_START = 12000;
	
	public static final int ROBOT_FLEEING_HEALTH_THRESHOLD = 30;
	public static final int ROBOT_RECOVERING_HEALTH_THRESHOLD = 80;
	
	public static final int COMMAND_NOT_RECEIVED_YET = 0;
	public static final int COMMAND_BUILD_PASTR = 1;
	public static final int COMMAND_ASSEMBLE_AT_LOCATION = 2;
	public static final int COMMAND_ATTACK_LOCATION = 3;
	
	public static final int MAX_ROBOTS_SPAWN = 20;
	
	public static final int HQ_FIELD_WIDTH = 7;
	public static final int HQ_FIELD_HEIGHT = 7;
	
}
