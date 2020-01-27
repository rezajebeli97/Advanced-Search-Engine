import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jhazm.tokenizer.WordTokenizer;

public class Test {
	public static void main(String[] args) {
		ArrayList<Integer> selective1 = new ArrayList<Integer>();
		selective1.add(1);
		selective1.add(3);
		selective1.add(5);
		selective1.add(7);
		
		ArrayList<Integer> selective2 = new ArrayList<Integer>();
		selective2.add(1);
		selective2.add(3);
		selective2.add(4);
		selective2.add(7);
		
		ArrayList<Integer> selective = interSection(selective1, selective2);
		for (Integer integer : selective) {
			System.out.println(integer);
		}
	}

	private static ArrayList<Integer> interSection(ArrayList<Integer> selective1, ArrayList<Integer> selective2) {

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
}