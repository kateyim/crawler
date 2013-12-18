package mo.umac.rtree;

import gnu.trove.TIntProcedure;
import gnu.trove.TIntStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import mo.umac.crawler.CrawlerStrategy;
import mo.umac.metadata.APOI;

import org.apache.log4j.Logger;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.Node;
import com.infomatiq.jsi.rtree.RTree;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class MyRTree extends RTree {

	public static Logger logger = Logger.getLogger(MyRTree.class.getName());

	/**
	 * For testing
	 * 
	 * @deprecated
	 */
	public static MyRTree rtree = new MyRTree();

	/**
	 * only used for covered rectangles
	 * <p>
	 * The map of all rectangles
	 */
	public static Map mapRectangleId = new HashMap<Integer, Rectangle>();

	public MyRTree() {
		Properties props = new Properties();
		props.setProperty("MaxNodeEntries", "50");
		props.setProperty("MinNodeEntries", "20");
		this.init(props);
	}

	/**
	 * @param points
	 * 
	 * @deprecated
	 */
	public MyRTree(List<Coordinate> points) {
		this();

		Rectangle tmpRect = null;
		float[] values = new float[2];

		for (int i = 0; i < points.size(); i++) {
			values[0] = (float) points.get(i).x;
			values[1] = (float) points.get(i).y;
			tmpRect = new Rectangle(values[0], values[1], values[0], values[1]);
			this.add(tmpRect, i);
		}
	}

	public MyRTree(HashMap<Integer, APOI> pois) {
		this();

		Rectangle tmpRect = null;
		float[] values = new float[2];

		for (Iterator iterator = pois.entrySet().iterator(); iterator.hasNext();) {
			Entry entry = (Entry) iterator.next();
			int id = (Integer) entry.getKey();
			APOI poi = (APOI) entry.getValue();

			values[0] = (float) poi.getCoordinate().x;
			values[1] = (float) poi.getCoordinate().y;
			tmpRect = new Rectangle(values[0], values[1], values[0], values[1]);
			this.add(tmpRect, id);
		}
	}

	public List<Integer> rangeSearch(Coordinate point, double range) {
		Point query = coordinateToPoint(point);
		AddToListProcedure v = new AddToListProcedure();

		this.nearestN(query, v, Integer.MAX_VALUE, (float) range);

		return v.getList();
	}

	public List<Integer> searchNN(Coordinate point, int N, double maxDistance) {
		Point query = coordinateToPoint(point);
		AddToListProcedure v = new AddToListProcedure();

		this.nearestN(query, v, N, (float) maxDistance);

		return v.getList();
	}

	public List<Integer> searchNN(Coordinate point, int N) {

		return this.searchNN(point, N, Float.MAX_VALUE);
	}

	public void addPoint(int pointId, Coordinate point) {
		Rectangle tmpRect = null;
		float[] values = new float[2];
		values[0] = (float) point.x;
		values[1] = (float) point.y;
		tmpRect = new Rectangle(values[0], values[1], values[0], values[1]);
		this.add(tmpRect, pointId);
	}

	/**
	 * @param rectangleId
	 * @param envelope
	 */
	public void addRectangle(int rectangleId, Envelope envelope) {
		Rectangle tmpRect = null;
		tmpRect = new Rectangle((float) envelope.getMinX(), (float) envelope.getMinY(), (float) envelope.getMaxX(), (float) envelope.getMaxY());
		this.add(tmpRect, rectangleId);
		// add at 2013-07-30
		mapRectangleId.put(rectangleId, tmpRect);
	}

	/**
	 * whether this point is contained by a rectangle
	 * 
	 * @param p
	 * @return
	 */
	public boolean contains(Coordinate p) {
		// stacks used to store nodeId and entry index of each node
		// from the root down to the leaf. Enables fast lookup
		// of nodes when a split is propagated up the tree.
		TIntStack parents = new TIntStack();
		// find a rectangle in the tree that contains the passed
		// rectangle
		// written to be non-recursive (should model other searches on this?)

		AddToListProcedure v = new AddToListProcedure();

		int rootNodeId = this.getRootNodeId();

		parents.reset();
		parents.push(rootNodeId);
		boolean contain = false;
		Rectangle rN;
		// CrawlerStrategy.rectangleId > 0 means that there are relative
		// rectangles
		while (CrawlerStrategy.rectangleId > 0 && parents.size() > 0) {
			Node n = getNode(parents.pop());
			if (logger.isDebugEnabled()) {
				rN = new Rectangle(n.mbrMinX, n.mbrMinY, n.mbrMaxX, n.mbrMaxY);
				logger.debug("");
				logger.debug(rN.toString());
				logger.debug("-------");
			}
			if (!n.isLeaf()) {
				// The children of n are not the leaves
				// go through every entry in the index node to check
				// if it intersects the passed rectangle. If so, it
				// could contain entries that are contained.
				contain = false;
				for (int i = 0; i < n.entryCount; i++) {
					if (logger.isDebugEnabled()) {
						rN = new Rectangle(n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i]);
						logger.debug(rN.toString());
					}
					Envelope nEnvelope = new Envelope(n.entriesMinX[i], n.entriesMaxX[i], n.entriesMinY[i], n.entriesMaxY[i]);
					if (nEnvelope.contains(p)) {
						// the first one contains the second one
						if (logger.isDebugEnabled()) {
							logger.debug("contained by a non-leaf node");
						}
						parents.push(n.ids[i]);
						contain = true;
						break;
					}
				}
				if (contain) {
					continue;
				} else {
					return false;
				}
			} else {
				// go through every entry in the leaf to check if
				// it is contained by the passed rectangle
				for (int i = 0; i < n.entryCount; i++) {
					if (logger.isDebugEnabled()) {
						rN = new Rectangle(n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i]);
						logger.debug(rN.toString());
					}
					Envelope nEnvelope = new Envelope(n.entriesMinX[i], n.entriesMaxX[i], n.entriesMinY[i], n.entriesMaxY[i]);
					if (nEnvelope.contains(p)) {
						// the objective rectangle is contained by a single
						// rectangle
						if (logger.isDebugEnabled()) {
							logger.debug("contained by a leaf node");
						}
						return true;
					}
				}
				return false;
			}
		}
		return false;
	}

	/**
	 * whether this envelope has been contained by any other envelope indexed
	 * before
	 * 
	 * @param envelope
	 * @return
	 */
	public boolean contains(Envelope envelope) {
		Rectangle r = new Rectangle((float) envelope.getMinX(), (float) envelope.getMinY(), (float) envelope.getMaxX(), (float) envelope.getMaxY());
		//
		AddToListProcedure v = new AddToListProcedure();
		int contain = containsStep1(r, v);
		if (contain == 1) {
			return true;
		} else if (contain == 0) {
			return false;
		}
		// else maybe
		List<Integer> list = v.getList();
		logger.debug(list.size());
		// FIXME wrong, the objective is bigger than all relative rectangles
		if (list != null && list.size() == 0) {
			return true;
		}

		// sorting the list
		ArrayList<Rectangle> listIntersectRectangles = new ArrayList<Rectangle>();
		for (int i = 0; i < list.size(); i++) {
			int id = list.get(i);
			logger.debug("id = " + id);
			Rectangle rr = (Rectangle) mapRectangleId.get(id);
			if (rr == null) {
				logger.debug("null rectangle");
			} else {
				logger.debug(rr.minX + ", " + rr.maxX + ", " + rr.minY + ", " + rr.maxY);
				listIntersectRectangles.add(rr);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("before sorting");
			for (int i = 0; i < listIntersectRectangles.size(); i++) {
				Rectangle a = listIntersectRectangles.get(i);
				logger.debug(a.toString());
			}
		}
		sorting(listIntersectRectangles);
		if (logger.isDebugEnabled()) {
			logger.debug("after sorting");
			for (int i = 0; i < listIntersectRectangles.size(); i++) {
				Rectangle a = listIntersectRectangles.get(i);
				logger.debug(a.toString());
			}
		}
		boolean contain2 = containsStep2(listIntersectRectangles, r);
		return contain2;
	}

	/**
	 * Looking for a rectangle fully covered the objective rectangle, or find a
	 * set of rectangles intersecting with the objective rectangle.
	 * 
	 * @param r
	 * @param v
	 * @return 1: contains 0: don't contain -1: maybe
	 */
	private int containsStep1(Rectangle r, TIntProcedure v) {
		// stacks used to store nodeId and entry index of each node
		// from the root down to the leaf. Enables fast lookup
		// of nodes when a split is propagated up the tree.
		TIntStack parents = new TIntStack();
		// find a rectangle in the tree that contains the passed
		// rectangle
		// written to be non-recursive (should model other searches on this?)

		int rootNodeId = this.getRootNodeId();

		parents.reset();
		parents.push(rootNodeId);
		boolean contain = false;
		Rectangle rN;
		// CrawlerStrategy.rectangleId > 0 means that there are relative
		// rectangles
		while (CrawlerStrategy.rectangleId > 0 && parents.size() > 0) {
			Node n = getNode(parents.pop());
			if (logger.isDebugEnabled()) {
				rN = new Rectangle(n.mbrMinX, n.mbrMinY, n.mbrMaxX, n.mbrMaxY);
				logger.debug("");
				logger.debug(rN.toString());
				logger.debug("-------");
			}
			if (!n.isLeaf()) {
				// The children of n are not the leaves
				// go through every entry in the index node to check
				// if it intersects the passed rectangle. If so, it
				// could contain entries that are contained.
				contain = false;
				for (int i = 0; i < n.entryCount; i++) {
					if (logger.isDebugEnabled()) {
						rN = new Rectangle(n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i]);
						logger.debug(rN.toString());
					}

					if (Rectangle.contains(n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i], r.minX, r.minY, r.maxX, r.maxY)) {
						// the first one contains the second one
						if (logger.isDebugEnabled()) {
							logger.debug("contained by a non-leaf node");
						}
						parents.push(n.ids[i]);
						contain = true;
						break;
					}
				}
				if (contain) {
					continue;
				} else {
					return 0;
				}
			} else {
				// go through every entry in the leaf to check if
				// it is contained by the passed rectangle
				for (int i = 0; i < n.entryCount; i++) {
					if (logger.isDebugEnabled()) {
						rN = new Rectangle(n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i]);
						logger.debug(rN.toString());
					}
					if (Rectangle.contains(n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i], r.minX, r.minY, r.maxX, r.maxY)) {
						// the objective rectangle is contained by a single
						// rectangle
						if (logger.isDebugEnabled()) {
							logger.debug("contained by a leaf node");
						}
						return 1;
					} else {
						if (Rectangle.intersects(r.minX, r.minY, r.maxX, r.maxY, n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i])) {
							if (logger.isDebugEnabled()) {
								logger.debug("intersect");
							}
							logger.debug("n.ids[i] = " + n.ids[i]);
							if (!v.execute(n.ids[i])) {
								// maybe
								logger.debug(!v.execute(n.ids[i]));
							}
						}
					}
				}
				return -1;
			}
		}
		return 0;
	}

	/**
	 * judge whether containing from a set of intersect rectangles. These
	 * rectangles are sorted
	 * 
	 * @param listIntersectRectangles
	 * @param r
	 * @return
	 */
	private boolean containsStep2(ArrayList<Rectangle> listIntersectRectangles, Rectangle r) {
		ArrayList<Rectangle> listR = new ArrayList<Rectangle>();
		ArrayList<Rectangle> listR2 = new ArrayList<Rectangle>();
		listR2.add(r);
		for (int i = 0; i < listIntersectRectangles.size(); i++) {
			Rectangle around = listIntersectRectangles.get(i);
			if (logger.isDebugEnabled()) {
				logger.debug("around: " + around.toString());
			}
			listR.clear();
			listR.addAll(listR2);
			listR2.clear();
			for (int j = 0; j < listR.size(); j++) {
				Rectangle objective = listR.get(j);
				if (logger.isDebugEnabled()) {
					logger.debug("objective: " + objective.toString());
				}
				if (around.contains(objective)) {
					// this inside small rectangle is covered by the outside
					// rectangle
					if (logger.isDebugEnabled()) {
						logger.debug("around contains objective");
					}
				} else if (around.intersects(objective)) {
					if (logger.isDebugEnabled()) {
						logger.debug("around intersects objective");
					}
					// compute the intersect parts
					// maxX >= r.minX && minX <= r.maxX && maxY >= r.minY &&
					// minY <= r.maxY;
					// divide the inside rectangle
					if (around.minX <= objective.minX) { // 1, 7, 8, 9, 11, 12
						if (around.maxX < objective.maxX) { // 1, 7, 8, 12
							if (around.minY < objective.minY) { // 1, 12
								if (around.maxY < objective.maxY) { // 1
									// case 1: left up corner
									Rectangle r1 = new Rectangle(around.maxX, objective.minY, objective.maxX, around.maxY);
									Rectangle r2 = new Rectangle(objective.minX, around.maxY, around.maxX, objective.maxY);
									Rectangle r3 = new Rectangle(around.maxX, around.maxY, objective.maxX, objective.maxY);
									listR2.add(r1);
									listR2.add(r2);
									listR2.add(r3);
									if (logger.isDebugEnabled()) {
										logger.debug("case 1: left up corner");
										logger.debug(r1.toString());
										logger.debug(r2.toString());
										logger.debug(r3.toString());
									}
								} else { // 12
									// case 12: left fully
									Rectangle r1 = new Rectangle(around.maxX, objective.minY, objective.maxX, objective.maxY);
									listR2.add(r1);
									if (logger.isDebugEnabled()) {
										logger.debug("case 12: left fully");
										logger.debug(r1.toString());
									}
								}
							} else { // 7, 8
								if (around.maxY < objective.maxY) { // 7
									// case 7: left bottom corner
									Rectangle r1 = new Rectangle(objective.minX, objective.minY, around.maxX, around.minY);
									Rectangle r2 = new Rectangle(around.maxX, objective.minY, objective.maxX, around.minY);
									Rectangle r3 = new Rectangle(around.maxX, around.minY, objective.maxX, objective.maxY);
									listR2.add(r1);
									listR2.add(r2);
									listR2.add(r3);
									if (logger.isDebugEnabled()) {
										logger.debug("case 7: left bottom corner");
										logger.debug(r1.toString());
										logger.debug(r2.toString());
										logger.debug(r3.toString());
									}
								} else { // 8
									// case 8: left middle
									Rectangle r1 = new Rectangle(objective.minX, objective.minY, around.maxX, around.minY);
									Rectangle r2 = new Rectangle(around.maxX, objective.minY, objective.maxX, around.minY);
									Rectangle r3 = new Rectangle(around.maxX, around.minY, objective.maxX, objective.maxY);
									Rectangle r4 = new Rectangle(around.maxX, around.maxY, objective.maxX, objective.maxY);
									Rectangle r5 = new Rectangle(objective.minX, around.maxY, around.maxX, objective.maxY);
									listR2.add(r1);
									listR2.add(r2);
									listR2.add(r3);
									listR2.add(r4);
									listR2.add(r5);
									if (logger.isDebugEnabled()) {
										logger.debug("case 8: left middle");
										logger.debug(r1.toString());
										logger.debug(r2.toString());
										logger.debug(r3.toString());
										logger.debug(r4.toString());
										logger.debug(r5.toString());
									}
								}

							}

						} else { // 9, 11
							if (around.minY < objective.minY) {// 9
								// case 9: up fully
								Rectangle r1 = new Rectangle(objective.minX, around.maxY, objective.maxX, objective.maxY);
								listR2.add(r1);
								if (logger.isDebugEnabled()) {
									logger.debug("case 9: up fully");
									logger.debug(r1.toString());
								}
							} else {// 11
								// case 11: bottom fully
								Rectangle r1 = new Rectangle(objective.minX, objective.minY, objective.maxX, around.minY);
								listR2.add(r1);
								if (logger.isDebugEnabled()) {
									logger.debug("case 11: bottom fully");
									logger.debug(r1.toString());
								}
							}
						}
					} else { // 2, 3, 4, 5, 6, 10, 13
						if (around.maxX < objective.maxX) { // 2, 6,13
							if (around.minY < objective.minY) {// 2
								// case 2: up middle
								Rectangle r1 = new Rectangle(objective.minX, objective.minY, around.minX, around.maxY);
								Rectangle r2 = new Rectangle(objective.minX, around.maxY, around.minX, objective.maxY);
								Rectangle r3 = new Rectangle(around.minX, around.maxY, around.maxX, objective.maxY);
								Rectangle r4 = new Rectangle(around.maxX, around.maxY, objective.maxX, objective.maxY);
								Rectangle r5 = new Rectangle(around.maxX, objective.minY, objective.maxX, around.maxY);
								listR2.add(r1);
								listR2.add(r2);
								listR2.add(r3);
								listR2.add(r4);
								listR2.add(r5);
								if (logger.isDebugEnabled()) {
									logger.debug("case 2: up middle");
									logger.debug(r1.toString());
									logger.debug(r2.toString());
									logger.debug(r3.toString());
									logger.debug(r4.toString());
									logger.debug(r5.toString());
								}
							} else {// 6,13
								if (around.maxY < objective.maxY) {// 13
									// case 13: middle
									Rectangle r1 = new Rectangle(objective.minX, objective.minY, around.minX, around.minY);
									Rectangle r2 = new Rectangle(around.minX, objective.minY, around.maxX, around.minY);
									Rectangle r3 = new Rectangle(around.maxX, objective.minY, objective.maxX, around.minY);
									Rectangle r4 = new Rectangle(around.maxX, around.minY, objective.maxX, around.maxY);
									Rectangle r5 = new Rectangle(around.maxX, around.maxY, objective.maxX, objective.maxY);
									Rectangle r6 = new Rectangle(around.minX, around.maxY, around.maxX, objective.maxY);
									Rectangle r7 = new Rectangle(objective.minX, around.maxY, around.minX, objective.maxY);
									Rectangle r8 = new Rectangle(objective.minX, around.minY, around.minX, around.maxY);
									listR2.add(r1);
									listR2.add(r2);
									listR2.add(r3);
									listR2.add(r4);
									listR2.add(r5);
									listR2.add(r6);
									listR2.add(r7);
									listR2.add(r8);
									if (logger.isDebugEnabled()) {
										logger.debug("case 13: middle");
										logger.debug(r1.toString());
										logger.debug(r2.toString());
										logger.debug(r3.toString());
										logger.debug(r4.toString());
										logger.debug(r5.toString());
										logger.debug(r6.toString());
										logger.debug(r7.toString());
										logger.debug(r8.toString());
									}
								} else {// 6
									// case 6: bottom middle
									Rectangle r1 = new Rectangle(objective.minX, around.minY, around.minX, objective.maxY);
									Rectangle r2 = new Rectangle(objective.minX, objective.minY, around.minX, around.minY);
									Rectangle r3 = new Rectangle(around.minX, objective.minY, around.maxX, around.minY);
									Rectangle r4 = new Rectangle(around.maxX, objective.minY, objective.maxX, around.minY);
									Rectangle r5 = new Rectangle(around.maxX, objective.minY, objective.maxX, objective.maxY);
									listR2.add(r1);
									listR2.add(r2);
									listR2.add(r3);
									listR2.add(r4);
									listR2.add(r5);
									if (logger.isDebugEnabled()) {
										logger.debug("case 6: bottom middle");
										logger.debug(r1.toString());
										logger.debug(r2.toString());
										logger.debug(r3.toString());
										logger.debug(r4.toString());
										logger.debug(r5.toString());
									}
								}
							}

						} else {// 3, 4, 5, 10
							if (around.minY < objective.minY) {// 3, 10
								if (around.maxY < objective.maxY) {// 3
									// case 3: right up corner
									Rectangle r1 = new Rectangle(objective.minX, objective.minY, around.minX, around.maxY);
									Rectangle r2 = new Rectangle(objective.minX, around.maxY, around.minX, objective.maxY);
									Rectangle r3 = new Rectangle(around.minX, around.maxY, objective.maxX, objective.maxY);
									listR2.add(r1);
									listR2.add(r2);
									listR2.add(r3);
									if (logger.isDebugEnabled()) {
										logger.debug("case 3: right up corner");
										logger.debug(r1.toString());
										logger.debug(r2.toString());
										logger.debug(r3.toString());
									}
								} else {// 10
									// case 10: right fully
									Rectangle r1 = new Rectangle(objective.minX, objective.minY, around.minX, objective.maxY);
									listR2.add(r1);
									if (logger.isDebugEnabled()) {
										logger.debug("case 10: right fully");
										logger.debug(r1.toString());
									}
								}
							} else {// 4, 5
								if (around.maxY < objective.maxY) {// 4
									// case 4: right middle
									Rectangle r1 = new Rectangle(around.minX, objective.minY, objective.maxX, around.minY);
									Rectangle r2 = new Rectangle(objective.minX, objective.minY, around.minX, around.minY);
									Rectangle r3 = new Rectangle(objective.minX, around.minY, around.minX, around.maxY);
									Rectangle r4 = new Rectangle(objective.minX, around.maxY, around.minX, objective.maxY);
									Rectangle r5 = new Rectangle(around.minX, around.maxY, objective.maxX, objective.maxY);
									listR2.add(r1);
									listR2.add(r2);
									listR2.add(r3);
									listR2.add(r4);
									listR2.add(r5);
									if (logger.isDebugEnabled()) {
										logger.debug("case 4: right middle");
										logger.debug(r1.toString());
										logger.debug(r2.toString());
										logger.debug(r3.toString());
										logger.debug(r4.toString());
										logger.debug(r5.toString());
									}
								} else {// 5
									// case 5: right bottom corner
									Rectangle r1 = new Rectangle(around.minX, objective.minY, objective.maxX, around.minY);
									Rectangle r2 = new Rectangle(objective.minX, objective.minY, around.minX, around.minY);
									Rectangle r3 = new Rectangle(objective.minX, around.minY, around.minX, objective.maxY);
									listR2.add(r1);
									listR2.add(r2);
									listR2.add(r3);
									if (logger.isDebugEnabled()) {
										logger.debug("case 5: right bottom corner");
										logger.debug(r1.toString());
										logger.debug(r2.toString());
										logger.debug(r3.toString());
									}
								}
							}

						}

					}
				} else {
					listR2.add(objective);
					if (logger.isDebugEnabled()) {
						logger.debug("around doesn't intersect objective");
					}
				}
			}
		}

		if (listR2.isEmpty()) {
			return true;
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("listR2 is not empty.");
				for (int i = 0; i < listR2.size(); i++) {
					logger.debug(listR2.get(i).toString());
				}
			}
			return false;
		}
	}

	private void sorting(List listIntersectRectangles) {
		// sort all circles in the middle line
		Collections.sort(listIntersectRectangles, new RectangleComparable());

	}

	public class RectangleComparable implements Comparator<Rectangle> {
		@Override
		public int compare(Rectangle r1, Rectangle r2) {
			double minX1 = r1.minX;
			double maxX1 = r1.maxX;
			double minY1 = r1.minY;
			double maxY1 = r1.maxY;
			double minX2 = r2.minX;
			double maxX2 = r2.maxX;
			double minY2 = r2.minY;
			double maxY2 = r2.maxY;
			if (minX1 < minX2) {
				return -1;
			} else if (minX1 > minX2) {
				return 1;
			} else {
				if (maxX1 < maxX2) {
					return -1;
				} else if (maxX1 > maxX2) {
					return 1;
				} else {
					if (minY1 < minY2) {
						return -1;
					} else if (minY1 > minY2) {
						return 1;
					} else {
						if (maxY1 < maxY2) {
							return -1;
						} else if (maxY1 > maxY2) {
							return 1;
						} else {
							return 0;
						}
					}
				}
			}

		}
	}

	public static Point coordinateToPoint(Coordinate v) {
		return new Point((float) v.x, (float) v.y);
	}

	/**
	 * print the rtree
	 */
	private void print() {
		TIntStack parents = new TIntStack();
		int rootNodeId = this.getRootNodeId();

		parents.reset();
		parents.push(rootNodeId);

		while (parents.size() > 0) {
			Node n = getNode(parents.pop());
			Rectangle rN = new Rectangle(n.mbrMinX, n.mbrMinY, n.mbrMaxX, n.mbrMaxY);
			System.out.println("");
			System.out.println(rN.toString());
			System.out.println("-------");

			if (!n.isLeaf()) {// The children of n are not the leaves
				System.out.println("not leaf");
				// go through every entry in the index node to check
				// if it intersects the passed rectangle. If so, it
				// could contain entries that are contained.
				for (int i = 0; i < n.entryCount; i++) {
					rN = new Rectangle(n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i]);

					System.out.println(rN.toString());

					// if (rN.contains(r)) {
					// System.out.println("this contains");
					parents.push(n.ids[i]);
				}
			} else {
				// go through every entry in the leaf to check if
				// it is contained by the passed rectangle
				System.out.println("leaf");
				for (int i = 0; i < n.entryCount; i++) {
					rN = new Rectangle(n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i]);
					System.out.println(rN.toString());

				}
			}
		}
	}

}
