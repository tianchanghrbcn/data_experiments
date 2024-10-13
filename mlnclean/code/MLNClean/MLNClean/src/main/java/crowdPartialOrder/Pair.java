package crowdPartialOrder;

public class Pair {
	public int id1, id2;
	public float[] similar; // 每个属性上都有对应的相似度
	// float allsimilar;
	public String[] t1, t2;
	// Tuple t1, t2;

	public void setSimilar(int k, float similar) {
		this.similar[k] = similar;
	}

	public Pair(String[] t1, String[] t2, int attribute_num) {
		this.t1 = t1;
		this.t2 = t2;
		this.similar = new float[attribute_num];
	}
	
	public Pair(int id1, int id2, int attribute_num) {
		this.id1 = id1;
		this.id2 = id2;
		this.similar = new float[attribute_num];
	}

	public Pair(int id1, int id2, String[] t1, String[] t2, int attribute_num) {
		this.id1 = id1;
		this.id2 = id2;
		this.t1 = t1;
		this.t2 = t2;
		this.similar = new float[attribute_num];
	}

}
