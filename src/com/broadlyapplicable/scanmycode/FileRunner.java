package com.broadlyapplicable.scanmycode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author dbell
 */
public class FileRunner {

    private List<StartPath> startPathList;
    private Set<String> includeSet;
    private final Set<String> excludedExtensions;

    public FileRunner() {
        excludedExtensions = new HashSet<>();
        buildIncludeList();
    }

    public void run() {
        buildStartPathList();
        for (StartPath startPath : startPathList) {
            //System.out.println("run:"+startPath.getPathName());
            processDirectory(startPath, new File(startPath.getPathName()));
        }
        generateOutput();
    }

    private void processDirectory(StartPath startPath, File dir) {

        if (!dir.isDirectory()) {
            processFile(startPath, dir);
            return;
        }
        //System.out.println("ProcessDirectory:"+dir.getAbsolutePath());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                processDirectory(startPath, file);
            } else {
                processFile(startPath, file);
            }
        }
    }

    private void processFile(StartPath startPath, File file) {
        //System.out.println("ProcessFile:"+file.getAbsolutePath());
        String[] nameData = file.getName().split("\\.");
        String extension = nameData[nameData.length - 1].toLowerCase();
        if (extension.length() > 15) {
            return;
        }

        if (!includeSet.contains(extension)) {
            excludedExtensions.add(extension);
            return;
        }
        int cntr = 0;
        if (startPath.getFilesMap().containsKey(extension)) {
            cntr = startPath.getFilesMap().get(extension);
        }
        cntr++;
        startPath.getFilesMap().put(extension, cntr);
        processLines(startPath, extension, file);
    }

    private void processLines(StartPath startPath, String extension, File file) {
        int cntr = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String lne;
            while ((lne = br.readLine()) != null) {
                lne = lne.trim();
                if (lne != null && !lne.isEmpty()) {
                    cntr++;
                }
            }
        } catch (IOException iox) {
            iox.printStackTrace();
            System.exit(-1);
        }
        addLinesToMap(startPath, extension, cntr);
    }

    private void addLinesToMap(StartPath startPath, String extension, int totalLines) {
        if (startPath.getLinesMap().containsKey(extension)) {
            totalLines = totalLines + startPath.getLinesMap().get(extension);
        }
        startPath.getLinesMap().put(extension, totalLines);
    }

    private void generateOutput() {

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mma");
        String css = "<style type=\"text/css\">a {color:cadetblue;} body {padding:10px; font-family:arial;} table {border:.1em solid cadetblue;} th {background-color:#eeeeee; padding:15px; border:.1em solid #eeeeee;} td {border:.1em solid #eeeeee; padding:15px;}</style>";
        StringBuilder html = new StringBuilder();
        html.append("<script type=\"text/javascript\">");
        html.append("function showhide(id) {");
        html.append("document.getElementById(id).style.display = '';");
        html.append("}");
        html.append("</script>");
        html.append("<html><head>").append(css).append("</head><body>");
        html.append("<h2>Code Report</h2>");
        String generated = "Generated on: " + sdf.format(new Date());
        html.append(generated);
        for (StartPath startPath : startPathList) {
            updateHTMLTableWithExtensionData(html, startPath.getPathName(), startPath.getExtensionList());
        }

        List<Extension> totalExtensions = getTotalExtensions();
        updateHTMLTableWithExtensionData(html, "Total Projects: " + startPathList.size(), totalExtensions);

        html.append("<br/><br/><hr/><br/>Excluded Extensions: ").append(excludedExtensions.toString());
        html.append("</body></html>");
        writeFile(html, "CodeReport" + ".html");
    }

    private void updateHTMLTableWithExtensionData(StringBuilder html, String path, List<Extension> extentionList) {
        int totalLines = 0;
        int totalFiles = 0;
        html.append("<h3 id=\"t_").append(path).append("\"><a onclick=\"showhide('").append(path).append("')\" href=\"#t_").append(path).append("\">").append(path).append("</a></h3>");
        html.append("<table style=\"display:none;\" id=\"").append(path).append("\">");
        html.append("<tr><th>Extension</th><th>Files</th><th>Lines</th></tr>");
        for (Extension ext : extentionList) {
            totalLines += ext.getLines();
            totalFiles += ext.getFiles();
            html.append("<tr><td>").append(ext.getName()).append("</td><td align=\"right\">").append(ext.getFiles()).append("</td><td align=\"right\">").append(ext.getLines()).append("</td></tr>");
        }
        html.append("<tr><td colspan=\"4\" style=\"background-color:#eeeeee;\"></td></tr>");
        html.append("<tr><td></td><td><b>Files</b></td><td><b>Lines</b></td></tr>");
        html.append("<tr><td><b>Totals</b></td><td align=\"right\">").append(totalFiles).append("</td><td align=\"right\">").append(totalLines).append("</td><tr>");
        html.append("</table>");
    }

    private List<Extension> getTotalExtensions() {
        List<Extension> extensionList = new ArrayList<Extension>();
        for (StartPath startPath : startPathList) {
            for (Extension ext : startPath.getExtensionList()) {
                boolean extensionFound = false;
                for (Extension ext2 : extensionList) {
                    if (ext.getName().equals(ext2.getName())) {
                        ext2.addFiles(ext.getFiles());
                        ext2.addLines(ext.getLines());
                        extensionFound = true;
                        break;
                    }
                }
                if (!extensionFound) {
                    Extension e = new Extension(ext.getName(), ext.getFiles(), ext.getLines());
                    extensionList.add(e);
                }
            }
        }
        return extensionList;
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
        FileRunner fr = new FileRunner();
        fr.run();
    }

    private void buildStartPathList() {
        startPathList = new ArrayList<StartPath>();
        File extFile = new File("paths.txt");
        if (!extFile.exists()) {
            System.out.println("paths.txt missing. Please create a file with that name and comma separate any subdirectories you wish to be counted by ScanMyCode.");
            return;
        }
        try {
            String entireFileText = new Scanner(new File("paths.txt"))
                    .useDelimiter("\\n").next();
            if (entireFileText != null && !entireFileText.isEmpty()) {
                String[] dta = entireFileText.split("\\,");
                for (String path : dta) {
                    StartPath startPath = new StartPath(path);
                    startPathList.add(startPath);
                    System.out.println("pn: \"" + startPath.getPathName() + "\"");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void buildIncludeList() {
        includeSet = new HashSet<>();
        
        File extFile = new File("extensions.txt");
        if (!extFile.exists()) {
            System.out.println("extensions.txt missing. Please create a file with that name and comma separate any file extensions you wish to be counted by ScanMyCode.");
            return;
        }
        
        try {
            
            String entireFileText = new Scanner(new File("extensions.txt")).useDelimiter("\\n").next();
            if (entireFileText != null && !entireFileText.isEmpty()) {
                String[] extData = entireFileText.split("\\,");
                for (String ext : extData) {
                    includeSet.add(ext);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
