package com.broadlyapplicable.scanmycode;

/**
 *
 * @author dbell
 */
public class Extension implements Comparable {

    @Override
    public int compareTo(Object o) {
        Extension e = (Extension) o;
        if (lines == e.getLines()) {
            return 0;
        }
        return (e.getLines() > lines) ? 1 : -1;
    }

    private final String name;
    private int files;
    private int lines;

    public Extension(String n, int f, int l) {
        this.name = n;
        this.files = f;
        this.lines = l;
    }
    
    public void addFiles(int n) {
        files+=n;
    }
    
    public void addLines(int n) {
        lines+=n;
    }
    
    

    public String getName() {
        return name;
    }

    public int getFiles() {
        return files;
    }

    public int getLines() {
        return lines;
    }

}
