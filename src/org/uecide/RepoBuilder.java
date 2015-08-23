package org.uecide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.util.regex.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

public class RepoBuilder extends JFrame implements MouseListener, TreeSelectionListener {

    public static HashMap<String, ArrayList<String>> libs = new HashMap<String, ArrayList<String>>();

    String pkgSource = "";
    String pkgSection = "";
    String pkgFamily = "";
    String pkgPriority = "extra";
    String pkgMaintainer = "";
    String pkgBuildDepends = "debhelper (>= 8.0.0)";
    String pkgStandardsVersion = "3.9.4";
    String pkgHomepage = "";

    public static Repo repo;

    JTree repoTree;
    JPanel repoPanel;
    DefaultMutableTreeNode rootNode;

    HashMap<String, Package> packages;

    DefaultTreeModel treeModel;
    DefaultMutableTreeNode libsNode;
    DefaultMutableTreeNode boardsNode;
    DefaultMutableTreeNode coresNode;
    DefaultMutableTreeNode compilersNode;
    DefaultMutableTreeNode pluginsNode;

    public static String[] families = {
        "arm", "arm-sam", "avr", "avrtiny", "lm4f", "maple", "msp430",
        "pic18", "pic32", "teensy3", "teensy-avr", "native", "esp8266"
    };

    File root;
    File lastParent = null;

