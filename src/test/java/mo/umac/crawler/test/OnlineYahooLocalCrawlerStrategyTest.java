package mo.umac.crawler.test;

import java.io.File;

import mo.umac.crawler.online.OnlineStrategy;
import mo.umac.crawler.online.QuadTreeCrawler;
import mo.umac.metadata.yahoolocal.ResultSetYahooOnline;
import mo.umac.parser.StaXParser;

public class OnlineYahooLocalCrawlerStrategyTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OnlineYahooLocalCrawlerStrategyTest test = new OnlineYahooLocalCrawlerStrategyTest();
		test.testQueryFunction();

	}

	public void testQueryFunction() {
		OnlineStrategy strategy = new QuadTreeCrawler();
		StaXParser parseXml = new StaXParser();
		String filePath = "./src/test/resources/bugs/localSearch.xml";
		File xmlFile = new File(filePath);
		ResultSetYahooOnline resultSet = parseXml.readConfig(xmlFile.getPath());
		System.out.println(resultSet.getXmlType());
	}

}
