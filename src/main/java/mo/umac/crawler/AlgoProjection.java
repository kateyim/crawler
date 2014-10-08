package mo.umac.crawler;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import paint.PaintShapes;
import mo.umac.metadata.APOI;
import mo.umac.metadata.AQuery;
import mo.umac.metadata.ResultSetD1;
import mo.umac.metadata.ResultSetD2;
import mo.umac.spatial.Circle;

import com.infomatiq.jsi.Point;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;

/**
 * Cover the nearest point, or the biggest rectangle of the union of a list of circles.
 * 
 * @author kate
 */
public class AlgoProjection extends Strategy {

	// public static Logger logger = Logger.getLogger(AlgoSlice.class.getName());

	public AlgoProjection() {
		super();
		// logger.info("------------AlgoProjection------------");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mo.umac.crawler.OfflineYahooLocalCrawlerStrategy#crawl(java.lang.String,
	 * int, java.lang.String, com.vividsolutions.jts.geom.Envelope)
	 * This is the implementation of the upper bound algorithm.
	 */
	@Override
	public void crawl(String state, int category, String query, Envelope envelope) {
//		logger.info("------------AlgoProjection---------" + envelope.toString());
		// finished crawling
		if (envelope == null) {
			return;
		}

		// first find the middle line, and then use the 1 dimensional method to issue queries on this line.
		LineSegment middleLine = middleLine(envelope);
		//
		ResultSetD1 oneDimensionalResultSet = CrawlerD1.oneDimCrawlProject(state, category, query, middleLine);
		oneDimensionalResultSet.setLine(middleLine);

		sortingCircles(oneDimensionalResultSet);

		double distanceX = distanceCovered(envelope, oneDimensionalResultSet);
		if (distanceX >= (envelope.getMaxX() - envelope.getMinX()) / 2) {
			return;
		}
		double middle = middleLine.p0.x;
		if (logger.isDebugEnabled()) {
			Envelope coveredE = new Envelope(middle - distanceX, middle + distanceX, envelope.getMinY(), envelope.getMaxY());
			if (PaintShapes.painting) {
				PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
				PaintShapes.paint.addRectangle(coveredE);
				PaintShapes.paint.myRepaint();
			}
		}
		// left
		Envelope leftRemainedEnvelope = new Envelope(envelope.getMinX(), middle - distanceX, envelope.getMinY(), envelope.getMaxY());
		if (leftRemainedEnvelope != null) {
			crawl(state, category, query, leftRemainedEnvelope);
		}
		// right
		Envelope rightRemainedEnvelope = new Envelope(middle + distanceX, envelope.getMaxX(), envelope.getMinY(), envelope.getMaxY());
		if (rightRemainedEnvelope != null) {
			crawl(state, category, query, rightRemainedEnvelope);
		}
	}

	/**
	 * sorting these circles according to the y-value
	 * 
	 * @param oneDimensionalResultSet
	 */
	private void sortingCircles(ResultSetD1 oneDimensionalResultSet) {
		if (logger.isDebugEnabled()) {
			// print
			logger.debug("before sorting the circles: ");
			for (int i = 0; i < oneDimensionalResultSet.getCircles().size(); i++) {
				Circle circle = oneDimensionalResultSet.getCircles().get(i);
				logger.debug(circle.getCenter().toString());
			}
		}
		// sort all circles in the middle line
		Collections.sort(oneDimensionalResultSet.getCircles(), new CircleComparable());
		if (logger.isDebugEnabled()) {
			// print sorting results
			logger.debug("After sorting the circles: ");
			for (int i = 0; i < oneDimensionalResultSet.getCircles().size(); i++) {
				Circle circle = oneDimensionalResultSet.getCircles().get(i);
				logger.debug(circle.getCenter().toString());
			}
		}

	}

	public class CircleComparable implements Comparator<Circle> {
		@Override
		public int compare(Circle circle1, Circle circle2) {
			Coordinate center1 = circle1.getCenter();
			Coordinate center2 = circle2.getCenter();
			return center1.compareTo(center2);
		}
	}

	/**
	 * Compute the farthest distance (in the x-axis) of the covered region of
	 * the one dimensional results
	 * 
	 * @return
	 */
	private double distanceCovered(Envelope envelopeState, ResultSetD1 oneDimensionalResultSet) {
		List<Circle> circleList = oneDimensionalResultSet.getCircles();
		if (circleList == null) {
			logger.error(circleList == null);
		}
		// double minDistance = (envelopeState.getMaxX() -
		// envelopeState.getMinX()) / 2;
		// revised at 2013-08-21
		// It doesn't matter if we set a bigger distance initially. Mainly for
		// the left and the right line segment
		double minDistance = envelopeState.getMaxX() - envelopeState.getMinX();
		// intersect with the boarder line of the envelope
		Circle c1 = circleList.get(0);
		double r1 = c1.getRadius();
		double y1 = c1.getCenter().y;
		double distanceX;
		double y;
		double minY = envelopeState.getMinY();
		// intersection of the circle and the boarder line
		distanceX = Math.sqrt((r1 * r1 - (minY - y1) * (minY - y1)));
		if (distanceX < minDistance) {
			minDistance = distanceX;
		}
		// intersection of circles
		for (int i = 1; i < circleList.size(); i++) {
			Circle c2 = circleList.get(i);
			double r2 = c2.getRadius();
			Coordinate o2 = c2.getCenter();
			double y2 = o2.y;
			// the intersect points
			y = (r1 * r1 - r2 * r2 - (y1 * y1 - y2 * y2)) / (2 * (y2 - y1));
			distanceX = Math.sqrt(r1 * r1 - (y - y1) * (y - y1));
			if (distanceX < minDistance) {
				minDistance = distanceX;
			}
			c1 = c2;
			r1 = c1.getRadius();
			y1 = c1.getCenter().y;
		}
		double maxY = envelopeState.getMaxY();
		// intersection of the circle and the boarder line
		distanceX = Math.sqrt((r1 * r1 - (maxY - y1) * (maxY - y1)));
		if (distanceX < minDistance) {
			minDistance = distanceX;
		}

		return minDistance;
	}

	/**
	 * fine the middle line
	 * 
	 * @param envelopeState
	 * @return the longitude of the middle line
	 */
	private LineSegment middleLine(Envelope envelopeState) {
		double x0 = envelopeState.getMinX();
		double x1 = envelopeState.getMaxX();
		double x = (x0 + x1) / 2;
		double y0 = envelopeState.getMinY();
		double y1 = envelopeState.getMaxY();

		LineSegment middleLine = new LineSegment(x, y0, x, y1);
		return middleLine;
	}

}
