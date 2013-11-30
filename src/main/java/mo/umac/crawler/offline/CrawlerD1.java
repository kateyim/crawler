package mo.umac.crawler.offline;

import java.util.ArrayList;
import java.util.List;

import mo.umac.metadata.APOI;
import mo.umac.metadata.AQuery;
import mo.umac.metadata.DefaultValues;
import mo.umac.metadata.ResultSet;
import mo.umac.paint.PaintShapes;
import mo.umac.spatial.Circle;
import mo.umac.spatial.GeoOperator;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geomgraph.Position;

/**
 * One dimensional crawler.
 * 
 * @author Kate
 * 
 */
public class CrawlerD1 extends OfflineStrategy {

	public static Logger logger = Logger.getLogger(CrawlerD1.class.getName());

	public static ResultSetOneDimensional oneDimCrawl(String state, int category, String query, LineSegment middleLine) {
		ResultSetOneDimensional finalResultSet = new ResultSetOneDimensional();
		Coordinate up = middleLine.p0;
		Coordinate down = middleLine.p1;
		if (logger.isDebugEnabled()) {
			logger.debug("up = " + up.toString());
			logger.debug("down = " + down.toString());
		}
		// query the one end point
		ResultSet resultSet = queryOnePointOnD1(state, category, query, middleLine, finalResultSet, up);
		double radius = resultSet.getCircles().get(0).getRadius();
		double newUp = up.y + radius;
		//
		if (logger.isDebugEnabled()) {
			logger.debug("new up = " + newUp);
			logger.debug("middleLine.getLength() = " + middleLine.getLength());
		}
		//
		if (radius >= middleLine.getLength()) {
			// finished crawling
			if (logger.isDebugEnabled()) {
				logger.debug("finished crawling");
			}
			return finalResultSet;
		}

		// query another end point
		resultSet = queryOnePointOnD1(state, category, query, middleLine, finalResultSet, down);
		radius = resultSet.getCircles().get(0).getRadius();
		double newDown = down.y - radius;
		if (logger.isDebugEnabled()) {
			logger.debug("new down = " + newDown);
		}

		if (radius >= down.y - newUp) {
			// finished crawling
			if (logger.isDebugEnabled()) {
				logger.debug("finished crawling");
			}
			return finalResultSet;
		}

		LineSegment newMiddleLine = new LineSegment(up.x, newUp, up.x, newDown);
		oneDimCrawlFromMiddle(state, category, query, newMiddleLine, finalResultSet);
		// addResults(finalResultSet, middleResultSet);
		return finalResultSet;
	}

	public static ResultSet queryOnePointOnD1(String state, int category, String query, LineSegment middleLine, ResultSetOneDimensional finalResultSet, Coordinate point) {
		AQuery aQuery = new AQuery(point, state, category, query, MAX_TOTAL_RESULTS_RETURNED);
		ResultSet resultSet = query(aQuery);
		if (logger.isDebugEnabled()) {
			logger.debug("resultSet.getPOIs().size() = " + resultSet.getPOIs().size());
		}
		double radius = farthestOnTheLine(resultSet, point);

		Circle aCircle = new Circle(point, radius);
		resultSet.addACircle(aCircle);

		addResults(point, middleLine, finalResultSet, resultSet);

		if (logger.isDebugEnabled() && PaintShapes.painting) {

			PaintShapes.paint.color = PaintShapes.paint.redTranslucence;
			PaintShapes.paint.addCircle(aCircle);
			PaintShapes.paint.myRepaint();
		}
		return resultSet;
	}

