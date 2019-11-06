import java.util.ArrayList;
import java.util.LinkedList;

public class PostingList {
	public PostingList(String word, int numberOfArticles, ArrayList<Article> articles) {
		// TODO Auto-generated constructor stub
		this.word = word;
		this.numberOfArticles = numberOfArticles;
		this.articles = articles;
	}
	public String word;
	public int numberOfArticles;
	ArrayList<Article> articles;
}