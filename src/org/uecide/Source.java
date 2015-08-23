package org.uecide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;
import java.net.*;

class Source extends Package {
    public Source(File r, String name) {
        super(r, name);

        set("Priority", "extra");
        set("Build-Depends", "debhelper (>= 8.0.0)");
        set("Standards-Version", "3.9.4");
    }

    public String getBlock() {
        StringBuilder sb = new StringBuilder();

        sb.append("Source: " + packagename + "\n");

        sb.append(getEntry("Section"));
        sb.append(getEntry("XBSC-Family"));
        sb.append(getEntry("Priority"));
        sb.append(getEntry("Maintainer"));
        sb.append(getEntry("XBSC-TieCore"));
        sb.append(getEntry("Build-Depends"));
        sb.append(getEntry("Standards-Version"));
        sb.append(getEntry("Homepage"));

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


        addCodename(p, c, "Repository Codename:");
        addOption(p, c, "Family", "XBSC-Family", RepoBuilder.families);
        addEntry(p, c, "Main Section", "Section");
        addEntry(p, c, "Tied to core", "XBSC-TieCore");
    }

}
