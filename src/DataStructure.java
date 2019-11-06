import java.io.File;

public interface DataStructure {
	public void build(File myFile, File stopWordsFile, File stemFile, File standardFile);
	public PostingList search(String myString);	//myString is any number of inputs
	public PostingList interSection(String word1, String word2);
	public PostingList neighbourhood(String word1, String word2);
	public PostingList not(String word);
}
