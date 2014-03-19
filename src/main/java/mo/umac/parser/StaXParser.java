/**
 * 
 */
package mo.umac.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import mo.umac.metadata.yahoolocal.APOI;
import mo.umac.metadata.yahoolocal.ResultSetYahooOnline;

import org.apache.log4j.Logger;

/**
 * Parse the returned xml file.
 * 
 * @author Kate Yim
 * 
 */
public class StaXParser {
    public static Logger logger = Logger.getLogger(StaXParser.class.getName());

    static final String RESULT_SET = "ResultSet";
    // add at 2013-4-5
    static final String ERROR = "Error";
    static final String MESSAGE = "Message";
    static final String LIMIT_EXCEED = "limit exceeded";
    //
    static final String TOTAL_RESULTS_AVAILABLE = "totalResultsAvailable";
    static final String TOTAL_RESULTS_RETURNED = "totalResultsReturned";
    static final String FIRST_RESULT_POSITION = "firstResultPosition";
    static final String RESULT = "Result";
    static final String ID = "id";
    static final String TITLE = "Title";
    static final String ADDRESS = "Address";
    static final String CITY = "City";
    static final String STATE = "State";
    static final String PHONE = "Phone";
    static final String LATITUDE = "Latitude";
    static final String LONGITUDE = "Longitude";
    static final String RATING = "Rating";
    static final String AVERAGE_RATING = "AverageRating";
    static final String TOTAL_RATINGS = "TotalRatings";
    static final String TOTAL_REVIEWS = "TotalReviews";
    static final String LAST_REVIEW_DATE = "LastReviewDate";
    static final String LAST_REVIEW_INTRO = "LastReviewIntro";
    static final String DISTANCE = "Distance";
    static final String URL = "Url";
    static final String CLICK_URL = "ClickUrl";
    static final String MAP_URL = "MapUrl";
    static final String BUSINESS_URL = "BusinessUrl";
    static final String BUSINESS_CLICK_URL = "BusinessClickUrl";
    static final String CATEGORIES = "Categories";
    static final String CATEGORY = "Category";