    public RepoBuilder(File f) {
        if (f != null) {
            doLoadRepo(f);
        }
        setLayout(new BorderLayout());

        JMenuBar mainMenu = new JMenuBar();;
        JMenu fileMenu = new JMenu("File");
        JMenu addMenu = new JMenu("Add");
        mainMenu.add(fileMenu);
        mainMenu.add(addMenu);

        add(mainMenu, BorderLayout.NORTH);

        JMenuItem openRepo = new JMenuItem("Open Package Root");
        fileMenu.add(openRepo);
        openRepo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadRepo();
            }
        });

        JMenuItem newRepo = new JMenuItem("Initialise New Repository");
        fileMenu.add(newRepo);
        newRepo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createNewRepo();
            }
        });

        JMenuItem saveRepo = new JMenuItem("Save Repository Data");
        fileMenu.add(saveRepo);
        saveRepo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (repo != null) {
                    repo.save();
                }
            }
        });

        JMenuItem quitProgram = new JMenuItem("Quit");
        fileMenu.add(quitProgram);
        quitProgram.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });



        JMenuItem addLibrary = new JMenuItem("Add Library");
        JMenuItem addBoard = new JMenuItem("Add Board");
        JMenuItem addCore = new JMenuItem("Add Core");
        JMenuItem addCompiler = new JMenuItem("Add Compiler");
        JMenuItem addPlugin = new JMenuItem("Add Plugin");

        addMenu.add(addLibrary);
        addMenu.add(addBoard);
        addMenu.add(addCore);
        addMenu.add(addCompiler);
        addMenu.add(addPlugin);

        addLibrary.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addNewLibrary();
            }
        });

        addBoard.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addNewBoard();
            }
        });

        addCore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addNewCore();
            }
        });

        addCompiler.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addNewCompiler();
            }
        });

        addPlugin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addNewPlugin();
            }
        });


        JMenu wizardMenu = new JMenu("Wizards");
        mainMenu.add(wizardMenu);
        
        JMenuItem wizLib = new JMenuItem("Create Library");
        wizardMenu.add(wizLib);
        wizLib.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createLibraryWizard();
            }
        });

        repoTree = new JTree();
        JPanel repoOuter = new JPanel();
        repoOuter.setLayout(new BorderLayout());
        repoPanel = new JPanel();

        repoOuter.add(repoPanel, BorderLayout.CENTER);


        generateTree();
        JScrollPane leftScroll = new JScrollPane(repoTree);

        JSplitPane lrSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, repoOuter);

        lrSplit.setDividerLocation(250);
        add(lrSplit, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);



        setVisible(true);
    }

    public static void main(String[] args) {

        initCategories();

        File f = null;

        if (args.length > 0) {
            f = new File(args[0]);
            if (!f.exists()) {
                f = null;
            }
        }
        RepoBuilder prog = new RepoBuilder(f);
            
    }

    public static String getFileAsString(File f) {
        if (f == null) {
            return "";
        }
        if (!f.exists()) {
            return "";
        }
        if (f.isDirectory()) {
            return "";
        }
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(f));
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
        return "";
    }

    public void loadRepo() {
        JFileChooser fc = new JFileChooser();

        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int rv = fc.showOpenDialog(this);

        if(rv == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            doLoadRepo(f);
            
        }

    }

    public void doLoadRepo(File f) {
        root = f;
        System.err.println("Loading repo: " + f.getAbsolutePath());
        File debian = new File(f, "debian");
        if (!debian.exists()) {
            return;
        }

        File control = new File(debian, "control");
        if (!control.exists()) {
            return;
        }
        repo = new Repo(f, getFileAsString(control));
    }

    public static File getConfigDir() {
        File f = new File(System.getProperty("user.home"), ".repobuilder");
        if (!f.exists()) {
            f.mkdirs();
        }
        return f;
    }

    public static void initCategories() {
        File configDir = getConfigDir();
        File catFile = new File(configDir, "librarysections.txt");
        if (!catFile.exists()) {
            try {
                PrintWriter pw = new PrintWriter(catFile);
                pw.println("IO");
                pw.println("    Digital");
                pw.println("    Analog");
                pw.println("Math");
                pw.println("Audio");
                pw.println("Board Support");
                pw.println("Communications");
                pw.println("    Networking");
                pw.println("    Serial");
                pw.println("    Parallel");
                pw.println("    SPI");
                pw.println("    I2C");
                pw.println("    Protocols");
                pw.println("Control");
                pw.println("Motor");
                pw.println("    Stepper");
                pw.println("    Servo");
                pw.println("Display");
                pw.println("    LCD");
                pw.println("    LED");
                pw.println("    TFT");
                pw.println("    OLED");
                pw.println("Meta Packages");
                pw.println("Misc");
                pw.println("Robotics");
                pw.println("Sensors");
                pw.println("    Pressure");
                pw.println("    Accelerometer");
                pw.println("    Gyroscope");
                pw.println("    GPS");
                pw.println("Storage");
                pw.println("    SD and MMC");
                pw.println("    EEPROM");
                pw.println("Time");
                pw.println("    RTC");
                pw.println("    Timers");
                pw.close();
            } catch (Exception e) {
            }
        }

        String inData = getFileAsString(catFile);

        String[] lines = inData.split("\n");

        String currentEntry = "";
        ArrayList<String> subgroup = new ArrayList<String>();

        for (String line : lines) {
            if (line.trim().equals("")) {
                continue;
            }

            if (line.startsWith(" ")) {
                subgroup.add(line.trim());
            } else {
                if (!currentEntry.equals("")) {
                    libs.put(currentEntry, subgroup);
                    subgroup = new ArrayList<String>();
                }
                currentEntry = line.trim();
            }
        }
        libs.put(currentEntry, subgroup);
    }

    public void generateTree() {
        if (repo == null) {
            rootNode = new DefaultMutableTreeNode("No Repo Loaded");
            repoTree = new JTree(rootNode);
            return;
        }

        rootNode = new DefaultMutableTreeNode("root");
        rootNode.setUserObject(repo);
        treeModel = new DefaultTreeModel(rootNode);
        repoTree = new JTree(treeModel);


        libsNode = new DefaultMutableTreeNode("Libraries");
        boardsNode = new DefaultMutableTreeNode("Boards");
        coresNode = new DefaultMutableTreeNode("Cores");
        compilersNode = new DefaultMutableTreeNode("Compilers");
        pluginsNode = new DefaultMutableTreeNode("Plugins");

        rootNode.add(libsNode);
        rootNode.add(boardsNode);
        rootNode.add(coresNode);
        rootNode.add(compilersNode);
        rootNode.add(pluginsNode);

        buildTree();

        repoTree.addMouseListener(this);
        repoTree.addTreeSelectionListener(this);
    }

    public void buildTree() {
        libsNode.removeAllChildren();
        boardsNode.removeAllChildren();
        coresNode.removeAllChildren();
        compilersNode.removeAllChildren();
        pluginsNode.removeAllChildren();

        for (Package p : repo.getPackages()) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(p.toString());
            node.setUserObject(p);
            if (p instanceof Library) {
                libsNode.add(node);
            } else if (p instanceof Board) {
                boardsNode.add(node);
            } else if (p instanceof Core) {
                coresNode.add(node);
            } else if (p instanceof Compiler) {
                compilersNode.add(node);
            } else if (p instanceof Plugin) {
                pluginsNode.add(node);
            }
        }
        treeModel.reload();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        repoPanel.removeAll();
        int selRow = repoTree.getRowForLocation(e.getX(), e.getY());
        TreePath selPath = repoTree.getPathForLocation(e.getX(), e.getY());
        repoTree.setSelectionPath(selPath);
        if (selPath == null) {
            return;
        }

        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)selPath.getLastPathComponent();
        if (selectedNode == null) {
            return;
        }
        Object ob = selectedNode.getUserObject();

        if (ob instanceof Package) {
            Package p = (Package)(selectedNode.getUserObject());
            if (p != null) {
                p.populatePanel(repoPanel);
                repoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            }
        }

        if (ob instanceof Repo) {
            Repo r = (Repo)ob;
            Source s = r.getSource();
            s.populatePanel(repoPanel);
            repoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        }

        repoPanel.revalidate();
        repoPanel.repaint();
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void valueChanged(TreeSelectionEvent e) {
        repoPanel.removeAll();
        TreePath selPath = e.getNewLeadSelectionPath();
        if (selPath == null) {
            return;
        }

        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)selPath.getLastPathComponent();
        if (selectedNode == null) {
            return;
        }
        Object ob = selectedNode.getUserObject();

        if (ob instanceof Package) {
            Package p = (Package)(selectedNode.getUserObject());
            if (p != null) {
                p.populatePanel(repoPanel);
                repoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            }
        }

        if (ob instanceof Repo) {
            Repo r = (Repo)ob;
            Source s = r.getSource();
            s.populatePanel(repoPanel);
            repoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        }

        repoPanel.revalidate();
        repoPanel.repaint();
    }

    public String askName(String title, String def) {
        String ret = JOptionPane.showInputDialog(this, title, def);
        return ret;
    }

    public void addNewLibrary() {
        String sourcename = repo.getSourceName();
        if (sourcename == null) {
            sourcename = "";
        }
        
        if (!sourcename.equals("")) {
            sourcename = "-" + sourcename;
        }

        String libname = askName("New Library Codename", "lib" + sourcename);
        if (libname == null) {
            return;
        }
        if (libname.equals("")) {
            return;
        }
        Library lib = new Library(root, libname);
        repo.addPackage(lib);
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(libname);
        newNode.setUserObject(lib);
        libsNode.add(newNode);
        treeModel.reload(libsNode);
    }

    public void addNewBoard() {
        String boardname = askName("New Board Codename", "");
        if (boardname == null) {
            return;
        }
        if (boardname.equals("")) {
            return;
        }
        Board board = new Board(root, boardname);
        repo.addPackage(board);
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(boardname);
        newNode.setUserObject(board);
        boardsNode.add(newNode);
        treeModel.reload(boardsNode);
    }

    public void addNewCore() {
        String corename = askName("New Core Codename", "");
        if (corename == null) {
            return;
        }
        if (corename.equals("")) {
            return;
        }
        Core core = new Core(root, corename);
        repo.addPackage(core);
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(corename);
        newNode.setUserObject(core);
        coresNode.add(newNode);
        treeModel.reload(coresNode);
    }

    public void addNewCompiler() {
        String compilername = askName("New Compiler Codename", "");
        if (compilername == null) {
            return;
        }
        if (compilername.equals("")) {
            return;
        }
        Compiler compiler = new Compiler(root, compilername);
        repo.addPackage(compiler);
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(compilername);
        newNode.setUserObject(compiler);
        compilersNode.add(newNode);
        treeModel.reload(compilersNode);
    }

    public void addNewPlugin() {
        String pluginname = askName("New Plugin Codename", "");
        if (pluginname == null) {
            return;
        }
        if (pluginname.equals("")) {
            return;
        }
        Plugin plugin = new Plugin(root, pluginname);
        repo.addPackage(plugin);
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(pluginname);
        newNode.setUserObject(plugin);
        pluginsNode.add(newNode);
        treeModel.reload(pluginsNode);
    }

    public void createNewRepo() {
        JFileChooser fc = new JFileChooser();

        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int rv = fc.showOpenDialog(this);

        if(rv == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            doCreateRepo(f);
        }
    }

    public void doCreateRepo(File f) {
        repo = new Repo(f, f.getName().toLowerCase(), "libraries");
        rootNode.setUserObject(repo);

        buildTree();

    }

    public void createLibraryWizard() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Main Library Header", "h");
        fc.setFileFilter(filter);
        fc.setDialogTitle("Select master header file for library");
        if (lastParent == null) {
            lastParent = root;
        }
        fc.setCurrentDirectory(lastParent);

        int rv = fc.showOpenDialog(this);
        if (rv == JFileChooser.APPROVE_OPTION) {
            doCreateLibraryWizard(fc.getSelectedFile());
        }
    }

    class GroupSub {
        String group;
        String subgroup;
        GroupSub(String g, String s) {
            group = g;
            subgroup = s;
        }

        public String toString() {
            return group + " :: " + subgroup;
        }

        public String getGroup() { 
            return group;
        }

        public String getSubgroup() {
            return subgroup;
        }
    }

    public void doCreateLibraryWizard(File f) {
        Object[] groups = libs.keySet().toArray();

        ArrayList<GroupSub> grouplist = new ArrayList<GroupSub>();

        for (Object g : groups) {
            ArrayList<String> subgroups = libs.get(g);
            for (String sg : subgroups) {
                GroupSub opt = new GroupSub((String)g, sg);
                grouplist.add(opt);
            }
        }

        Object[] options = grouplist.toArray();

        Object groupob = JOptionPane.showInputDialog(this, "Main Section:", "Select Main Section",
            JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (groupob == null) {
            return;
        }

        GroupSub gs = (GroupSub)groupob;

        String group = gs.getGroup();
        String subgroup = gs.getSubgroup();
        

        String provides = f.getName();
        String parts[] = provides.split("\\.");
        String libname = parts[0];
        String tie = repo.getSource().get("XBSC-TieCore");

        String codename = "lib";
        codename += libname.toLowerCase();
        if (tie != null) {
            codename += "-";
            codename += tie;
        }

        
        Library lib = new Library(root, codename);
        lib.set("Provides", provides.replace("_", "-UL"));
        if (tie != null) {
            lib.set("Depends", tie);
            lib.set("Description", libname + " library for " + tie);
        } else {
            lib.set("Description", libname + " library");
        }
        lib.set("XBSC-Group", group);
        lib.set("XBSC-Subgroup", subgroup);


        String path = "libraries/" + group + "/";
        if (tie != null) {
            path += tie + "/";
        }

        path += libname;

        File dir = f.getParentFile();
        lastParent = dir.getParentFile();
        File[] list = dir.listFiles();
        for (File file : list) {
            lib.addRelativeFile(file, path);
        }

        repo.addPackage(lib);
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(codename);
        newNode.setUserObject(lib);
        libsNode.add(newNode);
        treeModel.reload(libsNode);
    }
}
