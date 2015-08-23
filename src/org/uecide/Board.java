package org.uecide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;
import java.net.*;

class Board extends Package {

    public Board(File r, String name) {
        super(r, name);
    }

    public String getBlock() {
        StringBuilder sb = new StringBuilder();

        sb.append("Package: " + packagename + "\n");
        sb.append("Section: boards\n");
        sb.append("Architecture: all\n");

        sb.append(getEntry("XBSC-Group"));
        sb.append(getEntry("XBSC-Subroup"));
        sb.append(getEntry("XBSC-Image"));
        sb.append(getEntry("Depends"));
        sb.append(getEntry("Recommends"));
        sb.append(getEntry("Homepage"));
        sb.append(getEntry("Description"));

        return sb.toString();
    }

    public void populatePanel(final JPanel p) {
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;


        addCodename(p, c, "Board Codename:");
        addDescriptionPair(p, c, "Board Name:");

        addEntry(p, c, "Group", "XBSC-Group");
        addEntry(p, c, "Subgroup", "XBSC-Subgroup");
        addEntry(p, c, "Depends", "Depends");
        addEntry(p, c, "Recommends", "Recommends");

        addFileTable(p, c);

    }
}
