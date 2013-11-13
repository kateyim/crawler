package mo.umac.crawler.test;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import mo.umac.crawler.MainCrawler;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

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
		// DOMConfigurator.configure(MainCrawler.LOG_PROPERTY_PATH);
		// TODO Auto-generated method stub
		int sum = 0;
		// System.setOut(outptuFile("lily5.txt"));
		String fileName = "glass.txt";
		File file = new File(fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));

			WebDriver driver = new FirefoxDriver();
			List<WebElement> webElement = new ArrayList<WebElement>();

			for (int i = 1; i < 131; i++) {
				// driver.get("http://www.coastal.com/glasses/catalog/refinedSearch?size_glassesLensDiameter=31#minPrice=0&maxPrice=500&sorting=featuredAnywhere-asc&page=130&searchFamily=glasses&categoryCode=Eyeglasses&filterGroup=glassesLensDiameter&pdi_glassesLensDiameter=[]&widgetExpanded=false&perfectFitExpanded=false&requestIdentifier=319222&hotSpotsEnabled=true&order=[]");
				driver.get("http://www.coastal.com/glasses/catalog/refinedSearch?size_glassesLensDiameter=31#minPrice=0&maxPrice=500&sorting=featuredAnywhere-asc&page="
						+ i
						+ "&searchFamily=glasses&categoryCode=Eyeglasses&filterGroup=glassesLensDiameter&pdi_glassesLensDiameter=[]&widgetExpanded=false&perfectFitExpanded=false&requestIdentifier=319222&hotSpotsEnabled=true&order=[]");
				// WebElement webElement = driver.findElement(By.xpath("//*"));

				webElement = driver.findElements(By.xpath("//*[@id='browsed-product']//*[@class='product-inner-grid-container']"));
				for (WebElement element : webElement) {

					String item = element.getAttribute("rel");
					System.out.println(item);
					// logger.info(item);
					bw.write(item);
					bw.newLine();
					sum++;

					// String urlToOpen = element.getAttribute("rel");
					// driver.get(urlToOpen);
					// driver.navigate().to(urlToOpen);
				}

				try {
					// TimeUnit.SECONDS.sleep(120);
					Thread.sleep(8000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(sum);
	}

}
