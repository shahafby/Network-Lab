import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

/**
 * A runnable class which downloads a given url. It reads CHUNK_SIZE at a time
 * and writs it into a BlockingQueue. It supports downloading a range of data,
 * and limiting the download rate using a token bucket.
 */
public class HTTPRangeGetter implements Runnable {
	static final int CHUNK_SIZE = 4096;
	private static final int CONNECT_TIMEOUT = 50000;
	private static final int READ_TIMEOUT = 200000;
	private final String url;
	private final Range range;
	private final BlockingQueue<Chunk> outQueue;
	private TokenBucket tokenBucket;

	HTTPRangeGetter(String url, Range range, BlockingQueue<Chunk> outQueue, TokenBucket tokenBucket) {
		this.url = url;
		this.range = range;
		this.outQueue = outQueue;
		this.tokenBucket = tokenBucket;
	}

	private void downloadRange() throws IOException, InterruptedException {
		int bytesRead;
		long offset = this.range.getStart();
		byte[] data = new byte[CHUNK_SIZE];
		Chunk chunk;
		InputStream stream;
		URL urlObj = new URL(this.url);
		HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();

		// setting connection: timeout, read timeout method and protected range
		// of reading (this thread range)
		connection.setConnectTimeout(CONNECT_TIMEOUT);
		connection.setReadTimeout(READ_TIMEOUT);
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Range", "bytes=" + this.range.getStart() + "-" + (this.range.getEnd()));
		connection.connect();

		stream = connection.getInputStream();
		tokenBucket.take(CHUNK_SIZE);
		// while the worker is still in his defined range
		while ((bytesRead = stream.read(data, 0, (int) CHUNK_SIZE)) != -1) {
				chunk = new Chunk(data, offset, bytesRead, this.range);
				this.outQueue.add(chunk);
				offset += bytesRead + 1;
				tokenBucket.take(CHUNK_SIZE);
		}
		stream.close();
		connection.disconnect();
	}

	@Override
	public void run() {
		try {
			this.downloadRange();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			// TODO
		}
	}
}
