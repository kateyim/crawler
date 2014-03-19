/**
 * 
 */
package mo.umac.crawler.offline.yahoolocal;

import java.util.List;

import mo.umac.metadata.yahoolocal.APOI;
import mo.umac.metadata.yahoolocal.AQuery;
import mo.umac.metadata.yahoolocal.ResultSet;
import mo.umac.paint.PaintShapes;
import mo.umac.spatial.Circle;
import mo.umac.spatial.GeoOperator;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geomgraph.Position;

/**
 * The one dimensional crawler.
 * 
 * @author Kate
 * 
 */
public class OneDimensionalCrawler extends OfflineStrategy {

	public static Logger logger = Logger.getLogger(OneDimensionalCrawler.class.getName());

	public static ResultSetOneDimensional oneDimCrawl(String state, int category, String query, LineSegment middleLine) {
		ResultSetOneDimensional finalResultSet = new ResultSetOneDimensional();
		Coordinate up = middleLine.p0;
		Coordinate down = middleLine.p1;
		if (logger.isDebugEnabled()) {
			logger.debug("up = " + up.toString());
			logger.debug("down = " + down.toString());
		}
		// query the one end point
		// TODO only return
		AQuery aQuery = new AQuery(up, state, category, query, MAX_TOTAL_RESULTS_RETURNED);
		ResultSet resultSet = query(aQuery);
		if (logger.isDebugEnabled()) {
			logger.debug("resultSet.getPOIs().size() = " + resultSet.getPOIs().size());
		}
		List<APOI> resultPoints = resultSet.getPOIs();

		// if (logger.isDebugEnabled()) {
		// for (int i = 0; i < resultPoints.size(); i++) {
		// APOI aPoint = resultPoints.get(i);
		// logger.debug("APoint: " + aPoint.getId() + ", ["
		// + aPoint.getCoordinate().toString() + "]");
		// }
		// }

		// farthest point lower than the up point
		Coordinate farthestCoordinate = farthest(resultSet, up, true);
		if (farthestCoordinate == null) {
			logger.error("farestest point is null");
			// farthestCoordinate = farthest(resultSet, up, true);
		}
		double radius = up.distance(farthestCoordinate);
		if (logger.isDebugEnabled()) {
			logger.debug("farthestCoordinate = " + farthestCoordinate.toString());
			logger.debug("radius = " + radius);
		}
		Circle aCircle = new Circle(up, radius);
		resultSet.addACircle(aCircle);
		double newUp = up.y + radius;
		if (logger.isDebugEnabled()) {
			logger.debug("new up = " + newUp);
		}
		//
		if (logger.isDebugEnabled() && PaintShapes.painting) {

			PaintShapes.paint.color = PaintShapes.paint.redTranslucence;
			PaintShapes.paint.addCircle(aCircle);
			PaintShapes.paint.myRepaint();
		}
		addResults(up, middleLine, finalResultSet, resultSet);
		if (logger.isDebugEnabled()) {
			logger.debug("middleLine.getLength() = " + middleLine.getLength());
		}
		if (radius >= middleLine.getLength()) {
			// finished crawling
			if (logger.isDebugEnabled()) {
				logger.debug("finished crawling");
			}
			return finalResultSet;
		}

		// query another end point
		aQuery = new AQuery(down, state, category, query, MAX_TOTAL_RESULTS_RETURNED);
		resultSet = query(aQuery);
		if (logger.isDebugEnabled()) {
			logger.debug("resultSet.getPOIs().size() = " + resultSet.getPOIs().size());
		}
		resultPoints = resultSet.getPOIs();

		// if (logger.isDebugEnabled()) {
		// for (int i = 0; i < resultPoints.size(); i++) {
		// APOI aPoint = resultPoints.get(i);
		// logger.debug("APoint: " + aPoint.getId() + ", ["
		// + aPoint.getCoordinate().toString() + "]");
		// }
		// }

		// farthest point lower than the up point
		farthestCoordinate = farthest(resultSet, down, false);
		if (farthestCoordinate == null) {
			logger.error("farestest point is null");
			// farthestCoordinate = farthest(resultSet, up, true);
		}
		radius = down.distance(farthestCoordinate);
		if (logger.isDebugEnabled()) {
			logger.debug("farthestCoordinate = " + farthestCoordinate.toString());
			logger.debug("radius = " + radius);
		}
		aCircle = new Circle(down, radius);
		resultSet.addACircle(aCircle);
		double newDown = down.y - radius;
		if (logger.isDebugEnabled()) {
			logger.debug("new down = " + newDown);
		}
		//
		if (logger.isDebugEnabled() && PaintShapes.painting) {
			PaintShapes.paint.color = PaintShapes.paint.redTranslucence;
			PaintShapes.paint.addCircle(aCircle);
			PaintShapes.paint.myRepaint();
		}
		addResults(down, middleLine, finalResultSet, resultSet);
		if (logger.isDebugEnabled()) {
			logger.debug("middleLine.getLength() = " + middleLine.getLength());
		}
		if (radius >= down.y - newUp) {
			// finished crawling
			if (logger.isDebugEnabled()) {
				logger.debug("finished crawling");
			}
			return finalResultSet;
		}

		LineSegment newMiddleLine = new LineSegment(up.x, newUp, up.x, newDown);
		ResultSetOneDimensional middleResultSet = oneDimCrawlFromMiddle(state, category, query, newMiddleLine);
		addResults(finalResultSet, middleResultSet);
		return finalResultSet;
	}
	
