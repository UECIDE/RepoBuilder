package org.uecide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;
import java.net.*;

class Core extends Package {
    JTextField recommendBox;

    public Core(File r, String name) {
        super(r, name);
    }

    public String getBlock() {
        StringBuilder sb = new StringBuilder();

        sb.append("Package: " + packagename + "\n");
        sb.append("Section: cores\n");
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


        addCodename(p, c, "Core Codename:");
        addDescriptionPair(p, c, "Core Name:");

        addEntry(p, c, "Group", "XBSC-Group");
        addEntry(p, c, "Subgroup", "XBSC-Subgroup");
        addEntry(p, c, "Depends", "Depends");
        recommendBox = addEntry(p, c, "Recommends", "Recommends");
        c.gridx = 1;
        JButton addLibs = new JButton("Add all libraries");
        addLibs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doAddAllLibs();
            }
        });
        p.add(addLibs, c);
        c.gridx = 0;
        c.gridy++;

        addFileTable(p, c);

    }

    public void doAddAllLibs() {
        String liblist = "";
        for (Package p : RepoBuilder.repo.getPackages()) {
            if (p instanceof Library) {
                if (!liblist.equals("")) {
                    liblist += ", ";
                }
                liblist += p.toString();
            }
        }
        set("Recommends", liblist);
        recommendBox.setText(liblist);
        
    }

}
