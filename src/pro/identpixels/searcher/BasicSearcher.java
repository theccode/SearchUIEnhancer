package pro.identpixels.searcher;

import java.awt.image.BufferedImage;



public class BasicSearcher extends AbstractSearcher {
	public BasicSearcher(BufferedImage raster, int side, double progress) {
		super(raster, side, progress);
	}

	@Override
	public void cancel() {
	}

}
