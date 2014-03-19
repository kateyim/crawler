package mo.umac.crawler.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import mo.umac.main.MainCrawlerYahooLocal;
import mo.umac.utils.FileOperator;

/**
 * This class tests kinds of crawler methods, include Jsoup, saveFile,
 * HttpClient.
 * 
 * @author Kate YAN
 * 
 */
public class CrawlingMethodsTest {
	private String demoUrl = "http://local.yahooapis.com/LocalSearchService/V3/localSearch?appid=YahooDemo&query=pizza&zip=94306&results=2";
	private String errorUrl = "http://local.yahooapis.com/LocalSearchService/V3/localSearch?appid=l6QevFbV34H1VKW58naZ8keJohc8NkMNvuWfVs2lR3ROJMtw63XOWBePbDcMBFfkDnU-&query=pizza&results=20&start=1000";
	private String demoFile = "./src/test/resources/demo.xml";
	private String errorFile = "./src/test/resources/error.xml";

	public static Logger logger = Logger.getLogger(CrawlingMethodsTest.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DOMConfigurator.configure(MainCrawlerYahooLocal.LOG_PROPERTY_PATH);
		CrawlingMethodsTest ct = new CrawlingMethodsTest();
		// ct.testCrawler();
		// ct.testSaveUrl();
		ct.testHttpClient();

	}

//	private void testCrawler() {
//		String[] urls = { errorUrl };
//		String[] filePaths = { errorFile };
//		BruteForce crawler = new BruteForce();
//		crawler.crawl(urls, filePaths);
//	}

	/**
	 * @throws java.net.MalformedURLException
	 *             : no protocol
	 */
	private void testSaveUrl() {
		String filename = demoUrl;
		String urlString = demoFile;
		try {
			saveUrl(filename, urlString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * A method saving the xml web page
	 * 
	 * @param filename
	 * @param urlString
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private void saveUrl(String filename, String urlString)
			throws MalformedURLException, IOException {
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			in = new BufferedInputStream(new URL(urlString).openStream());
			fout = new FileOutputStream(filename);

			byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
		} finally {
			if (in != null)
				in.close();
			if (fout != null)
				fout.close();
		}
	}

	private void testHttpClient() {
		String url = errorUrl;
		String filePath = errorFile;
		httpClient(url, filePath);
	}

	/**
	 * Using HttpClient to crawl the xml web page
	 * 
	 * @param url
	 * @param filePath
	 */
	private void httpClient(String url, String filePath) {

		HttpClient httpclient = createHttpClient();
		try {
			File file = new File(filePath);
			if (!file.exists()) {
				logger.debug("file doesn't exists, creating...");
				if (!file.createNewFile()) {
					logger.error("create file failed.");

				} else {

					OutputStream output = new BufferedOutputStream(
							new FileOutputStream(file));
					logger.debug("fetching... " + url);
					HttpGet httpget = new HttpGet(url);
					HttpResponse response;
					response = httpclient.execute(httpget);
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						entity.writeTo(output);
					}
					output.close();
				}

			} else {
				// logger.debug("file exists, do nothing...");
				logger.debug("having in local... " + url);
			}
		} catch (Exception e) {
			logger.error("Wrong simpleCrawl", e);
			logger.error(url);
			logger.error(filePath);
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}

	}

	public HttpClient createHttpClient() {
		ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager();

		HttpParams params = new BasicHttpParams();

		int timeout = 1000 * 60 * 5;
		HttpConnectionParams.setConnectionTimeout(params, timeout);
		HttpConnectionParams.setSoTimeout(params, timeout);
		// HttpConnectionParams.setSocketBufferSize(params, size);

		// HttpClient httpClient = new DefaultHttpClient(params);
		HttpClient httpClient = new DefaultHttpClient(manager, params);
		return httpClient;
	}

	/**
	 * Get the Html document from the web.
	 * 
	 * @param url
	 *            This corresponds to the resulting xml file
	 * @return A HTML document
	 */
	public static Document getDocoment(String url) {
		/* crawls the xml from url and changes it into DOM tree (using Jsoup) */
		Document doc = XmlFetcher.getHtml(url);
		return doc;
	}

}
