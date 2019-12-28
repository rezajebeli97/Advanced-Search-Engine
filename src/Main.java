import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
		RankedSearch rs = new RankedSearch();
		rs.build(new File("News/IR-F19-Project01-Input.xls"), new File("News/stopWords.txt"), new File("News/hamsanSaz.txt"), new File("News/tarkibi_porkarbord.txt"));
		rs.search("استقلال");
	}
}