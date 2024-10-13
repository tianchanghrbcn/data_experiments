package data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class DataSet {
	
	private ArrayList<Tuple> tupleList = new ArrayList<Tuple>();
	
	//属性列，如果数据集中没有给出，则构造一个属性列：Attr1,Attr2,Attr3,...,AttrN
	private String[] header = null; 	
	
	public DataSet(){}
	
	public void init(String fileURL,String splitString,boolean ifHeader){//check if the data has header
		// read file content from file 读取文件内容
        FileReader reader;
		try {
			reader = new FileReader(fileURL);
			BufferedReader br = new BufferedReader(reader);
	        String str = null;
	        int index = 1; //tuple index
	        
	        if(ifHeader && (str = br.readLine()) != null){  //The data has header
	        	header=str.split(splitString);
	        	while((str = br.readLine()) != null) {
		        	Tuple t = new Tuple();
		        	t.init(str,splitString,index);//init the tuple,split with ","
		        	tupleList.add(t);
		        	index++;
		        }
	        }else{
	        	
	        	while((str = br.readLine()) != null) {
	        		Tuple t = new Tuple();
		        	t.init(str,splitString,index);//init the tuple,split with ","
		        	tupleList.add(t);
		        	index++;
		        }
	        	int length = tupleList.get(0).getContext().length;
	        	header = new String[length];
	        	for(int i=1;i<=length;i++){
	        		header[i-1]="Attr"+i;
	        	}
	        }
	        
	        br.close();
	        reader.close();
	        
	        print();//测试：打印
	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
		}
	}
	
	private void count(int i, String str, String[] attribute, int n, ArrayList<String[]> searchLattice){ //辅助函数：求n个数的组合情况
		if(n==0){
            String[] multiple = str.split(",");       
            searchLattice.add(multiple);
            return;
        }
        if(i==attribute.length){
            return;
        }
        count(i+1,str+attribute[i]+",",attribute,n-1,searchLattice);
        count(i+1,str,attribute,n,searchLattice);
    }
	
	//形成查询网络
	public ArrayList<String[]> getSearchLattice(){ 
		ArrayList<String[]> searchLattice = new ArrayList<String[]>();
		
		//单个属性的添加，比如A B C D (level k=1)
		String tmp = "";
		count(0,tmp,header,1,searchLattice);

		//多个属性的添加, 比如, AB AC AD BC BD CD ABC... (level k=2,...n)
		for(int i=0;i<searchLattice.size();i++){
			String t_str = "";
			count(0,t_str,header,i+2,searchLattice);
		}
		//print();
		return searchLattice;
	} 
	
	public ArrayList<Tuple> getTupleList(){
		return tupleList;
	}
	
	public String[] getHeader(){
		return header;
	}
	
	public int getAttributeIndex(String name , String[] header){//返回属性名对应的序号
		boolean key = false;
		int index=0;
		for(;index<header.length;index++){
			if(name.equals(header[index])){
				key = true;
				return index;
			}
		}
		if(!key){
			System.err.println("Error: 该属性名不存在！");
			return -1;
		}
		return index;
	}
	
	public void print(){ //print the whole dataSet
		
		if(null!=header){
			System.out.println("Header:  "+Arrays.toString(header));  
		}
		
		for(int i=0;i<tupleList.size();i++){
        	Tuple tuple = tupleList.get(i);
        	String[] tupleContext = tuple.getContext();
        	System.out.print("tuple "+tuple.getTupleID()+": ");
        	for(int j=1;j<=tupleContext.length;j++){
        		System.out.print(tupleContext[j-1]+",");
        	}
        	System.out.println();
        }
        System.out.println();
	}
}
