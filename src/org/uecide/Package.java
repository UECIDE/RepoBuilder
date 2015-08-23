package org.uecide;

import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.net.*;


public class Package {
    HashMap<String, String>data = new HashMap<String,String>();
    HashMap<String, String>files = new HashMap<String,String>();

    String packagename;
    File root;

    File currentFolder;

    class FilesTableModel extends AbstractTableModel {
        public int getColumnCount() { return 2; }
        public int getRowCount() { return files.keySet().size(); }
        public Object getValueAt(int row, int col) {
            String[] keys = files.keySet().toArray(new String[0]);
            Arrays.sort(keys);
           
            if (col == 0) {
                return keys[row];
            }
            if (col == 1) {
                return files.get(keys[row]);
            }
            return "";
        }
        public String getColumnName(int col) {
            switch(col) {
                case 0: return "Source File or Folder";
                case 1: return "Destination Folder";
                default: return "";
            }
        }
        public boolean isCellEditable(int row, int col) {
            return col == 1;
        }

        public void setValueAt(Object val, int row, int col) {
            if (col != 1) {
                return;
            }
            String[] keys = files.keySet().toArray(new String[0]);
            Arrays.sort(keys);
            if (row >= keys.length) {
                return;
            }
            String key = keys[row];
            files.put(key, (String)val);
        }
    }

    FilesTableModel dataModel = new FilesTableModel();

    public Package(File rootFolder, String name) {
        root = rootFolder;
        currentFolder = root;
        packagename = name;
    }

    public void set(String key, String value) {
        data.put(key, value);
    }

    public String get(String key) {
        return data.get(key);
    }

    public String getEntry(String key) {
        if (get(key) == null) {
            return "";
        }
        if (get(key) == "") {
            return "";
        }
        String[] lines = get(key).split("\n");
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String line : lines) {
            if (first) {
                sb.append(key);
                sb.append(": ");
                sb.append(line.trim());
                sb.append("\n");
                first = false;
            } else {
                sb.append(" ");
                if (line.trim().equals("")) {
                    line = ".";
                }
                sb.append(line);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public String toString() {
        return packagename;
    }

    public String getBlock() {
        return "";
    }

    public void addFile(String src, String dest) {
        files.put(src, dest);
    }

    public String getFiles() {
        StringBuilder sb = new StringBuilder();
        String[] flist = files.keySet().toArray(new String[0]);
        Arrays.sort(flist);
        for (String f : flist) {
            sb.append(f);
            sb.append(" ");
            sb.append(files.get(f));
            sb.append("\n");
        }
        return sb.toString();
    }

    public void loadFiles(File root) {
        files = new HashMap<String, String>();
        File debian = new File(root, "debian");
        File install = new File(debian, packagename + ".install");
        if (install.exists()) {
            String data = RepoBuilder.getFileAsString(install);
            String[] lines = data.split("\n");
            for (String line : lines) {
                String[] bits = line.split(" ");
                if (bits.length == 2) {
                    String src = bits[0];
                    String dest = bits[1];
                    files.put(src, dest);
                }
            }
        }
    }

    public void populatePanel(JPanel p) {
        p.add(new JLabel("Not a valid object!"));
    }

    public void addOption(JPanel p, GridBagConstraints c, String label, final String entry, final Object[] options) {
        JLabel lab = new JLabel(label + ": ");
        final JComboBox data = new JComboBox(options);

        c.gridx = 0;
        c.weightx = 0.1;
        p.add(lab, c);
        c.gridx = 1;
        c.weightx = 0.9;
        p.add(data, c);
        c.gridx = 0;
        c.gridy++;

        data.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                set(entry, (String)data.getSelectedItem());
            }
        });

