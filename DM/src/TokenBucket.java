import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A Token Bucket (https://en.wikipedia.org/wiki/Token_bucket)
 *
 * This thread-safe bucket should support the following methods:
 *
 * - take(n): remove n tokens from the bucket (blocks until n tokens are available and taken)
 * - set(n): set the bucket to contain n tokens (to allow "hard" rate limiting)
 * - add(n): add n tokens to the bucket (to allow "soft" rate limiting)
 * - terminate(): mark the bucket as terminated (used to communicate between threads)
 * - terminated(): return true if the bucket is terminated, false otherwise
 *
 */
class TokenBucket {
	
	private AtomicLong tokenAmount;
	private AtomicBoolean isTerminate;

    TokenBucket() {
    	this.isTerminate = new AtomicBoolean(false);
    	this.tokenAmount = new AtomicLong();
    }

    synchronized void take(long tokens) {
    	long currentAmount = this.tokenAmount.get();
    	// setting the amount of available tokens to be the current amount minus the taken tokens
    	this.tokenAmount.set(currentAmount - tokens); 
    	
    }

    void terminate() {
    	this.isTerminate.set(true);
    }

    boolean terminated() {
    	return this.isTerminate.get();
    }

    void set(long tokens) {
        this.tokenAmount.set(tokens);
    }
    
    void add(long tokens) {
    	if(this.tokenAmount.addAndGet(tokens) > 0){
    	}
    }
    
}
