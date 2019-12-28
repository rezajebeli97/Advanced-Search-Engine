import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

public class Static {
	public static DataStructure searchEngine;
	public static HSSFSheet sheet;
	
	public static ArrayList<String> tarkibiPorkarbord;
	
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
}
