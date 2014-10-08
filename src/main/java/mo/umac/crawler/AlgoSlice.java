package mo.umac.crawler;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mo.umac.metadata.APOI;
import mo.umac.metadata.ResultSetD1;
import mo.umac.spatial.Circle;

import paint.PaintShapes;
import utils.GeoOperator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;

/**
 * Cover the nearest point, or the biggest rectangle of the union of a list of circles.
 * 
 * @author kate
 */
public class AlgoSlice extends Strategy {

	// public static Logger logger = Logger.getLogger(AlgoSlice.class.getName());

	public AlgoSlice() {
		super();
		// logger.info("------------SliceCrawler------------");
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
		if (logger.isDebugEnabled()) {
			logger.info("------------SliceCrawler---------" + envelope.toString());
		}
		// finished crawling
		if (envelope == null) {
			return;
		}
		// left
		LineSegment leftLine = leftLine(envelope);
		//
		if (logger.isDebugEnabled()) {
			logger.debug("leftLine = " + leftLine.toString());
		}
		//
		ResultSetD1 oneDimensionalResultSet = CrawlerD1.oneDimCrawl(state, category, query, leftLine);
		oneDimensionalResultSet.setLine(leftLine);

		sortingCircles(oneDimensionalResultSet);

		double distanceX1 = distanceCovered(envelope, oneDimensionalResultSet);
		if (logger.isDebugEnabled()) {
			logger.debug("distanceX1 = " + distanceX1);
		}
		//
		if (distanceX1 >= envelope.getMaxX() - envelope.getMinX()) {
			// this envelope has been covered, finished crawling;
			if (logger.isDebugEnabled()) {
				logger.debug("this envelope is covered by the one dimensional crawler");
				logger.debug(envelope.toString());
				if (PaintShapes.painting) {
					PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
					PaintShapes.paint.addRectangle(envelope);
					PaintShapes.paint.myRepaint();
				}
			}
			Strategy.rtreeRectangles.addRectangle(rectangleId++, envelope);
			return;
		} else {
			Envelope envelopeDistanceX = new Envelope(envelope.getMinX(), envelope.getMinX() + distanceX1, envelope.getMinY(), envelope.getMaxY());
			if (logger.isDebugEnabled()) {
				logger.debug("envelopeDistanceX is covered by the one dimensional crawler");
				logger.debug(envelopeDistanceX.toString());
				if (PaintShapes.painting) {

					PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
					PaintShapes.paint.addRectangle(envelopeDistanceX);
					PaintShapes.paint.myRepaint();
				}
			}
			Strategy.rtreeRectangles.addRectangle(rectangleId++, envelopeDistanceX);
		}

		// right
		LineSegment rightLine = rightLine(envelope);
		//
		if (logger.isDebugEnabled()) {
			logger.debug("rightLine = " + rightLine.toString());
		}
		//
		oneDimensionalResultSet = CrawlerD1.oneDimCrawl(state, category, query, rightLine);
		oneDimensionalResultSet.setLine(rightLine);

		sortingCircles(oneDimensionalResultSet);

		double distanceX2 = distanceCovered(envelope, oneDimensionalResultSet);
		if (logger.isDebugEnabled()) {
			logger.debug("distanceX2 = " + distanceX2);
		}
		//
		if (distanceX2 >= envelope.getMaxX() - envelope.getMinX()) {
			// this envelope has been covered, finished crawling;
			if (logger.isDebugEnabled()) {
				logger.debug("this envelope is covered by the one dimensional crawler");
				logger.debug(envelope.toString());
				if (PaintShapes.painting) {

					PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
					PaintShapes.paint.addRectangle(envelope);
					PaintShapes.paint.myRepaint();
				}
			}
			Strategy.rtreeRectangles.addRectangle(rectangleId++, envelope);
			return;
		} else {
			Envelope envelopeDistanceX = new Envelope(envelope.getMaxX() - distanceX2, envelope.getMaxX(), envelope.getMinY(), envelope.getMaxY());
			if (logger.isDebugEnabled()) {
				logger.debug("envelopeDistanceX is covered by the one dimensional crawler");
				logger.debug(envelopeDistanceX.toString());
				if (PaintShapes.painting) {

					PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
					PaintShapes.paint.addRectangle(envelopeDistanceX);
					PaintShapes.paint.myRepaint();
				}
			}
			Strategy.rtreeRectangles.addRectangle(rectangleId++, envelopeDistanceX);
		}

		Envelope middleEnvelope = new Envelope(envelope.getMinX() + distanceX1, envelope.getMaxX() - distanceX2, envelope.getMinY(), envelope.getMaxY());

		crawlFromMiddle(state, category, query, middleEnvelope);

	}

