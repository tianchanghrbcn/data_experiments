package main;

import data.Rule;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

import static main.Main.baseURL;

/**
 * Created by gcc on 17-9-25.
 */
public class PickData {

    public static void generateData(String sourceFile, String writeFile, int sampleSize){
        FileReader reader;
        ArrayList<String> tupleList = new ArrayList<String>();
        try {
            File writefile = new File(writeFile);
            if (!writefile.exists()) {
                writefile.createNewFile();
            }
            FileWriter fw = new FileWriter(writefile);
            BufferedWriter bw = new BufferedWriter(fw);
            reader = new FileReader(sourceFile);
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            String header = br.readLine();
            bw.write(header+"\n");
            while((line = br.readLine()) != null && line.length()!=0) {
                tupleList.add(line);
            }

            for(int i=0;i<sampleSize;i++){
                int index = (int)(Math.random()*tupleList.size());
                bw.write(tupleList.get(index)+"\n");
            }

            br.close();
            bw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception{
        String sourceFile = baseURL + "/syn-car/dataset/ground_truth-10q.csv";
        String writeFile = baseURL + "/syn-car/dataset/ground_truth_1q.csv";
        int sampleSize = 1000;
        generateData(sourceFile,writeFile,sampleSize);
    }
}
