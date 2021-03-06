import java.io.Serializable;

/**
 * Describes a simple range, with a start, an end, and a length
 */
class Range implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long start;
	private Long end;
	private boolean wasWritten;

	Range(Long start, Long end) {
		this.start = start;
		this.end = end;
		this.wasWritten = false;
	}

	Long getStart() {
		return start;
	}

	Long getEnd() {
		return end;
	}

	Long getLength() {
		return end - start + 1;
	}

	void setWasWritten() {
		this.wasWritten = true;
	}
	
	boolean getWasWritten(){
		return this.wasWritten;
	}
}