	public void crawlFromMiddle(String state, int category, String query, Envelope envelopeStateECEF) {

		// finished crawling
		if (envelopeStateECEF == null) {
			return;
		}
		// revised at 2014-10-7
		if (covered(envelopeStateECEF)) {
			if (logger.isDebugEnabled()) {
				logger.debug("This region has been covered");
			}
			return;
		}
		// first find the middle line, and then use the 1 dimensional method to issue queries on this line.
		LineSegment middleLine = middleLine(envelopeStateECEF);
		//
		if (logger.isDebugEnabled()) {
			logger.debug("middleLine = " + middleLine.toString());
		}
		//
		ResultSetD1 oneDimensionalResultSet = CrawlerD1.oneDimCrawl(state, category, query, middleLine);
		oneDimensionalResultSet.setLine(middleLine);

		sortingCircles(oneDimensionalResultSet);

		double distanceX = distanceCovered(envelopeStateECEF, oneDimensionalResultSet);
		if (logger.isDebugEnabled()) {
			logger.debug("distanceX = " + distanceX);
		}
		if (distanceX >= (envelopeStateECEF.getMaxX() - envelopeStateECEF.getMinX()) / 2) {
			// this envelope has been covered, finished crawling;
			if (logger.isDebugEnabled()) {
				logger.debug("this envelope is covered by the one dimensional crawler");
				logger.debug(envelopeStateECEF.toString());
				if (PaintShapes.painting) {

					PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
					PaintShapes.paint.addRectangle(envelopeStateECEF);
					PaintShapes.paint.myRepaint();
				}
			}
			Strategy.rtreeRectangles.addRectangle(rectangleId++, envelopeStateECEF);
			return;
		} else { 
			double middle = middleLine.p0.x;
			Envelope envelopeDistanceX = new Envelope(middle - distanceX, middle + distanceX, envelopeStateECEF.getMinY(), envelopeStateECEF.getMaxY());
			if (logger.isDebugEnabled()) {
				logger.debug("envelopeDistanceX is covered by the one dimensional crawler");
				logger.debug(envelopeDistanceX.toString());
				if (PaintShapes.painting) {

					PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
					PaintShapes.paint.addRectangle(envelopeDistanceX);
					PaintShapes.paint.myRepaint();
				}
			}
			Strategy.rtreeRectangles.addRectangle(rectangleId++, envelopeDistanceX);
		}

		// left
		double xLeftNearestCoordinates = xNearestLeftCoordinates(envelopeStateECEF, middleLine, oneDimensionalResultSet);
		// boarder line of the left region
		LineSegment leftBoarderLine = null;
		LineSegment leftDistanceXLine = parallel(middleLine, middleLine.p0.x - distanceX);
		if (xLeftNearestCoordinates >= envelopeStateECEF.getMinX()) {
			// has left coordinates
			if (logger.isDebugEnabled()) {
				logger.debug("xLeftNearestCoordinates = " + xLeftNearestCoordinates);
			}
			if (xLeftNearestCoordinates < middleLine.p0.x - distanceX) {
				leftBoarderLine = parallel(middleLine, xLeftNearestCoordinates);
				fillGaps(state, category, query, /* middleLine */
						leftDistanceXLine, leftBoarderLine, oneDimensionalResultSet);
				Envelope e = new Envelope(leftBoarderLine.p0, leftDistanceXLine.p1);
				if (logger.isDebugEnabled()) {
					logger.debug("finished covering the left region");
					logger.debug(e.toString());
					if (PaintShapes.painting) {

						PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
						PaintShapes.paint.addRectangle(envelopeStateECEF);
						PaintShapes.paint.myRepaint();
					}
				}
				Strategy.rtreeRectangles.addRectangle(rectangleId++, e);
			} else {
				leftBoarderLine = leftDistanceXLine;
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("no xLeftNearestCoordinates!");
			}
			leftBoarderLine = parallel(middleLine, middleLine.p0.x - distanceX);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("leftBoarderLine = " + leftBoarderLine.toString());
			if (PaintShapes.painting) {
				PaintShapes.paint.color = PaintShapes.paint.blackTranslucence;
				PaintShapes.paint.addLine(leftBoarderLine);
				PaintShapes.paint.myRepaint();
			}
		}

		Envelope leftRemainedEnvelope = leftRemainedRegion(envelopeStateECEF, leftBoarderLine);
		if (leftRemainedEnvelope != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("leftRemainedEnvelope = " + leftRemainedEnvelope.toString());
			}
			crawlFromMiddle(state, category, query, leftRemainedEnvelope);
		}
		// right
		double xRightNearestCoordinates = xNearestRightCoordinates(envelopeStateECEF, middleLine, oneDimensionalResultSet);
		LineSegment rightBoarderLine = null;
		LineSegment rightDistanceXLine = parallel(middleLine, middleLine.p0.x + distanceX);
		if (xRightNearestCoordinates <= envelopeStateECEF.getMaxX()) {
			if (logger.isDebugEnabled()) {
				logger.debug("xRightNearestCoordinates = " + xRightNearestCoordinates);
			}
			// boarder line of the right region
			if (xRightNearestCoordinates > middleLine.p0.x + distanceX) {
				rightBoarderLine = parallel(middleLine, xRightNearestCoordinates);
				fillGaps(state, category, query, /* middleLine */
						rightDistanceXLine, rightBoarderLine, oneDimensionalResultSet);
				Envelope e = new Envelope(rightDistanceXLine.p0, rightBoarderLine.p1);
				if (logger.isDebugEnabled()) {
					logger.debug("finished covering the right region");
					logger.debug(e.toString());
					if (PaintShapes.painting) {

						PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
						PaintShapes.paint.addRectangle(e);
						PaintShapes.paint.myRepaint();
					}
				}
				Strategy.rtreeRectangles.addRectangle(rectangleId++, e);
			} else {
				rightBoarderLine = parallel(middleLine, middleLine.p0.x + distanceX);
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("no xRightNearestCoordinates!");
			}
			rightBoarderLine = rightDistanceXLine;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("rightBoarderLine = " + rightBoarderLine.toString());
			if (PaintShapes.painting) {

				PaintShapes.paint.color = PaintShapes.paint.blackTranslucence;
				PaintShapes.paint.addLine(rightBoarderLine);
				PaintShapes.paint.myRepaint();
			}
		}
		// FIXME record this covered region & combine with previous covered
		// regions

