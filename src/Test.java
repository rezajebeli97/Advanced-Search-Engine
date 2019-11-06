import java.io.IOException;
import java.util.List;

import jhazm.*;
import jhazm.tokenizer.WordTokenizer;

public class Test {
	public static void main(String[] args) throws IOException {
		
		
		Normalizer n = new Normalizer(true, true, true);
		String str1 = n.run("");
		System.out.println(str1);
		
		WordTokenizer wrdt = new WordTokenizer();
		List<String> strs = wrdt.tokenize(str1);
		for (String s : strs) {
			System.out.println(s);
		}
		
		for (String s : strs) {
			System.out.println(s);
			
			Lemmatizer l = new Lemmatizer();
			String y = l.lemmatize(s);
			System.out.println(y);
			
			Stemmer stm = new Stemmer();
			String x = stm.stem(y);
			System.out.println(x);
			
			
			
			System.out.println();
		}
		
		
	}
}
