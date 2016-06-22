package serilogj.parsing;

public class Alignment {
	private AlignmentDirection direction = AlignmentDirection.Left;
	private int width = 0;

	public Alignment() {
	}

	public Alignment(AlignmentDirection direction, int width) {
		this.direction = direction;
		this.width = width;
	}

	/**
	 * The text alignment direction.
	 */
	public AlignmentDirection getDirection() {
		return direction;
	}

	/**
	 * The width of the text.
	 */
	public int getWidth() {
		return width;
	}

	@Override
	public boolean equals(Object obj) {
		Alignment sv = (Alignment) ((obj instanceof Alignment) ? obj : null);
		return sv != null && direction == sv.direction && width == sv.width;
	}

	@Override
	public int hashCode() {
		return direction.hashCode() ^ width;
	}
}
