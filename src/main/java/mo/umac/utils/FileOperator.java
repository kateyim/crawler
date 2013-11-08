/**
 * 
 */
package mo.umac.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import mo.umac.crawler.online.OnlineStrategy;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author Kate Yim
 * 
 */
public class FileOperator {

	public static Logger logger = Logger.getLogger(FileOperator.class);

	/**
	 * Create the corresponding .xml file for the query.
	 * 
	 * @param folder
	 * @param number
	 *            the number of queries when issuing this corresponding query.
	 * @param suffix
	 *            which is ".xml"
	 * @return
	 */
	public static File createFileAutoAscending(String folder, int number, String suffix) {
		String filePath = folder + number + suffix;
		File file = new File(filePath);
		String lastNumString;
		int lastNum;
		while (file.exists()) {
			if (filePath.indexOf("-") != -1) {
				// has "-"
				lastNumString = filePath.substring(filePath.indexOf("-") + 1, filePath.indexOf(".xml"));
				lastNum = Integer.parseInt(lastNumString);
				filePath = filePath.substring(0, filePath.indexOf("-")) + "-" + (lastNum + 1) + ".xml";
			} else {
				filePath = filePath.substring(0, filePath.indexOf(".xml")) + "-" + 0 + ".xml";
			}
			file = new File(filePath);
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	public static File createFile(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
				logger.info("creating file " + fileName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			logger.info("file already exists: " + fileName);
		}
		return file;
	}

	/**
	 * Create a folder under the parent folder
	 * 
	 * @param parentFolderPath
	 * @param folderName
	 * @return the subfolder's path
	 */
	public static String createFolder(String parentFolderPath, String folderName) {
		StringBuffer sb = new StringBuffer();
		sb.append(parentFolderPath);
		sb.append(folderName);
		String folderPath = sb.toString();
		File dir = new File(folderPath);
		boolean success = dir.mkdir();
		if (!success) {
			logger.error("Creating folder " + folderPath + " failed!");
		}
		return folderPath + "/";
	}

	/**
	 * @param folderPath
	 * @return
	 */
	public static List traverseFolder(String folderPath) {
		List<File> list = new ArrayList<File>();
		File folder = new File(folderPath);
		recursivelyTraverse(folder, list);
		return list;
	}

	/**
	 * @param folderPath
	 * @return
	 */
	public static List traverseFolder(String folderPath, String extension) {
		List<File> list = new ArrayList<File>();
		File folder = new File(folderPath);
		recursivelyTraverse(folder, list, extension);
		return list;
	}

	private static void recursivelyTraverse(File file, List list) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				recursivelyTraverse(files[i], list);
			}
		} else {
			if (!file.getName().contains("~")) {
				list.add(file.getAbsolutePath());
				// System.out.println(file.getName());
				// System.out.println(file.getAbsolutePath());
			}
		}
	}

	private static void recursivelyTraverse(File file, List list, String extension) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				recursivelyTraverse(files[i], list, extension);
			}
		} else {
			if (!file.getName().contains("~")) {
				// FIXME extension test
				String ext = FilenameUtils.getExtension(file.getName());
				if (ext.equals(extension)) {
					list.add(file.getAbsolutePath());
					// System.out.println(file.getName());
					// System.out.println(file.getAbsolutePath());
				}

			}
		}
	}

	/**
	 * Reading Yahoo! develop appid from the property file
	 * 
	 * @param propertyFile
	 */
	public static String readAppid(String propertyFile) {
		Properties props = new Properties();
		String appid = null;
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(propertyFile));
			props.load(in);
			appid = props.getProperty(OnlineStrategy.APPID);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error in load " + propertyFile, e);
		}
		return appid;
	}

	public static void printFile(String fileName) {
		File file = new File(fileName);
		try {
			if (file.exists()) {
				BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
				String data = null;
				while ((data = input.readLine()) != null) {
					System.out.println(data.toString());
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reading the category ids of Yahoo! Local
	 * 
	 * @param categrotyIDFile
	 * @return
	 */
	public static HashMap<Integer, String> readCategoryID(String categrotyIDFile) {
		HashMap<Integer, String> categoryID = new HashMap<Integer, String>();
		File file = new File(categrotyIDFile);
		if (!file.exists()) {
			logger.error("file: " + categrotyIDFile + "does not exist");
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(categrotyIDFile)));
			String data = null;
			String[] split;
			while ((data = br.readLine()) != null) {
				data = data.trim();
				split = data.split(",");
				// System.out.println(Integer.parseInt(split[0]) + "=" +
				// split[1]);
				categoryID.put(Integer.parseInt(split[0]), split[1]);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return categoryID;
	}

}
