
public class Node {
	private String val;
	private static int ct = 0;
	private int count;
	
	public Node(String val){
		this.val = val;
		this.count = ct++;
	}
	
	public String getValue(){
		return val;
	}
	
	public boolean equals(Object otherObj){
		if(this == otherObj) return true;
		if(otherObj == null) return false;
		if(getClass() != otherObj.getClass()) return false;
		Node other = (Node)otherObj;
		return other.count == this.count;
	}
	
}