        data.setSelectedItem(get(entry));

    }

    public JTextField addEntry(JPanel p, GridBagConstraints c, String label, final String entry) {
        JLabel lab = new JLabel(label + ": ");
        final JTextField data = new JTextField(get(entry));

        c.gridx = 0;
        c.weightx = 0.1;
        p.add(lab, c);
        c.gridx = 1;
        c.weightx = 0.9;
        p.add(data, c);
        c.gridx = 0;
        c.gridy++;

        data.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                System.err.println(entry + "=" + data.getText());
                set(entry, data.getText());
            }

            public void keyTyped(KeyEvent e) {
            }
        });

        return data;
    }

    public void addFileToList(File f) {
        if (f.isDirectory()) {
            currentFolder = f;
        } else {
            currentFolder.getParentFile();
        }
        try {
            URI rootUri = root.toURI();
            URI fileUri = f.toURI();
            String path = rootUri.relativize(fileUri).getPath();

            String g = get("XBSC-Group");
            if (g == null) {
                g = "";
            }
            g = g.replace(" ", "");

            if (this instanceof Board) {
                files.put(path, "boards/" + g + "/" + packagename);
            } else if (this instanceof Core) {
                files.put(path, "cores/" + packagename);
            } else if (this instanceof Compiler) {
                files.put(path, "compiler/" + packagename);
            } else if (this instanceof Library) {
                String tie = RepoBuilder.repo.getSource().get("XBSC-TieCore");
                String prov = get("Provides");
                if (prov == null) {
                    prov = packagename;
                }
                prov = prov.replace("-UL-", "_");
                String[] seg = prov.split("\\.");
                String libname = packagename;
                if (seg.length > 0) {
                    libname = seg[0];
                }
                String dpath = "libraries/" + g + "/";
                if (tie != null) {
                    dpath += tie + "/";
                }
                dpath += libname;
                
                files.put(path, dpath);
            }
            dataModel.fireTableDataChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteFileFromList(int row) {
        Object[] keys = files.keySet().toArray();

        if (row < 0 || row >= files.size()) {
            return;
        }
        String src = (String)keys[row];
        files.remove(src);
        dataModel.fireTableDataChanged();
    }

    public void addFileTable(final JPanel p, GridBagConstraints c) {
        p.add(new JLabel("Files:"), c);

        final JTable filetab = new JTable(dataModel);
        dataModel.fireTableDataChanged();

        filetab.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        c.gridy++;
        c.gridwidth = 2;
        c.weighty = 0.8d;
        c.fill = GridBagConstraints.BOTH;
        JScrollPane sFiles = new JScrollPane(filetab);
        p.add(sFiles, c);

        c.fill = GridBagConstraints.HORIZONTAL;

        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());

        JButton addFile = new JButton("Add File");
        JButton addDir = new JButton("Add Directory");
        JButton delSelected = new JButton("Delete Selected");

        addFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(currentFolder);
                fc.setMultiSelectionEnabled(true);

                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                int rv = fc.showOpenDialog(p);

                if(rv == JFileChooser.APPROVE_OPTION) {
                    File[] files = fc.getSelectedFiles();
                    for (File f : files) {
                        addFileToList(f);
                    }
                }
            }
        });

        addDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(currentFolder);

                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int rv = fc.showOpenDialog(p);

                if(rv == JFileChooser.APPROVE_OPTION) {
                    addFileToList(fc.getSelectedFile());
                }
            }
        });

        delSelected.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int r = filetab.getSelectedRow();
                deleteFileFromList(r);
                if (r >= dataModel.getRowCount()) {
                    r = dataModel.getRowCount() - 1;
                }
                try {
                    filetab.setRowSelectionInterval(r, r);
                } catch (Exception ex) {
                }
            }
        });

        buttons.add(addFile);
        buttons.add(addDir);
        buttons.add(delSelected);

        c.gridy++;
        c.gridwidth = 2;
        c.weighty = 0.0d;
        c.gridx = 0;
        p.add(buttons, c);
        c.gridy++;
        c.gridwidth = 1;
        c.gridx = 0;
    }

    public void addCodename(final JPanel p, GridBagConstraints c, String label) {
        p.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 0.1;
        c.weighty = 0.0d;
        final JTextField pkgname = new JTextField(packagename);
        p.add(pkgname, c);
        c.weightx = 0.9;
        c.gridx = 0;
        c.gridy++;

        pkgname.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {}
            public void keyTyped(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {
                packagename = pkgname.getText();
            }
        });

    }

    public void addDescriptionPair(final JPanel p, GridBagConstraints c, String label) {
    
        String desc = get("Description");
        String pName = "";
        String pDesc = "";
        if (desc != null) {
            String[] lines = desc.split("\n");
            pName = lines[0];
            for (int i = 1; i < lines.length; i++) {
                pDesc += lines[i] + "\n";
            }
        }

        final JTextField fName = new JTextField(pName);
        final JTextArea fDesc = new JTextArea(8, 80);
        fDesc.setText(pDesc);


        c.gridy++;
        c.weightx = 0.1;
        c.gridx = 0;
        c.weighty = 0.0d;
        p.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 0.9;
        p.add(fName, c);
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.1;
        c.weighty = 0.0d;
        p.add(new JLabel("Description:"), c);
        c.gridx = 0;
        c.gridy++;

        c.gridwidth = 2;
        c.weightx = 0.9;
        c.weighty = 0.2;
        JScrollPane sDesc = new JScrollPane(fDesc);
        c.fill = GridBagConstraints.BOTH;
        p.add(sDesc, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0.0d;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy++;

        fName.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {}
            public void keyTyped(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {
                set("Description", fName.getText() + "\n" + fDesc.getText());
            }
        });

        fDesc.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {}
            public void keyTyped(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {
                set("Description", fName.getText() + "\n" + fDesc.getText());
            }
        });

    }

    public void updateFileTree() {
        dataModel.fireTableDataChanged();
    }

    public void addRelativeFile(File src, String dest) {
        URI rootUri = root.toURI();
        URI fileUri = src.toURI();
        String path = rootUri.relativize(fileUri).getPath();
        files.put(path, dest);
    }
}

