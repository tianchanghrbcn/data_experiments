package data;

public class ProbAttribute {
	
	private String name = null;		//属性名
	private String value = null;	//属性取值
	private double prob = 0.0;		//该属性值的概率
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public double getProb() {
		return prob;
	}
	
	public void setProb(double prob) {
		this.prob = prob;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
