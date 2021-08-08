package pro.identpixels.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import pro.identpixels.Settings;
import pro.identpixels.searcher.AbstractSearcher;
import pro.identpixels.searcher.BasicSearcher;
import pro.identpixels.searcher.Searcher;
import pro.identpixels.searcher.Searcher.SearchListener;
import pro.searchimagepanel.ui.SearchImagePanel;

public class SearchUIEnhancement extends JFrame implements SearchListener, PropertyChangeListener{
	private JToolBar mSearchToolBar = new JToolBar();
	private JButton mOpenButton = new JButton(new ImageIcon("open.png"));
	private JButton mStartButton = new JButton(new ImageIcon("start.png"));
	private JButton mCancelButton = new JButton(new ImageIcon("cancel.png"));
	private JButton mResetButton = new JButton(new ImageIcon("reset.gif"));
	private SearchImagePanel mImagePanel;
	public static JLabel mProcessOutputLabel = new JLabel();
	private JFileChooser mImageChooser;
	private BufferedImage mRasterImage;
	private JLabel mProgressLabel = new JLabel();
	private JProgressBar mProcessProgressBar = new JProgressBar(0, 100);
	public static JLabel mImagePath = new JLabel();;
	private static SearchUIEnhancement mSearchUIEnhancer;
	private GridBagConstraints mGridConstraints;
	private Dimension mButtonDimensions;
	private String[] mButtonLabels = new String[] {
			"open", "start", "cancel", "reset"
	};
	private Searcher mSearcher;
	private double mProgressValue = 0;
	private ExecutorService mExec = Executors.newFixedThreadPool(2);
	private SearchWorker searchWorker = new SearchWorker(); 
	private SearchProgressMonitor searchMonitor = new SearchProgressMonitor();
	private boolean mFlag = false;
	private boolean cancel = true;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SearchUIEnhancement() {
		UIManager.put("ProgressBar.background", Color.ORANGE);
		UIManager.put("ProgressBar.foreground", Color.GREEN);
		UIManager.put("ProgressBar.selectionBackground", Color.RED);
		UIManager.put("ProgressBar.selectionForeground", Color.RED);
		init();
	}
	
