import java.io.File;

public interface DataStructure {
	public void build(File myFile, File stopWordsFile, File stemFile, File standardFile);
	public PostingList search(String myString);	//myString is any number of inputs
	public PostingList interSection(PostingList postingList1, PostingList postingList2);
	public PostingList neighbourhood(PostingList[] pls);
	public PostingList not(PostingList postingList);
}