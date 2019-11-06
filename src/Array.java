import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class Array implements DataStructure {
	ArrayList<PostingList> dictionary = new ArrayList<PostingList>();

	@Override
	public void build(File mainFile, File stopWordsFile, File stemFile, File standardFile) {
		addWord("Amirkabir", 3, 4);
		addWord("Ali", 3, 4);
		addWord("Amirkabir", 3, 48);
		addWord("Amirkabir", 3, 4);
		addWord("Ali", 5, 4);
	}

	private void addWord(String word, int articleNumber, int position) {
		PostingList postingList = getDictionary(word);
		if (postingList == null) {
			ArrayList<Integer> positions = new ArrayList<Integer>();
			positions.add(position);
			ArrayList<Article> articles = new ArrayList<Article>();
			Article article = new Article(articleNumber, positions);
			articles.add(article);
			PostingList newPstList = new PostingList(word, 1, articles);
			dictionary.add(newPstList);
		}
		else {
			Article article = getArticle(postingList, articleNumber);
			if (article == null) {
				ArrayList<Integer> positions = new ArrayList<Integer>();
				positions.add(position);
				Article newArticle = new Article(articleNumber, positions);
				postingList.articles.add(newArticle);
				postingList.numberOfArticles++;
			}
			else if (!articleContains(article, position)) 
					article.positions.add(position);
		}	
	}

	private PostingList getDictionary(String word) {
		for (PostingList postingList : dictionary)
			if (postingList.word.equals(word))
				return postingList;
		return null;
	}

	private Article getArticle(PostingList postingList, int articleIndex) {
		for (Article article : postingList.articles)
			if (article.articleNumber == articleIndex)
				return article;
		return null;
	}
	
	private boolean articleContains(Article article, int positionIndex) {
		for (Integer position : article.positions)
			if (position == positionIndex)
				return true;
		return false;
	}

	@Override
	public PostingList search(String myString) {
		// TODO Auto-generated method stub
		return null;
	}

}
