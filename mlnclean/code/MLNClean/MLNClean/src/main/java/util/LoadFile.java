package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import crowdPartialOrder.Tuple;

public class LoadFile {
    public static String str_header = "";

    public static HashMap<Integer, String> readFile(String fileURL, String splitString) {
//		ArrayList<Tuple> tupleList = new ArrayList<Tuple>();
        HashMap<Integer, String> dataset_map = new HashMap<Integer, String>();

        File file = new File(fileURL);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String str = null;
            int index = 1;

            // 一次读入一行，直到读入null为文件结束  
            if ((str = reader.readLine()) != null) {
                str_header = str.substring(str.indexOf(splitString) + 1);
                /*Parameters.Anum = header.split(splitString).length;
            	System.out.println("Attribute Num = " + Parameters.Anum);*/
                while ((str = reader.readLine()) != null) {
                    // 显示行号
                    int tupleID = Integer.parseInt(str.substring(0, str.indexOf(splitString)));
                    String content = str.substring(str.indexOf(splitString) + 1);
                    String[] data = content.split(",");
                    for (int i = 0; i < data.length; i++) {
                        if (data[i] == null || data[i].isEmpty()) {
                            data[i] = "null";
                        }
                    }
                    String content_concat = String.join(",", data);
                    dataset_map.put(tupleID, content_concat);
                    index++;
                }
            }
//            header = str_header.split(splitString);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return dataset_map;
    }

    public static HashMap<String, ArrayList<Integer>> readFile_and_init(String fileURL, String splitString) {
//		ArrayList<Tuple> tupleList = new ArrayList<Tuple>();
        HashMap<String, ArrayList<Integer>> dataset_map = new HashMap<String, ArrayList<Integer>>();

        File file = new File(fileURL);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String str = null;
            int index = 1;
            String header = null;

            // 一次读入一行，直到读入null为文件结束  
            if ((str = reader.readLine()) != null) {
                header = str.substring(str.indexOf(",") + 1);
            	/*Parameters.Anum = header.split(splitString).length;
            	System.out.println("Attribute Num = " + Parameters.Anum);*/
                while ((str = reader.readLine()) != null) {
                    // 显示行号
                    Tuple t = new Tuple();
                    int tupleID = Integer.parseInt(str.substring(0, str.indexOf(",")));
                    String content = str.substring(str.indexOf(",") + 1);
                    if (dataset_map.containsKey(content)) {
                        dataset_map.get(content).add(tupleID);
                    } else {
                        ArrayList<Integer> tmpIDs = new ArrayList<Integer>();
                        tmpIDs.add(tupleID);
                        dataset_map.put(content, tmpIDs);
                    }

//		        	t.init(str.substring(str.indexOf(",")+1),splitString,index,header);
//		        	tupleList.add(t);

//	                System.out.println("ID: " + index  + "\t tuple: " + Arrays.toString(t.getContext()));
                    index++;
                }

            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return dataset_map;
    }
}
