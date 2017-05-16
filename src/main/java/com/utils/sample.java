package com.utils;

/**
 * Created by SP48716 on 08-05-2017.
 */

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleInsets;
import org.sonar.wsclient.SonarClient;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueClient;
import org.sonar.wsclient.issue.IssueQuery;
import org.sonar.wsclient.issue.Issues;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceSearchResult;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.List;

class Sample {

    public static void main(String args[]) throws DocumentException, IOException, ParseException{

        String login = "admin";
        String password = "admin";

        SonarClient client = SonarClient.create("http://localhost:9000");
        client.builder().login(login);
        client.builder().password(password);



        IssueQuery query = IssueQuery.create();
        query.severities("CRITICAL", "MAJOR", "MINOR");
        //System.out.println(query.

        IssueClient issueClient = client.issueClient();
        Issues issues = issueClient.find(query);
        List<Issue> issueList = issues.list();
        createExcel(issueList);
        createPdf(issueList,"FileApplicationUsingMavenTest");
    }

    private static void createExcel(List<Issue> issueList) {
// TODO Auto-generated method stub

        try {
            String filename = "D:/report/SonarIssueDemo.xls";

            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("FirstSheet");

            HSSFRow rowhead = sheet.createRow((short) 0);
            rowhead.createCell(0).setCellValue("Project Key");
            rowhead.createCell(1).setCellValue("Component");
            rowhead.createCell(2).setCellValue("Line");
            rowhead.createCell(3).setCellValue("Rule Key");
            rowhead.createCell(4).setCellValue("Severity");
            rowhead.createCell(5).setCellValue("Message");

           // issueList.size()

            for (int i = 0; i < issueList.size(); i++) {
                HSSFRow row = sheet.createRow((short) i+1);
                row.createCell(0).setCellValue(issueList.get(i).projectKey());
                System.out.println("issueList.get(i).projectKey() :: " + issueList.get(i).projectKey());
                row.createCell(1).setCellValue(issueList.get(i).componentKey());

                System.out.println("issueList.get(i).componentKey() :: " + issueList.get(i).componentKey());

                row.createCell(2).setCellValue(
                        String.valueOf(issueList.get(i).line()));
                System.out.println("issueList.get(i).line() :: " + issueList.get(i).line());
                row.createCell(3).setCellValue(issueList.get(i).ruleKey());
                System.out.println("issueList.get(i).ruleKey() :: " + issueList.get(i).ruleKey());
                row.createCell(4).setCellValue(issueList.get(i).severity());
                System.out.println("issueList.get(i).severity() :: " + issueList.get(i).severity());
                row.createCell(5).setCellValue(issueList.get(i).message());
                System.out.println("issueList.get(i).message() :: " + issueList.get(i).message());

                System.out.println("issueList.get(i).ruleKey().contains :: "+issueList.get(i).ruleKey().contains("SkippedUnitTests"));

                System.out.println("issueList.get(i).ruleKey().actionPlan :: "+issueList.get(i).ruleKey());
                System.out.println("Status:: "+issueList.get(i).status());

                System.out.println("==================================== ");

            }

            FileOutputStream fileOut = new FileOutputStream(filename);
            workbook.write(fileOut);
            fileOut.close();
            System.out.println("Your excel file has been generated!");

        } catch (Exception ex) {
            System.out.println(ex);

        }
    }

