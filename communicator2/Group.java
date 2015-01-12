package communicator2;


import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Group {

	SoldierStack soldiers;
	int currentCommand;
	public Group(){
		soldiers = new SoldierStack();
		currentCommand = StaticVariables.COMMAND_NOT_RECEIVED_YET;
	}
	
	public void add(RobotController rc, RobotRepresentation add) throws GameActionException{
		soldiers.push(add);
		if(currentCommand != StaticVariables.COMMAND_NOT_RECEIVED_YET){
			rc.broadcast(StaticVariables.ROBOT_COMMAND_CHANNEL_START+add.iD, currentCommand);
		}
	}
	public int getSize(){
		return soldiers.size;
	}
	public void assignToOtherGroup(RobotController rc, Group others, int minimumGroup1, int minimumGroup2) throws GameActionException{
		int possible = soldiers.size-minimumGroup1;
		int wanted = Math.min(possible, minimumGroup2-others.getSize());
		for(int i = 0; i < wanted; i ++){
			others.add(rc, soldiers.pop());
		}
	}
	public void sendCommandToGroup(MapLocation loc, int command, RobotController rc) throws GameActionException{
		int location = loc!=null?StaticFunctions.locToInt(loc):0;
		int broadcast = location + (command*10000);
		if(broadcast != currentCommand){
			soldiers.deliverCommand(rc, broadcast);
			currentCommand = broadcast;
		}
	}
	private class SoldierStack{
		Node start;
		int size;
		public SoldierStack(){
			start = null;
			size = 0;
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
			System.out.println(command);
			Node current = start;
			rc.broadcast(StaticVariables.ROBOT_COMMAND_CHANNEL_START+current.curr.iD, command);
			while((current = current.prev) != null){
				rc.broadcast(StaticVariables.ROBOT_COMMAND_CHANNEL_START+current.curr.iD, command);		
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
	
}
