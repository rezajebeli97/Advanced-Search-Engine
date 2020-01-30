import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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

import edu.stanford.nlp.ling.Word;
import jhazm.Lemmatizer;
import jhazm.Normalizer;
import jhazm.tokenizer.WordTokenizer;

public class Classifier {

	public int numberOfRows1;
	public int numberOfDocs1;
	public int numberOfRows2;
	public int numberOfDocs2;
	ArrayList<PostingList> dictionary = new ArrayList<PostingList>();
	HashMap<String, Integer> map = new HashMap<>();
	ArrayList<String> stopWords = new ArrayList<String>();
	ArrayList<String> correctHamsansaz = new ArrayList<String>();
	ArrayList<String> wrongHamsansaz = new ArrayList<String>();
	ArrayList<String> labels = new ArrayList<String>();

	SparseArray<Integer>[] vectors;
	SparseArray<Float>[] tfidfVectors;
	int[] nt; // how many doc contains word t?
	int k = 3;

	private void build(File mainFile, File stopWordsFile, File hamsansazFile, File abbreviationFile,
			File tarkibiPorkarbordFile, File labelFile, String file, File outputLabels, File mainFile2) {

		generateStopWords(stopWordsFile);

		generateHamsanSazWords(hamsansazFile);

		generateabbreviationWords(abbreviationFile);

		generateTarkibiPorkarbordWords(tarkibiPorkarbordFile);

		generateLabelWords(labelFile);

		try {

			// for first doc
			POIFSFileSystem fs1 = new POIFSFileSystem(new FileInputStream(mainFile));
			HSSFWorkbook wb1 = new HSSFWorkbook(fs1);
			Static.sheet[0] = wb1.getSheetAt(0);
			HSSFRow row1;
			HSSFCell cell1;

			numberOfRows1 = Static.sheet[0].getPhysicalNumberOfRows(); // No of rows
			numberOfDocs1 = numberOfRows1 - 2;
			System.out.println("num of docs : " + numberOfDocs1);
			System.out.println();

			int cols1 = 0; // No. of columns
			int tmp1 = 0;

			// This trick ensures that we get the data properly even if it doesn't start
			// from first few rows
			for (int i = 0; i < 10 || i < numberOfRows1; i++) {
				row1 = Static.sheet[0].getRow(i);
				if (row1 != null) {
					tmp1 = Static.sheet[0].getRow(i).getPhysicalNumberOfCells();
					if (tmp1 > cols1)
						cols1 = tmp1;
				}
			}

			Normalizer normalizer1 = new Normalizer();
			WordTokenizer tokenizer1 = new WordTokenizer();
			Lemmatizer lemmatizer1 = new Lemmatizer();

			for (int r = 2; r < 1002; r++) {
				row1 = Static.sheet[0].getRow(r);
				if (row1 != null) {
					int c = 5; // choosing content column // for (int c = 0; c < cols; c++) {
					cell1 = row1.getCell((short) c);

					if (cell1 != null) {
						int position = 0;
						String rawText = cell1.getRichStringCellValue().getString();

						String normalized = parse_noPunct_normal_porKarbord_mokhafaf(normalizer1, rawText);

						List<String> tokens = tokenizer1.tokenize(normalized);

						for (String s : tokens) {
							String word = lemmatizer1.lemmatize(s);
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

			///////////////////////////////////////////////////
			// for second doc
			POIFSFileSystem fs2 = new POIFSFileSystem(new FileInputStream(mainFile2));
			HSSFWorkbook wb2 = new HSSFWorkbook(fs2);
			Static.sheet[0] = wb2.getSheetAt(0);
			HSSFRow row2;
			HSSFCell cell2;

			numberOfRows2 = Static.sheet[0].getPhysicalNumberOfRows(); // No of rows
			numberOfDocs2 = numberOfRows2 - 2;
			System.out.println("num of docs : " + numberOfDocs2);
			System.out.println();

			int cols2 = 0; // No. of columns
			int tmp2 = 0;

			// This trick ensures that we get the data properly even if it doesn't start
			// from first few rows
			for (int i = 0; i < 10 || i < numberOfRows2; i++) {
				row2 = Static.sheet[0].getRow(i);
				if (row2 != null) {
					tmp2 = Static.sheet[0].getRow(i).getPhysicalNumberOfCells();
					if (tmp2 > cols2)
						cols2 = tmp2;
				}
			}

			Normalizer normalizer2 = new Normalizer();
			WordTokenizer tokenizer2 = new WordTokenizer();
			Lemmatizer lemmatizer2 = new Lemmatizer();

			for (int r = 2; r < numberOfRows2; r++) {
				row2 = Static.sheet[0].getRow(r);
				if (row2 != null) {
					int c = 5; // choosing content column // for (int c = 0; c < cols; c++) {
					cell2 = row2.getCell((short) c);

					if (cell2 != null) {
						int position = 0;
						String rawText = cell2.getRichStringCellValue().getString();

						String normalized = parse_noPunct_normal_porKarbord_mokhafaf(normalizer2, rawText);

						List<String> tokens = tokenizer2.tokenize(normalized);

						for (String s : tokens) {
							String word = lemmatizer2.lemmatize(s);
							// stopword
							if (stopWords.contains(word)) {
								position++;
								continue;
							}
							// همسان ساز
							if (wrongHamsansaz.contains(word)) {
								word = correctHamsansaz.get(wrongHamsansaz.indexOf(word));
							}
							addWord(word, r + 1000, position);
							position++;
						}
					}
				}
			}

			/////////////////////////////////////////////
			System.out.println("Dictionary Built");
			System.out.println("num of unique words : " + map.size());
			System.out.println();

			int numberOfDocs = 1000 + numberOfDocs2;
			vectors = new SparseArray[1000 + numberOfDocs2];
			for (int i = 0; i < vectors.length; i++) {
				vectors[i] = new SparseArray<Integer>();
			}
			nt = new int[map.size()];

			// FOR FIRST FILE
			for (int r = 2; r < 1002; r++) {
				row1 = Static.sheet[0].getRow(r);
				if (row1 != null) {
					int c = 5; // choosing content column // for (int c = 0; c < cols; c++) {
					cell1 = row1.getCell((short) c);

					if (cell1 != null) {
						String rawText = cell1.getRichStringCellValue().getString();

						String normalized = parse_noPunct_normal_porKarbord_mokhafaf(normalizer1, rawText);

						List<String> tokens = tokenizer1.tokenize(normalized);

						for (String s : tokens) {
							String word = lemmatizer1.lemmatize(s);
							// stopword
							if (stopWords.contains(word))
								continue;
							// همسان ساز
							if (wrongHamsansaz.contains(word)) {
								word = correctHamsansaz.get(wrongHamsansaz.indexOf(word));
							}
							updateVector(r - 2, word);
						}
					}
				}
			}

			// FOR SECOND FILE
			for (int r = 2; r < numberOfRows2; r++) {
				row2 = Static.sheet[0].getRow(r);
				if (row2 != null) {
					int c = 5; // choosing content column // for (int c = 0; c < cols; c++) {
					cell2 = row2.getCell((short) c);

					if (cell2 != null) {
						String rawText = cell2.getRichStringCellValue().getString();

						String normalized = parse_noPunct_normal_porKarbord_mokhafaf(normalizer2, rawText);

						List<String> tokens = tokenizer2.tokenize(normalized);

						for (String s : tokens) {
							String word = lemmatizer2.lemmatize(s);
							// stopword
							if (stopWords.contains(word))
								continue;
							// همسان ساز
							if (wrongHamsansaz.contains(word)) {
								word = correctHamsansaz.get(wrongHamsansaz.indexOf(word));
							}
							updateVector(r - 2 + 1000, word);
						}
					}
				}
			}

			updateNt();

			// generating tfidf
			tfidfVectors = new SparseArray[numberOfDocs];
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
								(float) (value * Math.log10((float) (numberOfDocs) / nt[index]))));
						break;
					case 2:
						tfidfVectors[i].array.add(new Sparse<Float>(index, (float) (1 + Math.log10(value))));
						break;
					case 3:
						tfidfVectors[i].array.add(new Sparse<Float>(index,
								(float) ((1 + Math.log10(value)) * Math.log10((float) (numberOfDocs) / nt[index]))));
						break;
					}
				}
			}

		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
		System.out.println("TfIdf Finished");
		System.out.println();

		knnForAll(k, outputLabels);
		// NaiveBayesForAll(outputLabels);

		System.out.println(tfidfVectors.length);
	}

