package data;

import java.util.ArrayList;

/**
 * Created by gcc on 18-4-3.
 */
public class ConvertInfo {
    public ArrayList<Integer> idlist = new ArrayList<>();
    public String content = "";

    public ConvertInfo(ArrayList<Integer> idlist, String content){
        this.idlist = idlist;
        this.content = content;
    }
}
