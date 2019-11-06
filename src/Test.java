import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jhazm.*;
import jhazm.tokenizer.WordTokenizer;

public class Test {
	public static void main(String[] args) {
		
		ArrayList<Integer> a1 = new ArrayList<Integer>();
		a1.add(2);
		a1.add(3);
		a1.add(4);
		
		ArrayList<Integer> a2 = new ArrayList<Integer>();
		a2.add(6);
		a2.add(7);
		a2.add(8);
		
		a1.addAll(a2);
		
		System.out.println(a2.size());
	}
}
