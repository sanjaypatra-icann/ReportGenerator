package com.utils;

/**
 * Created by SP48716 on 10-05-2017.
 */
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;



public class NestedTables {

    public static final String DEST = "D:\\report\\nested_tables.pdf";

    public static void main(String[] args) throws IOException,
            DocumentException {
        File file = new File(DEST);
        file.getParentFile().mkdirs();
        new NestedTables().createPdf(DEST);
    }

    public void createPdf(String dest) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();
        PdfPTable table = new PdfPTable(6);
        //table.setWidths(new int[]{2,2, 2, 2, 2,2});
        PdfPCell cell;
        //cell = new PdfPCell(new Phrase("S/N"));
        //cell.setRowspan(2);
        //table.addCell(cell);
        for(int i=0;i<2;i++) {
            cell = new PdfPCell(new Phrase("VULNERABILITY"));
            cell.setColspan(6);
            table.addCell(cell);
            /*cell = new PdfPCell(new Phrase("TOOL"));
            table.addCell(cell);*/
            table.addCell("BLOCKER");
            table.addCell("CRITICAL");
            table.addCell("MAJOR");
            table.addCell("MINOR");
            table.addCell("INFO");
            table.addCell("TOOL");
        /*    cell.setRowspan(1);
            table.addCell("");*/
            table.addCell("James");
            table.addCell("Fish");
            table.addCell("Stone");
            table.addCell("Fish");
            table.addCell("Stone");
             table.addCell("17");

            table.addCell("James2");
            table.addCell("Fish2");
            table.addCell("Stone2");
            table.addCell("Fish2");
            table.addCell("Stone2");
            table.addCell("18");

            document.add(table);
        }
        document.close();
    }
}
