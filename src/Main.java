import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import jhazm.Lemmatizer;
import jhazm.Normalizer;
import jhazm.Stemmer;
import jhazm.tokenizer.WordTokenizer;

public class Main {
	public static void main(String[] args) throws IOException {
//		Array array = new Array();
//		array.build(new File("News/IR-F19-Project01-Input.xls"), null, null, null);
//		array.search("علی !\"سیب\"");
		 
		
		String input = "این روز ها به مدرسه میرویم علی ای حال دانشگاه نیز مع ذلک بنابر این است.";
		String noTag = Jsoup.parse(input).text();
		System.out.println(noTag);
		
		String noPunctuationTemp = noTag.replaceAll("\\p{Punct}", "");
		System.out.println(noPunctuationTemp);
		
		String noPunctuation = noPunctuationTemp.replaceAll("،", "");
		System.out.println(noPunctuation);
		
		List<String> strs = new WordTokenizer().tokenize(new Normalizer().run(noPunctuation));
		for (String s : strs) {
			String output = new Lemmatizer().lemmatize(s);
			System.out.println(output);
		}
		
		
		
//		//test query
//		ArrayList<String> stopWords = new ArrayList<String>();
//		ArrayList<String> correctHamsansaz = new ArrayList<String>();
//		ArrayList<String> wrongHamsansaz = new ArrayList<String>();
//		ArrayList<String> tarkibiPorkarbord = new ArrayList<String>();
//		
//		// generating stopwords array
//				try {
//					Scanner scr = new Scanner(new File("News/stopWords.txt"));
//					while (scr.hasNextLine()) {
//						String stopWord = scr.nextLine();
//						stopWords.add(stopWord);
//					}
//				} catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				// generating همسان ساز array. I supposed hamsansaz is for 2 words
//				try {
//					Scanner scr = new Scanner(new File("News/hamsanSaz.txt"));
//					while (scr.hasNextLine()) {
//						String hamsan = scr.nextLine();
//						String[] hamsans = hamsan.split(" ");
//						correctHamsansaz.add(hamsans[0]);
//						wrongHamsansaz.add(hamsans[1]);
//					}
//				} catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				// generating tarkibiPorkarbord array
//				try {
//					Scanner scr = new Scanner(new File("News/tarkibi_porkarbord.txt"));
//					while (scr.hasNextLine()) {
//						String tarkibi = scr.nextLine();
//						tarkibiPorkarbord.add(tarkibi);
//					}
//				} catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		
//		
//		
//		String myString = "مدرسه در علی بنابر این بنا بر این مع ذلک !کلاغ";
//
//		Normalizer normal = new Normalizer();
//		myString = normal.run(myString);
//		
//		myString = myString.replaceAll("\"", " \" ");
//		myString = myString.replaceAll("!", " ! ");
//
//		// converting بنا بر این to بنابراین
//		for (String s : tarkibiPorkarbord) {
//			myString = myString.replaceAll(s, s.replace(" ", ""));
//		}
//
//		String[] strs_basic = myString.split(" ");
//		int z = 0;
//		for (int i = 0; i < strs_basic.length; i++) {
//			if (!strs_basic[i].equals("") && !stopWords.contains(strs_basic[i])) {
//				strs_basic[z] = strs_basic[i];
//				z++;
//			}
//		}
//
//		String[] strs = new String[z];
//		for (int i = 0; i < z; i++) {
//			// همسان ساز
//			if (wrongHamsansaz.contains(strs_basic[i]))
//				strs[i] = correctHamsansaz.get(wrongHamsansaz.indexOf(strs_basic[i]));
//
//			else
//				strs[i] = strs_basic[i];
//
//		}
//
//		Lemmatizer lemmatize = null;
//		try {
//			lemmatize = new Lemmatizer();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		for (int i = 0; i < strs.length; i++) {
//			strs[i] = lemmatize.lemmatize(strs[i]);
//		}
//		
//		for (String string : strs) {
//			System.out.println(string);
//		}
		
	}
}