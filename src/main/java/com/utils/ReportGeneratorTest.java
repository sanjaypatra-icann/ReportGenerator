package com.utils;

import com.dto.Issues;
import com.google.gson.Gson;
import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleInsets;
import org.sonar.wsclient.Host;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.connectors.HttpClient4Connector;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReportGeneratorTest {

    public static void main(String args[]) {
        int countbug;
         String projectName = "FellowShipPOCDemo";///args[0];
        //String projectName = "FileApplicationUsingMavenTest";//args[0];
        Issues issues = null;
        Issues issuesTemp = null;
        Resource resource=null;
        int pageNumber = 1;
        HttpGet httpGet = new HttpGet("http://localhost:9000/api/issues/search?componentKeys=" + projectName + "&ps=500");

        try {

            Sonar sonar= new Sonar(new HttpClient4Connector(new Host("http://localhost:9000")));

            resource=sonar.find(ResourceQuery.createForMetrics(projectName,"coverage","lines","tests"));
            resource.getMeasure("coverage");

            System.out.println("resource.getMeasure coverage ========"+resource.getMeasure("coverage").getFormattedValue());

            System.out.println("resource.getMeasure lines ========"+resource.getMeasure("lines"));

            System.out.println("resource.getMeasure Unit Tests ========"+resource.getMeasure("tests"));



            issuesTemp = getIssues(httpGet);
            issues = issuesTemp;
            System.out.println("issuesTemp.getIssue().size() :::::::::::: "+issuesTemp.getIssue().size());
            while (issuesTemp!=null && issuesTemp.getIssue()!=null && issuesTemp.getIssue().size() != 0 ) {
                pageNumber =pageNumber + 1;

                System.out.println("projectName :::::::::::: "+projectName);
                System.out.println("pageNumber :::::::::::: "+pageNumber);
                //System.out.println("issuesTemp.getIssue() :::::::::::: "+issuesTemp.getIssue());
                ;

                issuesTemp = getIssues(new HttpGet("http://localhost:9000/api/issues/search?componentKeys=" + projectName + "&ps=500&p=" + pageNumber));

                System.out.println("issuesTemp :::::::::::: "+issuesTemp);
                if(issuesTemp!=null && issuesTemp.getIssue()!=null) {
                    issues.getIssue().addAll(issuesTemp.getIssue());
                }
            }

                createExcel(issues, projectName);
                createPdf(issues, projectName);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static Issues getIssues(HttpGet httpGet) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        CloseableHttpResponse response = httpClient.execute(httpGet);

        System.out.println("response.response.getEntity() :::::::::::"+response);

        System.out.println("response.getStatusLine() :::::::::::"+response.headerIterator().next());
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        Gson gson = new Gson();
        Issues issues = gson.fromJson(json, Issues.class);
        httpClient.close();
        return issues;
    }

    private static void createExcel(Issues issues, String projectName) {
// TODO Auto-generated method stub

        try {
            DateFormat dt = new SimpleDateFormat("dd-MM-yyyy");
            String date = dt.format(new Date()).toString();
           // String filename = System.getenv("WORKSPACE") + "\\" + projectName + "-" + date + ".xls";
            String filename = "D:\\report\\" + projectName + "-" + date + ".xls";
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("FirstSheet");

            HSSFRow rowhead = sheet.createRow((short) 0);
            rowhead.createCell(0).setCellValue("Type");
            rowhead.createCell(1).setCellValue("Status");
            rowhead.createCell(2).setCellValue("Creation Date");
            rowhead.createCell(3).setCellValue("Update Date");
            rowhead.createCell(4).setCellValue("Severity");
            rowhead.createCell(5).setCellValue("Component");
            rowhead.createCell(6).setCellValue("Message");
            rowhead.createCell(7).setCellValue("Profile");

            for (int i = 0; i < issues.getIssue().size(); i++) {
                HSSFRow row = sheet.createRow((short) i + 1);
                row.createCell(0).setCellValue(issues.getIssue().get(i).getType());
                row.createCell(1).setCellValue(issues.getIssue().get(i).getStatus());
                row.createCell(2).setCellValue(
                        String.valueOf(issues.getIssue().get(i).getCreationDate()));
                row.createCell(3).setCellValue(
                        String.valueOf(issues.getIssue().get(i).getUpdateDate()));
                row.createCell(4).setCellValue(issues.getIssue().get(i).getSeverity());
                row.createCell(5).setCellValue(issues.getIssue().get(i).getComponent());
                row.createCell(6).setCellValue(issues.getIssue().get(i).getMessage());
                row.createCell(7).setCellValue(issues.getIssue().get(i).getRule().substring(0, issues.getIssue().get(i).getRule().lastIndexOf(":")));


            }

            FileOutputStream fileOut = new FileOutputStream(filename);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            System.out.println("Your excel file has been generated!");

        } catch (Exception ex) {
            System.out.println(ex);

        }
    }

    private static void createPdf(Issues issues, String projectName)
            throws DocumentException, IOException, ParseException {

        Map<String, Map<String, Integer>> issuesMap = segData(issues);
        Sonar sonar= new Sonar(new HttpClient4Connector(new Host("http://localhost:9000")));
        Resource resource=sonar.find(ResourceQuery.createForMetrics(projectName,"coverage","lines","violation","tests","new_coverage"));
        resource.getMeasure("coverage");
        Document document = new Document();
        DateFormat dt = new SimpleDateFormat("dd-MM-yyyy");
        String date = dt.format(new Date()).toString();
       // PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(System.getenv("WORKSPACE") + "\\" + projectName + "-" + date + ".pdf"));
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
        preface.add(new Paragraph("Company              :     ICANN ", FontFactory.getFont(FontFactory.TIMES_ROMAN, 10)));
        preface.add(new Paragraph("Application          :     " + projectName, FontFactory.getFont(FontFactory.TIMES_ROMAN, 10)));
        preface.add(new Paragraph("Scan Date            :     " + date, FontFactory.getFont(FontFactory.TIMES_ROMAN, 10)));
        preface.add(new Paragraph("Code Coverage   :     " + resource.getMeasure("coverage").getFormattedValue(), FontFactory.getFont(FontFactory.TIMES_ROMAN, 10)));
        preface.add(new Paragraph("Unit Tests           :     " + resource.getMeasure("tests").getFormattedValue(), FontFactory.getFont(FontFactory.TIMES_ROMAN, 10)));
        preface.add(new Paragraph(" "));
        document.add(preface);

       /* document.add(createTable("VULNERABILITY", issuesMap.get("VULNERABILITY")));
        document.add(createTable("FINDBUG", issuesMap.get("FINDBUGS_VULNERABILITY")));
        document.add(createTable("BUGS", issuesMap.get("BUGS")));
        document.add(createTable("FINDBUG", issuesMap.get("FINDBUGS_BUG")));
        document.add(createTable("CODE_SMELL", issuesMap.get("CODE_SMELL")));
        document.add(createTable("FINDBUG", issuesMap.get("FINDBUGS_CODE_SMELL")));*/
        System.out.println("issuesMap.get(\"VULNERABILITY\") :::::::::   " +issuesMap.get("VULNERABILITY"));
        System.out.println("issuesMap.get(\"FINDBUGS_VULNERABILITY\"):::::::::::::  "+issuesMap.get("FINDBUGS_VULNERABILITY"));
        document.add(createTable2("VULNERABILITY", issuesMap.get("VULNERABILITY"),issuesMap.get("FINDBUGS_VULNERABILITY")));
       // document.add(createTable2("FINDBUGV", issuesMap.get("FINDBUGS_VULNERABILITY")));
        document.add(createTable2("BUGS", issuesMap.get("BUGS"),issuesMap.get("FINDBUGS_BUG")));
       // document.add(createTable2("FINDBUG", issuesMap.get("FINDBUGS_BUG")));
        document.add(createTable2("CODE_SMELL", issuesMap.get("CODE_SMELL"),issuesMap.get("FINDBUGS_CODE_SMELL")));
       // document.add(createTable2("FINDBUG", issuesMap.get("FINDBUGS_CODE_SMELL")));

        JFreeChart chart = generatePieChart(issuesMap);
        PdfContentByte contentByte = writer.getDirectContent();
        PdfTemplate template = contentByte.createTemplate(300, 350);
        Graphics2D graphics2d = template.createGraphics(300, 270,
                new DefaultFontMapper());
        Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, 300, 350);

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
    }



    private static Map<String, Map<String, Integer>> segData(Issues issues) {

        Map<String, Map<String, Integer>> issuesMap = new HashMap<String, Map<String, Integer>>();


        ArrayList<String> listfindbugsibBug= new ArrayList<String>();
        ArrayList<String> listfindbugsibCodeSmell= new ArrayList<String>();
        ArrayList<String> listfindbugsibVulnerability= new ArrayList<String>();
        ArrayList<String> listSeverity= new ArrayList<String>();
        ArrayList<String> listSeverityCodeSmell= new ArrayList<String>();
        ArrayList<String> listSeverityVulnerability= new ArrayList<String>();


        int findbugSeverityMajor=0;
        int findbugSeverityMinor=0;
        int findbugSeverityInfo=0;
        int findbugSeverityCritical=0;
        int findbugSeverityBlocker=0;

        int findbugSeverityMajorCodeSmell=0;
        int findbugSeverityMinorCodeSmell=0;
        int findbugSeverityInfoCodeSmell=0;
        int findbugSeverityCriticalCodeSmell=0;
        int findbugSeverityBlockerCodeSmell=0;

        int findbugSeverityMajorVulnerability=0;
        int findbugSeverityMinorVulnerability=0;
        int findbugSeverityInfoVulnerability=0;
        int findbugSeverityCriticalVulnerability=0;
        int findbugSeverityBlockerVulnerability=0;



        for (int i = 0; i < issues.getIssue().size(); i++) {
            Issues.Issue issue = issues.getIssue().get(i);
            System.out.println("==================================");
            System.out.println("Type :::::: "+issues.getIssue().get(i).getType());
            System.out.println("Project Name :::::: "+issues.getIssue().get(i).getProject());
            System.out.println("Rules :::::: "+issues.getIssue().get(i).getRule());
            System.out.println("Status :::::: "+issues.getIssue().get(i).getStatus());
            System.out.println("==================================");




            if (!"CLOSED".equalsIgnoreCase(issue.getStatus())) { ///Excluding Closed issues
                if ("Bug".equalsIgnoreCase(issue.getType()) && issues.getIssue().get(i).getRule().contains("findbugs") == false) {

                    if (issuesMap.containsKey("BUGS")) {
                        System.out.println("issuesMap.get(\"BUGS\") ::::::::: "+issuesMap.get("BUGS"));
                        Map<String, Integer> bugsMap = issuesMap.get("BUGS");

                        if (bugsMap.containsKey(issue.getSeverity())) {
                            int count = bugsMap.get(issue.getSeverity());
                            count = count + 1;
                            bugsMap.put(issue.getSeverity(), count);
                            issuesMap.put("BUGS", bugsMap);
                        } else {
                            bugsMap.put(issue.getSeverity(), 1);
                            issuesMap.put("BUGS", bugsMap);
                        }


                    } else {
                        Map<String, Integer> bugsMap = new HashMap<String, Integer>();
                        bugsMap.put(issue.getSeverity(), 1);
                        issuesMap.put("BUGS", bugsMap);

                    }


                } else if ("VULNERABILITY".equalsIgnoreCase(issue.getType())&& issues.getIssue().get(i).getRule().contains("findbugs") == false) {
                    if (issuesMap.containsKey("VULNERABILITY")) {
                        Map<String, Integer> vulnerabilityMap = issuesMap.get("VULNERABILITY");
                        if (vulnerabilityMap.containsKey(issue.getSeverity())) {
                            int count = vulnerabilityMap.get(issue.getSeverity());
                            count = count + 1;
                            vulnerabilityMap.put(issue.getSeverity(), count);
                            issuesMap.put("VULNERABILITY", vulnerabilityMap);
                        } else {
                            vulnerabilityMap.put(issue.getSeverity(), 1);
                            issuesMap.put("VULNERABILITY", vulnerabilityMap);
                        }

                    } else {
                        Map<String, Integer> vulnerabilityMap = new HashMap<String, Integer>();
                        vulnerabilityMap.put(issue.getSeverity(), 1);
                        issuesMap.put("VULNERABILITY", vulnerabilityMap);

                    }
                } else if ("CODE_SMELL".equalsIgnoreCase(issue.getType())&& issues.getIssue().get(i).getRule().contains("findbugs") == false) {

                    if (issuesMap.containsKey("CODE_SMELL")) {
                        Map<String, Integer> codeSmellMap = issuesMap.get("CODE_SMELL");
                        if (codeSmellMap.containsKey(issue.getSeverity())) {
                            int count = codeSmellMap.get(issue.getSeverity());
                            count = count + 1;
                            codeSmellMap.put(issue.getSeverity(), count);
                            issuesMap.put("CODE_SMELL", codeSmellMap);
                        } else {
                            codeSmellMap.put(issue.getSeverity(), 1);
                            issuesMap.put("CODE_SMELL", codeSmellMap);

                        }

                    } else {
                        Map<String, Integer> codeSmellMap = new HashMap<String, Integer>();
                        codeSmellMap.put(issue.getSeverity(), 1);
                        issuesMap.put("CODE_SMELL", codeSmellMap);


                    }

                }

                if ("Bug".equalsIgnoreCase(issues.getIssue().get(i).getType()) && issues.getIssue().get(i).getRule().contains("findbugs") == true) {
                    listfindbugsibBug.add(issues.getIssue().get(i).getRule().substring(0, issues.getIssue().get(i).getRule().lastIndexOf(":")));
                    System.out.println("getSeverity() in Bugs ::: " + issues.getIssue().get(i).getSeverity());
                    Map<String, Integer> bugFindbugsMap = new HashMap<String, Integer>();
                    listSeverity.add(issues.getIssue().get(i).getSeverity());

                    findbugSeverityMajor = Collections.frequency(listSeverity, "MAJOR");
                    findbugSeverityMinor = Collections.frequency(listSeverity, "MINOR");
                    findbugSeverityInfo = Collections.frequency(listSeverity, "INFO");
                    findbugSeverityCritical = Collections.frequency(listSeverity, "CRITICAL");
                    findbugSeverityBlocker = Collections.frequency(listSeverity, "BLOCKER");

                    bugFindbugsMap.put("MAJOR",findbugSeverityMajor);
                    bugFindbugsMap.put("MINOR",findbugSeverityMinor);
                    bugFindbugsMap.put("INFO",findbugSeverityInfo);
                    bugFindbugsMap.put("CRITICAL",findbugSeverityCritical);
                    bugFindbugsMap.put("BLOCKER",findbugSeverityBlocker);
                    issuesMap.put("FINDBUGS_BUG", bugFindbugsMap);
                }
                if ("CODE_SMELL".equalsIgnoreCase(issues.getIssue().get(i).getType()) && issues.getIssue().get(i).getRule().contains("findbugs") == true) {
                    listfindbugsibCodeSmell.add(issues.getIssue().get(i).getRule().substring(0, issues.getIssue().get(i).getRule().lastIndexOf(":")));
                    System.out.println("getSeverity() in  CODE_SMELL :: " + issues.getIssue().get(i).getSeverity());
                    Map<String, Integer> codeSmellFindbugsMap = new HashMap<String, Integer>();
                    listSeverityCodeSmell.add(issues.getIssue().get(i).getSeverity());
                    findbugSeverityMajorCodeSmell = Collections.frequency(listSeverityCodeSmell, "MAJOR");
                    findbugSeverityMinorCodeSmell = Collections.frequency(listSeverityCodeSmell, "MINOR");
                    findbugSeverityInfoCodeSmell = Collections.frequency(listSeverityCodeSmell, "INFO");
                    findbugSeverityCriticalCodeSmell = Collections.frequency(listSeverityCodeSmell, "CRITICAL");
                    findbugSeverityBlockerCodeSmell = Collections.frequency(listSeverityCodeSmell, "BLOCKER");

                    codeSmellFindbugsMap.put("MAJOR",findbugSeverityMajorCodeSmell);
                    codeSmellFindbugsMap.put("MINOR",findbugSeverityMinorCodeSmell);
                    codeSmellFindbugsMap.put("INFO",findbugSeverityInfoCodeSmell);
                    codeSmellFindbugsMap.put("CRITICAL",findbugSeverityCriticalCodeSmell);
                    codeSmellFindbugsMap.put("BLOCKER",findbugSeverityBlockerCodeSmell);
                    issuesMap.put("FINDBUGS_CODE_SMELL", codeSmellFindbugsMap);

                }
                if ("VULNERABILITY".equalsIgnoreCase(issues.getIssue().get(i).getType()) && issues.getIssue().get(i).getRule().contains("findbugs") == true) {
                    listfindbugsibVulnerability.add(issues.getIssue().get(i).getRule().substring(0, issues.getIssue().get(i).getRule().lastIndexOf(":")));
                    System.out.println("getSeverity() in VULNERABILITY :::: " + issues.getIssue().get(i).getSeverity());
                    Map<String, Integer> vulnerabilityFindbugsMap = new HashMap<String, Integer>();
                    listSeverityVulnerability.add(issues.getIssue().get(i).getSeverity());
                    findbugSeverityMajorVulnerability = Collections.frequency(listSeverityVulnerability, "MAJOR");
                    findbugSeverityMinorVulnerability = Collections.frequency(listSeverityVulnerability, "MINOR");
                    findbugSeverityInfoVulnerability = Collections.frequency(listSeverityVulnerability, "INFO");
                    findbugSeverityCriticalVulnerability = Collections.frequency(listSeverityVulnerability, "CRITICAL");
                    findbugSeverityBlockerVulnerability = Collections.frequency(listSeverityVulnerability, "BLOCKER");
                    vulnerabilityFindbugsMap.put("MAJOR", findbugSeverityMajorVulnerability);
                    vulnerabilityFindbugsMap.put("MINOR", findbugSeverityMinorVulnerability);
                    vulnerabilityFindbugsMap.put("INFO", findbugSeverityInfoVulnerability);
                    vulnerabilityFindbugsMap.put("CRITICAL", findbugSeverityCriticalVulnerability);
                    vulnerabilityFindbugsMap.put("BLOCKER", findbugSeverityBlockerVulnerability);


                    issuesMap.put("FINDBUGS_VULNERABILITY", vulnerabilityFindbugsMap);

                }


            }


        }

        System.out.println("count of findbugs in Bug ::::::::"+listfindbugsibBug.size()+ "  Major :: "+findbugSeverityMajor+ " Minor :: "+findbugSeverityMinor+" INFO "+findbugSeverityInfo);
        System.out.println("count of findbugs in CodeSmell ::::::::"+listfindbugsibCodeSmell.size()+ "  Major :: "+findbugSeverityMajorCodeSmell+ " Minor :: "+findbugSeverityMinorCodeSmell+" INFO "+findbugSeverityInfoCodeSmell);
        System.out.println("count of findbugs in Vulnerability ::::::::"+listfindbugsibVulnerability.size()+ "  Major :: "+findbugSeverityMajorVulnerability+ " Minor :: "+findbugSeverityMinorVulnerability+" INFO "+findbugSeverityInfoVulnerability);
        /*System.out.println("Count of  findbug :::: " + Collections.frequency(list, "findbugs"));
        System.out.println("Size of the List is :::::: "+list.size());
        System.out.println("List is :::::: "+list);*/
        return issuesMap;
    }
    public static PdfPTable createTable2(String type, Map<String, Integer> data,Map<String, Integer> findbugdata) throws DocumentException, IOException {

        PdfPTable table = new PdfPTable(6);
        table.setWidths(new int[]{2,2, 2, 2,2,3});
        PdfPCell cell;
        if(type=="CODE_SMELL"){
            type="CODE SMELL";
        }
        if(type=="VULNERABILITY"){
            type="VULNERABILITIES";
        }
        cell = new PdfPCell(new Phrase(type));
        cell.setPaddingLeft(170.0F);


        if (type == "BUGS") {
            cell.setBackgroundColor(new BaseColor(255, 165, 0));

        } else if (type == "VULNERABILITIES") {
            cell.setBackgroundColor(new BaseColor(255, 99, 71));
        } else if (type == "CODE SMELL") {
            cell.setBackgroundColor(new BaseColor(255, 215, 0));
        }
        cell.setColspan(6);
        table.addCell(cell);
        table.addCell(getPhraseHeader("BLOCKER"));
        table.addCell(getPhraseHeader("CRITICAL"));
        table.addCell(getPhraseHeader("MAJOR"));
        table.addCell(getPhraseHeader("MINOR"));
        table.addCell(getPhraseHeader("INFO"));
        table.addCell(getPhraseHeader("CODE ANALYSER"));

        if(data!=null) {
            if (data.get("BLOCKER") != null) {
                table.addCell(data.get("BLOCKER").toString());
            } else {
                table.addCell("0");
            }
            if (data.get("CRITICAL") != null) {
                table.addCell(data.get("CRITICAL").toString());

            } else {
                table.addCell("0");
            }
            if (data.get("MAJOR") != null) {
                table.addCell(data.get("MAJOR").toString());

            } else {
                table.addCell("0");
            }
            if (data.get("MINOR") != null) {
                table.addCell(data.get("MINOR").toString());
            } else {
                table.addCell("0");
            }
            if (data.get("INFO") != null) {
                table.addCell(data.get("INFO").toString());
            } else {
                table.addCell("0");
            }
        }else {
            table.addCell("0");
        }
        table.addCell(getPhrase("sonarQube"));
        if (findbugdata!=null) {
            if (findbugdata.get("BLOCKER") != null) {
                table.addCell(findbugdata.get("BLOCKER").toString());
            }
            if (findbugdata.get("CRITICAL") != null) {
                table.addCell(findbugdata.get("CRITICAL").toString());
            }
            if (findbugdata.get("MAJOR") != null) {
                table.addCell(findbugdata.get("MAJOR").toString());
            }
            if (findbugdata.get("MINOR") != null) {
                table.addCell(findbugdata.get("MINOR").toString());
            } if (findbugdata.get("INFO") != null) {
                table.addCell(findbugdata.get("INFO").toString());
            }
        }else {
            table.addCell("0");
        }

        table.addCell(getPhrase("findBugs"));
        return table;
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

            // now we add a cell with rowspan 2\\\\\\
          //  System.out.println("BLOCKER :::::::::::::  "+data.get("BLOCKER"));

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

    private static Phrase getPhraseHeader(String text) throws DocumentException, IOException {
        FontSelector selector1 = new FontSelector();
        Font f1 = FontFactory.getFont(FontFactory.COURIER_BOLD, 10);
        f1.setColor(BaseColor.BLACK);
        selector1.addFont(f1);
        Phrase phrase = selector1.process(text);
        return phrase;
    }
    private static Phrase getPhrase(String text) throws DocumentException, IOException {
        FontSelector selector1 = new FontSelector();
        Font f1 = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9);
        f1.setColor(BaseColor.BLUE);
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
        if (issuesMap.get("VULNERABILITY") != null && issuesMap.get("VULNERABILITY").containsKey("CRITICAL")) {
            criticalCount = criticalCount + issuesMap.get("VULNERABILITY").get("CRITICAL");
        }
        if (issuesMap.get("BUGS") != null && issuesMap.get("BUGS").containsKey("CRITICAL")) {
            criticalCount = criticalCount + issuesMap.get("BUGS").get("CRITICAL");
        }
        if (issuesMap.get("CODE_SMELL") != null && issuesMap.get("CODE_SMELL").containsKey("CRITICAL")) {
            criticalCount = criticalCount + issuesMap.get("CODE_SMELL").get("CRITICAL");
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
        if (issuesMap.get("VULNERABILITY") != null && issuesMap.get("VULNERABILITY").containsKey("MINOR")) {
            minorCount = minorCount + issuesMap.get("VULNERABILITY").get("MINOR");
        }
        if (issuesMap.get("BUGS") != null && issuesMap.get("BUGS").containsKey("MINOR")) {
            minorCount = minorCount + issuesMap.get("BUGS").get("MINOR");
        }
        if (issuesMap.get("CODE_SMELL") != null && issuesMap.get("CODE_SMELL").containsKey("MINOR")) {
            minorCount = minorCount + issuesMap.get("CODE_SMELL").get("MINOR");
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
        dataSet.setValue("BLOCKER", blockerCount);
        dataSet.setValue("CRITICAL", criticalCount);
        dataSet.setValue("MAJOR", majorCount);
        dataSet.setValue("MINOR", minorCount);
        dataSet.setValue("INFO", innfoCount);

        JFreeChart chart = ChartFactory.createPieChart(
                "Most Prevalent Issues by Category", dataSet, true, false, false);
        chart.setBackgroundPaint(new Color(255, 255, 255));
        chart.setBackgroundImageAlignment(100);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(null);
        plot.setNoDataMessage("No data available");
        plot.setCircular(true);
        plot.setDrawingSupplier(new ChartDrawingSupplier());
        plot.setLabelGap(0.02);
        TextTitle title = new TextTitle("Most Prevalent Issues by Category");
        title.setFont(new java.awt.Font("SansSerif", 0, 10));
        chart.setTitle(title);
        chart.setPadding(new RectangleInsets(50F, 50F, 100F, 50F));
        return chart;
    }
}

