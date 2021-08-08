package pro.identpixels.searcher;

import java.awt.Color;
import java.awt.image.BufferedImage;

import pro.identpixels.Settings;
import pro.identpixels.Settings.Configuration;


/**
 * Class providing the basic infrastructure for concrete Searchers.
 */
public abstract class AbstractSearcher implements Searcher {

	private final BufferedImage raster;
	private final int side;
	private final int firstPosition;
	private final int endPosition;
	private final int rasterEndPosition;
	private final int numberOfPositionsToTry;
	private boolean flag = false;

	public static volatile int counter = 0;
	private volatile int currentPosition; // source region being searched

  private int searchPosition; // search position somewhere after source region
	private int x0;
	private int y0;
	double progress;

	/**
	 * Constructs an AbstractSearcher that will search the given image for square regions
	 * of {@code side} pixels in size that match each other. Every possible region is 
	 * compared to every possible other region.
	 */
	protected AbstractSearcher(final BufferedImage raster, final int side, double progress) {
		this( raster, side, 0, (raster.getWidth() * raster.getHeight()) - 1, progress);
//	  this.raster = raster;
//		this.side = side;
//		this.firstPosition = 0;
//    this.rasterEndPosition = (raster.getWidth() * raster.getHeight());
//		this.endPosition = this.rasterEndPosition - 1;
//		this.currentPosition = 0;
	}

	/**
   * Constructs an AbstractSearcher that will search the given image for square regions
   * of {@code side} pixels in size that match each other. This searcher will try to 
   * match every possible region following each region specified in the range between
   * {@code firstPosition} (inclusive) and {@code endPosition} (exclusive). Regions are
   * numbered left-to-right, top-to-bottom across the image.
	 */
	protected AbstractSearcher(final BufferedImage raster, final int side, final int firstPosition, final int endPosition, double progress2) {
		this.raster = raster;
		this.side = side;
		this.firstPosition = firstPosition;
		this.rasterEndPosition = (raster.getWidth() * raster.getHeight());
		this.endPosition = endPosition;
		this.currentPosition = firstPosition;
		int total = 0;
	    for (int i = this.firstPosition; i < this.endPosition; i++) {
	      total += this.rasterEndPosition - i;
	    }
	    this.numberOfPositionsToTry = total;
	    this.progress = progress2;
	}

	/** {@inheritDoc} */
	@Override
	public final int numberOfPositionsToTry() {
		return this.numberOfPositionsToTry;
	}

	/** {@inheritDoc} */
	@Override
	public final int numberOfPositionsTriedSoFar() {
		return this.counter;
	}
	@Override
	public void cancel()  {
		this.flag = true;
	}

	/**
	 * Resets the searcher to re-run its search. If any state is added in a
	 * subclass, this method probably needs to be overridden to reset that state, as
	 * well as calling this implementation.
	 */
	@Override
	public void reset() {
		this.counter = 0;
		this.currentPosition = this.firstPosition;
		this.searchPosition = this.currentPosition;
	}

	/**
	 * Attempts all positions specified for this Searcher in turn, emitting
	 * information about any position that appears to produce a match to the
	 * provided {@link Searcher.SearchListener} object.
	 * <p>
	 * The SearchListener methods are invoked on the thread that calls this method.
	 */
	@Override
	public void runSearch(final SearchListener listener, boolean mFlag) {
		this.reset();
		boolean done = false;
		this.flag = mFlag;
		this.x0 = this.firstPosition % this.raster.getWidth();
		this.y0 = this.firstPosition / this.raster.getWidth();

		listener.information("SEARCHING...");
		final long startTime = System.currentTimeMillis();
		while (this.flag) {
			if (mFlag == false) {
				break;
			}
			final int[] foundMatch = this.findMatch(listener, startTime);
			if (foundMatch != null) {
				done = true;
				listener.possibleMatch(this.x0,this.y0,foundMatch[0],foundMatch[1], System.currentTimeMillis() - startTime,
						numberOfPositionsTriedSoFar());
			} else {
				break;
			}
			++this.progress;
		}
		listener.information("Finished at " + ((System.currentTimeMillis() - startTime) / 1000.0) + "s\n");
	}

	private int[] findMatch(final SearchListener listener, final long startTime) {	  
		while (this.currentPosition < this.endPosition) {
			double progress =  ((double) this.numberOfPositionsTriedSoFar() / (double)this.numberOfPositionsToTry) * 100;
			if (this.flag == false) {
				break;
			}
			this.searchPosition++; // advance region 2
			if ( this.searchPosition >= this.rasterEndPosition) {
				this.currentPosition++; // advance region 1
		      	this.x0 = this.currentPosition % this.raster.getWidth();
		      	this.y0 = this.currentPosition / this.raster.getWidth();
				this.searchPosition = this.currentPosition + 1;
				if ( this.searchPosition >= this.rasterEndPosition) {
				    break;
				 }
			}
      final int[] hit = tryPosition();
			this.counter++;
			if (hit != null) {
				return hit;
			} else if (this.counter % (this.numberOfPositionsToTry / 10) == 0) {
				listener.update(this.currentPosition - 1, System.currentTimeMillis() - startTime,	numberOfPositionsTriedSoFar(), progress);
			}
		}
		return null;
	}

	/**
	 * Tries the next possible position and returns an int array containing the
	 * coordinates of the matched region, or null if there was no match.
	 * 
	 * @return
	 */
	protected int[] tryPosition() {
		final int x1 = this.searchPosition % this.raster.getWidth();
		final int y1 = this.searchPosition / this.raster.getWidth();

		int match = 0;
		for (int xi = 0; xi < this.side; xi++) {
		  int sx = this.x0 + xi; 
		  if ( sx >= this.raster.getWidth()) {
		    sx -= this.raster.getWidth();
		  }
      int cx = x1 + xi; 
      if ( cx >= this.raster.getWidth()) {
        cx -= this.raster.getWidth();
      }

			for (int yi = 0; yi < this.side; yi++) {
	      int sy = this.y0 + yi; 
	      if ( sy >= this.raster.getHeight()) {
	        sy -= this.raster.getHeight();
	      }	      
			  int cy = y1 + yi; 
	      if ( cy >= this.raster.getHeight()) {
	        cy -= this.raster.getHeight();
	      }

				final int srcRgb = this.raster.getRGB(sx, sy);
        final int cmpRgb = this.raster.getRGB(cx, cy);
        final float[] srcComponents = new Color(srcRgb).getRGBComponents(null);
        final float[] cmpComponents = new Color(cmpRgb).getRGBComponents(null);
				
        double delta = 0;
        for (int i = 0; i < cmpComponents.length; i++) {
          delta += Math.abs(srcComponents[i] - cmpComponents[i]);
        }
        if ( delta <= Configuration.thresholdForPixelMatch) { match++; }
	    
			}
		}

		if ((match / (double)(Configuration.regionSide * Configuration.regionSide)) >= Configuration.proportionForRegionMatch) { 
//		  System.out.println( x0 + ", " + y0 + " = " + x1 + ", " + y1 + " m = " + match);
		  return new int[] { x1, y1 };
		} else {
		  return null;
		}
	}

}
