package data;

public class Tuple {
	
	public String[] reason = null;	//记录Tuple的reason value
	public String[] result = null;	//记录Tuple的result value
	public int tupleID = -1; //index from 0 to dataset.size;
	
	public int[] resultAttributeIndex = null;
	public int[] reasonAttributeIndex = null;
	
	
	public String[] AttributeNames = null;	//记录Tuple的所有属性名
	
	public String[] TupleContext = null;
	public int[] AttributeIndex = null;
	
	public double probablity = -1;
	
	public Tuple(){}
	
	public Tuple(ConflictTuple ct){
		this.reason = ct.reason;
		this.result = ct.result;
		this.tupleID = ct.tupleID;
		this.resultAttributeIndex = ct.resultAttributeIndex;
		this.reasonAttributeIndex = ct.reasonAttributeIndex;
		this.AttributeNames = ct.AttributeNames;
		this.TupleContext = ct.TupleContext;
		this.AttributeIndex = ct.AttributeIndex;
		this.probablity = ct.probablity;
	}
	
	public void setContext(String[] TupleContext){
		this.TupleContext = TupleContext;
	}
	
	public void setReason(String[] reason){
		this.reason = new String[reason.length];
		for(int i=0;i<reason.length;i++){
			this.reason[i] = reason[i];
		}
	}
	public void setResult(String[] result){
		this.result = new String[result.length];
		for(int i=0;i<result.length;i++){
			this.result[i] = result[i];
		}
	}
	
	public void init(String tupleLine,String splitString,int index){//Init the tuple
		this.TupleContext = tupleLine.split(splitString);
		this.tupleID = index;
		//System.out.println("Tuple.length = "+Tuple.length);
	}
	
	public String[] getAttributeNames(){
		if(null!=AttributeNames){
			return AttributeNames;
		}else
			System.out.println("Error: Attribute Names is empty.");
			return null;
	}
	
	public void setAttributeNames(String[] attributeNames){
		int length = attributeNames.length;
		this.AttributeNames = new String[length];
		for(int i=0;i<length;i++){
			this.AttributeNames[i] = attributeNames[i];
		}
	}
	
	public String[] getContext(){
		if(null!=TupleContext){
			return TupleContext;
		}else
			System.out.println("Error: tuple context is empty.");
			return null;
	}
	
	public int getTupleID(){
		return tupleID;
	}
	
	public int[] getReasonAttributeIndex() {
		return reasonAttributeIndex;
	}

	public void setReasonAttributeIndex(int[] attributeIndex) {
		int length = attributeIndex.length;
		reasonAttributeIndex = new int[length];
		for(int i=0;i<length;i++){
			reasonAttributeIndex[i] = attributeIndex[i];
		}
	}
	
	public int[] getResultAttributeIndex() {
		return resultAttributeIndex;
	}

	public void setResultAttributeIndex(int[] attributeIndex) {
		int length = attributeIndex.length;
		resultAttributeIndex = new int[length];
		for(int i=0;i<length;i++){
			resultAttributeIndex[i] = attributeIndex[i];
		}
	}

	public int[] getAttributeIndex() {
		return AttributeIndex;
	}

	public void setAttributeIndex(int[] attributeIndex) {
		AttributeIndex = attributeIndex;
	}

//	public double[] getProbablity() {
//		return probablity;
//	}
//
//	public void setProbablity(double[] probablity) {
//		this.probablity = probablity;
//	}
}
