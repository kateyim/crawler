/**
 * 
 */
package mo.umac.uscensus;

import java.util.List;

import mo.umac.crawler.Main;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Crawl data from US census
 * 
 * @author kate
 * 
 */
public class UScensusCrawler {

	public static Logger logger = Logger.getLogger(UScensusCrawler.class.getName());

	private static final String ROADS = "Roads";
	private static final String FEATURES = "Features";
	private static final String NEWYORK = "New York";
	private static final String ROADS_ = "roads_";

	public static void main(String[] args) {
		DOMConfigurator.configure(Main.LOG_PROPERTY_PATH);
		crawlRoad();
	}

	public static void crawlRoad() {
		String url = "http://www.census.gov/cgi-bin/geo/shapefiles2013/main";
		String folder = "../data-map/us-road/new-york/";
		crawler(url, folder);
	}

	/**
	 * @param url
	 * @param folder
	 */
	public static void crawler(String url, String folder) {
		// Create a new instance of the Firefox driver
		// Notice that the remainder of the code relies on the interface,
		// not the implementation.
		WebDriver driver = new FirefoxDriver();

		driver.get(url);

		// Fill the form in the first page
		WebElement select = driver.findElement(By.tagName("select"));
		List<WebElement> optgroups = select.findElements(By.tagName("optgroup"));
		for (int i = 0; i < optgroups.size(); i++) {
			WebElement optgroup = optgroups.get(i);
			// <optgroup label = "Features">
			String label = optgroup.getAttribute("label");
			if (label.equals(FEATURES)) {
				List<WebElement> optionElements = optgroup.findElements(By.tagName("option"));
				for (int j = 0; j < optionElements.size(); j++) {
					WebElement option = optionElements.get(j);
					String name = option.getAttribute("value");
					if (name.equals(ROADS)) {
						option.click();
					}
				}
			}
		}
		// Now submit the form. WebDriver will find the form for us from the element
		WebElement buttonElement = driver.findElement(By.tagName("button"));
		buttonElement.click();

		// switching to the new window
		// for (String handle : driver.getWindowHandles()) {
		// driver.switchTo().window(handle);
		driver.switchTo().window(driver.getWindowHandle());
		// ---------------------------
		// //<form name="roads_"
		// <select
		// traverse option : <option.. ></option>, except the first one
		select = driver.findElement(By.name(ROADS_));
		select = select.findElement(By.tagName("select"));
		List<WebElement> optionElements = select.findElements(By.tagName("option"));
		// except the first one
		for (int j = 1; j < optionElements.size(); j++) {
			WebElement option = optionElements.get(j);
			// <option value="36">New York</option>
			String name = option.getText();
			if (name.equals(NEWYORK)) {
				option.click();
				break;
			}
		}
		// <button type="submit">Submit</button>
		List<WebElement> buttonElements = driver.findElements(By.tagName("button"));
		for (int j = 0; j < buttonElements.size(); j++) {
			buttonElement = buttonElements.get(j);
			String name = buttonElement.getText();
			if (name.equals("Submit")) {
				buttonElement.click();
				break;
			}
		}

		//
		// ---------------------------
		// <select name="selection" style="width: 330px;">
		// traverse option : <option.. ></option>, except the first one
		select = driver.findElement(By.tagName("select"));
		optionElements = select.findElements(By.tagName("option"));
		// except the first one
		for (int j = 1; j < optionElements.size(); j++) {
			WebElement option = optionElements.get(j);
			logger.debug("County = " + option.getText());
			option.click();
			// <button type="submit">Download</button>
			buttonElement = driver.findElement(By.tagName("button"));
			buttonElement.click();
			// FIXME how to backward
		}

		// }

		// Check the title of the page
		System.out.println("Page title is: " + driver.getTitle());

		// Google's search is rendered dynamically with JavaScript.
		// Wait for the page to load, timeout after 10 seconds
		(new WebDriverWait(driver, 100)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return d.getTitle().toLowerCase().startsWith("cheese!");
			}
		});

		// Should see: "cheese! - Google Search"
		System.out.println("Page title is: " + driver.getTitle());

		// Close the browser
		driver.quit();
	}
}
