package com.lexlang.AlltoHtml;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.poi.hssf.converter.ExcelToHtmlConverter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.core.FileURIResolver;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.util.PDFText2HTML;
import org.apache.pdfbox.util.PDFTextStripper;

public class PoiUtil {
	
	public static String removeAllAlphaNum(String html){
		return html.replaceAll("[\\pP+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]" , "")
				.replaceAll("[\\p{P}+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]" , "").replaceAll("[a-zA-Z0-9]", "")
				.replace(" ","").replace("\r", "").replace("\n", "").replace("\t", "");
	}
	
    public static String pdfToHtml(InputStream in) {   
    	String html=pdfToHtml(in,"utf-8");
    	if(isMessyCode(html)){
    		html=pdfToHtml(in,"gbk");
    	}
        return html;
    } 
    
    private static String pdfToHtml(InputStream in,String encoding){  
        try {   
            PDFParser parser = new PDFParser(in);   
            parser.parse();   
            PDDocument pdfDoc = parser.getPDDocument();
            PDFTextStripper pdf_ts = new PDFText2HTML(encoding);
            String result_html1 = StringEscapeUtils.unescapeHtml(pdf_ts.getText(pdfDoc));
            //System.out.println(result_html1);
            org.jsoup.nodes.Document doc = Jsoup.parse(result_html1);
            return doc.body().html();
        } catch (Exception e) {
            e.printStackTrace();   
        }
        return "";
    }
    
	
    public static String word2007ToHtml(InputStream in) throws IOException {
           XWPFDocument document = new XWPFDocument(in);   
           XHTMLOptions options = XHTMLOptions.create();
           ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
           XHTMLConverter.getInstance().convert(document, baos, options);  
           String content = StringEscapeUtils.unescapeHtml(baos.toString());
           baos.close();
           org.jsoup.nodes.Document doc = Jsoup.parse(content);
           return doc.body().html();
    }  
    
    public static String word2003ToHtml(InputStream is) throws TransformerException, IOException,ParserConfigurationException {  
    	String html=word2003ToHtml(is,"UTF-8");
    	if(isMessyCode(html)){
    		html=word2003ToHtml(is,"GBK");
    	}
    	return html;
    }
	
    public static String word2003ToHtml(InputStream is,String encode)throws TransformerException, IOException,ParserConfigurationException {  
        HWPFDocument wordDocument = new HWPFDocument(is);  
        WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(  
                DocumentBuilderFactory.newInstance().newDocumentBuilder()  
                        .newDocument());  
        wordToHtmlConverter.processDocument(wordDocument);  
        Document htmlDocument = wordToHtmlConverter.getDocument();  
        ByteArrayOutputStream out = new ByteArrayOutputStream();  
        DOMSource domSource = new DOMSource(htmlDocument);  
        StreamResult streamResult = new StreamResult(out);  
  
        TransformerFactory tf = TransformerFactory.newInstance();  
        Transformer serializer = tf.newTransformer();  
        serializer.setOutputProperty(OutputKeys.ENCODING, encode);  
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");  
        serializer.setOutputProperty(OutputKeys.METHOD, "html");  
        serializer.transform(domSource, streamResult);  
        out.close();  
        org.jsoup.nodes.Document doc = Jsoup.parse(new String(out.toByteArray()));
        return doc.body().html();
    }
    
    public static String excel2007ToHtml(InputStream is)throws FileNotFoundException, IOException, ParserConfigurationException, TransformerConfigurationException, TransformerException, InvalidFormatException {
    		is =xlsx2xls_progress(is);
    		return excel2003ToHtml(is);
    }
    
