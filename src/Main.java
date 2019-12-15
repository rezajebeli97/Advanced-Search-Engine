import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileInputStream;



import jhazm.Lemmatizer;
import jhazm.Normalizer;
import jhazm.tokenizer.WordTokenizer;

public class Main {
	public static void main(String[] args) {
		 Array array = new Array();
		 array.build(new File("News.xls"), null, null, null);
//		array.addWord("علی", 1, 1);
//		array.addWord("علی", 1, 2);
//		array.addWord("علی", 1, 3);
//		array.addWord("علی", 3, 1);
//		array.addWord("علی", 3, 2);
//		array.addWord("علی", 5, 3);
//		array.addWord("علی", 5, 4);
//		
//		array.addWord("رضا", 1, 4);
//		array.addWord("رضا", 1, 5);
//		array.addWord("رضا", 1, 6);
//		array.addWord("رضا", 2, 4);
//		array.addWord("رضا", 2, 5);
//		array.addWord("رضا", 3, 6);
//		array.addWord("رضا", 3, 7);
		
		array.search("علی رضا");
		
	}
}