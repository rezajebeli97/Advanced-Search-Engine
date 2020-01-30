import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jhazm.tokenizer.WordTokenizer;

public class Test {
	public static void main(String[] args) {
		try {
			FileWriter myWriter = new FileWriter("News/labels1.txt");

			for (int i = 0; i < 100; i++)
				myWriter.append(i + "\n");

			for (int i = 100; i < 1000; i++) {
				String category = "x" + i;
				myWriter.append(category + "\n");
			}
			myWriter.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}