    public static String excel2003ToHtml(InputStream is)throws FileNotFoundException, IOException, ParserConfigurationException, TransformerConfigurationException, TransformerException, InvalidFormatException {
    	String html=excel2003ToHtml(is,"UTF-8");
    	if(isMessyCode(html)){
    		html=excel2003ToHtml(is,"GBK");
    	}
    	return html;
    }
    
    
    public static String excel2003ToHtml(InputStream is,String encode)throws FileNotFoundException, IOException, ParserConfigurationException, TransformerConfigurationException, TransformerException, InvalidFormatException {
        HSSFWorkbook excelBook = new HSSFWorkbook(is);

        ExcelToHtmlConverter ethc = new ExcelToHtmlConverter(
                DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        ethc.setOutputColumnHeaders(false);
        ethc.setOutputRowNumbers(false);

        ethc.processWorkbook(excelBook);

        Document htmlDocument = ethc.getDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DOMSource domSource = new DOMSource(htmlDocument);
        StreamResult streamResult = new StreamResult(out);
        
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        //serializer.setOutputProperty(OutputKeys.ENCODING, "GBK");
        serializer.setOutputProperty(OutputKeys.ENCODING, encode);
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setOutputProperty(OutputKeys.METHOD, "html");
        serializer.transform(domSource, streamResult);
        out.close();
        
        String htmlStr = new String(out.toByteArray());
        org.jsoup.nodes.Document doc = Jsoup.parse(htmlStr);
        return doc.body().html();
    }
    
    private static boolean isChinese(char c) {  
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);  
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS  
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS  
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A  
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION  
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION  
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {  
            return true;  
        }  
        return false;  
    } 
    
    /** 
     * 判断字符串是否是乱码 
     * 
     * @param strName 字符串 
     * @return 是否是乱码 
     */  
    private static boolean isMessyCode(String strName) {  
        Pattern p = Pattern.compile("\\s*|t*|r*|n*");  
        Matcher m = p.matcher(removeAllAlphaNum(strName));  
        String after = m.replaceAll("");  
        String temp = after.replaceAll("\\p{P}", "");  
        char[] ch = temp.trim().toCharArray();  
        float chLength = ch.length;  
        float count = 0;  
        for (int i = 0; i < ch.length; i++) {  
            char c = ch[i];  
            if (!Character.isLetterOrDigit(c)) {  
                if (!isChinese(c)) {  
                    count = count + 1;  
                }  
            }  
        }  
        float result = count / chLength;  
        if (count > 4) {  
            return true;  
        } else {  
            return false;  
        }  
   
    }  
    
    private static InputStream xlsx2xls_progress(InputStream in) throws InvalidFormatException,IOException {
        try {
            XSSFWorkbook wbIn = new XSSFWorkbook(in);
            Workbook wbOut = new HSSFWorkbook();
            int sheetCnt = wbIn.getNumberOfSheets();
            for (int i = 0; i < sheetCnt; i++) {
                Sheet sIn = wbIn.getSheetAt(i);
                System.out.println(sIn.getSheetName());
                Sheet sOut = wbOut.createSheet(sIn.getSheetName());
                Iterator<Row> rowIt = sIn.rowIterator();
                while (rowIt.hasNext()) {
                    Row rowIn = rowIt.next();
                    Row rowOut = sOut.createRow(rowIn.getRowNum());

                    Iterator<Cell> cellIt = rowIn.cellIterator();
                    while (cellIt.hasNext()) {
                        Cell cellIn = cellIt.next();
                        Cell cellOut = rowOut.createCell(cellIn.getColumnIndex(), cellIn.getCellType());

                        switch (cellIn.getCellType()) {
                        case Cell.CELL_TYPE_BLANK: break;

                        case Cell.CELL_TYPE_BOOLEAN:
                            cellOut.setCellValue(cellIn.getBooleanCellValue());
                            break;

                        case Cell.CELL_TYPE_ERROR:
                            cellOut.setCellValue(cellIn.getErrorCellValue());
                            break;

                        case Cell.CELL_TYPE_FORMULA:
                            cellOut.setCellFormula(cellIn.getCellFormula());
                            break;

                        case Cell.CELL_TYPE_NUMERIC:
                            cellOut.setCellValue(cellIn.getNumericCellValue());
                            break;

                        case Cell.CELL_TYPE_STRING:
                            cellOut.setCellValue(cellIn.getStringCellValue());
                            break;
                        }

                        {
                            CellStyle styleIn = cellIn.getCellStyle();
                            CellStyle styleOut = cellOut.getCellStyle();
                            styleOut.setDataFormat(styleIn.getDataFormat());
                        }cellOut.setCellComment(cellIn.getCellComment());

                        }
                }
            }
            ByteArrayOutputStream   baos=new   ByteArrayOutputStream();
            try {
                wbOut.write(baos);
                ByteArrayInputStream swapStream = new ByteArrayInputStream(baos.toByteArray());
                return swapStream;
            } finally {
            	baos.close();
            }
        } finally {
            in.close();
        }
    }
    
}