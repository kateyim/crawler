package mo.umac.uscensus;

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
import java.util.Random;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Read the roads and then write them to a file
 * 
 * @author kate
 */
public class DrawInJOSM {

	public static void main(String[] args) {

		String roadFile = "../data-experiment/partition/ny-5.roads";
		ArrayList<Coordinate[]> roadList = USDensity.readRoad(USDensity.UN_ZIP_FOLDER_PATH);
		int rate = 5;
//		writePartition(roadFile, roadList, rate);
		countLineNum(roadFile);

	}

	/**
	 * sample from all roads
	 * 
	 * @param roadFile
	 * @param roadList
	 * @param rate
	 */
	public static void writePartition(String roadFile, ArrayList<Coordinate[]> roadList, int rate) {
		try {

			File file = new File(roadFile);

			if (file.exists()) {
				file.delete();
			}
			if (!file.exists()) {
				file.createNewFile();
			}

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)));
			bw.write(Integer.toString(roadList.size()));
			bw.newLine();

			for (int i = 0; i < roadList.size(); i++) {
				Coordinate[] aRoad = roadList.get(i);
				StringBuffer sb = new StringBuffer();
				for (int j = 0; j < aRoad.length; j++) {
					sb.append(aRoad[j].y);
					sb.append(";");
					sb.append(aRoad[j].x);
					sb.append(";");
				}
				String s = sb.toString();
				// add sampling
				s = s.substring(0, s.length() - 1);
				Random random = new Random(System.currentTimeMillis());
				int r = random.nextInt() % rate;
				if (r == 0) {
					bw.write(s);
					bw.newLine();
				}
			}

			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * count the number of roads, and then revise the .roads file manully
	 * 
	 * @param fileName
	 */
	public static void countLineNum(String fileName){
		int c = 0;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String data = null;
			while ((data = br.readLine()) != null) {
				data = data.trim();
				if (data.length() != 0) {
					c++;
				} 
			}
			c--;
			System.out.println("c = " + c);
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
