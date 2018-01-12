import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;

/**
 * This class takes chunks from the queue, writes them to disk and updates the
 * file's metadata.
 *
 * NOTE: make sure that the file interface you choose writes every update to the
 * file's content or metadata synchronously to the underlying storage device.
 */
public class FileWriter implements Runnable {

	private final BlockingQueue<Chunk> chunkQueue;
	private DownloadableMetadata downloadableMetadata;

	FileWriter(DownloadableMetadata downloadableMetadata, BlockingQueue<Chunk> chunkQueue) {
		this.chunkQueue = chunkQueue;
		this.downloadableMetadata = downloadableMetadata;
	}

	private void writeChunks() throws IOException, InterruptedException {

		int totalNUmOfChunksInQueue, numOfWrittenChunks = 0;
		Chunk currentChuk;
		try (RandomAccessFile raf = new RandomAccessFile(new File(downloadableMetadata.getFilename()), "rws")) {
			totalNUmOfChunksInQueue = downloadableMetadata.totalNumOfChunks;

			while (numOfWrittenChunks < totalNUmOfChunksInQueue) {
				currentChuk = chunkQueue.take();
				raf.seek(currentChuk.getOffset());
				raf.write(currentChuk.getData());
				numOfWrittenChunks++;
			}
		}
	}

	@Override
	public void run() {
		try {
			this.writeChunks();
		} catch (Exception e) {
			e.printStackTrace();
			// TODO
		}
	}
}