	/**
	 * Begin at the center of the line
	 * 
	 * 
	 * @param state
	 * @param category
	 * @param query
	 * @param middleLine
	 * @param finalResultSet
	 *            TODO
	 * @param envelopeState
	 * @return
	 */
	public static void oneDimCrawlFromMiddle(String state, int category, String query, LineSegment middleLine, ResultSetOneDimensional finalResultSet) {
		Coordinate up = middleLine.p0;
		Coordinate down = middleLine.p1;
		Coordinate center = middleLine.midPoint();
		// add at 2013-08-21
		if (logger.isDebugEnabled()) {
			logger.debug("up = " + up.toString());
			logger.debug("down = " + down.toString());
			logger.debug("center = " + center.toString());
		}

		ResultSet resultSet = queryOnePointOnD1(state, category, query, middleLine, finalResultSet, center);
		double radius = resultSet.getCircles().get(0).getRadius();

		if (logger.isDebugEnabled()) {
			logger.debug("middleLine.getLength() / 2 = " + middleLine.getLength() / 2);
		}
		if (radius > middleLine.getLength() / 2 && (radius - middleLine.getLength() / 2) > DefaultValues.Epsilon) {
			// finished crawling
			if (logger.isDebugEnabled()) {
				logger.debug("finished crawling");
			}
			return;
		} 
//		else if(radius == middleLine.getLength() / 2){
//			// TODO there are 2 circles tangency to each other, then add one more query at each tangency point
//			oneDimCrawlFromMiddle(state, category, query, middleLine, finalResultSet);
//			
//			
//		}

		// recursively crawl
		// upper
		// Coordinate newRight = middleLine.pointAlongOffset(0.5, -radius);
		Coordinate newDown = newDown(center, radius);
		LineSegment upperLine = new LineSegment(up, newDown);
		oneDimCrawlFromMiddle(state, category, query, upperLine, finalResultSet);
		// lower
		// Coordinate newLeft = middleLine.pointAlongOffset(0.5, radius);
		Coordinate newUp = newUp(center, radius);
		LineSegment lowerLine = new LineSegment(newUp, down);
		oneDimCrawlFromMiddle(state, category, query, lowerLine, finalResultSet);

		return;
	}

	/**
	 * This line is perpendicular, so it has the same x as center.x
	 * 
	 * @param center
	 * @param radius
	 * @return
	 */
	private static Coordinate newDown(Coordinate center, double radius) {
		Coordinate newDown = new Coordinate(center.x, center.y - radius);
		return newDown;
	}

	private static Coordinate newUp(Coordinate center, double radius) {
		Coordinate newDown = new Coordinate(center.x, center.y + radius);
		return newDown;
	}

	public static Coordinate farthest(ResultSet resultSet) {
		Coordinate farthestCoordinate;
		int size = resultSet.getPOIs().size();
		if (size == 0) {
			return null;
		} else {
			APOI farthestPOI = resultSet.getPOIs().get(size - 1);
			farthestCoordinate = farthestPOI.getCoordinate();
		}
		return farthestCoordinate;
	}

	/**
	 * Mapping all the crawled points into 1 dimensional space, (remain the same x)
	 * </p>
	 * and then find the farthest point
	 * 
	 * @param
	 * @return
	 */
	private static double farthestOnTheLine(ResultSet resultSet, Coordinate point) {
		// FIXME check
		ArrayList<APOI> listPOIs = (ArrayList<APOI>) resultSet.getPOIs();
		int size = listPOIs.size();
		if (size == 0) {
			logger.error("farestest point is null");
			return 0;
		} else {
			// get y-coordinate
			double maxY = listPOIs.get(0).getLatitude();
			double maxRadius = Math.abs(maxY - point.y);
			for (int i = 1; i < listPOIs.size(); i++) {
				double newY = listPOIs.get(i).getLatitude();
				double newRadius = Math.abs(newY - point.y);
				if (newRadius > maxRadius) {
					maxRadius = newRadius;
				}
			}
			return maxRadius;
		}
	}

	private static void addResults(Coordinate center, LineSegment line, ResultSetOneDimensional finalResultSet, ResultSet resultSet) {
		List<APOI> pois = resultSet.getPOIs();
		for (int i = 0; i < pois.size(); i++) {
			APOI poi = pois.get(i);
			int position = GeoOperator.findPosition(line, poi.getCoordinate());
			switch (position) {
			case Position.LEFT:
				finalResultSet.getLeftPOIs().add(poi);
				break;
			case Position.RIGHT:
				finalResultSet.getRightPOIs().add(poi);
				break;
			case Position.ON:
				finalResultSet.getOnPOIs().add(poi);
				break;
			}
		}
		finalResultSet.addAll(finalResultSet.getCircles(), resultSet.getCircles());
		finalResultSet.setNumQueries(finalResultSet.getNumQueries() + 1);
	}

	private static void addResults(ResultSetOneDimensional finalResultSet, ResultSetOneDimensional newResultSet) {
		finalResultSet.addAll(finalResultSet.getLeftPOIs(), newResultSet.getLeftPOIs());
		finalResultSet.addAll(finalResultSet.getRightPOIs(), newResultSet.getRightPOIs());
		finalResultSet.addAll(finalResultSet.getCircles(), newResultSet.getCircles());
	}

	@Override
	public void crawl(String state, int category, String query, Envelope envelopeState) {
		// TODO Auto-generated method stub

	}

}
