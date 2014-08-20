package mo.umac.analytics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import mo.umac.db.DBExternal;
import mo.umac.db.H2DB;

public class MainData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MainData m = new MainData();
		// ny
		// String stateQ = "NY";
		// String categoryQ = "Restaurants";
		// String dbNameSource = "../crawler-data/yahoolocal-h2/source/ny";
		// String dbNameTarget = "../crawler-data/yahoolocal-h2/source/ny-prun";
		// ut
		String stateQ = "UT";
		String categoryQ = "Restaurants";
		String dbNameSource = "../data-experiment/yahoo/ut";
		String dbNameTarget = "../crawler-data/yahoolocal-h2/ut-prun";
		// ok
		// String stateQ = "OK";
		// String categoryQ = "Restaurants";
		// String dbNameSource = "../crawler-data/yahoolocal-h2/source/ok";
		// String dbNameTarget = "../crawler-data/yahoolocal-h2/source/ok-prun";
		// step 1
		// String folderPath = DBExternal.FOLDER_NAME + "/96926236+Restaurants/" + stateQ + "/";
		// m.convertFromQRFileToH2(dbNameSource, folderPath);
		// m.examData(dbNameSource);
		// step 2
		// m.prunH2(dbNameSource, dbNameTarget, categoryQ, stateQ);
		// m.examData(dbNameTarget);

		// step 3

		// step 4
		// String dbName = "../data-experiment/synthetic/skew-1000-0.3";
		// String fileName = "../data-experiment/partition/skew-1000-0.3.pois";
		// m.convertFromH2ToFile(dbName, fileName);

		// step 4
		// String logFile = "../crawlerlog/info.log";
		// m.numVSCrawled(logFile);

		// test remove duplicate
		// m.examData(dbName);
		// m.removeDuplicate(dbName);
		// m.examData(dbName);

		// for sample: reduce the size of yahoo/ut/ok
		String dbNameFull = "../data-experiment/yahoo/ut-prun";
		String dbNameSample = "../data-experiment/yahoo/ut-prun-2";
		int factor = 2;
		m.sample(dbNameFull, dbNameSample, factor);

		DBExternal.distroyConn();
		// m.prunPoisFile();
	}

	/**
	 * Step 1: convert the crawled file data into h2 database
	 */
	public void convertFromQRFileToH2(String dbNameSource, String folderPath) {
		String dbNameTarget = "";
		H2DB h2 = new H2DB(dbNameSource, dbNameTarget);
		h2.convertFileDBToH2DB(folderPath, "");
		// exam the converted data
		int c1 = h2.count(dbNameSource, "QUERY");
		System.out.println("c1 = " + c1);
		int c2 = h2.count(dbNameSource, "ITEM");
		System.out.println("c2 = " + c2);
	}

	/**
	 * Step 2: prun the h2 database results
	 */
	public void prunH2(String dbNameSource, String dbNameTarget, String categoryQ, String stateQ) {
		// FIXME add the boundary when pruning!!!
		H2DB h2 = new H2DB(dbNameSource, dbNameTarget);
		h2.prun(categoryQ, stateQ);

		int c1 = h2.count(dbNameSource, "ITEM");
		System.out.println("dbNameSource = " + c1);
		int c2 = h2.count(dbNameTarget, "ITEM");
		System.out.println("dbNameTarget = " + c2);
	}

	/**
	 * Exam data in a h2 database
	 */
	public void examData(String dbNameTarget) {
		H2DB h2 = new H2DB(dbNameTarget, "");
		h2.examData(dbNameTarget);
	}

	/**
	 * Step 3: write the points file for drawing in the openstreetmap
	 */
	public void convertFromH2ToFile(String dbName, String fileName) {
		String tableName = "item";
		H2DB h2 = new H2DB(dbName, "");
		h2.extractValuesFromItemTable(dbName, tableName, fileName);
	}

	/**
	 * Read from the log file, print the number of queries ,and the number of
	 * points crawled
	 * 
	 * @param logFile
	 */
	public void numVSCrawled(String logFile) {
		File file = new File(logFile);
		if (!file.exists()) {
			System.out.println("file not exist!");
		}
		ArrayList<Integer> numQueries = new ArrayList<Integer>();
		ArrayList<Integer> numPoints = new ArrayList<Integer>();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String data = null;
			String[] split;
			while ((data = br.readLine()) != null) {
				data = data.trim();
				if (data.contains("countNumQueries")) {
					split = data.split("=");
					int numQuery = Integer.parseInt(split[1].trim());
					numQueries.add(numQuery);
				} else if (data.contains("number of points crawled")) {
					split = data.split("=");
					int numPoint = Integer.parseInt(split[1].trim());
					numPoints.add(numPoint);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// print
		System.out.println("numQueries");
		for (int i = 0; i < numQueries.size(); i++) {
			System.out.println(numQueries.get(i));
		}
		System.out.println("numPoints");
		for (int i = 0; i < numPoints.size(); i++) {
			System.out.println(numPoints.get(i));
		}
	}

	/**
	 * @deprecated
	 */
	public void prunPoisFile() {
		// TODO
		int threshold = 50;
		String fileSrc = "../data-map/ny-prun-target.pois";
		String fileTarget = "../data-map/ny-prun-target" + "-" + threshold + ".pois";
		// numCrawledFrequency(fileSrc);

		numCrawledByThreshold(fileSrc, fileTarget, threshold);

	}

	/**
	 * Find the frequency of the number of numCrawled
	 * 
	 * @param fileName
	 * @return
	 * @deprecated
	 */
	private Map numCrawledFrequency(String fileName) {
		Map map = new HashMap<Integer, Integer>();

		File file = new File(fileName);
		if (!file.exists()) {
			System.out.println("file not exist!");
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String data = null;
			String[] split;
			while ((data = br.readLine()) != null) {
				data = data.trim();
				split = data.split(";");
				int num = Integer.parseInt(split[1]);
				if (!map.containsKey(num)) {
					map.put(num, 1);
				} else {
					int freqPre = (Integer) map.get(num);
					map.put(num, freqPre + 1);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			int num = (Integer) entry.getKey();
			int freq = (Integer) entry.getValue();
			System.out.println(num + ":" + freq);
			// System.out.println(num);
			// System.out.println(freq);
		}
		return map;
	}

	/**
	 * prun the .pois file by the numCrawled
	 * 
	 * @param fileSrc
	 * @param fileTarget
	 * @param threshold
	 */
	private void numCrawledByThreshold(String fileSrc, String fileTarget, int threshold) {
		ArrayList<Integer> list0 = new ArrayList<Integer>();
		ArrayList<Double> list2 = new ArrayList<Double>();
		ArrayList<Double> list3 = new ArrayList<Double>();
		// read
		File file = new File(fileSrc);
		if (!file.exists()) {
			System.out.println("file not exist!");
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String data = null;
			String[] split;
			while ((data = br.readLine()) != null) {
				data = data.trim();
				split = data.split(";");
				int num = Integer.parseInt(split[1]);
				if (num >= threshold) {
					list0.add(Integer.parseInt(split[0].trim()));
					list2.add(Double.parseDouble(split[2].trim()));
					list3.add(Double.parseDouble(split[3].trim()));
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// writing to the file
		file = new File(fileTarget);
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
			int n = list0.size();
			for (int i = 0; i < n; i++) {
				int id = list0.get(i);
				double longitude = list2.get(i);
				double latitude = list3.get(i);
				int numCrawled = threshold;
				bw.write(Integer.toString(id));
				bw.write(";");
				bw.write(Integer.toString(numCrawled));
				bw.write(";");
				bw.write(Double.toString(longitude));
				bw.write(";");
				bw.write(Double.toString(latitude));
				bw.newLine();
			}
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void removeDuplicate(String dbNameTarget) {
		H2DB h2 = new H2DB("", dbNameTarget);
		h2.removeDuplicate();
	}

	public void sample(String dbNameSource, String dbNameSample, int factor) {
		H2DB h2 = new H2DB(dbNameSource, dbNameSample);
		h2.sample(factor);

		int c1 = h2.count(dbNameSource, "ITEM");
		System.out.println("dbNameSource = " + c1);
		int c2 = h2.count(dbNameSample, "ITEM");
		System.out.println("dbNameSample = " + c2);
	}
}
