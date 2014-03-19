/**
 * 
 */
package mo.umac.metadata.yahoolocal;

import java.io.BufferedWriter;
import java.io.File;
import java.util.List;

import mo.umac.parser.YahooXmlType;

/**
 * ResultsSet (the xml page) of one query returned by Yahoo Local!
 * 
 * @author kate
 * 
 */
public class ResultSetYahooOnline extends ResultSet {

    // add at 2013-4-5
    /**
     * The page returned by Yahoo! Local. There are mainly two kinds of pages:
     * valid returned page, or an error page
     */
    private YahooXmlType xmlType = YahooXmlType.UNKNOWN;

    private String xmlFileName = "";

    private File xmlFile = null;

    private BufferedWriter resultsOutput;

    // private boolean limitExceeded = false;

    private boolean UnexpectedError = false;

    public ResultSetYahooOnline() {

    }

    public YahooXmlType getXmlType() {
	return xmlType;
    }

    public void setXmlType(YahooXmlType xmlType) {
	this.xmlType = xmlType;
    }

    public void setUnexpectedError(boolean unexpectedError) {
	UnexpectedError = unexpectedError;
    }

    public boolean isUnexpectedError() {
	return UnexpectedError;
    }

    public String getXmlFileName() {
	return xmlFileName;
    }

    public void setXmlFileName(String xmlFileName) {
	this.xmlFileName = xmlFileName;
    }

    public File getXmlFile() {
	return xmlFile;
    }

    public void setXmlFile(File xmlFile) {
	this.xmlFile = xmlFile;
    }

    public BufferedWriter getResultsOutput() {
	return resultsOutput;
    }

    public void setResultsOutput(BufferedWriter resultsOutput) {
	this.resultsOutput = resultsOutput;
    }

}
