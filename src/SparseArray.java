import java.util.ArrayList;

public class SparseArray<T> {
	public ArrayList<Sparse<T>> array;
	
	public SparseArray<T> clone(){
		SparseArray<T> copyArr = new SparseArray<>();
		for (Sparse<T> sparse : this.array) {
			Sparse copySparse = new Sparse<T>(sparse.index, sparse.value);
			copyArr.array.add(copySparse);
		}
		return copyArr;
	}
	
	public SparseArray() {
		array = new ArrayList<Sparse<T>>();
	}
}
