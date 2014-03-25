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
	public static final String NUM_POINT = "crawled = ";

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
		String fileName = "results/ny-2-slice-270";
		String queryFile = "results/ny-2-slice-270.query";
		String pointFile = "results/ny-2-slice-270.point";
		a.readLog(fileName, queryFile, pointFile);

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
				if (query % 1000 == 0) {
					bw.write(queryList.get(i));
					bw.newLine();
					//
					bw2.write(pointList.get(i));
					bw2.newLine();
				}
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
