import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Array implements DataStructure{

	@Override
	public void build(File mainFile, File stopWordsFile, File stemFile, File standardFile) {
		Scanner mainFileScnr = new Scanner(mainFile);
		while (mainFileScnr.hasNext()) {
			String myWord = scanner.next();
			String[] words = myWord.split("[^a-zA-Z]+");

			for (String string : words) {
				indexInFile++;
				string = string.toLowerCase();
				myFileString.filePart += string + " ";
				if (!string.equals("") && !findKey(StopWordRoot, string)) {		// check if the word doesn't be a stopWord if(findKey(StopWordRoot , myWord))
					isRepeated = false;
					addKey(root, null , string, file , indexInFile , false);
				}
			}
		}
		
	}

	@Override
	public PostingList search(String myString) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
