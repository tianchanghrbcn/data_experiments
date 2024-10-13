package data;

import java.util.ArrayList;

/**
 * Created by gcc on 17-9-27.
 */
public class GroundRule {
    public String weight = "";
    public int number = 0;//the number of this rule
    public ArrayList<Integer> dataList = new ArrayList<>();

    public GroundRule(String weight, int number){
        this.weight = weight;
        this.number = number;
    }
}
