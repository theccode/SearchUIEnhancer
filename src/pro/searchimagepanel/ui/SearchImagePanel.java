package pro.searchimagepanel.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import pro.identpixels.ui.SearchUIEnhancement;

public class SearchImagePanel extends JPanel{

	/**
	 * This is the ImagePanel component, few codes have been borrowed from SearchUI.
	 */
	private static final long serialVersionUID = 1L;
	private final List<Rectangle[]> highlights = new ArrayList<Rectangle[]>();
	double scale = 1;
	public void paintComponent(Graphics graphics) {
		Graphics2D graphics2D = (Graphics2D) graphics;
		super.paintComponent(graphics2D);
		graphics2D.setPaint(Color.LIGHT_GRAY);
		graphics2D.draw(new Rectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1));
		SearchUIEnhancement.getSearchInstance();
		Image fileImage = new ImageIcon(SearchUIEnhancement.mImagePath.getText()).getImage();
		int width = getWidth();
		int height = getHeight();
		
		double rWidth = (double) getWidth() / (double) fileImage.getWidth(null);
		double rHeight = (double)  getHeight() / (double) fileImage.getHeight(null);
		if (rWidth > rHeight) {
			width = (int) (fileImage.getWidth(null) * rHeight);
		} else {
			height = (int) (fileImage.getHeight(null) * rWidth);
		}
		
		graphics2D.setColor( Color.YELLOW);
		graphics2D = (Graphics2D) graphics2D.create();
		graphics2D.clearRect(0, 0, getWidth(), getHeight()); // clear background
				final double scale = getWidth() / (double)fileImage.getWidth(null);
				graphics2D.drawImage( fileImage, 0, 0, getWidth(), (int)(fileImage.getHeight(null) * scale), this);
				graphics2D.setColor( Color.YELLOW);
		synchronized( this.highlights) {
				for( final Rectangle[] r : this.highlights) {
					final Rectangle s1 = new Rectangle(
							(int)(r[0].x * scale),
							(int)(r[0].y * scale),
							(int)(r[0].width * scale),
							(int)(r[0].height * scale));
					graphics2D.draw( s1);
				final Rectangle s2 = new Rectangle(
		        (int)(r[1].x * scale),
		        (int)(r[1].y * scale),
		        (int)(r[1].width * scale),
		        (int)(r[1].height * scale));
		    	graphics2D.draw( s2);
		    
			    boolean leftToRight = s1.x < s2.x;
			    boolean higherToLower = s1.y < s2.y;
			    
			    int lx1 = s1.x;
			    int lx2 = s2.x;
			    int ly1 = s1.y;
			    int ly2 = s2.y;
			    
			    if ( leftToRight) {
			      lx1 += s1.width;
			    } else {
			      lx2 += s2.width; 
			    }
			    if ( higherToLower) {
			      ly1 += s1.height;
			    } else {
			      ly2 += s2.height; 
			    }
			    
			    graphics2D.drawLine(lx1, ly1, lx2, ly2);
			}
		}
		graphics2D.dispose();
			
	}
	public void addHighlight( final Rectangle r1, final Rectangle r2) {
		synchronized( this.highlights) {
			this.highlights.add( new Rectangle[] { r1, r2 });
		}
		repaint();
	}

	public void resetHighlights() {
		synchronized( this.highlights) {
			this.highlights.clear();
		}
		repaint();
	}
	
	private class Worker extends SwingWorker<Void, Void>{

		@Override
		protected Void doInBackground() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
