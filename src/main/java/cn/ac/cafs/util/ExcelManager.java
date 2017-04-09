package cn.ac.cafs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelManager
{
  private String excelFileName;
  private String excelTitle = "Shrimp Body Length";
  private String excelCreator = "Image Ruler";
  private String excelSheetName = "result";
  private String[] header = { "图片名", "头胸甲", "第一腹节", "第二腹节", "第三腹节", "第四腹节", "第五腹节", "第六腹节", "尾节" };
  
  public ExcelManager(String excelFileName)
  {
    this.excelFileName = excelFileName;
  }
  
  public void create()
  {
    File excelFile = new File(this.excelFileName);
    if ((excelFile.isFile()) && (excelFile.exists())) {
      excelFile.delete();
    }
    
    try
    {
      XSSFWorkbook workbook = new XSSFWorkbook();
      POIXMLProperties.CoreProperties properties = workbook.getProperties().getCoreProperties();
      properties.setCreator(this.excelCreator);
      properties.setTitle(this.excelTitle);
      
      XSSFSheet sheet = workbook.createSheet(this.excelSheetName);
      XSSFRow row = sheet.createRow(0);
      for (int i = 0; i < this.header.length; i++)
      {
        XSSFCell cell = row.createCell(i);
        cell.setCellValue(this.header[i]);
      }
      FileOutputStream fos = new FileOutputStream(excelFile);
      workbook.write(fos);
      workbook.close();
      fos.close();
    } catch (IOException  e)
    {
      e.printStackTrace();
    }   
  }
  
  public void addRow(String imageName, List<Integer> cols)
  {
    File excelFile = new File(this.excelFileName);
    try
    {
      FileInputStream fis = new FileInputStream(excelFile);
      XSSFWorkbook workbook = new XSSFWorkbook(fis);
      XSSFSheet sheet = workbook.getSheetAt(0);
      int numRows = sheet.getPhysicalNumberOfRows();
      XSSFRow row = sheet.createRow(numRows);
      
      XSSFCell cell = row.createCell(0);
      cell.setCellValue(imageName);
      for (int i = 0; i < cols.size(); i++)
      {
        cell = row.createCell(i + 1);
        cell.setCellValue(((Integer)cols.get(i)).intValue());
      }
      fis.close();
      
      FileOutputStream fos = new FileOutputStream(excelFile);
      workbook.write(fos);
      workbook.close();
      fos.close();
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
}
