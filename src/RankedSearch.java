import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Sheet;
import org.jsoup.Jsoup;

import jhazm.Lemmatizer;
import jhazm.Normalizer;
import jhazm.tokenizer.WordTokenizer;

public class RankedSearch implements DataStructure {
	public int numOfRows = 2;
	public int numOfDocs = 0;
	ArrayList<PostingList> dictionary = new ArrayList<PostingList>();
	HashMap<String, Integer> map = new HashMap<>();
	ArrayList<String> stopWords = new ArrayList<String>();
	ArrayList<String> correctHamsansaz = new ArrayList<String>();
	ArrayList<String> wrongHamsansaz = new ArrayList<String>();
	ArrayList<Integer>[] categories = new ArrayList[8];

	SparseArray<Integer>[] vectors;
	SparseArray<Float>[] tfidfVectors;
	int[] nt; // how many doc contains word t?
	private Cluster[] clusters;

	@Override
	public void build(File[] mainFiles, File stopWordsFile, File hamsansazFile, File abbreviationFile,
			File tarkibiPorkarbordFile) {
		
		Static.sheet = new HSSFSheet[15];

		generateStopWords(stopWordsFile);

		generateHamsanSazWords(hamsansazFile);

		generateabbreviationWords(abbreviationFile);

		generateTarkibiPorkarbordWords(tarkibiPorkarbordFile);

		generateCategories(Static.categoryFiles);

		try {

			Normalizer normalizer = new Normalizer();
			WordTokenizer tokenizer = new WordTokenizer();
			Lemmatizer lemmatizer = new Lemmatizer();
			POIFSFileSystem fs;
			HSSFWorkbook wb = null;
				
			
			for (int i = 0; i < 1; i++) {
				fs = new POIFSFileSystem(new FileInputStream(mainFiles[i]));
				wb = new HSSFWorkbook(fs);
				Static.sheet[i] = wb.getSheetAt(0);
				int cumulativeNumOfDocs = addWordsOfFile(normalizer, tokenizer, lemmatizer, Static.sheet[i], numOfDocs);
				numOfDocs += cumulativeNumOfDocs;
				numOfRows += cumulativeNumOfDocs;
				System.out.println("file[" + i + "] added to dictionary");
				System.out.println("num of unique words : " + map.size());
				System.out.println();
			}

			System.out.println("dictionary built");
			System.out.println("num of unique words : " + map.size());
			System.out.println();
			
			vectors = new SparseArray[numOfDocs];
			for (int i = 0; i < vectors.length; i++) {
				vectors[i] = new SparseArray<Integer>();
			}
			nt = new int[map.size()];

			numOfDocs = 0;
			numOfRows = 2;
			for (int i = 0; i < 1; i++) {
				int cumulativeNumOfDocs = buildVectorsOfFile(normalizer, tokenizer, lemmatizer, Static.sheet[i], numOfDocs);
				numOfDocs += cumulativeNumOfDocs;
				numOfRows += cumulativeNumOfDocs;
			}

			updateNt();

			// generating tfidf
			tfidfVectors = new SparseArray[numOfDocs];
			for (int i = 0; i < tfidfVectors.length; i++) {
				tfidfVectors[i] = new SparseArray<Float>();
			}
			for (int i = 0; i < vectors.length; i++) {
				SparseArray<Integer> vector = vectors[i];
				for (Sparse<Integer> sparse : vector.array) {
					int index = sparse.index;
					int value = sparse.value;
					switch (Static.weightingScheme) {
					case 1:
						tfidfVectors[i].array.add(new Sparse<Float>(index,
								(float) (value * Math.log10((float) (numOfDocs) / nt[index]))));
						break;
					case 2:
						tfidfVectors[i].array.add(new Sparse<Float>(index, (float) (1 + Math.log10(value))));
						break;
					case 3:
						tfidfVectors[i].array.add(new Sparse<Float>(index,
								(float) ((1 + Math.log10(value)) * Math.log10((float) (numOfDocs) / nt[index]))));
						break;
					}
				}
			}

		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
		System.out.println("TfIdf Finished");
		System.out.println();

		// TODO
		clustering(Static.numOfClusters);
		System.out.println("Clustering Finished");
		for (Cluster cluster : clusters) {
			System.out.println(cluster.indexes.size());
		}
	}

	private int buildVectorsOfFile(Normalizer normalizer, WordTokenizer tokenizer, Lemmatizer lemmatizer, HSSFSheet sheet, int previousDocs) {
		HSSFRow row = null;
		HSSFCell cell = null;
		
		int numberOfRows = sheet.getPhysicalNumberOfRows(); // No of rows
		int numberOfDocs = numberOfRows - 2;
		for (int r = 2; r < numberOfRows; r++) {
			row = sheet.getRow(r);
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
						normalized = normalized.replaceAll(s, s.replace(" ", ""));
					}

					// مخفف converting ج.ا to جمهوری اسلامی
					for (int i = 0; i < Static.wrongAbbreviation.size(); i++) {
						String wrong = Static.wrongAbbreviation.get(i);
						String correct = Static.correctAbbreviation.get(i);
						normalized = normalized.replaceAll(wrong, correct);
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
						updateVector(r - 2 + previousDocs, word);
					}
				}
			}
		}
		return numberOfDocs;
	}

	private int addWordsOfFile(Normalizer normalizer, WordTokenizer tokenizer, Lemmatizer lemmatizer, HSSFSheet sheet, int previousDocs) {
		HSSFRow row = null;
		HSSFCell cell = null;

		int numberOfRows = sheet.getPhysicalNumberOfRows(); // No of rows
		int numberOfDocs = numberOfRows - 2;
		System.out.println("num of docs : " + numberOfDocs);
		System.out.println();

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

		for (int r = 2; r < numberOfRows; r++) {
			row = sheet.getRow(r);
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
						normalized = normalized.replaceAll(s, s.replace(" ", ""));
					}
					// مخفف converting ج.ا to جمهوری اسلامی
					for (int i = 0; i < Static.wrongAbbreviation.size(); i++) {
						String wrong = Static.wrongAbbreviation.get(i);
						String correct = Static.correctAbbreviation.get(i);
						normalized = normalized.replaceAll(wrong, correct);
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

						addWord(word, r + previousDocs, position);
						position++;
					}
				}
			}
		}
		return numberOfDocs;

	}

	private void generateCategories(File[] categoryFiles) {
		for (int i = 0; i < categories.length; i++) {
			categories[i] = new ArrayList<Integer>();
		}
		try {
			for (int i = 0; i < categoryFiles.length; i++) {
				File category = categoryFiles[i];
				Scanner scr = new Scanner(category);
				while (scr.hasNextLine()) {
					String index = scr.nextLine();
					categories[i].add(Integer.parseInt(index));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	private void clustering(int k) { // k = num of clusters
		clusters = new Cluster[k];
		for (int i = 0; i < k; i++) {
			clusters[i] = new Cluster();
			int centroidIndex = (int) (Math.random() * numOfDocs);
			clusters[i].centroid = tfidfVectors[centroidIndex];
		}
		for (int iteration = 0; iteration < Static.numOfClusteringIterations; iteration++) {
			for (int i = 0; i < clusters.length; i++) {
				clusters[i].indexes = new ArrayList<>();
			}
			for (int docIndex = 0; docIndex < tfidfVectors.length; docIndex++) { // detect cluster for each data; به
																					// کدوم کلاستر نزدیکتره هر داک
				float max = 0;
				int maxClusterIndex = 0;
				for (int clusterIndex = 0; clusterIndex < clusters.length; clusterIndex++) {
					SparseArray<Float> centroid = clusters[clusterIndex].centroid;
					float similarity = similaritySparse(centroid, tfidfVectors[docIndex]);
					if (similarity > max) {
						max = similarity;
						maxClusterIndex = clusterIndex;
					}
				}
				clusters[maxClusterIndex].indexes.add(docIndex);

			}

			for (int clusterIndex = 0; clusterIndex < clusters.length; clusterIndex++) { // compute centroid of each
																							// cluster
				SparseArray<Float> tmp = clusters[clusterIndex].centroid.clone();
				for (int docIndex : clusters[clusterIndex].indexes) {
					tmp = addSparse(tfidfVectors[docIndex], tmp);
				}
				tmp = divisionSparse(tmp, clusters[clusterIndex].indexes.size());
				clusters[clusterIndex].centroid = tmp;
			}
		}
	}

	private SparseArray<Float> divisionSparse(SparseArray<Float> tmp, int size) {
		for (int i = 0; i < tmp.array.size(); i++) {
			tmp.array.get(i).value = (float) tmp.array.get(i).value / (float) 2;
		}
		return tmp;
	}

	private SparseArray<Float> addSparse(SparseArray<Float> sparseArray1, SparseArray<Float> sparseArray2) {
		float tmp = 0;
		int index1 = 0;
		int index2 = 0;
		SparseArray<Float> resultArray = new SparseArray<Float>();

		while (index1 < sparseArray1.array.size() && index2 < sparseArray2.array.size()) {
			int arr1Index = sparseArray1.array.get(index1).index;
			int arr2Index = sparseArray2.array.get(index2).index;
			float arr1Value = sparseArray1.array.get(index1).value;
			float arr2Value = sparseArray2.array.get(index2).value;

			if (arr1Index == arr2Index) {
				tmp = arr1Value + arr2Value;
				resultArray.array.add(new Sparse<Float>(arr1Index, tmp));
				index1++;
				index2++;
			} else if (arr1Index < arr2Index) {
				resultArray.array.add(new Sparse<Float>(arr1Index, arr1Value));
				index1++;
			} else {
				resultArray.array.add(new Sparse<Float>(arr2Index, arr2Value));
				index2++;
			}
		}

		if (index1 < sparseArray1.array.size()) {
			for (; index1 < sparseArray1.array.size(); index1++) {
				resultArray.array.add(
						new Sparse<Float>(sparseArray1.array.get(index1).index, sparseArray1.array.get(index1).value));
			}
		}
		if (index2 < sparseArray2.array.size()) {
			for (; index2 < sparseArray2.array.size(); index2++) {
				resultArray.array.add(
						new Sparse<Float>(sparseArray2.array.get(index2).index, sparseArray2.array.get(index2).value));
			}
		}
		return resultArray;
	}

	private float similaritySparse(SparseArray<Float> sparseArr1, SparseArray<Float> sparseArr2) {
		float tmp = 0;
		int index1 = 0;
		int index2 = 0;

		while (index1 < sparseArr1.array.size() && index2 < sparseArr2.array.size()) {
			int sparseArr1Index = sparseArr1.array.get(index1).index;
			int sparseArr2Index = sparseArr2.array.get(index2).index;

			if (sparseArr1Index == sparseArr2Index) {
				tmp += sparseArr1.array.get(index1).value * sparseArr2.array.get(index2).value;
				index1++;
				index2++;
			} else if (sparseArr1Index < sparseArr2Index) {
				index1++;
			} else {
				index2++;
			}
		}

		float sizeSparseArr1 = size(sparseArr1);
		float sizeSparseArr2 = size(sparseArr2);
		tmp /= ((float) sizeSparseArr1 * (float) sizeSparseArr2);

		return tmp;
	}

	private void zipfLaw() {
		MaxHeapInt maxHeap = new MaxHeapInt(nt);
		int[] res = maxHeap.heapSortValues(1000);

		for (int i : res) {
			System.out.println(i);
		}
	}

	private void updateNt() {
		for (SparseArray<Integer> arrayList : vectors)
			for (Sparse sparse : arrayList.array)
				nt[sparse.index] += 1;
	}

	@Override
	public PostingList search(String myString) {

		Normalizer normal = new Normalizer(true, false, true);
		myString = normal.run(myString);

		// converting بنا بر این to بنابراین
		for (String s : Static.tarkibiPorkarbord) {
			myString = myString.replaceAll(s, s.replace(" ", ""));
		}

		// مخفف converting ج.ا to جمهوری اسلامی
		for (int i = 0; i < Static.wrongAbbreviation.size(); i++) {
			String wrong = Static.wrongAbbreviation.get(i);
			String correct = Static.correctAbbreviation.get(i);
			myString = myString.replaceAll(wrong, correct);
		}

		/////////////////////

		if (myString.contains("cat")) {
			String queryCategory = getCategory(myString);
			myString = removeCategory(myString);

			PostingList postingList = search3(myString);
			String[] strs = tokenizeRanked(myString);
			SparseArray<Float> queryTfidfVector = computeTfIdf(strs);
			int bestClusterIndex = bestClusterIndex(queryTfidfVector);
			ArrayList<Integer> clusterSet = clusters[bestClusterIndex].indexes;

			// TODO: after developing cat
			ArrayList<Integer> classificationSet = getIndexesOfClass(queryCategory);

			PostingList selectedPstList = interSection(postingList,  classificationSet, clusterSet);
			if (selectedPstList == null)
				return null;
			return rankArticles(queryTfidfVector, selectedPstList.articles);

		} else {
			PostingList postingList = search3(myString);
			String[] strs = tokenizeRanked(myString);
			SparseArray<Float> queryTfidfVector = computeTfIdf(strs);
			int bestClusterIndex = bestClusterIndex(queryTfidfVector);
			ArrayList<Integer> clusterSet = clusters[bestClusterIndex].indexes;
			PostingList selectedPstList = interSection(postingList, clusterSet);
			if (selectedPstList == null)
				return null;
			return rankArticles(queryTfidfVector, selectedPstList.articles);
		}
	}

	private ArrayList<Integer> getIndexesOfClass(String queryCategory) {
		switch (queryCategory) {
		case "sc":
			return categories[0];

		case "c":
			return categories[1];

		case "p":
			return categories[2];

		case "e":
			return categories[3];

		case "so":
			return categories[4];

		case "i":
			return categories[5];

		case "sp":
			return categories[6];

		case "m":
			return categories[7];

		default:
			System.out.println("error");
		}
		return null;
	}

	private int bestClusterIndex(SparseArray<Float> tfIdfVector) {
		float max = 0;
		int maxCluster = 0;
		for (int i = 0; i < clusters.length; i++) {
			float similarity = similaritySparse(clusters[i].centroid, tfIdfVector);
			if (similarity > max) {
				max = similarity;
				maxCluster = i;
			}
		}
		return maxCluster;
	}

	private static String removeCategory(String myString) {
		String category = "";
		int start = myString.indexOf("cat");
		for (int i = start; i < myString.length(); i++) {
			char c = myString.charAt(i);
			if (c == ' ')
				break;
			else
				category += c;
		}
		return myString.replace(category, "");
	}

	private String getCategory(String myString) {
		String category = "";
		int start = myString.indexOf("cat");
		for (int i = start + 4; i < myString.length(); i++) {
			char c = myString.charAt(i);
			if (c == ' ')
				break;
			else
				category += c;
		}
		return category;
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
			String[] strs = tokenize(myString);
			myString = strs[0];
			Lemmatizer lemmatize = null;
			try {
				lemmatize = new Lemmatizer();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			myString = lemmatize.lemmatize(myString);
			if (wrongHamsansaz.contains(myString)) {
				myString = correctHamsansaz.get(wrongHamsansaz.indexOf(myString));
			}
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

	public PostingList rankArticles(SparseArray<Float> queryTfidfVector, ArrayList<Article> articles) {

		// Normalizer normal = new Normalizer();
		//
		// // converting بنا بر این to بنابراین
		// for (String s : Static.tarkibiPorkarbord) {
		// myString = myString.replaceAll(s, s.replace(" ", ""));
		// }
		// // مخفف converting ج.ا to جمهوری اسلامی
		// for (int i = 0; i < Static.wrongAbbreviation.size() ; i++) {
		// String wrong = Static.wrongAbbreviation.get(i);
		// String correct = Static.correctAbbreviation.get(i);
		// myString = myString.replaceAll(wrong, correct);
		// }

		// String[] strs = tokenizeRanked(myString);

		float[] similarity = new float[articles.size()];
		for (int i = 0; i < articles.size(); i++) {
			// check articles[i].articleNumber - 1
			SparseArray<Float> tfidfVector = tfidfVectors[articles.get(i).articleNumber - 2];
			float tmp = similaritySparse(tfidfVector, queryTfidfVector);
			// float sizeTfidfVector = size(tfidfVector);
			// float sizeQueryTfidfVector = size(queryTfidfVector);
			// tmp /= ((float)sizeTfidfVector * (float)sizeQueryTfidfVector);
			similarity[i] = tmp;
		}

		MaxHeap maxHeap = new MaxHeap(similarity);
		int[] sortedArticlesIndexes = maxHeap.heapSortIndexes(Static.selectedNumber); // this returns article indexes
																						// from 0 to
		// similarity.length - 1

		ArrayList<Article> sortedArticles = new ArrayList<>();

		for (int i : sortedArticlesIndexes) {
			sortedArticles.add(articles.get(i));
		}

		return new PostingList("", sortedArticles);
	}

	private SparseArray<Float> computeTfIdf(String[] strs) {
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

		for (int i = 0; i < strs.length; i++) {
			if (wrongHamsansaz.contains(strs[i])) {
				strs[i] = correctHamsansaz.get(wrongHamsansaz.indexOf(strs[i]));
			}
		}

		SparseArray<Integer> queryVector = new SparseArray<Integer>();

		for (String string : strs) {
			if (map.get(string) == null)
				continue;

			int wordIndex = map.get(string);

			for (int i = 0; i <= queryVector.array.size(); i++) {
				if (i == queryVector.array.size()) {
					Sparse sparse = new Sparse<Integer>(wordIndex, 1);
					queryVector.array.add(i, sparse);
					break;
				} else if (queryVector.array.get(i).index == wordIndex) {
					queryVector.array.get(i).value++;
					break;
				} else if (queryVector.array.get(i).index > wordIndex) {
					Sparse sparse = new Sparse<Integer>(wordIndex, 1);
					queryVector.array.add(i, sparse);
					break;
				}
			}

		}

		SparseArray<Float> queryTfidfVector = new SparseArray<Float>();

		float max = 0;
		for (Sparse<Integer> sparse : queryVector.array) {
			max = Math.max(max, sparse.value);
		}
		for (Sparse<Integer> sparse : queryVector.array) {
			int index = sparse.index;
			int value = sparse.value;
			switch (Static.weightingScheme) {
			case 1:
				queryTfidfVector.array.add(new Sparse<Float>(index, (float) ((0.5 + (float) sparse.value / (2 * max))
						* Math.log10((float) (numOfDocs) / nt[index]))));
				break;
			case 2:

				queryTfidfVector.array
						.add(new Sparse<Float>(index, (float) Math.log10(1 + (float) (numOfDocs) / nt[index])));
				break;
			case 3:
				queryTfidfVector.array.add(new Sparse<Float>(index,
						(float) ((1 + Math.log10(sparse.value)) * Math.log10((float) (numOfDocs) / nt[index]))));
				break;
			}
		}

		return queryTfidfVector;
	}

	private float size(SparseArray<Float> arrayList) {
		float result = 0;
		for (Sparse<Float> sparse : arrayList.array) {
			result += Math.pow(sparse.value, 2);
		}
		if (result == 0)
			return (float) 0.001;
		result = (float) Math.sqrt(result);
		return result;
	}

	private void updateVector(int articleIndex, String word) {
		int wordIndex = map.get(word);
		SparseArray<Integer> vector = vectors[articleIndex];
		for (int i = 0; i <= vector.array.size(); i++) {
			if (i == vector.array.size()) {
				Sparse sparse = new Sparse<Integer>(wordIndex, 1);
				vector.array.add(i, sparse);
				break;
			}
			if (vector.array.get(i).index == wordIndex) {
				vector.array.get(i).value++;
				break;
			} else if (vector.array.get(i).index > wordIndex) {
				Sparse sparse = new Sparse<Integer>(wordIndex, 1);
				vector.array.add(i, sparse);
				break;
			}
		}
	}

	private void addWord(String word, int articleNumber, int position) {
		if (map.get(word) == null) {
			map.put(word, map.size());
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

	private void generateabbreviationWords(File abbreviationFile) {
		Static.wrongAbbreviation = new ArrayList<String>();
		Static.correctAbbreviation = new ArrayList<String>();
		try {
			Scanner scr = new Scanner(abbreviationFile);
			while (scr.hasNextLine()) {
				String hamsan = scr.nextLine();
				int tmp = hamsan.indexOf(" ");
				String wrong = hamsan.substring(0, tmp);
				String correct = hamsan.substring(tmp + 1);
				Static.correctAbbreviation.add(correct);
				Static.wrongAbbreviation.add(wrong);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generate(File hamsansazFile) {
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
		if (('"' == myString.charAt(myString.length() - 1)) && ('"' == myString.charAt(0))
				&& !removeQuote(myString).contains("\""))
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
		String[] strs = tokenize(myString);
		if (strs.length == 1) {
			return true;
		}
		else if (myString.contains(" ")) {
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

	private PostingList interSection(PostingList pls, ArrayList<Integer> selective1, ArrayList<Integer> selective2) {
		ArrayList<Integer> selective = interSection(selective1, selective2);
		return interSection(pls, selective);
	}

	private PostingList interSection(PostingList pstList, ArrayList<Integer> selective) {

		PostingList newPostingList = new PostingList("", new ArrayList<Article>());
		if (pstList == null || selective == null)
			return null;

		int x = 0;
		int y = 0;
		while (x < pstList.articles.size() && y < selective.size()) {
			Article article = pstList.articles.get(x);
			int select = selective.get(y);
			if (article.articleNumber == select) {
				// ArrayList<Integer> newPositions = new ArrayList<Integer>();
				// for (Integer integer : article.positions)
				// newPositions.add(integer);
				// Article newArticle = new Article(article.articleNumber, newPositions);
				newPostingList.articles.add(article);
				x++;
				y++;
			} else if (article.articleNumber < select)
				x++;
			else
				y++;
		}
		return newPostingList;
	}

	private ArrayList<Integer> interSection(ArrayList<Integer> selective1, ArrayList<Integer> selective2) {

		ArrayList<Integer> newSelective = new ArrayList<Integer>();
		if (selective1 == null || selective2 == null)
			return null;

		int x = 0;
		int y = 0;
		while (x < selective1.size() && y < selective2.size()) {
			int select1 = selective1.get(x);
			int select2 = selective2.get(y);
			if (select1 == select2) {
				newSelective.add(select1);
				x++;
				y++;
			} else if (select1 < select2)
				x++;
			else
				y++;
		}
		return newSelective;
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
				+ 1; j <= numOfRows; j++) { // from [last] article ill the last article
			Article article = new Article(j, new ArrayList<Integer>());
			newPostingList.articles.add(article);
		}
		return newPostingList;
	}

}
