import java.io.File;

public interface DataStructure {
	public void build(File mainFile, File stopWordsFile, File hamsansazFile, File abbreviationFile, File tarkibiPorkarbordFile);
	public PostingList search(String myString);	//myString is any number of inputs
	public PostingList interSection(PostingList postingList1, PostingList postingList2);
	public PostingList neighbourhood(PostingList[] pls);
	public PostingList not(PostingList postingList);
}