	private String parse_noPunct_normal_porKarbord_mokhafaf(Normalizer normalizer, String rawText) {

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

		return normalized;
	}

	private void NaiveBayesForAll(File outputLabels) {
		System.out.println(labels.size());

		try {
			FileWriter myWriter = new FileWriter(outputLabels);
			for (int i = 1000; i < tfidfVectors.length; i++) {
				String category = NaiveBayes(i);
				myWriter.append(category + "\n");
			}
			myWriter.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private String NaiveBayes(int index) {
		int spNumber = 0;
		int spWord = 0;
		for (int i=0 ; i < labels.size(); i++) {
			if (labels.get(i) == "sp") {
				spNumber++;
				if (hasWord(i)) {
					spWord++;
				}
			}
		}
		
		int soNumber = 0;
		int soWord = 0;
		for (int i=0 ; i < labels.size(); i++) {
			if (labels.get(i) == "sp") {
				soNumber++;
				if (hasWord(i)) {
					soWord++;
				}
			}
		}
		
		int iNumber = 0;
		int iword = 0;
		for (int i=0 ; i < labels.size(); i++) {
			if (labels.get(i) == "sp") {
				iNumber++;
				if (hasWord(i)) {
					iword++;
				}
			}
		}
		
		int mNumber = 0;
		int mword = 0;
		for (int i=0 ; i < labels.size(); i++) {
			if (labels.get(i) == "sp") {
				mNumber++;
				if (hasWord(i)) {
					mword++;
				}
			}
		}
		
		int cNumber = 0;
		int cword = 0;
		for (int i=0 ; i < labels.size(); i++) {
			if (labels.get(i) == "sp") {
				cNumber++;
				if (hasWord(i)) {
					cword++;
				}
			}
		}
		
		int pNumber = 0;
		int pword = 0;
		for (int i=0 ; i < labels.size(); i++) {
			if (labels.get(i) == "sp") {
				pNumber++;
				if (hasWord(i)) {
					pword++;
				}
			}
		}

		
		return max(pword/pNumber, cword/cNumber, mword/mNumber, iword/iNumber, soWord/soNumber,spWord/spNumber);
	}

	
	private void knnForAll(int k, File outputLabels) {
		System.out.println(labels.size());

		try {
			FileWriter myWriter = new FileWriter(outputLabels);
			for (int i = 1000; i < tfidfVectors.length; i++) {
				String category = knn(i, k);
				myWriter.append(category + "\n");
			}
			myWriter.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private String knn(int myIndex, int k) {
		float[] similarity = new float[labels.size()];
		for (int i = 0; i < labels.size(); i++) {
			float s = similaritySparse(tfidfVectors[myIndex], tfidfVectors[i]);
			similarity[i] = s;
		}

		MaxHeap maxHeap = new MaxHeap(similarity);
		int[] sortedIndexes = maxHeap.heapSortIndexes(k);
		if (sortedIndexes.length == 0)
			return "m";

		int[] classes = new int[8];

		for (int j : sortedIndexes) {
			switch (labels.get(j)) {
			case "sc":
				classes[0]++;
				break;

			case "c":
				classes[1]++;
				break;

			case "p":
				classes[2]++;
				break;

			case "e":
				classes[3]++;
				break;

			case "so":
				classes[4]++;
				break;

			case "i":
				classes[5]++;
				break;

			case "sp":
				classes[6]++;
				break;

			case "m":
				classes[7]++;
				break;

			default:
				System.out.println("This class does not exist " + labels.get(j));
				System.out.println("This class does not exist " + j);
				break;
			}
		}
		MaxHeapInt maxHeap2 = new MaxHeapInt(classes);
		int[] bestClassIndex = maxHeap2.heapSortIndexes(1);

		switch (bestClassIndex[0]) {
		case 0:
			return "sc";

		case 1:
			return "c";

		case 2:
			return "p";

		case 3:
			return "e";

		case 4:
			return "so";

		case 5:
			return "i";

		case 6:
			return "sp";

		case 7:
			return "m";

		default:
			System.out.println("error");
			return null;
		}
	}
	
	
	private String max(int i, int j, int l, int m, int n, int o) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean hasWord(int i) {
		// TODO Auto-generated method stub
		return false;
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

	private void updateNt() {
		for (SparseArray<Integer> arrayList : vectors)
			for (Sparse sparse : arrayList.array)
				nt[sparse.index] += 1;
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

	private void generateLabelWords(File LabelFile) {
		try {
			Scanner scr = new Scanner(LabelFile);
			while (scr.hasNextLine()) {
				String stopWord = scr.nextLine();
				labels.add(stopWord);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	private void makeFiles() throws IOException {
		ArrayList<Integer>[] classesIndex = new ArrayList[8];
		for (int i = 0; i < classesIndex.length; i++) {
			classesIndex[i] = new ArrayList<Integer>();
		}

		File[] classesFile = new File[8];
		classesFile[0] = new File("News/Labels/classs0_sc.txt");
		classesFile[1] = new File("News/Labels/classs1_c.txt");
		classesFile[2] = new File("News/Labels/classs2_p.txt");
		classesFile[3] = new File("News/Labels/classs3_e.txt");
		classesFile[4] = new File("News/Labels/classs4_so.txt");
		classesFile[5] = new File("News/Labels/classs5_i.txt");
		classesFile[6] = new File("News/Labels/classs6_sp.txt");
		classesFile[7] = new File("News/Labels/classs7_m.txt");

		File lableFile = new File("News/Labels/allLables.txt");

		generateLabelWords(lableFile);

		for (int i = 0; i < labels.size(); i++) {
			switch (labels.get(i)) {
			case "sc":
				classesIndex[0].add(i);
				break;

			case "c":
				classesIndex[1].add(i);
				break;

			case "p":
				classesIndex[2].add(i);
				break;

			case "e":
				classesIndex[3].add(i);
				break;

			case "so":
				classesIndex[4].add(i);
				break;

			case "i":
				classesIndex[5].add(i);
				break;

			case "sp":
				classesIndex[6].add(i);
				break;

			case "m":
				classesIndex[7].add(i);
				break;

			default:
				System.out.println("error");
			}
		}

		for (int i = 0; i < classesFile.length; i++) {
			FileWriter myWriter = new FileWriter(classesFile[i]);
			for (Integer x : classesIndex[i]) {
				myWriter.append(x + "\n");
			}
			myWriter.close();
		}
	}

	public static void main(String[] args) throws IOException {
		Classifier classifier = new Classifier();
		classifier.makeFiles();

		// for (int i = 13; i <= 13; i++) {
		// String file = i + "";
		// File outputLabels = new File("News/Labels/" + file + ".txt");
		// File mainFile2 = new File("News/Large/" + file + ".xls");
		//
		// classifier.build(new File(Static.mainFile), new File(Static.stopWordsFile),
		// new File(Static.hamsanSazFile), new File(Static.abbreviationFile),
		// new File(Static.tarkibiPorkarbordFile), new File(Static.labelFile), file,
		// outputLabels, mainFile2);
		// }

	}
}