    private static void createPdf(List<Issue> issueList, String projectName)
            throws DocumentException, IOException, ParseException {

        Map<String, Map<String, Integer>> issuesMap = segData(issueList);
        Document document = new Document();
        DateFormat dt = new SimpleDateFormat("dd-MM-yyyy");
        String date = dt.format(new Date()).toString();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("D:\\report\\" + projectName + "-" + date + ".pdf"));
        document.open();
        Paragraph preface = new Paragraph();
        Paragraph paragraph = new Paragraph(" SONAR REPORT ", FontFactory.getFont(FontFactory.TIMES_ROMAN, 18));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        preface.setAlignment(Element.ALIGN_CENTER);
        preface.add(paragraph);
        preface.add(new Paragraph(" "));
        document.add(preface);
        preface = new Paragraph();
        preface.setAlignment(Element.ALIGN_MIDDLE);
        paragraph = new Paragraph("Executive Summary ", FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, new BaseColor(0, 148, 117)));
        paragraph.setAlignment(Element.ANCHOR);
        preface.setIndentationLeft(50);
        preface.add(paragraph);
        preface.add(new Paragraph("Company      :     ICANN ", FontFactory.getFont(FontFactory.TIMES_ROMAN, 10)));
        preface.add(new Paragraph("Application  :     " + projectName, FontFactory.getFont(FontFactory.TIMES_ROMAN, 10)));
        preface.add(new Paragraph("Scan Date    :     " + date, FontFactory.getFont(FontFactory.TIMES_ROMAN, 10)));
        preface.add(new Paragraph(" "));
        document.add(preface);
        document.add(createTable("VULNERABILITY", issuesMap.get("VULNERABILITY")));
        document.add(createTable("BUGS", issuesMap.get("BUGS")));
        document.add(createTable("CODE_SMELL", issuesMap.get("CODE_SMELL")));
        document.add(createTable("FINDBUGS", issuesMap.get("FINDBUGS")));


        JFreeChart chart = generatePieChart(issuesMap);
        PdfContentByte contentByte = writer.getDirectContent();
        PdfTemplate template = contentByte.createTemplate(350, 400);
        Graphics2D graphics2d = template.createGraphics(350, 400,
                new DefaultFontMapper());
        Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, 350, 400);

        chart.draw(graphics2d, rectangle2d);

        graphics2d.dispose();
        contentByte.addTemplate(template, 125, 125);
        preface = new Paragraph();
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));
        preface.add(new Paragraph(" "));

        paragraph = new Paragraph(" BLOCKER: Must be fixed Immediately ", FontFactory.getFont(FontFactory.TIMES_ROMAN, 7));
        preface.add(paragraph);
        paragraph = new Paragraph(" CRITICAL: Must be reviewed immediately and fixed soon", FontFactory.getFont(FontFactory.TIMES_ROMAN, 7));
        preface.add(paragraph);
        paragraph = new Paragraph(" MAJOR: High potential for significant to moderate impact ", FontFactory.getFont(FontFactory.TIMES_ROMAN, 7));
        preface.add(paragraph);
        paragraph = new Paragraph(" MINOR: Potential for moderate for minor impact ", FontFactory.getFont(FontFactory.TIMES_ROMAN, 7));
        preface.add(paragraph);
        paragraph = new Paragraph(" INFO: Neither a bug nor a quality flaw. No action required ", FontFactory.getFont(FontFactory.TIMES_ROMAN, 7));
        preface.add(paragraph);

        document.add(preface);
        document.close();
        System.out.println("Your pdf file has been generated!");
    }
    private static Map<String, Map<String, Integer>> segData(List<Issue> issueList) {

        Map<String, Map<String, Integer>> issuesMap = new HashMap<String, Map<String, Integer>>();

        for (int i = 0; i < issueList.size(); i++) {
            if (!"CLOSED".equalsIgnoreCase(issueList.get(i).status())) {
                if(issueList.get(i).ruleKey().contains("squid")==true){
                    String bugsev=issueList.get(i).severity();
                    Map<String, Integer> bugsMap =new HashMap<String,Integer>();
                        if (bugsMap.containsKey(bugsev)) {
                            int count = bugsMap.get(bugsev);
                            count = count + 1;
                            bugsMap.put(bugsev, count);
                            issuesMap.put("FINDBUGS", bugsMap);
                        } else {
                            bugsMap.put(bugsev, 1);
                            issuesMap.put("FINDBUGS", bugsMap);
                        }


                }
            }

        }


        return issuesMap;
    }
    public static PdfPTable createTable(String type, Map<String, Integer> data) {

        PdfPTable mailtable = new PdfPTable(1);
        PdfPTable table = new PdfPTable(5);
        PdfPCell cell;
        cell = new PdfPCell(new Phrase(type));
        cell.setPaddingLeft(170.0F);

        if (type == "BUGS") {
            cell.setBackgroundColor(new BaseColor(255, 165, 0));
        } else if (type == "VULNERABILITY") {
            cell.setBackgroundColor(new BaseColor(255, 99, 71));
        } else if (type == "CODE_SMELL") {
            cell.setBackgroundColor(new BaseColor(255, 215, 0));
        } else if (type == "FINDBUGS") {
            cell.setBackgroundColor(new BaseColor(255, 215, 0));
        }
        mailtable.addCell(cell);
        try {
            cell = new PdfPCell(getPhrase("BLOCKER"));
            cell.setColspan(1);
            table.addCell(cell);
            cell = new PdfPCell(getPhrase("CRITICAL"));
            cell.setColspan(1);
            table.addCell(cell);
            cell = new PdfPCell(getPhrase("MAJOR"));
            cell.setColspan(1);
            table.addCell(cell);
            cell = new PdfPCell(getPhrase("MINOR"));
            cell.setColspan(1);
            table.addCell(cell);
            cell = new PdfPCell(getPhrase("INFO"));
            cell.setColspan(1);
            table.addCell(cell);
            // now we add a cell with rowspan 2

            if (data.get("BLOCKER") != null) {
                table.addCell(new PdfPCell(getPhrase(data.get("BLOCKER").toString())));
            } else {
                table.addCell(new PdfPCell(getPhrase("0")));
            }
            if (data.get("CRITICAL") != null) {
                table.addCell(new PdfPCell(getPhrase(data.get("CRITICAL").toString())));
            } else {
                table.addCell(new PdfPCell(getPhrase("0")));
            }
            if (data.get("MAJOR") != null) {
                table.addCell(new PdfPCell(getPhrase(data.get("MAJOR").toString())));
            } else {
                table.addCell(new PdfPCell(getPhrase("0")));
            }
            if (data.get("MINOR") != null) {
                table.addCell(new PdfPCell(getPhrase(data.get("MINOR").toString())));
            } else {
                table.addCell(new PdfPCell(getPhrase("0")));
            }
            if (data.get("INFO") != null) {
                table.addCell(new PdfPCell(getPhrase(data.get("INFO").toString())));
            } else {
                table.addCell(new PdfPCell(getPhrase("0")));
            }
            // we add the four remaining cells with addCell()
            mailtable.addCell(table);
        } catch (Exception e) {

        }
        return mailtable;
    }

    private static Phrase getPhrase(String text) throws DocumentException, IOException {
        FontSelector selector1 = new FontSelector();
        Font f1 = FontFactory.getFont(FontFactory.TIMES_ROMAN, 8);
        f1.setColor(BaseColor.BLACK);
        selector1.addFont(f1);
        Phrase phrase = selector1.process(text);
        return phrase;
    }

    private static JFreeChart generatePieChart(Map<String, Map<String, Integer>> issuesMap) {
        DefaultPieDataset dataSet = new DefaultPieDataset();
        int blockerCount = 0;
        int criticalCount = 0;
        int majorCount = 0;
        int minorCount = 0;
        int innfoCount = 0;
        if (issuesMap.get("VULNERABILITY") != null && issuesMap.get("VULNERABILITY").containsKey("BLOCKER")) {
            blockerCount = blockerCount + issuesMap.get("VULNERABILITY").get("BLOCKER");
        }
        if (issuesMap.get("BUGS") != null && issuesMap.get("BUGS").containsKey("BLOCKER")) {
            blockerCount = blockerCount + issuesMap.get("BUGS").get("BLOCKER");
        }
        if (issuesMap.get("CODE_SMELL") != null && issuesMap.get("CODE_SMELL").containsKey("BLOCKER")) {
            blockerCount = blockerCount + issuesMap.get("CODE_SMELL").get("BLOCKER");
        }
        if (issuesMap.get("FINDBUGS") != null && issuesMap.get("FINDBUGS").containsKey("BLOCKER")) {
            blockerCount = blockerCount + issuesMap.get("FINDBUGS").get("BLOCKER");
        }
        if (issuesMap.get("VULNERABILITY") != null && issuesMap.get("VULNERABILITY").containsKey("CRITICAL")) {
            criticalCount = criticalCount + issuesMap.get("VULNERABILITY").get("CRITICAL");
        }
        if (issuesMap.get("BUGS") != null && issuesMap.get("BUGS").containsKey("CRITICAL")) {
            criticalCount = criticalCount + issuesMap.get("BUGS").get("CRITICAL");
        }
        if (issuesMap.get("CODE_SMELL") != null && issuesMap.get("CODE_SMELL").containsKey("CRITICAL")) {
            criticalCount = criticalCount + issuesMap.get("CODE_SMELL").get("CRITICAL");
        }
        if (issuesMap.get("FINDBUGS") != null && issuesMap.get("FINDBUGS").containsKey("CRITICAL")) {
            blockerCount = blockerCount + issuesMap.get("FINDBUGS").get("CRITICAL");
        }
        if (issuesMap.get("VULNERABILITY") != null && issuesMap.get("VULNERABILITY").containsKey("MAJOR")) {
            majorCount = majorCount + issuesMap.get("VULNERABILITY").get("MAJOR");
        }
        if (issuesMap.get("BUGS") != null && issuesMap.get("BUGS").containsKey("MAJOR")) {
            majorCount = majorCount + issuesMap.get("BUGS").get("MAJOR");
        }
        if (issuesMap.get("CODE_SMELL") != null && issuesMap.get("CODE_SMELL").containsKey("MAJOR")) {
            majorCount = majorCount + issuesMap.get("CODE_SMELL").get("MAJOR");
        }
        if (issuesMap.get("FINDBUGS") != null && issuesMap.get("FINDBUGS").containsKey("MAJOR")) {
            blockerCount = blockerCount + issuesMap.get("FINDBUGS").get("MAJOR");
        }
        if (issuesMap.get("VULNERABILITY") != null && issuesMap.get("VULNERABILITY").containsKey("MINOR")) {
            minorCount = minorCount + issuesMap.get("VULNERABILITY").get("MINOR");
        }
        if (issuesMap.get("BUGS") != null && issuesMap.get("BUGS").containsKey("MINOR")) {
            minorCount = minorCount + issuesMap.get("BUGS").get("MINOR");
        }
        if (issuesMap.get("CODE_SMELL") != null && issuesMap.get("CODE_SMELL").containsKey("MINOR")) {
            minorCount = minorCount + issuesMap.get("CODE_SMELL").get("MINOR");
        }
        if (issuesMap.get("FINDBUGS") != null && issuesMap.get("FINDBUGS").containsKey("MINOR")) {
            blockerCount = blockerCount + issuesMap.get("FINDBUGS").get("MINOR");
        }
        if (issuesMap.get("VULNERABILITY") != null && issuesMap.get("VULNERABILITY").containsKey("INFO")) {
            innfoCount = innfoCount + issuesMap.get("VULNERABILITY").get("INFO");
        }
        if (issuesMap.get("BUGS") != null && issuesMap.get("BUGS").containsKey("INFO")) {
            innfoCount = innfoCount + issuesMap.get("BUGS").get("INFO");
        }
        if (issuesMap.get("CODE_SMELL") != null && issuesMap.get("CODE_SMELL").containsKey("INFO")) {
            innfoCount = innfoCount + issuesMap.get("CODE_SMELL").get("INFO");
        }
        if (issuesMap.get("FINDBUGS") != null && issuesMap.get("FINDBUGS").containsKey("INFO")) {
            blockerCount = blockerCount + issuesMap.get("FINDBUGS").get("INFO");
        }
        dataSet.setValue("BLOCKER", blockerCount);
        dataSet.setValue("CRITICAL", criticalCount);
        dataSet.setValue("MAJOR", majorCount);
        dataSet.setValue("MINOR", minorCount);
        dataSet.setValue("INFO", innfoCount);

        JFreeChart chart = ChartFactory.createPieChart(
                "Most Prevalent Issues by Category", dataSet, true, false, false);
        chart.setBackgroundPaint(new Color(255, 255, 255));
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(null);
        plot.setNoDataMessage("No data available");
        plot.setCircular(true);
        plot.setDrawingSupplier(new ChartDrawingSupplier());
        plot.setLabelGap(0.02);
        TextTitle title = new TextTitle("Most Prevalent Issues by Category");
        title.setFont(new java.awt.Font("SansSerif", 0, 10));
        chart.setTitle(title);
        chart.setPadding(new RectangleInsets(0F, 0F, 200F, 0F));
        return chart;
    }
}
