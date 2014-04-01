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
		// String fileName = "results/k=1";
		// String outputFile = "results/k=1.output";
		// String fileName = "results/k=10";
		// String queryFile = "results/k=10.query";
		// String pointFile = "results/k=10.point";
		// String fileName = "results/k=270";
		// String queryFile = "results/k=270.query";
		// String pointFile = "results/k=270.point";
		// String fileName = "results/ny-2-slice-270";
		// String queryFile = "results/ny-2-slice-270.query";
		// String pointFile = "results/ny-2-slice-270.point";
		String fileName = "../data-experiment/info.log";
		String queryFile = "../data-experiment/info.query";
		String pointFile = "../data-experiment/info.point";
		String queryFile2 = "../data-experiment/info2.query";
		String pointFile2 = "../data-experiment/info2.point";
		a.readLog(fileName, queryFile, pointFile);
		a.readLog2(fileName, queryFile2, pointFile2);

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

}
