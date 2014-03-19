/**
 * 
 */
package mo.umac.crawler.offline.yahoolocal;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import mo.umac.main.CrawlerStrategy;
import mo.umac.metadata.yahoolocal.AQuery;
import mo.umac.metadata.yahoolocal.ResultSet;
import mo.umac.paint.PaintShapes;
import mo.umac.rtree.MyRTree;
import mo.umac.spatial.Circle;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * The heuristic algorithm in the paper
 * 
 * @author kate
 * 
 */
public class HexagonCrawler extends OfflineStrategy {

	public static Logger logger = Logger.getLogger(HexagonCrawler.class.getName());

	public HexagonCrawler() {
		super();
		logger.info("------------HexagonCrawler------------");
	}

	/**
	 * The parameter of shrinking the starting circle
	 */
	public static double alpha = 0.8;

	/*
	 * (non-Javadoc)
	 * @see mo.umac.crawler.offline.OfflineStrategy#crawl(java.lang.String, int,
	 * java.lang.String, com.vividsolutions.jts.geom.Envelope)
	 */
	@Override
	public void crawl(String state, int category, String query, Envelope envelope) {
		if (logger.isDebugEnabled()) {
			logger.info("------------crawling---------");
			logger.info(envelope.toString());
		}
		// finished crawling
		if (envelope == null) {
			return;
		}
		boolean heuristic = true;
		while (heuristic) {
			// issue a query randomly at the envelope
			// Coordinate start = random(envelope);
			Coordinate start = envelope.centre();

			if (!coveredPoint(CrawlerStrategy.rtreeRectangles, start)) {
				Queue<Coordinate> queue = new LinkedList<Coordinate>();
				queue.add(start);
				beginAClique(state, category, query, queue);
			} else {
				continue;
			}
			//
			heuristic = continueHeuristic(CrawlerStrategy.rtreeRectangles);
		}
		// fill the gaps with upper bound algorithm
		boolean finished = false;
		while (!finished) {
			Coordinate start = random(envelope);
			if (!coveredPoint(CrawlerStrategy.rtreeRectangles, start)) {
				Envelope aRectangle = expand(state, category, query, start);
				// SliceCrawler
				SliceCrawler sliceCrawler = new SliceCrawler();
				sliceCrawler.crawl(state, category, query, aRectangle);
			} else {
				continue;
			}
			//
			finished = covered(envelope);
		}
	}

	/**
	 * Expand to a rectangle
	 * 
	 * @param state
	 * @param category
	 * @param query
	 * @param start
	 * @return
	 */
	private Envelope expand(String state, int category, String query, Coordinate start) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * whether this point is covered by the rectangles which are already being
	 * crawled
	 * 
	 * @param rtreeRectangles
	 * @param start
	 * @return
	 */
	private boolean coveredPoint(MyRTree rtreeRectangles, Coordinate start) {
		return rtreeRectangles.contains(start);
	}

	/**
	 * Whether we should stop random choose the center
	 * 
	 * @param rtreeRectangles
	 * 
	 * @return
	 */
	private boolean continueHeuristic(MyRTree rtreeRectangles) {
		boolean continueHeuristic = false;
		// TODO next time: change to: the covered rectangles have covered half of the x-axis
		int num = rtreeRectangles.size();
		if (num > 10) {
			continueHeuristic = true;
		}

		return continueHeuristic;
	}

	/**
	 * Randomly choose a point at the envelope
	 * 
	 * @param envelope
	 * @return
	 */
	private Coordinate random(Envelope envelope) {
		double xMin = envelope.getMinX();
		double yMin = envelope.getMinY();
		double xMax = envelope.getMaxX();
		double yMax = envelope.getMaxY();

		Random r = new Random();
		double x = xMin + (xMax - xMin) * r.nextDouble();
		double y = yMin + (yMax - yMin) * r.nextDouble();

		Coordinate p = new Coordinate(x, y);

		return p;
	}

	/**
	 * Build the clique from a center point
	 * 
	 */
	private void beginAClique(String state, int category, String query, Queue<Coordinate> queue) {
		List circleList = new ArrayList<Circle>();
		// deal with the first point
		Coordinate center = queue.poll();
		ResultSet resultSet = oneQueryProcedure(state, category, query, center);
		Circle aCircle = resultSet.getCircles().get(0);

		if (logger.isDebugEnabled() && PaintShapes.painting) {
			PaintShapes.paint.color = PaintShapes.paint.redTranslucence;
			PaintShapes.paint.addCircle(aCircle);
			PaintShapes.paint.myRepaint();
		}
		Envelope regionRectangle = computeCoveredRegion(aCircle);
		CrawlerStrategy.rtreeRectangles.addRectangle(rectangleId++, regionRectangle);
		circleList.add(aCircle);
		double radius = aCircle.getRadius();
		double radiusAlpha = radius * alpha;
		// cover one edge of the smaller hexagon
		double minRadius = radiusAlpha;
		// cover the whole smaller hexagon
		double maxRadius = (Math.sqrt(3) + 1) * radiusAlpha;
		//
		List<Coordinate> nextCenters = aroundPoints(center, radiusAlpha);
		queue.addAll(nextCenters);

		if (logger.isDebugEnabled() && PaintShapes.painting) {
			PaintShapes.paint.color = Color.RED;
			for (int i = 0; i < nextCenters.size(); i++) {
				PaintShapes.paint.addPoint(nextCenters.get(i));
			}
			PaintShapes.paint.myRepaint();
		}

		// deal with other points
		while (!queue.isEmpty()) {
			Coordinate coordinate = queue.poll();
			resultSet = oneQueryProcedure(state, category, query, coordinate);
			aCircle = resultSet.getCircles().get(0);

			if (logger.isDebugEnabled() && PaintShapes.painting) {
				PaintShapes.paint.color = PaintShapes.paint.redTranslucence;
				PaintShapes.paint.addCircle(aCircle);
				PaintShapes.paint.myRepaint();
			}

			regionRectangle = computeCoveredRegion(aCircle);

			// if (logger.isDebugEnabled() && PaintShapes.painting) {
			// PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
			// PaintShapes.paint.addRectangle(regionRectangle);
			// PaintShapes.paint.myRepaint();
			// }

			CrawlerStrategy.rtreeRectangles.addRectangle(rectangleId++, regionRectangle);
			circleList.add(aCircle);
			radius = aCircle.getRadius();
			//
			if (radius < minRadius && radius > maxRadius) {
				// Envelope regionRectangle = computeCoveredRegion(circleList, center);
				// CrawlerStrategy.rtreeRectangles.addRectangle(rectangleId++, regionRectangle);
				break;
			}
			nextCenters = aroundPoints(coordinate, radiusAlpha);

			if (logger.isDebugEnabled() && PaintShapes.painting) {
				PaintShapes.paint.color = Color.RED;
				for (int i = 0; i < nextCenters.size(); i++) {
					PaintShapes.paint.addPoint(nextCenters.get(i));
				}
				PaintShapes.paint.myRepaint();
			}

			queue.addAll(nextCenters);
		}

	}

