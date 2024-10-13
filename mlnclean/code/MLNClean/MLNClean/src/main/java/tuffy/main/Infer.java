package tuffy.main;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import data.MLNClause;
import tuffy.db.RDB;
import tuffy.ground.Grounding;
import tuffy.ground.KBMC;
import tuffy.infer.DataMover;
import tuffy.mln.Clause;
import tuffy.mln.MarkovLogicNetwork;
import tuffy.mln.Predicate;
import tuffy.parse.CommandOptions;
import tuffy.util.Config;
import tuffy.util.ExceptionMan;
import tuffy.util.FileMan;
import tuffy.util.Timer;
import tuffy.util.UIMan;
/**
 * Common routines to inference procedures.
 */
public abstract class Infer {
	
	/**
	 * The DB.
	 */
	protected RDB db = null;
	
	public DataMover dmover = null;
	
	/**
	 * The MLN.
	 */
	public MarkovLogicNetwork mln = null;
	
	/**
	 * Command line options.
	 */
	protected CommandOptions options = null;
	
	/**
	 * Grounding worker.
	 */
	protected Grounding grounding = null;

	/**
	 * Ground the MLN into an MRF.
	 * @throws IOException 
	 */
	protected HashMap<String, Double>  ground() throws IOException{
		grounding = new Grounding(mln);
		HashMap<String, Double> attributes = grounding.constructMRF();
		return attributes;
	}

	/**
	 * Set up MLN inference, including the following steps:
	 * 
	 * 1) loadMLN {@link Infer#loadMLN};
	 * 2) store symbols and evidence {@link MarkovLogicNetwork#materializeTables};
	 * 3) run KBMC;
	 * 4) apply scoping rules;
	 * 5) mark query atoms in the database {@link MarkovLogicNetwork#storeAllQueries()}.
	 * 
	 * @param opt command line options.
	 * @throws IOException 
	 */
	protected void setUp(CommandOptions opt) throws IOException{
		options = opt;
		Timer.resetClock();

		Clause.mappingFromID2Const = new HashMap<Integer, String>();
		Clause.mappingFromID2Desc = new HashMap<String, String>();
		
		UIMan.println(">>> Connecting to RDBMS at " + Config.db_url);
		
		db = RDB.getRDBbyConfig();
		
		db.resetSchema(Config.db_schema);

		mln = new MarkovLogicNetwork();
		loadMLN(mln, db, options);

		mln.materializeTables();//将evidence.db中的值存入数据库，constant表中存了所有值，typle_xxx中存储xxx属性的值
		
		KBMC kbmc = new KBMC(mln);
		kbmc.run();
		mln.executeAllDatalogRules();
		mln.applyAllScopes();
		UIMan.verbose(1, ">>> Marking queries...");
		mln.storeAllQueries();
		
		for(Clause c : mln.listClauses){
			if(c.isHardClause()){
				c.isFixedWeight = true;
			}else{
				c.isFixedWeight = false;
			}
		}
		
	}
	
	protected void setUp_noloading(CommandOptions opt){
		options = opt;
		Timer.resetClock();

		//MLNClause.mappingFromID2Const = new HashMap<Integer, String>();
		//MLNClause.mappingFromID2Desc = new HashMap<String, String>();
		
		UIMan.println(">>> Connecting to RDBMS at " + Config.db_url);
		db = RDB.getRDBbyConfig();
		
	}
	
	/**
	 * Clean up temporary data: the schema in PostgreSQL and the working directory.
	 * @throws IOException 
	 */
	protected void cleanUp() throws IOException{		
		Config.exiting_mode = true;
		UIMan.println(">>> Cleaning up temporary data");
		if(!Config.keep_db_data){
			UIMan.print("    Removing database schema '" + Config.db_schema + "'...");
			UIMan.println(db.dropSchema(Config.db_schema)?"OK" : "FAILED");
		}else{
			UIMan.println("\tData remains in schema '" + Config.db_schema + "'.");
		}
		db.close();

		UIMan.print("\tRemoving temporary dir '" + Config.getWorkingDir() + "'...");
		UIMan.println(FileMan.removeDirectory(new File(Config.getWorkingDir()))?"OK" : "FAILED");

		UIMan.println("*** Tuffy exited at " + Timer.getDateTime() + " after running for " + Timer.elapsed());
		UIMan.closeDribbleFile();
	}
	
	/**
	 * Load the rules and data of the MLN program.
	 * 
	 * 1) {@link MarkovLogicNetwork#loadPrograms(String[])};
	 * 2) {@link MarkovLogicNetwork#loadQueries(String[])};
	 * 3) {@link MarkovLogicNetwork#parseQueryCommaList(String)};
	 * 4) Mark closed-world predicate specified by {@link CommandOptions#cwaPreds};
	 * 5) {@link MarkovLogicNetwork#prepareDB(RDB)};
	 * 6) {@link MarkovLogicNetwork#loadEvidences(String[])}.
	 * 
	 * @param mln the target MLN
	 * @param adb database object used for this MLN
	 * @param opt command line options
	 * @throws IOException 
	 */
	protected void loadMLN(MarkovLogicNetwork mln, RDB adb, CommandOptions opt) throws IOException{
		
		String[] progFiles = opt.fprog.split(",");
		mln.loadPrograms(progFiles);

		if(opt.fquery != null){
			String[] queryFiles = opt.fquery.split(",");
			mln.loadQueries(queryFiles);
		}
		
		if(opt.queryAtoms != null){
			UIMan.verbose(2, ">>> Parsing query atoms in command line");
			mln.parseQueryCommaList(opt.queryAtoms);
		}

		if(opt.cwaPreds != null){
			String[] preds = opt.cwaPreds.split(",");
			for(String ps : preds){
				Predicate p = mln.getPredByName(ps);
				if(p == null){
					mln.closeFiles();
					ExceptionMan.die("COMMAND LINE: Unknown predicate name -- " + ps);
				}else{
					p.setClosedWorld(true);
				}
			}
		}
		
		mln.prepareDB(adb);
		
		if(opt.fevid != null){
			String[] evidFiles = opt.fevid.split(",");
			mln.loadEvidences(evidFiles);
		}
		
		dmover = new DataMover(mln);
	}


	
	
}
