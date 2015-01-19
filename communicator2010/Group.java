package communicator2010;


import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
/**
 * Contains the soldiers of one group. The soldiers are stored in a stack like structure.
 * Soldiers can freely be distributed between different groups. If a soldier dies he will be
 * automatically deleted from the corresponding group to ensure consistency.
 * A command can be send to all soldiers and new added soldiers will automatically receive the latest
 * command.
 * Soldiers that recently spawned are found in groups[0] of the HQBehavior class.
 * 
 * 
 * @author Alexander Bartl
 *
 */
public class Group {

	SoldierStack soldiers;
	int currentCommand;
	int iD;
	public Group(int iD){
		this.iD = iD;
		soldiers = new SoldierStack();
		currentCommand = StaticVariables.COMMAND_NOT_RECEIVED_YET;
	}
	
	public void add(RobotController rc, RobotRepresentation add) throws GameActionException{
		soldiers.push(add);
		add.group = this;
//		System.out.println(add.iD+ " added to " + this.iD);
		if(currentCommand != StaticVariables.COMMAND_NOT_RECEIVED_YET){
			rc.broadcast(StaticVariables.ROBOT_COMMAND_CHANNEL_START+add.iD, currentCommand);
		}
	}
	public int getSize(){
		return soldiers.size;
	}
	/**
	 * Moves soldiers from one group to the other. The desired size for
	 * both groups can be adjusted by minimumGroup1 and minimumGroup2.
	 * For instance minimumGroup1=0 and minimumGroup2=20 will make sure that all soldiers
	 * from group1 are assigned to group2. MinimumGroup1=0 and minimumGroup2=1 will assure that
	 * at least one soldier is assigned to group2 iff the size of group2 is not already one.
	 * 
	 * 
	 * @param rc
	 * @param others is the group the soldiers get assigned to
	 * @param minimumGroup1 the target size of this group
	 * @param minimumGroup2 the target size of destination group
	 * @throws GameActionException
	 */
	public void assignToOtherGroup(RobotController rc, Group others, int minimumGroup1, int minimumGroup2) throws GameActionException{
		int possible = soldiers.size-minimumGroup1;
		int wanted = Math.min(possible, minimumGroup2-others.getSize());
//		System.out.println("tryToAdd");
		for(int i = 0; i < wanted; i ++){
			others.add(rc, soldiers.pop());
		}
	}
	/**
	 * Sends a command to all soldiers in the group. Doesn't send a command if the soldiers
	 * have already received the very same command. Soldiers that are added afterwards will receive the latest
	 * command automatically to ensure consistency.
	 * 
	 * 
	 * @param loc is the location regarded to the command
	 * @param command the command to be send
	 * @param rc
	 * @throws GameActionException
	 */
	public void sendCommandToGroup(MapLocation loc, int command, RobotController rc) throws GameActionException{
		int location = loc!=null?StaticFunctions.locToInt(loc):0;
		int broadcast = location + (command*100000) + (iD *10000);
		if(broadcast != currentCommand){
			soldiers.deliverCommand(rc, broadcast);
			currentCommand = broadcast;
		}
	}
	public RobotRepresentation[] getRobots(){
		RobotRepresentation[] result = new RobotRepresentation[soldiers.size];
		int num = 0;
		if(soldiers.size == 0){
			return null;
		}
		Node current = soldiers.start;
		
		result[num] = current.curr;
		num++;
		while((current = current.prev) != null){
			result[num] = current.curr;
			num++;
		}
		return result;
	}
	public int[] getIDs(){
		int[] result = new int[soldiers.size];
		int num = 0;
		if(soldiers.size == 0){
			return null;
		}
		Node current = soldiers.start;
		
		result[num] = current.curr.iD;
		num++;
		while((current = current.prev) != null){
			result[num] = current.curr.iD;
			num++;
		}
		return result;
	}
	public void notifyDeath(int iD){
		soldiers.delete(iD);
	}
	public boolean checkConsistency(){
		return soldiers.checkConsistency();
	}
	private class SoldierStack{
		Node start;
		int size;
		public SoldierStack(){
			start = null;
			size = 0;
		}
		public boolean checkConsistency(){
			int num = 0;
			if(start == null){
				return num == size;
			}
			num++;
			Node current = start;
			while((current = current.prev) != null){
				num++;
			}
			return num == size;
		}
		public void delete(int iD){
			if(iD == start.curr.iD){
				pop();
				return;
			}
			Node current = start.prev;
			Node prev = start;
			while(current.curr.iD != iD){
				current = current.prev;
				prev = prev.prev;
			}
			prev.prev = current.prev;
			size--;
		}
		public void push(RobotRepresentation push){
			size++;
			if(start == null){
				start = new Node(push,null);
				return;
			}
			start = new Node(push,start);
		}
		public RobotRepresentation pop(){
			if(start == null){
				return null;
			}
			RobotRepresentation result = start.curr;
			start = start.prev;
			size--;
			return result;
		}
		
		public void deliverCommand(RobotController rc, int command) throws GameActionException{
			if(start == null){
				return;
			}
			Node current = start;
			rc.broadcast(StaticVariables.ROBOT_COMMAND_CHANNEL_START+current.curr.iD, command);
			while((current = current.prev) != null){
				rc.broadcast(StaticVariables.ROBOT_COMMAND_CHANNEL_START+current.curr.iD, command);		
			}
		}
		
	}
	private class Node{
		public Node(RobotRepresentation curr, Node prev) {
			super();
			this.curr = curr;
			this.prev = prev;
		}
		RobotRepresentation curr;
		Node prev;
		
	}
	
}
