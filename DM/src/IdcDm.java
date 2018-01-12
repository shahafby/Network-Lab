import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.*;

public class IdcDm {

	/**
	 * Receive arguments from the command-line, provide some feedback and start
	 * the download.
	 *
	 * @param args
	 *            command-line arguments
	 */
	public static void main(String[] args) {
		int numberOfWorkers = 1;
		Long maxBytesPerSecond = null;

		if (args.length < 1 || args.length > 3) {
			System.err.printf("usage:\n\tjava IdcDm URL [MAX-CONCURRENT-CONNECTIONS] [MAX-DOWNLOAD-LIMIT]\n");
			System.exit(1);
		} else if (args.length >= 2) {
			numberOfWorkers = Integer.parseInt(args[1]);
			if (args.length == 3)
				maxBytesPerSecond = Long.parseLong(args[2]);
		}

		String url = args[0];

		System.err.printf("Downloading");
		if (numberOfWorkers > 1)
			System.err.printf(" using %d connections", numberOfWorkers);
		if (maxBytesPerSecond != null)
			System.err.printf(" limited to %d Bps", maxBytesPerSecond);
		System.err.printf("...\n");

		DownloadURL(url, numberOfWorkers, maxBytesPerSecond);
	}

	/**
	 * Initiate the file's metadata, and iterate over missing ranges. For each:
	 * 1. Setup the Queue, TokenBucket, DownloadableMetadata, FileWriter,
	 * RateLimiter, and a pool of HTTPRangeGetters 2. Join the HTTPRangeGetters,
	 * send finish marker to the Queue and terminate the TokenBucket 3. Join the
	 * FileWriter and RateLimiter
	 *
	 * Finally, print "Download succeeded/failed" and delete the metadata as
	 * needed.
	 *
	 * @param url
	 *            URL to download
	 * @param numberOfWorkers
	 *            number of concurrent connections
	 * @param maxBytesPerSecond
	 *            limit on download bytes-per-second
	 */
	private static void DownloadURL(String url, int numberOfWorkers, Long maxBytesPerSecond) {

		int workersWithExtraChunk, numOfChunksForWorker;
		long end, queueSize, contentLength, sizeOfRange, offset = 0;
		Thread t_httpRangeGetter, t_writer, t_limiter;
		Thread[] workers = new Thread[numberOfWorkers];
		BlockingQueue<Chunk> dataQueue;
		DownloadableMetadata DM;
		TokenBucket tokenBucket = new TokenBucket();

		try {
			contentLength = getLength(url);
			if (contentLength < 4096)
				numberOfWorkers = 1;
			queueSize = contentLength / 4096;
			// if the size of the file devided by the size of a chunk has no
			// reminder we set the queue size to that size.
			// o.w we add one more spce for the reminder chunk
			queueSize = (contentLength % 4096 == 0) ? queueSize : queueSize + 1;
			dataQueue = new ArrayBlockingQueue<Chunk>((int) queueSize);

			t_limiter = new Thread(new RateLimiter(tokenBucket, maxBytesPerSecond));
			t_limiter.start();

			numOfChunksForWorker = (int) (queueSize / numberOfWorkers);
			workersWithExtraChunk = (int) (queueSize % numberOfWorkers);
			
			DM = new DownloadableMetadata(url, numOfChunksForWorker);
			
			for (int i = 0; i < numberOfWorkers; i++) {
				// amounts of bytes to be read bt all workers
				end = (offset + numOfChunksForWorker * 4096) - 1;	// offset check
				// the workers that has extra chunks to read due to non
				// devisible of content length
				if (i < workersWithExtraChunk) { // offset check
					end = offset + (numOfChunksForWorker + 1) * 4096 - 1;
				} else if (i == numberOfWorkers - 1) {
					end = offset + numOfChunksForWorker * 4096 - 1 - (4096 - (contentLength % 4096));
				}
				Range range = new Range(offset, end);
				HTTPRangeGetter rg = new HTTPRangeGetter(url, range, dataQueue, tokenBucket);
				workers[i] = t_httpRangeGetter = new Thread(rg, "thread_" + i);
				t_httpRangeGetter.start();

				offset += end; // offset check
			}
			t_writer = new Thread(new FileWriter(DM, dataQueue), "writing thread");
			t_writer.start();

			// close worker threads
			for (Thread worker : workers) {
				try {
					worker.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			tokenBucket.terminate();
			// close writer and limiter threads
			try {
				t_writer.join();
				t_limiter.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		} catch (IOException e) {
			System.out.println("problen connecting to server");
			System.err.println(e);
		}
		

	}

	private static long getLength(String url) throws IOException {
		URL urlObj = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
		return connection.getContentLengthLong();
	}
	
	private static void creatMetadataFile(DownloadableMetadata DM) throws Exception{
		// Write metadata file
		try (ObjectOutputStream metadataOutStream = new ObjectOutputStream(new FileOutputStream(DM.getFilename() + ".metadata.tmp"))) {
			 metadataOutStream.writeObject(DM);
		}
	}
}
