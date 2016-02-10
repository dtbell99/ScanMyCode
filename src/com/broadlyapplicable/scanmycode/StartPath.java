package com.broadlyapplicable.scanmycode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author dbell
 */
public class StartPath {
 
    private final Map<String, Integer> linesMap;
    private final Map<String, Integer> filesMap;
    private final String pathName;
    
    public StartPath(String pathName) {
        this.pathName = pathName;
        linesMap = new LinkedHashMap<>();
        filesMap = new LinkedHashMap<>();
    }

    public Map<String, Integer> getLinesMap() {
        return linesMap;
    }

    public Map<String, Integer> getFilesMap() {
        return filesMap;
    }

    public String getPathName() {
        return pathName;
    }
    
    public List<Extension> getExtensionList() {
        List<Extension> extensionList = new ArrayList<Extension>();
        for (String ext : filesMap.keySet()) {
            int files = filesMap.get(ext);
            int lines = linesMap.get(ext);
            Extension extension = new Extension(ext, files, lines);
            extensionList.add(extension);
        }
        Collections.sort(extensionList);
        return extensionList;
    }
    
    
    
}
