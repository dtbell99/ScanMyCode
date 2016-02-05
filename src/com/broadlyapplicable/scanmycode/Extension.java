package com.broadlyapplicable.scanmycode;

/**
 *
 * @author dbell
 */
public class Extension implements Comparable{

    @Override
    public int compareTo(Object o) {
        Extension e = (Extension) o;
        if (lines == e.getLines()) { return 0; }
        return (e.getLines() > lines) ? 1 : -1;
    }
    
    private final String name;
    private final int files;
    private final int lines;
    private final int methods;
    
    public Extension(String n, int f, int l, int m) {
        this.name = n;
        this.files = f;
        this.lines = l;
        this.methods = m;
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

    public int getMethods() {
        return methods;
    }

}
