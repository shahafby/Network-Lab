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
	private String fileName;

	FileWriter(DownloadableMetadata downloadableMetadata, BlockingQueue<Chunk> chunkQueue, String fileName) {
		this.chunkQueue = chunkQueue;
		this.downloadableMetadata = downloadableMetadata;
		this.fileName = fileName;
	}

	private void writeChunks() throws IOException {
		// TODO

		try (RandomAccessFile raf = new RandomAccessFile(new File(this.fileName), "rws")) {

			while (true) {
				for (Chunk currentChuk : chunkQueue) {
					if (currentChuk != null) {
						raf.seek(currentChuk.getOffset());
						raf.write(currentChuk.getData());
					}
				}
			}
		}
	}

	@Override
	public void run() {
		try {
			this.writeChunks();
		} catch (IOException e) {
			e.printStackTrace();
			// TODO
		}
	}
}
