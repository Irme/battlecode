package communicator2;

import java.util.LinkedList;

public class OpenList {
	LinkedList<Node> bestNodes;
	Node[][] openListMap;
	
	public OpenList(int width, int height){
		openListMap = new Node[width][height];
		bestNodes = new LinkedList<Node>();
	}
	
	public boolean isEmpty(){
		return bestNodes.isEmpty();
	}
	
	public void insert(Node node){
		int x = node.loc.x;
		int y = node.loc.y;
		
		openListMap[x][y] = node;
		insertIntoBestNodes(node);
	}
	
	public Node getBest(){
		return bestNodes.get(0);
	}
	
	public Node getAndRemoveBest(){
		Node best = bestNodes.get(0);
		bestNodes.remove(0);
		return best;
	}
	
	public void replaceIfBetter(Node node){
		int x = node.loc.x;
		int y = node.loc.y;
		if(openListMap[x][y] == null){
			insert(node);
			return;
		}
		if(openListMap[x][y].f > node.f){
			openListMap[x][y] = node;
			findAndDeleteNode(node);
			insertIntoBestNodes(node);
		}else{
			// System.out.println("There already is a better node in openlist");
		}
	}
	
	private void insertIntoBestNodes(Node node){
		boolean inserted = false;
		for(int i = 0; i < bestNodes.size()-1; ++i){
			if(bestNodes.get(i).f > node.f){
				bestNodes.add(i, node);
				inserted = true;
				break;
			}
		}
		if(!inserted){
			bestNodes.add(node);
		}
	}
	
	private void findAndDeleteNode(Node node){
		for(int i = 0; i < bestNodes.size(); ++i){
			if(node == bestNodes.get(i)){
				bestNodes.remove(i);
			}
		}
	}
}
