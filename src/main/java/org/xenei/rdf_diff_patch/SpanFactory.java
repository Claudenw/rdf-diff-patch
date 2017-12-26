package org.xenei.rdf_diff_patch;

/**
 * A factory to create spans.
 *
 */
public class SpanFactory
{

	/**
	 * Construct a span from a starting position and an endpoint.
	 * 
	 * @param start
	 *            The starting position.
	 * @param end
	 *            The endpoint
	 * @return the new Span.
	 */
	public static Span fromEnd(int start, int end) {
		return new SpanImpl(start, end - start + 1);
	}

	/**
	 * Create a span from a starting position and a length.
	 * @param start the starting position.
	 * @param length the length.
	 * @return the new Span.
	 */
	public static Span fromLength(int start, int length) {
		return new SpanImpl(start, length);
	}


	/**
	 * An implementation of Span for factory use.
	 *
	 */
	private static class SpanImpl extends AbstractSpan {

		private final int start;
		private final int length;

		/**
		 * Constructor using a starting position and a length. To construct using a
		 * starting position and an endpoint use fromEnd().
		 * 
		 * @param start
		 *            The starting position.
		 * @param length
		 *            The length.
		 */
		SpanImpl(int start, int length) {
			Span.Util.checkIntAddLimit(start, length);
			if (length<0)
			{
				throw new IndexOutOfBoundsException( "Length may not be less than zero: "+length);
			}
			this.start = start;
			this.length = length;
		}

		@Override
		public final int getStart() {
			return start;
		}

		@Override
		public final int getLength() {
			return length;
		}

		@Override
		public final int getEnd() {
			return Span.Util.calcEnd(this);
		}

	}
}
