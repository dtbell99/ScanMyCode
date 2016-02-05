package com.broadlyapplicable.scanmycode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author dbell
 */
public class FileRunner {

    private final Map<String, Integer> linesMap;
    private final Map<String, Integer> filesMap;
    private final Map<String, Integer> methodsMap;
    private static final String PUBLIC = "public";
    private static final String PRIVATE = "private";
    private static final String PROTECTED = "protected";
    private static final String FUNCTION = "function";
    private static final String FUNC = "func";

    private String startPath;
    private String reportName;
    private List<String> excludeList;
    
    private int totalFiles;
    private int totalLines;
    private int totalMethods;

    public FileRunner() {
        linesMap = new LinkedHashMap<>();
        filesMap = new LinkedHashMap<>();
        methodsMap = new LinkedHashMap<>();
        buildExcludeList();
    }

    public void run(String aStartPath, String aReportName) {
        this.startPath = aStartPath;
        this.reportName = aReportName;
        processDirectory(new File(aStartPath));
        generateOutput();
    }

    private void processDirectory(File dir) {
        if (!dir.isDirectory()) {
            processFile(dir);
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                processDirectory(file);
            } else {
                processFile(file);
            }
        }
    }

    private void processFile(File file) {
        String[] nameData = file.getName().split("\\.");
        String extension = nameData[nameData.length - 1];
        if (extension.length() > 15) {
            return;
        }
        if (excludeList.contains(extension.toLowerCase())) {
            return;
        }
        int cntr = 0;
        if (filesMap.containsKey(extension)) {
            cntr = filesMap.get(extension);
        }
        cntr++;
        filesMap.put(extension, cntr);
        processLines(extension, file);
    }

    private void processLines(String extension, File file) {
        int cntr = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String lne;
            while ((lne = br.readLine()) != null) {
                lne = lne.trim();
                if (lne != null && !lne.isEmpty()) {
                    cntr++;
                    methodCheck(extension, lne);
                }
            }
        } catch (IOException iox) {
            iox.printStackTrace();
            System.exit(-1);
        }
        addLinesToMap(extension, cntr);
    }

    private void methodCheck(String extension, String lne) {
        if (lne.contains(PRIVATE) || lne.contains(PUBLIC) || lne.contains(PROTECTED) || lne.contains(FUNCTION) || lne.contains(FUNC)) {
            int methods = 0;
            if (lne.endsWith("{")) {
                if (methodsMap.containsKey(extension)) {
                    methods = methodsMap.get(extension);
                }
                methods++;
                methodsMap.put(extension, methods);
            }
        }
    }

    private void addLinesToMap(String extension, int totalLines) {
        if (linesMap.containsKey(extension)) {
            totalLines = totalLines + linesMap.get(extension);
        }
        linesMap.put(extension, totalLines);
    }

    private void generateOutput() {
        List<Extension> extensionList = processExtensionMaps();
        generateCSV(extensionList);
        generateTotals(extensionList);
        generateHTML(extensionList);
    }
    
    private void generateTotals(List<Extension> extList) {
        for (Extension ext : extList) {
            totalFiles += ext.getFiles();
            totalLines += ext.getLines();
            totalMethods += ext.getMethods();
        }
    }

    private List<Extension> processExtensionMaps() {
        List<Extension> extensionList = new ArrayList<Extension>();
        for (String ext : filesMap.keySet()) {
            int files = filesMap.get(ext);
            int lines = linesMap.get(ext);
            int methods = 0;
            if (methodsMap.containsKey(ext)) {
                methods = methodsMap.get(ext);
            }
            Extension extension = new Extension(ext, files, lines, methods);
            extensionList.add(extension);
        }
        Collections.sort(extensionList);
        return extensionList;
    }

    private void generateCSV(List<Extension> extensionList) {
        StringBuilder csv = new StringBuilder();
        csv.append("Extension,Files,Lines,Methods\n");
        for (Extension ext : extensionList) {
            String lne = ext.getName() + "," + ext.getFiles() + "," + ext.getLines() + "," + ext.getMethods();
            csv.append(lne).append("\n");
        }
        writeFile(csv, reportName + ".csv");
    }

    private void generateHTML(List<Extension> extensionList) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mma");
        String css = "<style type=\"text/css\">body {font-family:arial;} table {border:.1em solid cadetblue;} th {background-color:#eeeeee; padding:15px; border:.1em solid #eeeeee;} td {border:.1em solid #eeeeee; padding:15px;}</style>";
        StringBuilder html = new StringBuilder();
        html.append("<html><head>").append(css).append("</head><body>");
        html.append("<h2>").append(startPath).append("</h2>");
        String generated = "Generated on: " + sdf.format(new Date());
        html.append(generated);
        html.append("<br/><br/><table>");
        html.append("<tr><th>Extension</th><th>Files</th><th>Lines</th><th>Methods</th></tr>");
        for (Extension ext : extensionList) {
            html.append("<tr><td>").append(ext.getName()).append("</td><td align=\"right\">").append(ext.getFiles()).append("</td><td align=\"right\">").append(ext.getLines()).append("</td><td align=\"right\">").append(ext.getMethods()).append("</td></tr>");
        }
        html.append("<tr><td colspan=\"4\" style=\"background-color:#eeeeee;\"></td></tr>");
        html.append("<tr><td></td><td><b>Files</b></td><td><b>Lines</b></td><td><b>Methods</b></td></tr>");
        html.append("<tr><td><b>Totals</b></td><td align=\"right\">").append(totalFiles).append("</td><td align=\"right\">").append(totalLines).append("</td><td align=\"right\">").append(totalMethods).append("</td><tr>");
        html.append("</table></body></html>");
        writeFile(html, reportName + ".html");
    }

    private void writeFile(StringBuilder contents, String fileName) {
        File reportFile = new File(fileName);
        try (PrintWriter writer = new PrintWriter(fileName)) {
            reportFile.createNewFile();
            writer.println(contents.toString());
            System.out.println("File: " + fileName + " written");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Pass Starting Directory and Report Name Please");
            System.exit(-1);
        }
        String startPath = args[0];
        String reportName = args[1];
        FileRunner fr = new FileRunner();
        fr.run(startPath, reportName);
    }

    private void buildExcludeList() {
        excludeList = new ArrayList<>();
        excludeList.add("asdf");
        excludeList.add("class");
        excludeList.add("config");
        excludeList.add("csv");
        excludeList.add("docx");
        excludeList.add("ds_store");
        excludeList.add("gif");
        excludeList.add("gitignore");
        excludeList.add("head");
        excludeList.add("index");
        excludeList.add("jar");
        excludeList.add("jpg");
        excludeList.add("master");
        excludeList.add("mf");
        excludeList.add("png");
        excludeList.add("rtf");
        excludeList.add("txt");
        excludeList.add("war");
        excludeList.add("xslt");
        excludeList.add("zip");
    }
}
