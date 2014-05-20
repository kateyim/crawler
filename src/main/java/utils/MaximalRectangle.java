package utils;

import java.util.LinkedList;
import java.util.Stack;

import com.vividsolutions.jts.geom.Envelope;

final class Cell {

	final int col;
	final int row;

	Cell(final int col, final int row) {
		this.col = col;
		this.row = row;
	}

	@Override
	public String toString() {
		return String.format("[col=%d, row=%d]", col + 1, row + 1);
	}
}

final class Cache {

	private final LinkedList<Integer> aggregateHeights;

	Cache(final int size) {
		aggregateHeights = new LinkedList<Integer>();
		for (int i = 0; i <= size; i++) {
			aggregateHeights.add(0);
		}
	}

	public int get(final int col) {
		return aggregateHeights.get(col);
	}

	public void aggregate(final String row) {
		final String[] elements = row.split("\\s+");
		for (int col = 0; col < elements.length; col++) {
			final String element = elements[col];
			System.err.printf("%s ", element);

			if ("0".equals(element)) {
				aggregateHeights.set(col, 0);
			} else {
				aggregateHeights.set(col, aggregateHeights.get(col) + 1);
			}
		}
		System.err.printf("\n");
	}

	public void aggregate(final double[] elements) {
		// final String[] elements = row.split("\\s+");
		for (int col = 0; col < elements.length; col++) {
			final double element = elements[col];
			// System.err.printf("%s ", element);
			if (element == 0) {
				aggregateHeights.set(col, 0);
			} else {
				aggregateHeights.set(col, aggregateHeights.get(col) + 1);
			}
		}
		// System.err.printf("\n");
	}
}

/**
 * @see http://stackoverflow.com/a/20039017/1028367
 * @see http://www.drdobbs.com/database/the-maximal-rectangle-problem/184410529
 */
public class MaximalRectangle {

	public Envelope rectangle(int numColumns, int numRows, double[][] boader) throws Exception {
		int bestArea = 0;
		Cell bestLowerLeftCorner = new Cell(0, 0);
		Cell bestUpperRightCorner = new Cell(-1, -1);

		// try(final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
		// final Scanner scanner = new Scanner(reader.readLine());
		// final int numColumns = scanner.nextInt();
		// final int numRows = scanner.nextInt();

		System.err.printf("Reading %dx%d array (1 row == %d elements)\n", numColumns, numRows, numColumns);

		final Stack<Cell> stack = new Stack<Cell>();
		final Cache rectangleHeightCache = new Cache(numColumns);

		for (int row = 0; row < numRows; row++) {
			// rectangleHeightCache.aggregate(reader.readLine());
			rectangleHeightCache.aggregate(boader[row]);

			//
			for (int col = 0, currentRectHeight = 0; col <= numColumns; col++) {
				final int aggregateRectHeight = rectangleHeightCache.get(col);

				if (aggregateRectHeight > currentRectHeight) {
					stack.push(new Cell(col, currentRectHeight));
					currentRectHeight = aggregateRectHeight;
				} else if (aggregateRectHeight < currentRectHeight) {

					Cell rectStartCell;
					do {
						rectStartCell = stack.pop();
						final int rectWidth = col - rectStartCell.col;
						final int area = currentRectHeight * rectWidth;
						if (area > bestArea) {
							bestArea = area;
							bestLowerLeftCorner = new Cell(rectStartCell.col, row);
							bestUpperRightCorner = new Cell(col - 1, row - currentRectHeight + 1);
						}
						currentRectHeight = rectStartCell.row;
					} while (aggregateRectHeight < currentRectHeight);

					currentRectHeight = aggregateRectHeight;
					if (currentRectHeight != 0) {
						stack.push(rectStartCell);
					}
				}
			}
		}

		System.err.printf("The maximal rectangle has area %d.\n", bestArea);
		System.err.printf("Location: %s to %s\n", bestLowerLeftCorner, bestUpperRightCorner);
		Envelope envelope = new Envelope(bestLowerLeftCorner.row, bestUpperRightCorner.row, bestLowerLeftCorner.col, bestUpperRightCorner.col);
		return envelope;
		// }
	}

	public static void main(String[] args) {
		MaximalRectangle mr = new MaximalRectangle();
		int numColumns = 4;
		int numRows = 7;
		double[][] boader = { { 1, 1, 1, 0 }, { 1, 1, 1, 1 }, { 1, 1, 0, 0 }, { 1, 1, 1, 0 }, { 1, 1, 1, 1 }, { 1, 1, 1, 1 }, { 0, 0, 1, 1 } };
		try {
			mr.rectangle(numColumns, numRows, boader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
