
public class MaxHeapInt{
	private int[] heap;
	private int[] articles;
	private int currentIndex;

	public MaxHeapInt(int[] array) {
		heap = new int[array.length];
		articles = new int[array.length];
		currentIndex = -1;
		
		for (int i = 0; i < array.length; i++) {
			insert(array[i], i);
		}
	}
	
	private void insert(int t, int articleIndex) {
		if (currentIndex == heap.length)
			return;
		currentIndex++;
		int i = currentIndex;
		for (; i >= 0; i=(i-1)/2) {
			if (i == 0)
				break;
			if (!greater(t, heap[(i-1)/2])) {
				break;
			}
			heap[i] = heap[(i-1)/2];
			articles[i] = articles[(i-1)/2];
		}
		heap[i] = t;
		articles[i] = articleIndex;
	}

	private boolean greater(int t1, int t2) {
		return (t1 > t2);
	}

	private void heapify(int i, int maxCapacity) {
		int leftIndex = 2 * i + 1;
		int rightIndex = 2 * i + 2;
		int max;
		if ((leftIndex <= maxCapacity) && (heap[leftIndex] > heap[i])) 
			max = leftIndex;
		else
			max = i;
		
		if ((rightIndex <= maxCapacity) && (heap[rightIndex] > heap[max]))
			max = rightIndex;
		
		if (max != i) {
			int tmp = heap[max];
			int articleTmp = articles[max];
			heap[max] = heap[i];
			articles[max] = articles[i];
			heap[i] = tmp;
			articles[i] = articleTmp;
			heapify(max, maxCapacity);
		}
			
	}
	
	public int[] heapSort(int k) {
		if (k > heap.length) {
			k = heap.length;
		}
		int[] sorted = new int[k];
		int[] sortedArticles = new int[k];
		
		int i = 0;
		for (; i < k; i++) {
			if (heap[0] <= 0)
				break;
			
			sorted[i] = heap[0];
			sortedArticles[i] = articles[0];
			
			heap[0] = heap[currentIndex - i];
			articles[0] = articles[currentIndex - i];
			heapify(0, currentIndex - i - 1);
		}
		
		int[] nonZeroSortedArticles = removeZeroes(sortedArticles, i);
		
		return sorted;
	}

	private int[] removeZeroes(int[] sortedArticles, int boundary) {
		int[] nonZeroSortedArticles = new int[boundary];
		for (int i = 0; i < boundary; i++) {
			nonZeroSortedArticles[i] = sortedArticles[i];
		}
		return nonZeroSortedArticles;
	}
}
