package util;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by gcc on 17-12-20.
 */
public class ConvertRule {

    public static void main(String[] args) {
        String sourceURL = "/data/nw/DC_ED/References_inner_and_outer/mlnclean/dataset/" + "synthetic-car/rules.txt";
        String outURL = "/data/nw/DC_ED/References_inner_and_outer/mlnclean/dataset/" + "synthetic-car/rules-first-order.txt";
        convertToFirstOrder(sourceURL, outURL);
    }

    public static void convertToFirstOrder(String sourceURL, String outURL) {
        System.out.println("source File = " + sourceURL);
        System.out.println("out File=" + outURL);
        ArrayList<String> list = new ArrayList<>();

        //read
        try {
            FileReader reader = new FileReader(sourceURL);
            BufferedReader br = new BufferedReader(reader);
            String line;
            String[] partRule;
            while ((line = br.readLine()) != null && line.length() != 0) {
                partRule = line.split("=>");
                String left = "!" + partRule[0].replaceAll(",", " v !");
                String right = "v" + partRule[1].replaceAll(",", " v ");
                String newLine = left + right;
                list.add(newLine);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //write
        try {
            FileWriter writer = new FileWriter(outURL);
            BufferedWriter bw = new BufferedWriter(writer);
            for (String str : list) {
                bw.write(str);
                bw.newLine();
            }
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finished convert rules to First-Order-Logic");
    }


}
