package pro.identpixels.searcher;

public interface Searcher {
	int numberOfPositionsToTry();
	int numberOfPositionsTriedSoFar();
	void runSearch(SearchListener listener, boolean stopSearcher);
	void reset();
	void cancel();
	public interface SearchListener {
		void information(String message);
		void possibleMatch(int x0, int y0, int x1, int y1, long timeElapsed, long positionsTriedSoFar);
		void update(int position, long timeElapsed, long positionsTriedSoFar, double progress);
	}
}
