package mo.umac.analytics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Analyse {

	public static final String NUM_QUERY = "countNumQueries = ";
	public static final String NUM_POINT = "countCrawledPoints = ";

	public static final String NUM_QUERY2 = "countNumQueries2 = ";
	public static final String NUM_POINT2 = "countCrawledPoints2 = ";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Analyse a = new Analyse();
		// String fileName = "../data-experiment/info.log";
		// String queryFile = "../data-experiment/info.query";
		// String pointFile = "../data-experiment/info.point";
		// String queryFile2 = "../data-experiment/info2.query";
		// String pointFile2 = "../data-experiment/info2.point";
		// a.readLog(fileName, queryFile, pointFile);
		// a.readLog2(fileName, queryFile2, pointFile2);

		String fileName = "../data-experiment/info.log";
		String answerFile = "../data-experiment/answer";
		a.readLogPartitionParameters(fileName, answerFile);

	}

	public void readLog(String fileName, String queryFile, String pointFile) {
		ArrayList<String> queryList = new ArrayList<String>();
		ArrayList<String> pointList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String data = null;
			while ((data = br.readLine()) != null) {
				data = data.trim();
				if (data.contains(NUM_QUERY)) {
					int index = data.indexOf("=");
					String query = data.substring(index + 2, data.length());
					queryList.add(query);
				} else if (data.contains(NUM_POINT)) {
					int index = data.indexOf("=");
					String point = data.substring(index + 2, data.length());
					pointList.add(point);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// print query & points
		BufferedWriter bw = null;
		BufferedWriter bw2 = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(queryFile, false)));
			bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pointFile, false)));
			for (int i = 0; i < queryList.size(); i++) {
				int query = Integer.parseInt(queryList.get(i));
				// if (query % 1000 == 0) {
				bw.write(queryList.get(i));
				bw.newLine();
				//
				bw2.write(pointList.get(i));
				bw2.newLine();
				// }
			}
			bw.close();
			bw2.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void readLog2(String fileName, String queryFile, String pointFile) {
		ArrayList<String> queryList = new ArrayList<String>();
		ArrayList<String> pointList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String data = null;
			while ((data = br.readLine()) != null) {
				data = data.trim();
				if (data.contains(NUM_QUERY2)) {
					int index = data.indexOf("=");
					String query = data.substring(index + 2, data.length());
					queryList.add(query);
				} else if (data.contains(NUM_POINT2)) {
					int index = data.indexOf("=");
					String point = data.substring(index + 2, data.length());
					pointList.add(point);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// print query & points
		BufferedWriter bw = null;
		BufferedWriter bw2 = null;
		int pi = 1;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(queryFile, false)));
			bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pointFile, false)));
			for (int i = 0; i < queryList.size(); i++) {
				int query = Integer.parseInt(queryList.get(i));
				int point = Integer.parseInt(pointList.get(i));

				for (int j = pi; j <= point; j++) {
					bw2.write(Integer.toString(j));
					bw2.newLine();
					//
					bw.write(Integer.toString(query));
					bw.newLine();
				}
				pi = point + 1;

			}
			bw.close();
			bw2.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Analyse the log file generated after testing parameters for partition
	 * 
	 * @param fileName
	 * @param queryFile
	 * @param pointFile
	 */
	public void readLogPartitionParameters(String fileName, String answerFile) {
		ArrayList<String> answerList = new ArrayList<String>();
		String granularity1 = "granularity = 0.02";
		String granularity2 = "granularity = 0.03";
		String alpha = "alpha";
		String numDense = "numDense";
		String countNumQueries = "countNumQueries";
		String countCrawledPoints = "countCrawledPoints";
		//
		String countValue = "";
		int count = 0;
		String alphaValue = "";
		String numDenseValue = "";
		String countCrawledPointsValue = "";
		int crawled = 0;
		//
		int minCount = Integer.MAX_VALUE;
		String minAlpha = "";
		String minNumDense = "";
		String minCountCrawledPoints = "";
		//
		BufferedReader br = null;
		boolean flag = false; 
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String data = null;
			while ((data = br.readLine()) != null) {
				data = data.trim();
				System.out.println(data);
				if (data.contains(granularity1)) {
					flag = true;
				}
				if (data.contains(granularity2)) {
					flag = false;
					break;
				}
				if (data.contains(alpha) && flag) {
					alphaValue = data;
					answerList.add(data);
				}
				if (data.contains(numDense) && flag) {
					numDenseValue = data;
				}
				if (data.contains(countCrawledPoints) && flag) {
					int index = data.indexOf("=");
					countCrawledPointsValue = data.substring(index + 2, data.length());
					crawled = Integer.parseInt(countCrawledPointsValue);
					if(count < minCount && crawled == 57584){
						minCount = count;
						minAlpha = alphaValue;
						minNumDense = numDenseValue;
					}
				}
				if (data.contains(countNumQueries) && flag) {
					int index = data.indexOf("=");
					countValue = data.substring(index + 2, data.length());
					count = Integer.parseInt(countValue);
					
					answerList.add(countValue);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//
		System.out.println(answerList.size());
		System.out.println("minCount = " + minCount);
		System.out.println("minAlpha = " + minAlpha);
		System.out.println("minNumDense = " + minNumDense);
		// print query & points
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(answerFile, false)));
			for (int i = 0; i < answerList.size(); i++) {
				bw.write(answerList.get(i));
				bw.newLine();
			}
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
