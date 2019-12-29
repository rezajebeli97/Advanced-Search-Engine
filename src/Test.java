import java.io.IOException;
import java.util.List;

import jhazm.tokenizer.WordTokenizer;

public class Test {
	public static void main(String[] args) throws IOException {
		String myString = "ج.ا";
		WordTokenizer tokenizer = new WordTokenizer();
		List<String> strs = tokenizer.tokenize(myString);
		
		for (String string : strs) {
			System.out.println(string);
		}
		
	}
}