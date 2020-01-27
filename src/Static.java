import java.io.File;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

public class Static {
	public static DataStructure searchEngine;
	public static HSSFSheet sheet;
	
	public static ArrayList<String> tarkibiPorkarbord;
	public static ArrayList<String> correctAbbreviation;
	public static ArrayList<String> wrongAbbreviation;
	
	public static String getRowCell(int rowNum, int celNum) {
		HSSFRow row;
		HSSFCell cell;
		row = Static.sheet.getRow(rowNum);//remember to check article number
		cell = row.getCell((short) celNum);
		if (cell != null) {
			 return cell.getRichStringCellValue().getString();
		}
		else
			return "";
	}
	
	public static boolean rankedSearch = true;
	public static int weightingScheme = 3;	//3=normal
	
	public static String mainFile = "News/IR-F19-Project01-Input.xls";
	public static String stopWordsFile = "News/stopWords.txt";
	public static String hamsanSazFile = "News/hamsanSaz.txt";
	public static String abbreviationFile = "News/abbreviation.txt";
	public static String tarkibiPorkarbordFile = "News/tarkibi_porkarbord.txt";
	
	public static int selectedNumber = 8000;
}
