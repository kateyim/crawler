package mo.umac.crawler.test;

import java.io.*;

import mo.umac.crawler.MainCrawler;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * @author Li Yan
 *
 */
public class OpenGoogle {

	protected static Logger logger = Logger.getLogger(OpenGoogle.class.getName());

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	protected static PrintStream outptuFile(String name) throws FileNotFoundException {
		return new PrintStream(new BufferedOutputStream(new FileOutputStream(name)));
	}

	public static void main(String[] args) throws FileNotFoundException {
		DOMConfigurator.configure(MainCrawler.LOG_PROPERTY_PATH);
		// TODO Auto-generated method stub
		// System.setOut(outptuFile("resut.txt"));
		System.getProperties().setProperty("webdriver.chrome.driver", "/Users/yihua/Downloads/chromedriver");
		WebDriver driver = new FirefoxDriver();
		driver.get("http://www.coastal.com/glasses/catalog/refinedSearch?size_glassesLensDiameter=31#minPrice=0&maxPrice=500&sorting=featuredAnywhere-asc&page=1&searchFamily=glasses&categoryCode=Eyeglasses&filterGroup=glassesLensDiameter&pdi_glassesLensDiameter=[]&widgetExpanded=false&perfectFitExpanded=false&requestIdentifier=319222&hotSpotsEnabled=true&order=[]");
		// WebElement webElement = driver.findElement(By.xpath("//*"));

		// get the first pic
		WebElement webElement = driver.findElement(By.xpath("/html/body/div[2]/div/div[3]/div/div[4]/div/div/div[3]/div/div/div"));
		// System.out.println(webElement.getAttribute("outerHTML"));
		System.out.println(webElement.getAttribute("rel"));
		// driver.close();
		String urlToOpen = webElement.getAttribute("rel");
		logger.info("urlToOpen = " + urlToOpen);
		driver.get(urlToOpen);
		driver.navigate().to(urlToOpen);

	}

}
