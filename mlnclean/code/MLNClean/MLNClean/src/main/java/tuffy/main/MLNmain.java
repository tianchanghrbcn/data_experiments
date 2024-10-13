package tuffy.main;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import tuffy.learn.DNLearner;
import tuffy.learn.MultiCoreSGDLearner;
import tuffy.parse.CommandOptions;
import tuffy.util.Config;
import tuffy.util.UIMan;
/**
 * The Main.
 */
public class MLNmain {
	
	public static HashMap<String, Double> main(String[] args) throws SQLException, IOException {
		
		HashMap<String, Double> attributes = new HashMap<String, Double>();
		//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		/*String[] str = null;
		try {
			str = br.readLine().split(" ");
		} catch (Exception e) {
			 e.printStackTrace();
		}*/
		//CommandOptions options = UIMan.parseCommand(str);

		CommandOptions options = UIMan.parseCommand(args);
		
		UIMan.println("+++++++++ This is " + Config.product_name + "! +++++++++");
		if(options == null){
			return null;
		}
		if(!options.isDLearningMode){
			// INFERENCE
			if(!options.disablePartition){
				attributes = new PartInfer().run(options);
			}else{
				new NonPartInfer().run(options);
			}
		}else{
			if(options.mle){
				//SGDLearner l = new SGDLearner();
				MultiCoreSGDLearner l = new MultiCoreSGDLearner();
				l.run(options);
				l.cleanUp();
				
			}else{
				//LEARNING
				DNLearner l = new DNLearner();
				l.run(options);
			}
		}

		return attributes;
	}
}