	private void queryOnePointOneD1(){
		
	}

	/**
	 * Begin at the center of the line
	 * 
	 * 
	 * @param state
	 * @param category
	 * @param query
	 * @param envelopeState
	 * @param middleLine
	 * @return
	 */
	public static ResultSetOneDimensional oneDimCrawlFromMiddle(String state, int category, String query, LineSegment middleLine) {
		ResultSetOneDimensional finalResultSet = new ResultSetOneDimensional();
		Coordinate up = middleLine.p0;
		Coordinate down = middleLine.p1;
		Coordinate center = middleLine.midPoint();
		// add at 2013-08-21

		if (logger.isDebugEnabled()) {
			logger.debug("up = " + up.toString());
			logger.debug("down = " + down.toString());
			logger.debug("center = " + center.toString());
		}
		AQuery aQuery = new AQuery(center, state, category, query, MAX_TOTAL_RESULTS_RETURNED);
		ResultSet resultSet = query(aQuery);
		if (logger.isDebugEnabled()) {
			logger.debug("resultSet.getPOIs().size() = " + resultSet.getPOIs().size());
		}
		List<APOI> resultPoints = resultSet.getPOIs();

		// if (logger.isDebugEnabled()) {
		// for (int i = 0; i < resultPoints.size(); i++) {
		// APOI aPoint = resultPoints.get(i);
		// logger.debug("APoint: " + aPoint.getId() + ", ["
		// + aPoint.getCoordinate().toString() + "]");
		// }
		// }

		Coordinate farthestCoordinate = farthest(resultSet);
		double radius = center.distance(farthestCoordinate);
		if (logger.isDebugEnabled()) {
			logger.debug("farthestCoordinate = " + farthestCoordinate.toString());
			logger.debug("radius = " + radius);
		}
		Circle aCircle = new Circle(center, radius);
		resultSet.addACircle(aCircle);
		//
		if (logger.isDebugEnabled() && PaintShapes.painting) {
			PaintShapes.paint.color = PaintShapes.paint.redTranslucence;
			PaintShapes.paint.addCircle(aCircle);
			PaintShapes.paint.myRepaint();
		}
		addResults(center, middleLine, finalResultSet, resultSet);
		if (logger.isDebugEnabled()) {
			logger.debug("middleLine.getLength() / 2 = " + middleLine.getLength() / 2);
		}
		if (radius >= middleLine.getLength() / 2) {
			// finished crawling
			if (logger.isDebugEnabled()) {
				logger.debug("finished crawling");
			}
			return finalResultSet;
		}

		// recursively crawl
		// upper
		// Coordinate newRight = middleLine.pointAlongOffset(0.5, -radius);
		Coordinate newDown = newDown(center, radius);
		LineSegment upperLine = new LineSegment(up, newDown);
		if (logger.isDebugEnabled()) {
			logger.debug("newDown: " + newDown.toString());
			logger.debug("upperLine: " + upperLine.toString());
		}
		ResultSetOneDimensional newLeftResultSet = oneDimCrawlFromMiddle(state, category, query, upperLine);
		addResults(finalResultSet, newLeftResultSet);
		// lower
		// Coordinate newLeft = middleLine.pointAlongOffset(0.5, radius);
		Coordinate newUp = newUp(center, radius);
		LineSegment lowerLine = new LineSegment(newUp, down);
		if (logger.isDebugEnabled()) {
			logger.debug("newUp: " + newUp.toString());
			logger.debug("lowerLine: " + lowerLine.toString());
		}
		ResultSetOneDimensional newRightResultSet = oneDimCrawlFromMiddle(state, category, query, lowerLine);
		addResults(finalResultSet, newRightResultSet);

		return finalResultSet;
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
	 * Find the farthest point which is lower/higher than the point p
	 * 
	 * @param resultSet
	 *            , already ranged by the distance
	 * @param p
	 * @param lower
	 * @return
	 */
	private static Coordinate farthest(ResultSet resultSet, Coordinate p, boolean lower) {
		int size = resultSet.getPOIs().size();
		if (size == 0) {
			return null;
		} else {
			// for (int i = size - 1; i >= 0; i--) {
			// APOI farthestPOI = resultSet.getPOIs().get(i);
			// Coordinate c = farthestPOI.getCoordinate();
			// if (lower) {
			// if (c.y > p.y) {
			// return c;
			// }
			// } else {
			// if (c.y < p.y) {
			// return c;
			// }
			// }
			// }
			// add at 2013-10-01
			// FIXME revised farthest!!!
			return resultSet.getPOIs().get(size - 1).getCoordinate();
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
