package com.cloudera.phoenixdemo.util;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

/**
 * @author Charles
 * @package com.cloudera.phoenixdemo.util
 * @classname PdfReadUtils
 * @description 读取PDF文件内容工具类
 * @date 2019-5-10 12:02
 */
public class PdfReadUtils {

    public static Map<String, Object> readPdf(File file) {
        Map<String, Object> map = new HashMap<>();
        List<String> pdfLines = new ArrayList<>();
        try {
            PDDocument document = PDDocument.load(file);
            int pages = document.getNumberOfPages();
            //自己划定区间，将文件内容全部包含在内
            Rectangle rectBase = new Rectangle(0, 0, 682, 800);
            for (int i = 0; i < pages; i++) {
                PDPage page = document.getPage(i);
                PDFTextStripperByArea stripper;
                stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);
                stripper.addRegion("base", rectBase);
                stripper.extractRegions(page);
                //获取每一页的数据信息
                String lines = stripper.getTextForRegion("base");
                pdfLines.add(lines);
            }

            map.put("ok", true);
            map.put("pdfLines", pdfLines);
        } catch (InvalidPasswordException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return map;
        }

    }

    public static void main(String[] args) {
        //File file = new File("resource/12345.pdf");
//        File file = new File("C:\\Users\\110610172\\Desktop\\大数据治理.pdf");
//        Map<String, Object> readPdf = readPdf(file);
//        if ((boolean) readPdf.get("ok")) {
//            List<String> pdfLines = (List) readPdf.get("pdfLines");
//            for (String object : pdfLines) {
//                /**
//                 * 将该页信息进行切割
//                 * 因为在Linux上面使用\r容易出现问题(下标越界比较多)。小心使用
//                 */
//                //String[] line = object.split("\r\n");
//                String[] line = object.split("\\n");
//                for (String string : line) {
//                    System.out.println(string);
//                }
//            }
//        }

        try (PDDocument document = PDDocument.load(new File("C:/Users/110610172/Desktop/大数据治理.pdf"))) {

            document.getClass();

            if(!document.isEncrypted()) {
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);
                PDFTextStripper tStripper = new PDFTextStripper();

                String pdfFileInText = tStripper.getText(document);

                String[] lines = pdfFileInText.split("\\r?\\n");
                for(String line : lines) {
                    System.out.println(line);
                }

            }

        } catch (InvalidPasswordException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }
}