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
		long currentChukOffset, currentChukRangeEnd, bytesSum = 0;
		int currentChukSize;
		Chunk currentChuk;
		int prevPercent = 0, currentPercent;
		try (RandomAccessFile raf = new RandomAccessFile(new File(downloadableMetadata.getFilename()), "rws")) {
			System.out.println("Downloaded: 0%");
			while(!(currentChuk = chunkQueue.take()).getIsLastChunk()) { 
				
				raf.seek(currentChuk.getOffset());
				raf.write(currentChuk.getData());
				currentChukOffset = currentChuk.getOffset();
				currentChukSize = currentChuk.getSize_in_bytes();
				currentChukRangeEnd = currentChuk.getThisChunkRange().getEnd();
						
				// if the current chink is the last chunk of the current range mark this range as written.
				if(currentChukOffset + currentChukSize == currentChukRangeEnd){
					currentChuk.getThisChunkRange().setWasWritten();
				}
				downloadableMetadata.creatMetadataFile(downloadableMetadata);
				bytesSum += currentChuk.getSize_in_bytes();
				currentPercent = (int) (100 * bytesSum / this.downloadableMetadata.getSizeOfFile());
				if(currentPercent != prevPercent) {
					System.out.println("Downloaded: " + currentPercent + "%");
				}
				prevPercent = currentPercent;
			}
			System.out.println("Downloaded: 100%");
		} catch (Exception e) {
			
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
