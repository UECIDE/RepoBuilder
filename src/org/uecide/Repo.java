package org.uecide;

import java.util.*;
import java.util.regex.*;
import java.io.*;

class Repo {
    Source source = null;
    ArrayList<Package> packages;
    File root;

    public Repo(File rootfolder, String control) {

        root = rootfolder;
        packages = new ArrayList<Package>();
        control += "\n\n";
        String[] lines = control.split("\n");

        Pattern pat = Pattern.compile("^([a-zA-Z\\-]+):\\s+(.*)$");

        String lastKey = null;

        HashMap<String, String> chunk = new HashMap<String, String>();

        for (String line : lines) {

            if (line.trim().equals("")) { // End of block
                if (chunk.get("Source") != null) {
                    source = new Source(root, chunk.get("Source"));
                    for (String k : chunk.keySet()) {
                        source.set(k, chunk.get(k));
                    }
                } else if (chunk.get("Package") != null) {
                    Package p = null;
                    String sec = chunk.get("Section");
                    if (sec == null) {
                        sec = "";
                    }
                    if (sec.equals("libraries")) {
                        p = new Library(root, chunk.get("Package"));
                    } else if (sec.equals("boards")) {
                        p = new Board(root, chunk.get("Package"));
                    } else if (sec.equals("cores")) {
                        p = new Core(root, chunk.get("Package"));
                    } else if (sec.equals("compilers")) {
                        p = new Compiler(root, chunk.get("Package"));
                    } else if (sec.equals("plugins")) {
                        p = new Plugin(root, chunk.get("Package"));
                    } else {
                        System.err.println("> Bad entry: " + chunk.get("Package"));
                    }

                    if (p != null) {
                        p.loadFiles(root);
                        for (String k : chunk.keySet()) {
                            p.set(k, chunk.get(k));
                        }
                        packages.add(p);
                    }
                }
                chunk = new HashMap<String, String>();
                continue;
            }

            Matcher m = pat.matcher(line);
            if (m.find()) {
                String key = m.group(1);
                String val = m.group(2);
                chunk.put(key, val.trim());
                lastKey = key;
                continue;
            } 

            if (line.startsWith(" ")) {
                String d = chunk.get(lastKey);
                d += "\n" + line.trim();
                chunk.put(lastKey, d);
                continue;
            }

        }

                if (chunk.get("Source") != null) {
                    source = new Source(root, chunk.get("Source"));
                    for (String k : chunk.keySet()) {
                        source.set(k, chunk.get(k));
                    }
                } else if (chunk.get("Package") != null) {
                    Package p = null;
                    String sec = chunk.get("Section");
                    if (sec == null) {
                        sec = "";
                    }
                    if (sec.equals("libraries")) {
                        p = new Library(root, chunk.get("Package"));
                    } else if (sec.equals("boards")) {
                        p = new Board(root, chunk.get("Package"));
                    } else if (sec.equals("cores")) {
                        p = new Core(root, chunk.get("Package"));
                    } else if (sec.equals("compilers")) {
                        p = new Compiler(root, chunk.get("Package"));
                    } else if (sec.equals("plugins")) {
                        p = new Plugin(root, chunk.get("Package"));
                    } else {
                        System.err.println("> Bad entry: " + chunk.get("Package"));
                    }

                    if (p != null) {
                        p.loadFiles(root);
                        for (String k : chunk.keySet()) {
                            p.set(k, chunk.get(k));
                        }
                        packages.add(p);
                    }
                }

        if (source == null) {
            System.err.println("\n\n********\nNO SOURCE FOUND\n********\n\n");
        }
    }

    public Repo(File rootFolder, String name, String mainSection) {
        root = rootFolder;
        source = new Source(root, name);
        source.set("Section", mainSection);
        packages = new ArrayList<Package>();
    }

    public String toString() {
        return source.toString();
    }
   
    public String getData() {
        StringBuilder sb = new StringBuilder();
        sb.append(source.getBlock());
        sb.append("\n");
        for (Package p : packages) {
            sb.append(p.getBlock());
            sb.append("\n");
        }
        return sb.toString();
    }

    public ArrayList<Package> getPackages() {
        return packages;
    }

    public void save() {
        try {
            File debian = new File(root, "debian");
            if (!debian.exists()) {
                debian.mkdirs();
            }
            File control = new File(debian, "control");
            PrintWriter pw = new PrintWriter(control);
            pw.println(getData());
            pw.println();
            pw.close();

            for (Package p : packages) {
                String fn = p.toString() + ".install";
                File instFile = new File(debian, fn);
                pw = new PrintWriter(instFile);
                pw.print(p.getFiles());
                pw.close();
            }

            File compat = new File(debian, "compat");
            if (!compat.exists()) {
                pw = new PrintWriter(compat);
                pw.println("8");
                pw.close();
            }
            File rules = new File(debian, "rules");
            if (!rules.exists()) {
                pw = new PrintWriter(rules);
                pw.println("#!/usr/bin/make -f");
                pw.println();
                pw.println("%:");
                pw.println("	dh $@");
                pw.close();
            }
            File src = new File(debian, "source");
            if (!src.exists()) {
                src.mkdirs();
            }

            File format = new File(src, "format");
            if (!format.exists()) {
                pw = new PrintWriter(format);
                pw.println("3.0 (native)");
                pw.close();
            }
            
            File hooks = new File(debian, "hooks");
            if (!hooks.exists()) {
                hooks.mkdirs();
            }

            File boot = new File(hooks, "boot");
            if (!boot.exists()) {
                pw = new PrintWriter(boot);
                pw.println("#!/usr/bin/perl");
                pw.println("");
                pw.println("my $package = 'unknown';");
                pw.println("");
                pw.println("my $maj = 0;");
                pw.println("my $min = 0;");
                pw.println("");
                pw.println("my $out = '';");
                pw.println("");
                pw.println("open (CONTROL, '<debian/control');");
                pw.println("while (my $l = <CONTROL>) {");
                pw.println("    chomp($l);");
                pw.println("    if ($l =~ /Source: (.*)/) {");
                pw.println("        $package = $1;");
                pw.println("    }");
                pw.println("}");
                pw.println("close(CONTROL);");
                pw.println("");
                pw.println("open(LOG, 'git log --reverse --pretty=format:'%aN|%aE|%cD|%s'|');");
                pw.println("while (my $l = <LOG>) {");
                pw.println("    chomp($l);");
                pw.println("    my ($author, $email, $time, $subject) = split(/\\|/, $l);");
                pw.println("    my $add =\" $package (1.$maj.$min) uecide; urgency=low\\n\";");
                pw.println("    $add .= \"\\n\";");
                pw.println("    $add .= \"  * $subject\\n\";");
                pw.println("    $add .= \"\\n\";");
                pw.println("    $add .= \" -- $author <$email>  $time\\n\";");
                pw.println("    $add .= \"\\n\";");
                pw.println("    $min++;");
                pw.println("    if ($min == 10) {");
                pw.println("        $min = 0;");
                pw.println("        $maj++;");
                pw.println("    }");
                pw.println("    $out = $add . $out;");
                pw.println("}");
                pw.println("close(LOG);");
                pw.println("");
                pw.println("");
                pw.println("my $oldMD5 = `md5sum debian/changelog`;");
                pw.println("open(CL, \">debian/changelog\");");
                pw.println("print CL $out;");
                pw.println("close(CL);");
                pw.close();
            }

        } catch (Exception e) {
        }
    }

    public void addPackage(Package p) {
        packages.add(p);
    }

    public String getSourceName() {
        return source.toString();
    }

    public Source getSource() {
        return source;
    }

}