		//
		Envelope rightRemainedEnvelope = rightRemainedRegion(envelopeStateECEF, rightBoarderLine);
		if (rightRemainedEnvelope != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("rightRemainedEnvelope = " + rightRemainedEnvelope.toString());
			}
			crawlFromMiddle(state, category, query, rightRemainedEnvelope);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("finished covering this region");
			logger.debug(envelopeStateECEF.toString());
			if (PaintShapes.painting) {

				PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
				PaintShapes.paint.addRectangle(envelopeStateECEF);
				PaintShapes.paint.myRepaint();
			}
		}
		Strategy.rtreeRectangles.addRectangle(rectangleId++, envelopeStateECEF);
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
	 * find the gaps, and then cover the gaps
	 * 
	 * @param state
	 *            TODO
	 * @param category
	 *            TODO
	 * @param query
	 *            TODO
	 * @param middleLine
	 * @param oneDimensionalResultSet
	 * @param envelopeState
	 * @param envelopeNY
	 */
	private void fillGaps(String state, int category, String query, LineSegment middleLine, LineSegment boardLine, ResultSetD1 oneDimensionalResultSet) {
		if (logger.isDebugEnabled()) {
			logger.debug("...............fillGaps................");
		}
		// All of these circles are sorted in the line.
		double xMiddleLine = middleLine.p0.x;
		double xBoardLine = boardLine.p0.x;
		double yBegin;
		double yEnd;
		if (boardLine.p0.y < boardLine.p1.y) {
			yBegin = boardLine.p0.y;
			yEnd = boardLine.p1.y;
		} else {
			yBegin = boardLine.p1.y;
			yEnd = boardLine.p0.y;
		}
		double y1 = 0, y2 = 0;
		double yNewBegin = 0, yNewEnd = 0;
		double yLastEnd = yBegin;
		boolean firstCircle = true;
		List<Circle> circles = oneDimensionalResultSet.getCircles();
		for (int i = 0; i < circles.size(); i++) {
			if (logger.isDebugEnabled()) {
				logger.debug("yLastEnd = " + yLastEnd);
			}
			Circle circle = circles.get(i);
			List<Coordinate> list = GeoOperator.intersect(circle, boardLine);
			if (logger.isDebugEnabled()) {
				logger.debug("intersection of circle: " + circle.getCenter().toString() + ", " + circle.getRadius() + " with line: " + boardLine.toString()
						+ " is: ");
				if (list == null) {
					logger.error("not intersect with a circle");
				} else {
					for (int j = 0; j < list.size(); j++) {
						logger.debug(list.get(j).toString());
					}
					if (list.size() == 0) {
						logger.debug("Didn't intersect with the circle");
						// already covered
					}
				}
			}
			if (list != null) {

				if (list.size() == 1) {
					y1 = list.get(0).y;
					if (logger.isDebugEnabled()) {
						logger.debug("Only one intersect point");
						logger.debug("y1 = " + y1);
					}
					// whether tangent?
					if (Math.abs(xBoardLine - y1) < Strategy.EPSILON) {
						if (logger.isDebugEnabled()) {
							logger.debug("tangent");
						}
						// yes
						yNewBegin = y1;
						yNewEnd = y1;
					} else {
						// not tangent
						if (logger.isDebugEnabled()) {
							logger.debug("not tangent");
						}
						if (y1 < yEnd && !firstCircle) {// the last circle
							yNewBegin = y1;
							yNewEnd = yEnd + 1;
						}
						if (y1 > yBegin && firstCircle) {// the first circle
							yNewBegin = yBegin - 1;
							yNewEnd = y1;
							firstCircle = false;
						}
					}
				}
				//
				if (list.size() == 2) {
					if (logger.isDebugEnabled()) {
						logger.debug("two intersect points");
					}
					yNewBegin = list.get(0).y;
					yNewEnd = list.get(1).y;
					if (logger.isDebugEnabled()) {
						logger.debug("y1 = " + list.get(0).y);
						logger.debug("y2 = " + list.get(1).y);
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug("yNewBegin = " + yNewBegin);
					logger.debug("yNewEnd = " + yNewEnd);
				}
				if (yNewBegin > yLastEnd) {
					// not covered
					Envelope smallEnvelope = new Envelope(xMiddleLine, xBoardLine, yLastEnd, yNewBegin);
					if (logger.isDebugEnabled()) {
						logger.debug("smallEnvelope = " + smallEnvelope.toString());
						if (PaintShapes.painting) {

							PaintShapes.paint.color = PaintShapes.paint.greenTranslucence;
							PaintShapes.paint.addRectangle(smallEnvelope);
							PaintShapes.paint.myRepaint();
						}
					}
					crawl(state, category, query, smallEnvelope);
				}
				yLastEnd = yNewEnd;
			}
		}
		// the last one
		if (yLastEnd < yEnd) {
			Envelope smallEnvelope = new Envelope(xMiddleLine, xBoardLine, yLastEnd, yEnd);
			if (logger.isDebugEnabled()) {
				logger.debug("smallEnvelope = " + smallEnvelope.toString());
				if (PaintShapes.painting) {

					PaintShapes.paint.color = PaintShapes.paint.greenTranslucence;
					PaintShapes.paint.addRectangle(smallEnvelope);
					PaintShapes.paint.myRepaint();
				}
			}
			crawl(state, category, query, smallEnvelope);
		}
	}

	/**
	 * Find the remained envelope need be crawled later
	 * 
	 * @param envelopeState
	 * @param leftRightNearestEnvelope
	 * @return
	 */
	private Envelope leftRemainedRegion(Envelope envelopeState, LineSegment leftBoraderLine) {
		double minX = envelopeState.getMinX();
		double y1 = envelopeState.getMinY();
		double y2 = envelopeState.getMaxY();
		//
		if (logger.isDebugEnabled()) {
			logger.debug("--------------leftRemainedRegion");
			logger.debug("envelopeState = " + envelopeState);
			if (leftBoraderLine == null) {
				logger.debug("leftBoraderLine = null");
			}
		}
		//
		Envelope leftRemainedEnvelope;
		double maxXLeft = leftBoraderLine.p0.x;
		if (maxXLeft > minX) {
			leftRemainedEnvelope = new Envelope(minX, maxXLeft, y1, y2);
		} else {
			leftRemainedEnvelope = null;
		}

		return leftRemainedEnvelope;
	}

	private Envelope rightRemainedRegion(Envelope envelopeState, LineSegment rightBoraderLine) {
		double maxX = envelopeState.getMaxX();
		double y1 = envelopeState.getMinY();
		double y2 = envelopeState.getMaxY();
		if (logger.isDebugEnabled()) {
			logger.debug("--------------rightRemainedRegion");
			logger.debug("envelopeState = " + envelopeState);
			if (rightBoraderLine == null) {
				logger.debug("rightBoraderLine = null");
			}
		}
		Envelope rightRemainedEnvelope;
		double minXRight = rightBoraderLine.p0.x;
		if (minXRight < maxX) {
			rightRemainedEnvelope = new Envelope(minXRight, maxX, y1, y2);
		} else {
			rightRemainedEnvelope = null;
		}

		return rightRemainedEnvelope;
	}

	/**
	 * Find the nearest left and right Coordinates to the middle line. But not
	 * in the middle line
	 * 
	 * @param envelopeState
	 * @param middleLine
	 * @param oneDimensionalResultSet
	 * @return the left & the right nearest point
	 */
	private double xNearestLeftCoordinates(Envelope envelopeState, LineSegment middleLine, ResultSetD1 oneDimensionalResultSet) {
		double minXBoundary = envelopeState.getMinX() - 1;
		double bigX = minXBoundary;
		List<APOI> leftPOIs = oneDimensionalResultSet.getLeftPOIs();
		for (int i = 0; i < leftPOIs.size(); i++) {
			APOI point = leftPOIs.get(i);
			double x = point.getCoordinate().x;
			if (x > bigX) {
				bigX = x;
			}
		}

		return bigX;
	}

	private double xNearestRightCoordinates(Envelope envelopeState, LineSegment middleLine, ResultSetD1 oneDimensionalResultSet) {
		double maxXBoundary = envelopeState.getMaxX() + 1;
		double smallX = maxXBoundary;
		List<APOI> rightPOIs = oneDimensionalResultSet.getRightPOIs();
		for (int i = 0; i < rightPOIs.size(); i++) {
			APOI point = rightPOIs.get(i);
			double x = point.getCoordinate().x;
			if (x < smallX) {
				smallX = x;
			}
		}
		return smallX;
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

	private LineSegment leftLine(Envelope envelopeState) {
		double x = envelopeState.getMinX();
		double y0 = envelopeState.getMinY();
		double y1 = envelopeState.getMaxY();

		LineSegment middleLine = new LineSegment(x, y0, x, y1);
		return middleLine;
	}

	private LineSegment rightLine(Envelope envelopeState) {
		double x = envelopeState.getMaxX();
		double y0 = envelopeState.getMinY();
		double y1 = envelopeState.getMaxY();

		LineSegment middleLine = new LineSegment(x, y0, x, y1);
		return middleLine;
	}

	/**
	 * Compute the line segment which is parallel to the {@value middleLine} and
	 * get through to the {@value outsidePoint}
	 * 
	 * @param middleLine
	 * @param outsidePoint
	 * @return
	 */
	private LineSegment parallel(LineSegment middleLine, double xOutsideCoordinate) {
		Coordinate p0 = middleLine.p0;
		Coordinate p1 = middleLine.p1;
		double y0 = p0.y;
		double y1 = p1.y;
		LineSegment lineSeg = new LineSegment(xOutsideCoordinate, y0, xOutsideCoordinate, y1);
		return lineSeg;
	}

}
