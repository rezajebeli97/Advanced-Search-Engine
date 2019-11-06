import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import jhazm.Lemmatizer;
import jhazm.Normalizer;
import jhazm.tokenizer.WordTokenizer;

public class Array implements DataStructure {
	public int numberOfRows;
	ArrayList<PostingList> dictionary = new ArrayList<PostingList>();

	@Override
	public void build(File mainFile, File stopWordsFile, File stemFile, File standardFile) {
		try {

			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(new File("IR-F19-Project01-Input.xls")));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			HSSFRow row;
			HSSFCell cell;

			int numberOfRows = sheet.getPhysicalNumberOfRows(); // No of rows
			System.out.println(numberOfRows);

			int cols = 0; // No of columns
			int tmp = 0;

			// This trick ensures that we get the data properly even if it doesn't start
			// from first few rows
			for (int i = 0; i < 10 || i < numberOfRows; i++) {
				row = sheet.getRow(i);
				if (row != null) {
					tmp = sheet.getRow(i).getPhysicalNumberOfCells();
					if (tmp > cols)
						cols = tmp;
				}
			}

			for (int r = 1; r < /* numberOfRows */3; r++) {
				int position = 0;
				row = sheet.getRow(r);
				if (row != null) {
					int c = 5; // choosing content column // for (int c = 0; c < cols; c++) {
					cell = row.getCell((short) c);

					if (cell != null) {
						Normalizer normal = new Normalizer(true, true, true);
						String str1 = normal.run(cell.getRichStringCellValue().getString());
						WordTokenizer tokenize = new WordTokenizer();
						List<String> strs = tokenize.tokenize(str1);

						Lemmatizer lemmatize = new Lemmatizer();
						for (String s : strs) {
							String word = lemmatize.lemmatize(s);
							addWord(word, r, position);
							position++;
						}
					}
				}
			}
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}

	public void addWord(String word, int articleNumber, int position) {
		PostingList postingList = getDictionary(word);
		if (postingList == null) {
			ArrayList<Integer> positions = new ArrayList<Integer>();
			positions.add(position);
			ArrayList<Article> articles = new ArrayList<Article>();
			Article article = new Article(articleNumber, positions);
			articles.add(article);
			PostingList newPstList = new PostingList(word, articles);
			dictionary.add(newPstList);
		} else {
			Article article = getArticle(postingList, articleNumber);
			if (article == null) {
				ArrayList<Integer> positions = new ArrayList<Integer>();
				positions.add(position);
				Article newArticle = new Article(articleNumber, positions);
				postingList.articles.add(newArticle);
			} else if (!articleContains(article, position))
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

	@Override
	public PostingList interSection(String word1, String word2) {
		PostingList postingList1 = getDictionary(word1);
		PostingList postingList2 = getDictionary(word2);
		PostingList newPostingList = new PostingList("", new ArrayList<Article>());
		if (postingList1 == null || postingList2 == null)
			return null;

		int x = 0;
		int y = 0;
		while (x < postingList1.articles.size() && y < postingList2.articles.size()) {
			Article article1 = postingList1.articles.get(x);
			Article article2 = postingList2.articles.get(y);
			if (article1.articleNumber == article2.articleNumber) {
				ArrayList<Integer> newPositions = new ArrayList<Integer>();
				for (Integer integer : article1.positions)
					newPositions.add(integer);
				for (Integer integer : article2.positions)
					newPositions.add(integer);

				Article article = new Article(article1.articleNumber, newPositions);
				newPostingList.articles.add(article);
				x++;
				y++;
			} else if (article1.articleNumber < article2.articleNumber)
				x++;
			else
				y++;

		}
		return newPostingList;
	}

	@Override
	public PostingList neighbourhood(String word1, String word2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PostingList not(String word) {
		PostingList postingList = getDictionary(word);
		PostingList newPostingList = new PostingList("", new ArrayList<Article>());
		for (int j = 1; j < postingList.articles.get(0).articleNumber ; j++) {		//from 1st article till [0] article
			Article article = new Article(j, null);
			newPostingList.articles.add(article);
		}
		for (int i = 1 ; i < postingList.articles.size(); i++ ) {
			for (int j = postingList.articles.get(i-1).articleNumber + 1 ; j < postingList.articles.get(i).articleNumber ; j++) {
				Article article = new Article(j, null);
				newPostingList.articles.add(article);
			}
		}
		for (int j = postingList.articles.get(postingList.articles.size()-1).articleNumber + 1 ; j <= numberOfRows ; j++) {		//from [last] article ill the last article
			Article article = new Article(j, null);
			newPostingList.articles.add(article);
		}
		return newPostingList;
	}

}
