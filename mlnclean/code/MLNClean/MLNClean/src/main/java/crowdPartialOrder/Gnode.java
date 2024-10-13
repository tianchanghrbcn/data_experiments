package crowdPartialOrder;

import java.util.HashSet;
import java.util.Vector;

public class Gnode {
	public Group group;
	public Vector<Integer> father;
	public Vector<Integer> children;
    private Integer nextID;
    
    public Integer getNextID() {
		return nextID;
	}

	public void setNextID(Integer nextID) {
		this.nextID = nextID;
	}

	public Gnode() {
		this.father = new Vector<Integer>();
		this.children = new Vector<Integer>();
		this.group = new Group();
	}
    
    public Gnode(Group group, HashSet<Integer> children) {
		this.father = new Vector<Integer>();
		this.children = new Vector<Integer>(children.size());
		this.children.addAll(children);
		this.group = group;
	}
}