	public static SearchUIEnhancement getSearchInstance() {
		if (mSearchUIEnhancer == null) {
			mSearchUIEnhancer = new SearchUIEnhancement();
		}
		return mSearchUIEnhancer;
	}
	private void init() {
		enableButtons(false, "all");
		mResetButton.setIcon(new ImageIcon("resetStatic.png"));
		setTitle("Search UI Enhancement");
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				exitForm(evt);
			}
		});
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new GridBagLayout());
		
		/*
		 * Start UI widget definitions
		 */
			mSearchToolBar.setFloatable(false);
			mSearchToolBar.setBackground(Color.gray);
			mSearchToolBar.setOrientation(SwingConstants.VERTICAL);
			
			mGridConstraints = new GridBagConstraints();
			mGridConstraints.gridx = 0;
			mGridConstraints.gridy = 0;
			mGridConstraints.gridheight = 3;
			mGridConstraints.fill = GridBagConstraints.VERTICAL;
			getContentPane().add(mSearchToolBar, mGridConstraints);
			drawDivider();
			mButtonDimensions = new Dimension(70, 50);
			
			for (int i = 0; i < mButtonLabels.length; i++) {
				switch(mButtonLabels[i]) {
				case "open":
					setupButtons(mOpenButton, "Open", "Open an image");
					drawDivider();
					break;
				case "start":
					setupButtons(mStartButton, "Start", "Start the operation");
					drawDivider();
					break;
				case "cancel":
					setupButtons(mCancelButton, "Cancel", "Cancel the operation");
					drawDivider();
					break;
				case "reset":
					setupButtons(mResetButton, "Reset", "Reset the UI");
					drawDivider();
					break;	
				}
			}
			mImagePanel = new SearchImagePanel();
			mImagePanel.setPreferredSize(new Dimension(400, 400));
			mGridConstraints = new GridBagConstraints();
			mGridConstraints.gridx = 1;
			mGridConstraints.gridy = 0;
			mGridConstraints.gridheight = 2;
			mGridConstraints.fill = GridBagConstraints.HORIZONTAL;
			mGridConstraints.anchor = GridBagConstraints.WEST;
			mGridConstraints.insets = new Insets(2, 2, 0, 2);
			getContentPane().add(mImagePanel, mGridConstraints);
			
			
			mProcessOutputLabel.setPreferredSize(new Dimension(400, 40));
			mProcessOutputLabel.setBorder(BorderFactory.createTitledBorder("Process Output"));
			mGridConstraints = new GridBagConstraints();
			mGridConstraints.gridx = 0;
			mGridConstraints.gridy = 3;
			mGridConstraints.gridwidth = 3;
			mGridConstraints.fill = GridBagConstraints.HORIZONTAL;
			mGridConstraints.anchor = GridBagConstraints.EAST;
			mGridConstraints.insets = new Insets(2, 0, 0, 0);
			getContentPane().add(mProcessOutputLabel, mGridConstraints);
			
			mProcessProgressBar = new JProgressBar();
			mProcessProgressBar.setStringPainted(true);
			mProcessProgressBar.setStringPainted(true);
			mProcessProgressBar.setForeground(Color.GREEN);
			mProcessProgressBar.setBorder(BorderFactory.createEtchedBorder());
			mGridConstraints = new GridBagConstraints();
			mGridConstraints.gridx = 0;
			mGridConstraints.gridy = 4;
			mGridConstraints.gridwidth = 3;
			mGridConstraints.fill = GridBagConstraints.HORIZONTAL;
			mGridConstraints.anchor = GridBagConstraints.EAST;
			mGridConstraints.insets = new Insets(0, 2, 0, 2);
			getContentPane().add(mProcessProgressBar, mGridConstraints);
			
		/*
		 * End UI widget definitions
		 */
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(
				(int)(0.5 * (screenSize.width - getWidth())),
				(int)(0.5 * (screenSize.height - getHeight())),
				getWidth(), getHeight()
				);
	}
	//Helper Methods
	private void setupButtons(JButton button, String buttonText, String toolTipText) {
		button.setText(buttonText);
		sizeButtons(button, mButtonDimensions);
		button.setToolTipText(toolTipText);
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		mSearchToolBar.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switch(buttonText.toLowerCase()) {
					case "open":
						mImageChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
						mImageChooser.setAcceptAllFileFilterUsed(false);
						mImageChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						mImageChooser.setMultiSelectionEnabled(false);
						mImageChooser.setCurrentDirectory(new File("images"));
						mImageChooser.setDialogTitle("Select an Image file");
						FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only jpg, png, gif", "jpg","png", "gif");
						mImageChooser.addChoosableFileFilter(restrict);
						int optionSelected = mImageChooser.showOpenDialog(null);
						if (optionSelected == JFileChooser.APPROVE_OPTION) {
							enableButtons(true, "all");
							mResetButton.setIcon(new ImageIcon("reset.gif"));
							final File file = mImageChooser.getSelectedFile();
							try {
								mRasterImage =  ImageIO.read(file);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							mImagePath.setText(mImageChooser.getSelectedFile().getAbsolutePath());
							mImagePanel.repaint();
							setTitle("Search UI Enhancement-"+mImageChooser.getSelectedFile().getName());
						}
						break;
					case "start":
						mFlag = true;
						mStartButton.setIcon(new ImageIcon("search.gif"));
						mStartButton.setText("");
						enableButtons(false, "open");
						enableButtons(false, "reset");
						mResetButton.setIcon(new ImageIcon("resetStatic.png"));
						searchMonitor.addPropertyChangeListener(mSearchUIEnhancer);
						mExec.execute(searchWorker);
						mExec.execute(searchMonitor);
						cancel = false;
						mExec.shutdown();
						break;
					case "cancel":	
						mFlag = false;
						mSearcher.runSearch(mSearchUIEnhancer, mFlag);
						searchWorker.cancel(true);
						mStartButton.setIcon(new ImageIcon("start.png"));
						enableButtons(false, "start");
						mProcessOutputLabel.setText("Search has been cancelled!");
						enableButtons(true, "reset");
						break;
					case "reset":
						enableButtons(true, "all");
						enableButtons(false, "cancel");
						mImagePanel.removeAll();
				}
			}
		});
	}
	private void sizeButtons(JButton button, Dimension size) {
		button.setPreferredSize(size);
		button.setMinimumSize(size);
		button.setMaximumSize(size);
	}
	private void enableButtons(boolean buttonState, String which) {
		switch(which) {
		case "all":
			mResetButton.setEnabled(buttonState);
			mStartButton.setEnabled(buttonState);
			mCancelButton.setEnabled(buttonState);
			mOpenButton.setEnabled(true);
			break;
		case "open":
			mOpenButton.setEnabled(buttonState);
			break;
		case "start":
			mStartButton.setEnabled(buttonState);
			break;
		case "reset":
			mResetButton.setEnabled(buttonState);
			break;
		case "cancel":
			mCancelButton.setEnabled(buttonState);
			break;
		}
	}
	private void drawDivider() {
		mSearchToolBar.addSeparator();
	}
	//UI Events
	private void exitForm(WindowEvent evt) {
		System.exit(0);
	}
	
	//Searcher
	private void runSearch() {
		mSearcher = new BasicSearcher(mRasterImage, Settings.Configuration.regionSide, mProgressValue);
		mSearcher.runSearch(this, mFlag);
	}

	@Override
	public void information(String message) {
		mProcessOutputLabel.setText(message +"\n");
	}

	@Override
	public void possibleMatch(int x0, int y0, int x1, int y1, long timeElapsed, long positionsTriedSoFar) {
		mProcessOutputLabel.setText( "Possible match between: [" + x0 + ", " + y0  + "] and [" + x1 + ", " + y1 + "] at " + (timeElapsed / 1000.0) + "s (" + positionsTriedSoFar + " positions attempted)\n" + "count: ");
		Rectangle r1 = new Rectangle( x0, y0, Settings.Configuration.regionSide, Settings.Configuration.regionSide);
		Rectangle r2 = new Rectangle( x1, y1, Settings.Configuration.regionSide, Settings.Configuration.regionSide);
    	mImagePanel.addHighlight(r1, r2);	
	}

	@Override
	public void update(int position, long timeElapsed, long positionsTriedSoFar, double progress) {
		final int x = position % mRasterImage.getWidth();
		final int y = position / mRasterImage.getWidth();
		mProcessOutputLabel.setText( "Update at: [" + x + "," + y  + "] at " + (timeElapsed / 1000.0) + "s (" + positionsTriedSoFar + " positions attempted)\n");
		getSearchProgress(progress);
	}
	
	public String setCancelFlag(String cancel) {
		return cancel;
	}
	private void getSearchProgress(double progress) {
		mProgressValue = (int) Math.round(progress);
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			mProcessProgressBar.setValue(progress +12);
        } 
	}
	
	
	private class SearchWorker extends SwingWorker<Void, Integer>{	
		@Override
		protected Void doInBackground() throws Exception {
			runSearch();
			return null;
		}
		 
		@Override
		protected void done() {
			if (!isCancelled()) {
				SearchUIEnhancement.mProcessOutputLabel.setText("Search Completed.");
				mStartButton.setText("Start");
				mStartButton.setIcon(new ImageIcon("start.png"));
				mResetButton.setIcon(new ImageIcon("reset.gif"));
				enableButtons(false, "start");
				enableButtons(false, "cancel");
				enableButtons(true, "reset");
			}
		}
		
	}
	private class SearchProgressMonitor extends SwingWorker<Void, Void> implements Runnable{
		@Override
		protected Void doInBackground() throws Exception {
			setProgress(0);
			int progress = 0;
			while(!isCancelled()) {
				Thread.sleep(1000);
				setProgress((int) Math.round(mProgressValue));
				progress++;
				if (isCancelled()) {
					break;
				}
			}
			return null;
		}
		
	}
}