	/**
	 * Compute the covered rectangle of a circle
	 * 
	 * @param aCircle
	 * @return
	 */
	private Envelope computeCoveredRegion(Circle aCircle) {
		Coordinate center = aCircle.getCenter();
		double radius = aCircle.getRadius();
		double width = radius / Math.sqrt(2);
		double x1 = center.x - width;
		double x2 = center.x + width;
		double y1 = center.y - width;
		double y2 = center.y + width;

		Envelope coveredEnvelope = new Envelope(x1, x2, y1, y2);
		return coveredEnvelope;
	}

	/**
	 * The common procedure for a query
	 */
	private ResultSet oneQueryProcedure(String state, int category, String query, Coordinate center) {
		AQuery aQuery = new AQuery(center, state, category, query, MAX_TOTAL_RESULTS_RETURNED);
		ResultSet resultSet = query(aQuery);
		Coordinate farthestCoordinate = OneDimensionalCrawler.farthest(resultSet);
		if (farthestCoordinate == null) {
			logger.error("farestest point is null");
		}
		double radius = center.distance(farthestCoordinate);
		if (logger.isDebugEnabled()) {
			logger.debug("farthestCoordinate = " + farthestCoordinate.toString());
			logger.debug("radius = " + radius);
		}
		Circle aCircle = new Circle(center, radius);
		resultSet.addACircle(aCircle);
		return resultSet;
	}

	/**
	 * Get the next 6 issuing points from the center.
	 * 
	 * @param center
	 * @return
	 */
	private List<Coordinate> sixVertices(Coordinate center, double r) {
		List<Coordinate> nextCenters = new ArrayList<Coordinate>();

		double x0 = center.x;
		double y0 = center.y;
		//
		double x1 = x0 - r;
		double y1 = y0;
		Coordinate c1 = new Coordinate(x1, y1);
		nextCenters.add(c1);
		//
		double x2 = x0 - r / 2;
		double y2 = y0 + Math.sqrt(3) / 2 * r;
		Coordinate c2 = new Coordinate(x2, y2);
		nextCenters.add(c2);
		//
		double x3 = x0 + r / 2;
		double y3 = y0 + Math.sqrt(3) / 2 * r;
		Coordinate c3 = new Coordinate(x3, y3);
		nextCenters.add(c3);
		//
		double x4 = x0 - r;
		double y4 = y0;
		Coordinate c4 = new Coordinate(x4, y4);
		nextCenters.add(c4);
		//
		double x5 = x0 + r / 2;
		double y5 = y0 - Math.sqrt(3) / 2 * r;
		Coordinate c5 = new Coordinate(x5, y5);
		nextCenters.add(c5);
		//
		double x6 = x0 - r / 2;
		double y6 = y0 - Math.sqrt(3) / 2 * r;
		Coordinate c6 = new Coordinate(x6, y6);
		nextCenters.add(c6);

		return nextCenters;
	}

	private List<Coordinate> aroundPoints(Coordinate center, double r) {
		List<Coordinate> nextCenters = new ArrayList<Coordinate>();

		double x0 = center.x;
		double y0 = center.y;
		//
		double x1 = x0;
		double y1 = y0 + Math.sqrt(3) * r;
		Coordinate c1 = new Coordinate(x1, y1);
		nextCenters.add(c1);
		//
		double x2 = x0 + Math.sqrt(3) * r;
		double y2 = y0 + r;
		Coordinate c2 = new Coordinate(x2, y2);
		nextCenters.add(c2);
		//
		double x3 = x0 + Math.sqrt(3) * r;
		double y3 = y0 - r;
		Coordinate c3 = new Coordinate(x3, y3);
		nextCenters.add(c3);
		//
		double x4 = x0;
		double y4 = y0 - Math.sqrt(3) * r;
		Coordinate c4 = new Coordinate(x4, y4);
		nextCenters.add(c4);
		//
		double x5 = x0 - Math.sqrt(3) * r;
		double y5 = y0 - r;
		Coordinate c5 = new Coordinate(x5, y5);
		nextCenters.add(c5);
		//
		double x6 = x0 - Math.sqrt(3) * r;
		double y6 = y0 + r;
		Coordinate c6 = new Coordinate(x6, y6);
		nextCenters.add(c6);

		return nextCenters;
	}

}