    @SuppressWarnings({ "unchecked", "null" })
    public ResultSetYahooOnline readConfig(String configFile) {
	ResultSetYahooOnline resultSet = new ResultSetYahooOnline();
	try {
	    // First create a new XMLInputFactory
	    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	    // Setup a new eventReader
	    InputStream in = new FileInputStream(configFile);
	    XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
	    // TODO check other conditions
	    // Read the XML document
	    while (eventReader.hasNext()) {
		XMLEvent event = eventReader.nextEvent();
		if (event.isStartElement()) {
		    StartElement startElement = event.asStartElement();
		    if (startElement.getName().getLocalPart().equals(ERROR)) {
			resultSet = parseErrorInfo(eventReader, resultSet);
		    } else {
			if (startElement.getName().getLocalPart()
				.equals(RESULT_SET)) {
			    resultSet = parseResultSetInfo(eventReader,
				    resultSet, startElement);
			}
		    }
		}
	    }
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (XMLStreamException e) {
	    e.printStackTrace();
	}
	return resultSet;
    }

    private ResultSetYahooOnline parseErrorInfo(XMLEventReader eventReader,
	    ResultSetYahooOnline resultSet) throws XMLStreamException {
	while (eventReader.hasNext()) {
	    XMLEvent event = eventReader.nextEvent();
	    if (event.isStartElement()) {
		StartElement startElement = event.asStartElement();
		if (startElement.getName().getLocalPart().equals(MESSAGE)) {
		    event = eventReader.nextEvent();
		    String message = event.toString();
		    if (message.equals(LIMIT_EXCEED)) {
			resultSet.setXmlType(YahooXmlType.LIMIT_EXCEEDED);
		    } else {
			logger.error("Error Page: " + message);
			resultSet.setXmlType(YahooXmlType.OTHER_ERROR);
		    }
		}
	    }
	}
	return resultSet;
    }

    /**
     * @param eventReader
     * @param resultSet
     * @param startElement
     * @return
     * @throws XMLStreamException
     */
    private ResultSetYahooOnline parseResultSetInfo(XMLEventReader eventReader,
	    ResultSetYahooOnline resultSet, StartElement startElement)
	    throws XMLStreamException {
	List<APOI> results = new ArrayList<APOI>();
	APOI result = null;
	Rating rating = null;
	List<Category> categories = new ArrayList<Category>();
	Iterator<Attribute> attributes = startElement.getAttributes();
	while (attributes.hasNext()) {
	    Attribute attribute = attributes.next();
	    if (attribute.getName().toString().equals(TOTAL_RESULTS_AVAILABLE)) {
		resultSet.setTotalResultsAvailable(Integer.parseInt(attribute
			.getValue()));
	    }
	    if (attribute.getName().toString().equals(TOTAL_RESULTS_RETURNED)) {
		resultSet.setTotalResultsReturned(Integer.parseInt(attribute
			.getValue()));
	    }
	    if (attribute.getName().toString().equals(FIRST_RESULT_POSITION)) {
		resultSet.setFirstResultPosition(Integer.parseInt(attribute
			.getValue()));
	    }
	}
	// Parse the details information
	while (eventReader.hasNext()) {
	    XMLEvent event = eventReader.nextEvent();
	    if (event.isStartElement()) {
		startElement = event.asStartElement();

		if (startElement.getName().getLocalPart().equals(RESULT)) {
		    result = new APOI();
		    attributes = startElement.getAttributes();
		    while (attributes.hasNext()) {
			Attribute attribute = attributes.next();
			if (attribute.getName().toString().equals(ID)) {
			    result.setId(Integer.parseInt(attribute.getValue()));
			}

		    }
		} else if (startElement.getName().getLocalPart().equals(TITLE)) {
		    event = eventReader.nextEvent();
		    result.setTitle(event.toString());
		} else if (startElement.getName().getLocalPart().equals(CITY)) {
		    event = eventReader.nextEvent();
		    result.setCity(event.toString());
		} else if (startElement.getName().getLocalPart().equals(STATE)) {
		    event = eventReader.nextEvent();
		    result.setState(event.toString());
		} else if (startElement.getName().getLocalPart()
			.equals(LATITUDE)) {
		    event = eventReader.nextEvent();
		    result.setLatitude(Double.parseDouble(event.toString()));
		} else if (startElement.getName().getLocalPart()
			.equals(LONGITUDE)) {
		    event = eventReader.nextEvent();
		    result.setLongitude(Double.parseDouble(event.toString()));
		} else if (startElement.getName().getLocalPart().equals(RATING)) {
		    rating = new Rating();
		} else if (startElement.getName().getLocalPart()
			.equals(AVERAGE_RATING)) {
		    event = eventReader.nextEvent();
		    if (event.toString().equals(Rating.NO_AVERAGE_RATING_FLAG)) {
			rating.setAverageRating(Rating.noAverageRatingValue);
		    } else {
			rating.setAverageRating(Double.parseDouble(event
				.toString()));
		    }
		} else if (startElement.getName().getLocalPart()
			.equals(TOTAL_RATINGS)) {
		    event = eventReader.nextEvent();
		    rating.setTotalRatings(Integer.parseInt(event.toString()));
		} else if (startElement.getName().getLocalPart()
			.equals(TOTAL_REVIEWS)) {
		    event = eventReader.nextEvent();
		    rating.setTotalReviews(Integer.parseInt(event.toString()));
		}
		if (startElement.getName().getLocalPart()
			.equals(LAST_REVIEW_DATE)) {
		    event = eventReader.nextEvent();
		    rating.setLastReviewDate(event.toString());
		} else if (startElement.getName().getLocalPart()
			.equals(LAST_REVIEW_INTRO)) {
		    event = eventReader.nextEvent();
		    rating.setLastReviewIntro(event.toString());
		}
		//
		else if (startElement.getName().getLocalPart().equals(DISTANCE)) {
		    event = eventReader.nextEvent();
		    result.setDistance(Double.parseDouble(event.toString()));
		}
		//
		else if (startElement.getName().getLocalPart()
			.equals(CATEGORIES)) {
		    categories = new ArrayList<Category>();
		} else if (startElement.getName().getLocalPart()
			.equals(CATEGORY)) {
		    Category category = new Category();
		    Iterator<Attribute> attrCategories = startElement
			    .getAttributes();
		    while (attrCategories.hasNext()) {
			Attribute attribute = attrCategories.next();
			if (attribute.getName().toString().equals(ID)) {
			    category.setId(Integer.parseInt(attribute
				    .getValue()));
			}
		    }
		    event = eventReader.nextEvent();
		    category.setName(event.toString());
		    categories.add(category);
		}
	    }
	    // If we reach the end of an item element we add it to the list
	    else if (event.isEndElement()) {
		EndElement endElement = event.asEndElement();
		if (endElement.getName().getLocalPart().equals(RATING)) {
		    result.setRating(rating);
		} else if (endElement.getName().getLocalPart()
			.equals(CATEGORIES)) {
		    result.setCategories(categories);
		} else if (endElement.getName().getLocalPart().equals(RESULT)) {
		    results.add(result);
		}
	    }
	    resultSet.setXmlType(YahooXmlType.VALID);
	    resultSet.setPOIs(results);
	}
	return resultSet;
    }

}
