package crowdPartialOrder;

import java.util.ArrayList;

public class PartialNode implements Comparable<PartialNode> {
	int aid; // attribute index
	int leaf; //作为是否为leaf的flag, if true=1, else=0
	int searchTag = 0; //如果该点是用于构建tree,则=0，若是用于查找则为1. 默认为0
	ArrayList<Group> groups;

	public PartialNode() {
	}

	public PartialNode(int aid, ArrayList<Group> groups, int leaf) {
		this.aid = aid;
		this.groups = new ArrayList<Group>();
		this.groups.addAll(groups);
		this.leaf = leaf;
	}
	
	public int compare(Group other) {
		//如果是同一个node,则返回2
		if(this.groups.get(0).gid==other.gid && this.leaf==0){
			return 2;
		}
		float ans;
		
		if(this.searchTag==0 && this.leaf==1){
			ans = this.groups.get(0).range[this.aid].down - Parameters.minValue - other.range[this.aid].up;
//			System.out.println(""+(this.group.range[this.aid].down - Parameters.minValue)+"-"+this.group.range[this.aid].up+"="+ans);
		}else{
			ans = this.groups.get(0).range[this.aid].down - other.range[this.aid].up;
//			System.out.println(""+this.group.range[this.aid].down+"-"+this.group.range[this.aid].up+"="+ans);
		}
		
		if (ans > 0)
			return 1;
		else if (ans == 0)
			return 0;
		else
			return -1;
	}
	
	public int compareTo(PartialNode other) {
		//如果是同一个node,则返回2
		if(this==other)return 2;
		if(this.groups.size()==other.groups.size()){
			int count = 0;
			for(int i=0;i<this.groups.size();i++){
				if(this.groups.get(i).gid==other.groups.get(i).gid && this.leaf==other.leaf){
					count++;
				}
			}
			if(count == this.groups.size())
				return 2;
		}
		
		float ans = this.groups.get(0).range[this.aid].down - other.groups.get(0).range[this.aid].up;
		
		if(ans==0){
			float ans1 = this.groups.get(0).range[this.aid].up - this.groups.get(0).range[this.aid].down;
			//ans1==0代表这两个点的取值范围完全相等
			if(ans1==0){
				if(this.leaf==other.leaf){
					return 0;
				}else if(this.leaf==1){
					return -1;
				}else{
					return 1;
				}
			}else return 1;
		}else if(ans>0){
			return 1;
		}else{
			float ans2 = this.groups.get(0).range[this.aid].up - other.groups.get(0).range[this.aid].up;
			if(ans2<0){
				return -1;
			}else if(ans2>0){
				return 1;
			}else{//ans2==0
				float ans3 = this.groups.get(0).range[this.aid].down - other.groups.get(0).range[this.aid].down;
				if(ans3<0){
					return -1;
				}else if(ans3>0){
					return 1;
				}else{
					if(this.leaf==other.leaf){
						return 0;
					}else if(this.leaf==1){
						return -1;
					}else{
						return 1;
					}
				}
			}
			
		}
		
		//=================
		/*if(this.searchTag==0 && this.leaf==1 && other.leaf!=1){
			ans = this.groups.get(0).range[this.aid].down - Parameters.minValue - other.groups.get(0).range[this.aid].up;
		}else{
			ans = this.groups.get(0).range[this.aid].down - other.groups.get(0).range[this.aid].up;
		}
		
		if (ans > 0)
			return 1;
		else if (ans == 0){
			float ans1 = this.groups.get(0).range[this.aid].up - this.groups.get(0).range[this.aid].down;
			if(ans1==0){
				return 0;
			}else return -1;
		}
		else
			return -1;*/
	}
	
	public int compareToTest(PartialNode other) {
		//如果是同一个node,则返回2
		if(this==other)return 2;
		
		if(this.groups.size()==other.groups.size()){
			int count = 0;
			for(int i=0;i<this.groups.size();i++){
				if(this.groups.get(i).gid==other.groups.get(i).gid && this.leaf==other.leaf){
					count++;
				}
			}
			if(count == this.groups.size()){
				return 2;
			}
		}
		float ans = this.groups.get(0).range[this.aid].down - other.groups.get(0).range[this.aid].up;
		
		if(ans==0){
			float ans1 = this.groups.get(0).range[this.aid].up - this.groups.get(0).range[this.aid].down;
			//ans1==0代表这两个点的取值范围完全相等
			if(ans1==0){
				if(this.leaf==other.leaf){
					return 0;
				}else if(this.leaf==1){
					return -1;
				}else{
					return 1;
				}
			}else return 1;
		}else if(ans>0){
			return 1;
		}else{
			return -1;
		}
	}
}
