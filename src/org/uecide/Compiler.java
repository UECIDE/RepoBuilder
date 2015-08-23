package org.uecide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;
import java.net.*;

class Compiler extends Package {
    public Compiler(File r, String name) {
        super(r,name);
    }

    public String getBlock() {
        StringBuilder sb = new StringBuilder();

        sb.append("Package: " + packagename + "\n");
        sb.append("Section: compilers\n");
        sb.append("Architecture: any\n");

        sb.append(getEntry("XBSC-Group"));
        sb.append(getEntry("XBSC-Subroup"));
        sb.append(getEntry("XBSC-Image"));
        sb.append(getEntry("Depends"));
        sb.append(getEntry("Recommends"));
        sb.append(getEntry("Homepage"));
        sb.append(getEntry("Description"));

        return sb.toString();
    }
}
