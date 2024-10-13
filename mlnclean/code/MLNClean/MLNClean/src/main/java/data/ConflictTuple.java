package data;

import java.util.Arrays;

public class ConflictTuple extends Tuple{
	
	public int conflictIDs[] = null;	//记录出现冲突的属性所在列的ID
	
	public int domainID = -1;	////记录出现冲突的属性所在的Domain
	
	public ConflictTuple(Tuple t){
		this.AttributeIndex = t.AttributeIndex;
		this.AttributeNames = t.AttributeNames;
		this.tupleID = t.tupleID;
		this.probablity = t.probablity;
		this.reason = t.reason;
		this.reasonAttributeIndex = t.reasonAttributeIndex;
		this.result = t.result;
		this.resultAttributeIndex = t.resultAttributeIndex;
		this.TupleContext = t.TupleContext;
	}
	
	public void setConflictIDs(int[] IDs){
		conflictIDs = Arrays.copyOf(IDs, IDs.length);
	}
}
