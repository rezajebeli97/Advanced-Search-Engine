import java.io.File;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

public class Static {
	public static DataStructure searchEngine;
	public static HSSFSheet[] sheet;

	public static ArrayList<String> tarkibiPorkarbord;
	public static ArrayList<String> correctAbbreviation;
	public static ArrayList<String> wrongAbbreviation;

	public static String getRowCell(int rowNum, int celNum) {
		HSSFRow row = null;
		HSSFCell cell;
		if (rowNum < 2) {
			System.out.println("index less than expected");
		} else if (rowNum < 7746) {
			row = Static.sheet[0].getRow(rowNum);// remember to check article number
		} else if (rowNum < 18747) {
			row = Static.sheet[1].getRow(rowNum - 7746 + 2);// remember to check article number
		} else if (rowNum < 30019) {
			row = Static.sheet[2].getRow(rowNum - 18747 + 2);// remember to check article number
		} else if (rowNum < 37730) {
			row = Static.sheet[3].getRow(rowNum - 30019 + 2);// remember to check article number
		} else if (rowNum < 46355) {
			row = Static.sheet[4].getRow(rowNum - 37730 + 2);// remember to check article number
		} else if (rowNum < 55111) {
			row = Static.sheet[5].getRow(rowNum - 46355 + 2);// remember to check article number
		} else if (rowNum < 64437) {
			row = Static.sheet[6].getRow(rowNum - 55111 + 2);// remember to check article number
		} else if (rowNum < 75779) {
			row = Static.sheet[7].getRow(rowNum - 64437 + 2);// remember to check article number
		} else if (rowNum < 88766) {
			row = Static.sheet[8].getRow(rowNum - 75779 + 2);// remember to check article number
		} else if (rowNum < 100677) {
			row = Static.sheet[9].getRow(rowNum - 88766 + 2);// remember to check article number
		} else if (rowNum < 113032) {
			row = Static.sheet[10].getRow(rowNum - 100677 + 2);// remember to check article number
		} else if (rowNum < 127227) {
			row = Static.sheet[11].getRow(rowNum - 113032 + 2);// remember to check article number
		} else if (rowNum < 140808) {
			row = Static.sheet[12].getRow(rowNum - 127227 + 2);// remember to check article number
		} else if (rowNum < 149918) {
			row = Static.sheet[13].getRow(rowNum - 140808 + 2);// remember to check article number
		} else if (rowNum < 158272) {
			row = Static.sheet[14].getRow(rowNum - 149918 + 2);// remember to check article number
		} else {
			System.out.println("index out of bound");
		}

		cell = row.getCell((short) celNum);
		if (cell != null) {
			return cell.getRichStringCellValue().getString();
		} else
			return "";
	}

	public static boolean rankedSearch = true;
	public static int weightingScheme = 3; // 3=normal

	public static File[] mainFiles = new File[] { new File("News/Large/1.xls"), new File("News/Large/2.xls"),
			new File("News/Large/3.xls"), new File("News/Large/4.xls"), new File("News/Large/5.xls"),
			new File("News/Large/6.xls"), new File("News/Large/7.xls"), new File("News/Large/8.xls"),
			new File("News/Large/9.xls"), new File("News/Large/10.xls"), new File("News/Large/11.xls"),
			new File("News/Large/12.xls"), new File("News/Large/13.xls"), new File("News/Large/14.xls"),
			new File("News/Large/15.xls") };
	public static String stopWordsFile = "News/stopWords.txt";
	public static String hamsanSazFile = "News/hamsanSaz.txt";
	public static String abbreviationFile = "News/abbreviation.txt";
	public static String tarkibiPorkarbordFile = "News/tarkibi_porkarbord.txt";
	public static String labelFile = "News/Labels/Labels.txt";

	public static File[] categoryFiles = new File[] { new File("News/Labels/classs0_c.txt"),
			new File("News/Labels/classs1_sc.txt"), new File("News/Labels/classs2_p.txt"),
			new File("News/Labels/classs3_e.txt"), new File("News/Labels/classs4_so.txt"),
			new File("News/Labels/classs5_i.txt"), new File("News/Labels/classs6_sp.txt"),
			new File("News/Labels/classs7_m.txt") };

	public static int selectedNumber = 21;

	public static int numOfClusters = 7;
	public static int numOfClusteringIterations = 10;

}
