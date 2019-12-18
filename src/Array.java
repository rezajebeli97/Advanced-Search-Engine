import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.jsoup.Jsoup;

import jhazm.Lemmatizer;
import jhazm.Normalizer;
import jhazm.tokenizer.WordTokenizer;

public class Array implements DataStructure {
	public int numberOfRows;
	ArrayList<PostingList> dictionary = new ArrayList<PostingList>();
	ArrayList<String> stopWords = new ArrayList<String>();
	ArrayList<String> correctHamsansaz = new ArrayList<String>();
	ArrayList<String> wrongHamsansaz = new ArrayList<String>();
	ArrayList<String> tarkibiPorkarbord = new ArrayList<String>();

	@Override
	public void build(File mainFile, File stopWordsFile, File hamsansazFile, File tarkibiPorkarbordFile) {

		// generating stopwords array
		try {
			Scanner scr = new Scanner(stopWordsFile);
			while (scr.hasNextLine()) {
				String stopWord = scr.nextLine();
				stopWords.add(stopWord);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// generating همسان ساز array. I supposed hamsansaz is for 2 words
		try {
			Scanner scr = new Scanner(hamsansazFile);
			while (scr.hasNextLine()) {
				String hamsan = scr.nextLine();
				String[] hamsans = hamsan.split(" ");
				correctHamsansaz.add(hamsans[0]);
				wrongHamsansaz.add(hamsans[1]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// generating tarkibiPorkarbord array
		try {
			Scanner scr = new Scanner(tarkibiPorkarbordFile);
			while (scr.hasNextLine()) {
				String tarkibi = scr.nextLine();
				tarkibiPorkarbord.add(tarkibi);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {

			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(mainFile));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			Static.sheet = wb.getSheetAt(0);
			HSSFRow row;
			HSSFCell cell;

			numberOfRows = Static.sheet.getPhysicalNumberOfRows(); // No of rows
			System.out.println(numberOfRows);

			int cols = 0; // No of columns
			int tmp = 0;

			// This trick ensures that we get the data properly even if it doesn't start
			// from first few rows
			for (int i = 0; i < 10 || i < numberOfRows; i++) {
				row = Static.sheet.getRow(i);
				if (row != null) {
					tmp = Static.sheet.getRow(i).getPhysicalNumberOfCells();
					if (tmp > cols)
						cols = tmp;
				}
			}

			Normalizer normalizer = new Normalizer();
			WordTokenizer tokenizer = new WordTokenizer();
			Lemmatizer lemmatizer = new Lemmatizer();

			for (int r = 1; r < numberOfRows; r++) {
				row = Static.sheet.getRow(r);
				if (row != null) {
					int c = 5; // choosing content column // for (int c = 0; c < cols; c++) {
					cell = row.getCell((short) c);

					if (cell != null) {
						int position = 0;
						String rawText = cell.getRichStringCellValue().getString();
						String noTag = Jsoup.parse(rawText).text();

						String noPunctuationTemp = noTag.replaceAll("\\p{Punct}", "");

						String noPunctuation = noPunctuationTemp.replaceAll("،", "");

						String normalized = normalizer.run(noPunctuation);

						// converting tarkibiPorkarbord words into it's common shape
						for (String s : tarkibiPorkarbord) {
							noPunctuation = noPunctuation.replaceAll(s, s.replace(" ", ""));
						}

						List<String> tokens = tokenizer.tokenize(normalized);

						for (String s : tokens) {
							String word = lemmatizer.lemmatize(s);
							// stopword
							if (stopWords.contains(word))
								continue;
							// همسان ساز
							if (wrongHamsansaz.contains(word)) {
								word = correctHamsansaz.get(wrongHamsansaz.indexOf(word));
							}
							addWord(word, r, position);
							position++;
						}
					}
				}
			}
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
		System.out.println(dictionary.size());
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
		System.out.println(myString);

		Normalizer normal = new Normalizer();
		myString = normal.run(myString);
		
		myString = myString.replaceAll("\"", " \" ");
		myString = myString.replaceAll("!", " ! ");

		// converting بنا بر این to بنابراین
		for (String s : tarkibiPorkarbord) {
			myString = myString.replaceAll(s, s.replace(" ", ""));
		}

		String[] strs_basic = myString.split(" ");
		int z = 0;
		for (int i = 0; i < strs_basic.length; i++) {
			if (!strs_basic[i].equals("") && !stopWords.contains(strs_basic[i])) {
				strs_basic[z] = strs_basic[i];
				z++;
			}
		}

		String[] strs = new String[z];
		for (int i = 0; i < z; i++) {
			// همسان ساز
			if (wrongHamsansaz.contains(strs_basic[i]))
				strs[i] = correctHamsansaz.get(wrongHamsansaz.indexOf(strs_basic[i]));

			else
				strs[i] = strs_basic[i];

		}

		Lemmatizer lemmatize = null;
		try {
			lemmatize = new Lemmatizer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < strs.length; i++) {
			strs[i] = lemmatize.lemmatize(strs[i]);
		}

		PostingList result = new PostingList("", new ArrayList<Article>());
		// Remember
		int i = 0;

		if (strs[0].equals("")) {
			i++;
		}
		if (strs[i].equals("!")) {
			if (strs[i + 1].equals("\"")) {
				int h = 0;
				PostingList tmp = new PostingList("", new ArrayList<Article>());
				for (int j = i + 2; j < strs.length; j++) {
					if (strs[j].equals("\"")) {
						PostingList[] pstList = new PostingList[j - i - 2];
						for (int k = 0; k < pstList.length; k++) {
							pstList[k] = getDictionary(strs[k + i + 2]);
						}
						tmp = neighbourhood(pstList);
						h = j;
					}
				}
				result = not(tmp);
				i = h + 1 + 1;
			} else {
				PostingList pstList = getDictionary(strs[i + 1]);
				result = not(pstList);
				i = +(1 + 1);
			}
		} else if (strs[i].equals("\"")) {
			int h = 0;
			for (int j = i + 1; j < strs.length; j++) {
				if (strs[j].equals("\"")) {
					PostingList[] pstList = new PostingList[j - i - 1];
					for (int k = 0; k < pstList.length; k++) {
						pstList[k] = getDictionary(strs[k + i + 1]);
					}
					result = neighbourhood(pstList);
					h = j;
				}
			}
			i = h + 1 + 1;
		} else if (strs[i].equals("")) {
			i++;
		} else {
			PostingList pstList = getDictionary(strs[i]);
			result = pstList;
			i++;
		}

		for (; i < strs.length; i++) {
			if (strs[i].equals("!")) {
				if (strs[i + 1].equals("\"")) {
					int h = 0;
					PostingList tmp = new PostingList("", new ArrayList<Article>());
					for (int j = i + 2; j < strs.length; j++) {
						if (strs[j].equals("\"")) {
							PostingList[] pstList = new PostingList[j - i - 2];
							for (int k = 0; k < pstList.length; k++) {
								pstList[k] = getDictionary(strs[k + i + 2]);
							}
							tmp = neighbourhood(pstList);
							h = j;
							break;
						}
					}
					result = interSection(result, not(tmp));
					i = h + 1;
				} else {
					PostingList pstList = getDictionary(strs[i + 1]);
					result = interSection(result, not(pstList));
					i = +1;
				}
			} else if (strs[i].equals("\"")) {
				int h = 0;
				for (int j = i + 1; j < strs.length; j++) {
					if (strs[j].equals("\"")) {
						PostingList[] pstList = new PostingList[j - i - 1];
						for (int k = 0; k < pstList.length; k++) {
							pstList[k] = getDictionary(strs[k + i + 1]);
						}
						result = interSection(result, neighbourhood(pstList));
						h = j;
					}
				}
				i = h + 1;
			} else if (strs[i].equals("")) {
			} else {
				PostingList pstList = getDictionary(strs[i]);
				result = interSection(result, pstList);
			}

		}

		return result;
	}

	@Override
	public PostingList interSection(PostingList postingList1, PostingList postingList2) {
		// PostingList postingList1 = getDictionary(word1);
		// PostingList postingList2 = getDictionary(word2);
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
	public PostingList neighbourhood(PostingList[] pls) {
		// PostingList[] pls = new PostingList[words.length];
		// for (int i = 0; i < pls.length; i++) {
		// pls[i] = getDictionary(words[i]);
		// }
		PostingList result = new PostingList("", new ArrayList<Article>());
		for (int i = 0; i < pls[0].articles.size(); i++) {
			for (int j = 0; j < pls[0].articles.get(i).positions.size(); j++) {
				boolean t = true;
				for (int k = 1; k < pls.length; k++) {
					Article artc = getArticle(pls[k], pls[0].articles.get(i).articleNumber);
					if (artc != null) {
						if (articleContains(artc, pls[0].articles.get(i).positions.get(j) + k)) {
							if (k == pls.length - 1) {
								if (result.articles.size() == 0
										|| result.articles.get(result.articles.size() - 1).articleNumber != i)
									result.articles.add(new Article(pls[0].articles.get(i).articleNumber,
											new ArrayList<Integer>()));
								result.articles.get(result.articles.size() - 1).positions
										.add(pls[0].articles.get(i).positions.get(j));
							}
						} else
							break;
					} else {
						t = false;
						break;
					}
				}
				if (!t)
					break;
			}
		}
		return result;
	}

	@Override
	public PostingList not(PostingList postingList) {
		// PostingList postingList = getDictionary(word);
		PostingList newPostingList = new PostingList("", new ArrayList<Article>());
		for (int j = 1; j < postingList.articles.get(0).articleNumber; j++) { // from 1st article till [0] article
			Article article = new Article(j, new ArrayList<Integer>());
			newPostingList.articles.add(article);
		}
		for (int i = 1; i < postingList.articles.size(); i++) {
			for (int j = postingList.articles.get(i - 1).articleNumber + 1; j < postingList.articles
					.get(i).articleNumber; j++) {
				Article article = new Article(j, new ArrayList<Integer>());
				newPostingList.articles.add(article);
			}
		}
		for (int j = postingList.articles.get(postingList.articles.size() - 1).articleNumber
				+ 1; j <= numberOfRows; j++) { // from [last] article ill the last article
			Article article = new Article(j, new ArrayList<Integer>());
			newPostingList.articles.add(article);
		}
		return newPostingList;
	}

}
