package mo.umac.db.yahoolocal;

import java.util.HashMap;

import mo.umac.metadata.yahoolocal.APOI;
import mo.umac.metadata.yahoolocal.AQuery;
import mo.umac.metadata.yahoolocal.ResultSet;
import mo.umac.metadata.yahoolocal.ResultSetYahooOnline;
import mo.umac.metadata.yahoolocal.YahooLocalQueryFileDB;

public class Website extends DBExternal {

    @Override
    public void writeToExternalDBFromOnline(int queryID, int level, int parentID,
	    YahooLocalQueryFileDB qc, ResultSetYahooOnline resultSet) {
	// TODO Auto-generated method stub
	
    }


    @Override
    public void init() {
	// TODO Auto-generated method stub
	
    }


    @Override
    public HashMap<Integer, APOI> readFromExtenalDB(String category,
	    String state) {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public void writeToExternalDB(int queryID, AQuery query, ResultSet resultSet) {
	// TODO Auto-generated method stub
	
    }


    @Override
    public void createTables(String dbNameTarget) {
	// TODO Auto-generated method stub
	
    }


    @Override
    public int numCrawlerPoints() {
	// TODO Auto-generated method stub
	return 0;
    }


	@Override
	public void updataExternalDB() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void removeDuplicate() {
		// TODO Auto-generated method stub
		
	}



}
