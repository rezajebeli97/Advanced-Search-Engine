import java.io.File;

public interface DataStructure {
	public void build(File myFile, File stopWordsFile, File stemFile, File standardFile);
	public PostingList search(String myString);	//myString is one member of dictionary
	
}
