import java.util.ArrayList;

/**
 * Describes a file's metadata: URL, file name, size, and which parts already downloaded to disk.
 *
 * The metadata (or at least which parts already downloaded to disk) is constantly stored safely in disk.
 * When constructing a new metadata object, we first check the disk to load existing metadata.
 *
 * CHALLENGE: try to avoid metadata disk footprint of O(n) in the average case
 * HINT: avoid the obvious bitmap solution, and think about ranges...
 */
class DownloadableMetadata implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
    private final String metadataFilename;
    private String filename;
    private String url;
    protected long sizeOfRange;
    private ArrayList<Range> addedRanges;
	protected long sizeOfFile;
	protected int numOfRanges;
	protected int rangeCounter = 0;
	protected Chunk currentChunk;
	protected int totalNumOfChunks;

    DownloadableMetadata(String url, int numOfChunks) {
        this.url = url;
        this.filename = getName(url);
        this.metadataFilename = getMetadataName(filename);
        this.addedRanges = new ArrayList<Range>();
        this.totalNumOfChunks = numOfChunks;
    }

    private static String getMetadataName(String filename) {
        return filename + ".metadata";
    }

    private static String getName(String path) {
        return path.substring(path.lastIndexOf('/') + 1, path.length());
    }

    /**
     * add the sent range to array, remember which ranges was already sent.
     * 
     * @param range - range to add
     * @param place - place in array
     */
    void addRange(Range range, int place) {
        addedRanges.add(place, range);
        rangeCounter++;
    }

    String getFilename() {
        return filename;
    }
    
    /**
     * check if all the ranges is in the array, i.e. all ranges passed, if so return true.
     * @return 
     */
    boolean isCompleted() {
    	if(rangeCounter == numOfRanges) {
    		return true;
        }
        return false;
    }

    void delete() {
        //TODO
    }

    Range getMissingRange() {
    	for(int i = 0; i < sizeOfRange; i++) {
    		if(addedRanges.get(i) == null) {
    			return addedRanges.get(i);
    		}
    	}
    	return null;
    }

    String getUrl() {
        return url;
    }
    
    void calcCompletedPercentage() {
    	long oldSizeOfFile = sizeOfFile, newSizeOfFile = 0;
    	
    }
}
