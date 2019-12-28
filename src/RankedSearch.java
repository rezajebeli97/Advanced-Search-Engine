import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.jsoup.Jsoup;

import jhazm.Lemmatizer;
import jhazm.Normalizer;
import jhazm.tokenizer.WordTokenizer;

public class RankedSearch implements DataStructure {
	public int numberOfRows;
	public int numberOfDocs;
	ArrayList<PostingList> dictionary = new ArrayList<PostingList>();
	HashMap<String, Integer> map = new HashMap<>();
	ArrayList<String> stopWords = new ArrayList<String>();
	ArrayList<String> correctHamsansaz = new ArrayList<String>();
	ArrayList<String> wrongHamsansaz = new ArrayList<String>();
	int[][] vectors;
	float[][] tfidfVectors;
	int[] nt; // how many docs contain word t?
	int selectedNumber = 100;
	ArrayList<String> strings = new ArrayList<String>();

	@Override
	public void build(File mainFile, File stopWordsFile, File hamsansazFile, File tarkibiPorkarbordFile) {

		generateStopWords(stopWordsFile);

		generateHamsanSazWords(hamsansazFile);

		generateTarkibiPorkarbordWords(tarkibiPorkarbordFile);

		try {

			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(mainFile));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			Static.sheet = wb.getSheetAt(0);
			HSSFRow row;
			HSSFCell cell;

			numberOfRows = Static.sheet.getPhysicalNumberOfRows(); // No of rows
			numberOfDocs = numberOfRows - 1;
			System.out.println(numberOfDocs);

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
						for (String s : Static.tarkibiPorkarbord) {
							noPunctuation = noPunctuation.replaceAll(s, s.replace(" ", ""));
						}

						List<String> tokens = tokenizer.tokenize(normalized);

						for (String s : tokens) {
							String word = lemmatizer.lemmatize(s);
							// stopword
							if (stopWords.contains(word)) {
								position++;
								continue;
							}
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

			System.out.println(map.size());

			vectors = new int[numberOfDocs][map.size()];
			nt = new int[map.size()];

			for (int r = 1; r < numberOfRows; r++) {
				row = Static.sheet.getRow(r);
				if (row != null) {
					int c = 5; // choosing content column // for (int c = 0; c < cols; c++) {
					cell = row.getCell((short) c);

					if (cell != null) {
						String rawText = cell.getRichStringCellValue().getString();
						String noTag = Jsoup.parse(rawText).text();

						String noPunctuationTemp = noTag.replaceAll("\\p{Punct}", "");

						String noPunctuation = noPunctuationTemp.replaceAll("،", "");

						String normalized = normalizer.run(noPunctuation);

						// converting tarkibiPorkarbord words into it's common shape
						for (String s : Static.tarkibiPorkarbord) {
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
							updateVector(r - 1, word);
						}
						updateNt(r - 1);
					}
				}
			}

			// generating tfidf
			tfidfVectors = new float[numberOfDocs][map.size()];
			for (int i = 0; i < vectors.length; i++) {
				for (int j = 0; j < vectors[0].length; j++) {
					if (vectors[i][j] == 0)
						continue;
					switch (Static.weightingScheme) {
					case 1:
						tfidfVectors[i][j] = (float) (vectors[i][j] * Math.log10((numberOfDocs) / nt[j]));
						break;
					case 2:
						tfidfVectors[i][j] = (float) (1 + Math.log10(vectors[i][j]));
						break;
					case 3:
						tfidfVectors[i][j] = (float) ((1 + Math.log10(vectors[i][j])) * Math.log10((numberOfDocs) / nt[j]));
						break;
					}
					
				}
			}

		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
		System.out.println(map.size());

	}

	private void zipfLaw() {
		MaxHeapInt maxHeap = new MaxHeapInt(nt);
		int[] res = maxHeap.heapSort(nt.length);

		for (int i : res) {
			System.out.println(i);
		}
	}

	private void updateNt(int articleIndex) {
		for (int i = 0; i < nt.length; i++) {
			if (vectors[articleIndex][i] != 0) {
				nt[i] += 1;
			}
		}
	}

	@Override
	public PostingList search(String myString) {

		Normalizer normal = new Normalizer(true, false, true);
		myString = normal.run(myString);

		// converting بنا بر این to بنابراین
		for (String s : Static.tarkibiPorkarbord) {
			myString = myString.replaceAll(s, s.replace(" ", ""));
		}

		PostingList postingList = search3(myString);

		return rankArticles(myString, postingList.articles);

	}

	public PostingList search3(String myString) {
		PostingList result;
		if (quoteFul(myString)) {
			String subString = removeQuote(myString);
			String[] strs = tokenize(subString);
			PostingList[] pls = new PostingList[strs.length];
			for (int i = 0; i < strs.length; i++) {
				pls[i] = search3(strs[i]);
			}
			result = neighbourhood(pls);
		} else if (notFul(myString)) {
			String subString = removeNot(myString);
			PostingList pl = search3(subString);
			result = not(pl);
		} else if (singleWord(myString)) {
			Lemmatizer lemmatize = null;
			try {
				lemmatize = new Lemmatizer();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			myString = lemmatize.lemmatize(myString);

			result = getDictionary(myString);
		} else { // and
			String[] strs = tokenize(myString);
			PostingList[] pls = new PostingList[strs.length];
			for (int i = 0; i < strs.length; i++) {
				pls[i] = search3(strs[i]);
			}
			result = interSection(pls);
		}
		return result;

	}

	public PostingList rankArticles(String myString, ArrayList<Article> articles) {
		System.out.println(myString);

		Normalizer normal = new Normalizer();

		// converting بنا بر این to بنابراین
		for (String s : Static.tarkibiPorkarbord) {
			myString = myString.replaceAll(s, s.replace(" ", ""));
		}

		String[] strs = tokenizeRanked(myString);

		// Lemmatize
		try {
			Lemmatizer lemmatize = new Lemmatizer();
			for (int i = 0; i < strs.length; i++) {
				strs[i] = lemmatize.lemmatize(strs[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int[] queryVector = new int[map.size()];
		for (String string : strs) {
			if (map.get(string) != null) {
				queryVector[map.get(string)]++;
			}
		}

		float[] queryTfidfVector = new float[map.size()];
		for (int j = 0; j < map.size(); j++) {
			if (queryVector[j] == 0)
				continue;
			switch (Static.weightingScheme) {
			case 1:
				float max = 0;
				for (float q : queryVector) {
					max = Math.max(max, q);
				}
				queryTfidfVector[j] = (float) ((0.5 + queryVector[j]/(2 * max)) * Math.log10((numberOfDocs) / nt[j]));
				break;
			case 2:
				queryTfidfVector[j] = (float) Math.log10(1 + (numberOfDocs) / nt[j]);
				break;
			case 3:
				queryTfidfVector[j] = (float) ((1 + Math.log10(queryVector[j])) * Math.log10((numberOfDocs) / nt[j]));
				break;
			}
			
		}

		float[] similarity = new float[articles.size()];
		for (int i = 0; i < articles.size(); i++) {
			// check articles[i].articleNumber - 1
			float[] tfidfVector = tfidfVectors[articles.get(i).articleNumber - 1];
			float tmp = 0;
			for (int j = 0; j < tfidfVector.length; j++) {
				tmp += tfidfVector[j] * queryTfidfVector[j];
			}
			float sizeTfidfVector = size(tfidfVector);
			float sizeQueryTfidfVector = size(queryTfidfVector);
			tmp /= (sizeTfidfVector * sizeQueryTfidfVector);
			similarity[i] = tmp;
		}

		MaxHeap maxHeap = new MaxHeap(similarity);
		int[] sortedArticlesIndexes = maxHeap.heapSort(selectedNumber); // this returns article indexes from 0 to
																		// similarity.length - 1

		ArrayList<Article> sortedArticles = new ArrayList<>();

		for (int i : sortedArticlesIndexes) {
			sortedArticles.add(articles.get(i));
		}

		return new PostingList("", sortedArticles);
	}

	private float size(float[] arr) {
		float result = 0;
		for (int i = 0; i < arr.length; i++) {
			result += Math.pow(arr[i], 2);
		}
		if (result == 0)
			return (float) 0.001;
		result = (float) Math.sqrt(result);
		return result;
	}

	private void updateVector(int articleIndex, String word) {
		int wordIndex = map.get(word);
		vectors[articleIndex][wordIndex]++;
	}

	private void addWord(String word, int articleNumber, int position) {
		if (map.get(word) == null) {
			map.put(word, map.size());
			strings.add(word);
		}
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

	private void generateStopWords(File stopWordsFile) {
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
	}

	private void generateHamsanSazWords(File hamsansazFile) {
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

	}

	private void generateTarkibiPorkarbordWords(File tarkibiPorkarbordFile) {
		// generating tarkibiPorkarbord array
		Static.tarkibiPorkarbord = new ArrayList<String>();
		try {
			Scanner scr = new Scanner(tarkibiPorkarbordFile);
			while (scr.hasNextLine()) {
				String tarkibi = scr.nextLine();
				Static.tarkibiPorkarbord.add(tarkibi);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	private String removeNot(String myString) {
		return myString.substring(1, myString.length());
	}

	private String removeQuote(String myString) {
		return myString.substring(1, myString.length() - 1);
	}

	private boolean quoteFul(String myString) {
		if (('\"' == myString.charAt(myString.length() - 1)) && ('\"' == myString.charAt(0)))
			return true;
		return false;
	}

	private boolean notFul(String myString) {
		if ('!' == myString.charAt(0)) {
			String myString2 = myString.substring(1, myString.length());
			if (quoteFul(myString2)) {
				return true;
			}
			if (singleWord(myString2)) {
				return true;
			}
			return false;
		}
		return false;
	}

	private boolean singleWord(String myString) {
		if (myString.contains(" ")) {
			return false;
		}
		return true;
	}

	public String[] tokenize(String myString) {
		ArrayList<String> tokens = new ArrayList<>();
		for (int i = 0; i < myString.length();) {
			char c = myString.charAt(i);
			if (c == '"') {
				int j = i + 1;
				while (j < myString.length() && myString.charAt(j) != '"') {
					j++;
				}
				tokens.add(myString.substring(i, j + 1));
				i = j + 1;
			} else if (c == '!') {
				int j = i + 1;
				if (myString.charAt(j) == '"') {
					int k = j + 1;
					while (k < myString.length() && myString.charAt(k) != '"') {
						k++;
					}
					tokens.add(myString.substring(i, k + 1));
					i = k + 1;
				} else {
					int k = j + 1;
					while (k < myString.length() && myString.charAt(k) != ' ') {
						k++;
					}
					tokens.add(myString.substring(i, k));
					i = k + 1;
				}
			} else if (c == ' ') {
				i++;
			} else {
				int j = i + 1;
				while (j < myString.length() && myString.charAt(j) != ' ') {
					j++;
				}
				tokens.add(myString.substring(i, j));
				i = j + 1;
			}
		}
		String[] tokens_arr = new String[tokens.size()];
		for (int i = 0; i < tokens_arr.length; i++) {
			tokens_arr[i] = tokens.get(i);
		}
		return tokens_arr;
	}

	public String[] tokenizeRanked(String myString) {
		ArrayList<String> tokens = new ArrayList<>();
		for (int i = 0; i < myString.length();) {
			char c = myString.charAt(i);
			if (c == '"') {
				int j = i + 1;
				while (j < myString.length() && myString.charAt(j) != '"') {
					if (myString.charAt(j) == ' ') {
						tokens.add(myString.substring(i + 1, j));
						i = j;
					}
					j++;
				}
				tokens.add(myString.substring(i + 1, j));
				i = j + 1;
			} else if (c == '!') {
				int j = i + 1;
				if (myString.charAt(j) == '"') {
					int k = j + 1;
					while (k < myString.length() && myString.charAt(k) != '"') {
						k++;
					}
					i = k + 1;
				} else {
					int k = j + 1;
					while (k < myString.length() && myString.charAt(k) != ' ') {
						k++;
					}
					i = k + 1;
				}
			} else if (c == ' ') {
				i++;
			} else {
				int j = i + 1;
				while (j < myString.length() && myString.charAt(j) != ' ') {
					j++;
				}
				tokens.add(myString.substring(i, j));
				i = j + 1;
			}
		}

		String[] tokens_arr = new String[tokens.size()];
		for (int i = 0; i < tokens_arr.length; i++) {
			tokens_arr[i] = tokens.get(i);
		}
		return tokens_arr;
	}

	private PostingList interSection(PostingList[] pls) {
		PostingList result = interSection(pls[0], pls[1]);
		for (int i = 2; i < pls.length; i++) {
			result = interSection(result, pls[i]);
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
								if (result.articles.size() == 0 || result.articles.get(result.articles.size()
										- 1).articleNumber != pls[0].articles.get(i).articleNumber)
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
