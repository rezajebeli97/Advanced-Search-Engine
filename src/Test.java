import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jhazm.*;
import jhazm.tokenizer.WordTokenizer;

public class Test {
	public static void main(String[] args) throws IOException {
		Test t = new Test();
		String myString = "من کتابی را خریدم";
		System.out.println(myString);

		Normalizer normalizer = new Normalizer(true, false, true);
		myString = normalizer.run(myString);
		
		myString = new Stemmer().stem(myString);
		System.out.println(myString);
		
		List<String> strs = new WordTokenizer().tokenize(myString);
		
		Lemmatizer l = new Lemmatizer();
		for (String string : strs) {
			System.out.println(l.lemmatize(string));
		}
	}

	
}