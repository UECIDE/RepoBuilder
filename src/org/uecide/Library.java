package org.uecide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;
import java.net.*;

class Library extends Package {
    public Library(File r, String name) {
        super(r, name);
    }

    public String getBlock() {
        StringBuilder sb = new StringBuilder();

        sb.append("Package: " + packagename + "\n");
        sb.append("Section: libraries\n");
        sb.append("Architecture: all\n");

        sb.append(getEntry("XBSC-Group"));
        sb.append(getEntry("XBSC-Subgroup"));
        sb.append(getEntry("XBSC-Image"));
        sb.append(getEntry("Depends"));
        sb.append(getEntry("Recommends"));
        sb.append(getEntry("Provides"));
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


        addCodename(p, c, "Library Codename:");
        addDescriptionPair(p, c, "Library Name:");

        Object[] groupKeys = RepoBuilder.libs.keySet().toArray();
        JLabel groupLab = new JLabel("Group:");
        final JComboBox groupBox = new JComboBox(groupKeys);
        groupBox.setSelectedItem(get("XBSC-Group"));
        c.gridx = 0;
        p.add(groupLab, c);
        c.gridx = 1;
        p.add(groupBox, c);
        c.gridx = 0;
        c.gridy++;

        String g = get("XBSC-Group");
        if (g == null) {
            Object[] obs = RepoBuilder.libs.keySet().toArray();
            g = (String)obs[0];
        }
        Object[] subgroupKeys;
        ArrayList<String>sgl = RepoBuilder.libs.get(g);
        if (sgl != null) {
            subgroupKeys = sgl.toArray();
        } else {
            subgroupKeys = new String[0];
        }

        JLabel subgroupLab = new JLabel("Subgroup:");
        final JComboBox subgroupBox = new JComboBox(subgroupKeys);
        subgroupBox.setSelectedItem(get("XBSC-Subgroup"));
        c.gridx = 0;
        p.add(subgroupLab, c);
        c.gridx = 1;
        p.add(subgroupBox, c);
        c.gridx = 0;
        c.gridy++;

        groupBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Library.this.set("XBSC-Group", (String)groupBox.getSelectedItem());
                Object[] subgroupKeys = RepoBuilder.libs.get(groupBox.getSelectedItem()).toArray();

                subgroupBox.removeAllItems();
                for (Object o : subgroupKeys) {
                    subgroupBox.addItem(o);
                }
                subgroupBox.setSelectedItem(get("XBSC-Subgroup"));
            }
        });

        subgroupBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Library.this.set("XBSC-Subgroup", (String)subgroupBox.getSelectedItem());
            }
        });

        addEntry(p, c, "Provides", "Provides");
        addEntry(p, c, "Depends", "Depends");
        addEntry(p, c, "Recommends", "Recommends");

        addFileTable(p, c);

    }

}
