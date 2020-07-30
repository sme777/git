package gitlet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Arrays;
import java.util.TimeZone;

/** A mini version of Git which mimics many
 * functionalities of the real GitHub.
 * @author sm3*/
public class Gitlet implements Serializable {

    /** Constructor of Gitlet system. */
    public Gitlet() {
        init();
    }

    /** Initialize Gitlet version-control system. */
    void init() {
        String message = "initial commit";
        Date temp = new Date(0);
        String timestamp = timefy(temp);
        if (new File(".gitlet").exists()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
            System.exit(0);
        }
        File repo = new File(".gitlet");
        repo.mkdir();
        File stagingArea = Utils.join(repo, "staging");
        stagingArea.mkdir();
        File commits = Utils.join(repo, "commits");
        commits.mkdir();
        Commit init = new Commit(message, timestamp, "master");
        String sha = init.encrypt();
        init.serialize();
        _connector.put(sha, init);
        _linkedList.add(sha);
        _allConnections.put("master", _linkedList);
        _hashedCommits.add(init);
        File ds = Utils.join(repo, "dataStructs");

        ds.mkdir();
        try {
            ObjectOutputStream remoted = new ObjectOutputStream(
                    new FileOutputStream(Utils.join(ds, "remote")));
            remoted.writeObject(_remote);
            ObjectOutputStream con = new ObjectOutputStream(
                    new FileOutputStream(Utils.join(ds, "connector")));
            con.writeObject(_connector);
            con.close();
            ObjectOutputStream list = new ObjectOutputStream(
                    new FileOutputStream(Utils.join(ds, "list")));
            list.writeObject(_linkedList);
            list.close();
            ObjectOutputStream map = new ObjectOutputStream(
                    new FileOutputStream(Utils.join(ds, "map")));
            map.writeObject(_allConnections);
            map.close();
            ObjectOutputStream hash = new ObjectOutputStream(
                    new FileOutputStream(Utils.join(ds, "hash")));
            hash.writeObject(_hashedCommits);
            hash.close();
            ObjectOutputStream mark = new ObjectOutputStream(
                    new FileOutputStream(Utils.join(ds, "mark")));
            mark.writeObject(_marked);
            mark.close();
            ObjectOutputStream branch = new ObjectOutputStream(
                    new FileOutputStream(Utils.join(ds, "branch")));
            branch.writeObject("master");
            branch.close();
        } catch (IOException ex) {
            ex.fillInStackTrace();
        }
    }

    /** Adds a file to staging area, does not commit.
     * @param args list of arguments passed */
    @SuppressWarnings("unchecked")
    static void add(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else if (!new File(args[1]).exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        try {
            String inputRead = Utils.readContentsAsString(new File(args[1]));
            ObjectInputStream list = new ObjectInputStream(
                    new FileInputStream(".gitlet/dataStructs/list"));
            LinkedList<String> link = (LinkedList<String>) list.readObject();
            String toCompare = link.get(link.size() - 1);
            list.close();
            ObjectInputStream mark = new ObjectInputStream(
                    new FileInputStream(".gitlet/dataStructs/mark"));
            HashSet<String> marked = (HashSet<String>) mark.readObject();
            mark.close();
            marked.remove(args[1].substring(
                    args[1].lastIndexOf("/") + 1));
            if (new File(String.format(".gitlet/commits/%s/%s",
                    toCompare, args[1].substring(
                            args[1].lastIndexOf("/") + 1))).exists()) {
                File toCheck = new File(String.format(
                        ".gitlet/commits/%s/%s", toCompare,
                        args[1].substring(args[1].lastIndexOf("/") + 1)));
                String content = Utils.readContentsAsString(toCheck);
                String compare = Utils.readContentsAsString(new File(args[1]));
                if (compare.equals(content)) {
                    File checker = new File(String.format(".gitlet/staging/%s",
                            args[1].substring(args[1].lastIndexOf("/") + 1)));
                    if (checker.exists()) {
                        checker.delete();
                    }
                } else {
                    Utils.writeContents(Utils.join(".gitlet/staging",
                            args[1].substring(args[1].lastIndexOf(
                                    "/") + 1)), inputRead);
                }
            } else {
                Utils.writeContents(
                        Utils.join(".gitlet/staging",
                        args[1].substring(args[1].lastIndexOf(
                                "/") + 1)), inputRead);
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
    }
    /** Checks errors for main commit function.
     * @param args list of arguments */
    private static void checkCommit(String[] args) {
        if (args.length == 1) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        } else if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else if (args[1].equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
    }
    /** Adds a new commit to a the commit tree.
     * @param args list of arguments */
    @SuppressWarnings("unchecked")
    static void commit(String[] args) {
        checkCommit(args);
        try {
            ObjectInputStream br = new ObjectInputStream(new FileInputStream(
                    new File(".gitlet/dataStructs/branch")));
            String branch = (String) br.readObject();
            Commit newCommit = new Commit(args[1], timefy(new Date()), branch);
            String call = newCommit.encrypt();
            if (newCommit.serialize2() > 0) {
                ObjectInputStream obj = new ObjectInputStream(
                        new FileInputStream(".gitlet/dataStructs/list"));
                LinkedList<String> link = (LinkedList<String>) obj.readObject();
                link.add(call);
                ObjectOutputStream bj = new ObjectOutputStream(
                        new FileOutputStream(".gitlet/dataStructs/list"));
                bj.writeObject(link);
                ObjectInputStream hash = new ObjectInputStream(
                        new FileInputStream(".gitlet/dataStructs/hash"));
                HashSet<Commit> hashed = (HashSet<Commit>) hash.readObject();
                hashed.add(newCommit);
                ObjectOutputStream rehash = new ObjectOutputStream(
                        new FileOutputStream(".gitlet/dataStructs/hash"));
                rehash.writeObject(hashed);
                ObjectInputStream mappy = new ObjectInputStream(
                        new FileInputStream(new File(mapLoc)));
                HashMap<String, LinkedList<String>> newMap = (HashMap<String,
                        LinkedList<String>>) mappy.readObject();
                if (args[1].contains("Merged") && args[1].contains("into")) {
                    int pos1 = args[1].indexOf(" ");
                    int pos2 = args[1].indexOf(" ", pos1 + 1);
                    int pos3 = args[1].indexOf(" ", pos2 + 1);
                    String p1 = args[1].substring(pos1 + 1, pos2);
                    String p2 = args[1].substring(pos3 + 1,
                            args[1].length() - 1);
                    String first = newMap.get(p1).get(
                            newMap.get(p1).size() - 1);
                    String second = newMap.get(p2).get(newMap.get(
                                    p2).size() - 1);
                    String[] all = {p2, second, p1, first};
                    newCommit.mergeParent(all);
                }
                newMap.put(branch, link);
                ObjectOutputStream remap = new ObjectOutputStream(
                        new FileOutputStream(new File(mapLoc)));
                remap.writeObject(newMap);
                ObjectInputStream map = new ObjectInputStream(
                        new FileInputStream(".gitlet/dataStructs/connector"));
                HashMap<String, Commit> mapped = (HashMap<String,
                        Commit>) map.readObject();
                mapped.put(call, newCommit);
                ObjectOutputStream bbj = new ObjectOutputStream(
                        new FileOutputStream(".gitlet/dataStructs/connector"));
                bbj.writeObject(mapped);
            } else {
                System.exit(0);
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
    }

    /** Checks out a file, commit or an entire branch to working directory.
     * @param args list of arguments */
    static void checkout(String[] args) {
        if (args.length == 3 && args[1].equals("--")) {
            fileCheckout(args);
        } else if (args.length == 4) {
            commitCheckout(args);
        } else if (args.length == 2) {
            branchCheckout(args);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
    /** Checks out a file from most recent commit.
     * @param args list of arguments */
    @SuppressWarnings("unchecked")
    private static void fileCheckout(String[] args) {
        LinkedList<String> link = null;
        try {
            ObjectInputStream out = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/list")));
            link = (LinkedList<String>) out.readObject();
            out.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        if (!new File(String.format(".gitlet/commits/%s/%s",
                link.get(link.size() - 1), args[2])).exists()) {
            System.out.println("File does not exist in that commit");
            System.exit(0);
        } else {
            File toOverwrite = new File(args[2]);
            Utils.writeContents(toOverwrite, Utils.readContentsAsString(
                    new File(String.format(".gitlet/commits/%s/%s",
                            link.get(link.size() - 1), args[2]))));
        }
    }
    /** Checks out a file from a given commit.
     * @param args list of arguments */
    @SuppressWarnings("unchecked")
    private static void commitCheckout(String[] args) {
        if (args[2].equals("--")) {
            LinkedList<String> link = null;
            HashSet<Commit> allCom = null;
            try {
                ObjectInputStream out = new ObjectInputStream(
                        new FileInputStream(new File(
                                ".gitlet/dataStructs/list")));
                link = (LinkedList<String>) out.readObject();
                ObjectInputStream real = new ObjectInputStream(
                        new FileInputStream(new File(
                                ".gitlet/dataStructs/hash")));
                allCom = (HashSet<Commit>) real.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                ex.fillInStackTrace();
            }
            File toOverwrite = new File(args[3]);
            for (Commit com : allCom) {
                if (args[1].length() < 10) {
                    if (com.encrypt().substring(0,
                            args[1].length()).equals(args[1])) {
                        File lastCommit = new File(String.format(
                                ".gitlet/commits/%s/%s",
                                com.encrypt(), args[3]));
                        if (!lastCommit.exists()) {
                            System.out.println("File does not "
                                    + "exist in that commit.");
                            System.exit(0);
                        } else {
                            Utils.writeContents(toOverwrite,
                                    Utils.readContentsAsString(lastCommit));
                            break;
                        }
                    }
                } else {
                    if (!new File(String.format(".gitlet/commits/%s",
                            args[1])).exists()) {
                        System.out.println("No commit with that id exists.");
                        System.exit(0);
                    }
                    if (com.encrypt().equals(args[1])) {
                        File lastCommit = new File(String.format(
                                ".gitlet/commits/%s/%s", args[1], args[3]));
                        if (!lastCommit.exists()) {
                            System.out.println("File does not "
                                    + "exist in that commit.");
                            System.exit(0);
                        } else {
                            Utils.writeContents(toOverwrite,
                                    Utils.readContentsAsString(lastCommit));
                            break;
                        }
                    }
                }
            }
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
    /** Checks out a given branch.
     * @param args list of arguments */
    @SuppressWarnings("unchecked")
    private static void branchCheckout(String[] args) {
        try {
            ObjectInputStream marked = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/mark")));
            HashSet<String> marks = (HashSet<String>) marked.readObject();
            ObjectInputStream reader = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/map")));
            HashMap<String, LinkedList<String>> map = (HashMap<String,
                    LinkedList<String>>) reader.readObject();
            ObjectInputStream br = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/branch")));
            String branch = (String) br.readObject();
            if (!map.containsKey(args[1])) {
                System.out.println("No such branch exists.");
                System.exit(0);
            } else if (args[1].equals(branch)) {
                System.out.println("No need to checkout "
                        + "the current branch.");
                System.exit(0);
            } else if (trackTester() == -1) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it or add it first.");
                System.exit(0);
            }
            LinkedList<String> branchList = map.get(args[1]);
            deleteTester();
            String curr = branchList.get(branchList.size() - 1);
            File[] dir = new File(String.format(
                    ".gitlet/commits/%s", curr)).listFiles();
            if (dir != null) {
                for (File file : dir) {
                    if (!marks.contains(file.getName())) {
                        Utils.writeContents(new File(file.getName()),
                                Utils.readContentsAsString(file));
                    } else {
                        marks.remove(file.getName());
                        ObjectOutputStream remMark = new ObjectOutputStream(
                                new FileOutputStream(new File(
                                        ".gitlet/dataStructs/mark")));
                        remMark.writeObject(marks);
                    }
                }
            }
            for (File f: new File(".gitlet/staging").listFiles()) {
                f.delete(); }
            ObjectOutputStream newLink = new ObjectOutputStream(
                    new FileOutputStream(new File(
                            ".gitlet/dataStructs/list")));
            newLink.writeObject(branchList);
            newLink.close();
            ObjectOutputStream bran = new ObjectOutputStream(
                    new FileOutputStream(new File(
                            ".gitlet/dataStructs/branch")));
            bran.writeObject(args[1]);
            bran.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
    }
    /** Unstages files, marks to be untracked in the next commit,
     * and removes from working directory.
     * @param args list of arguments*/
    @SuppressWarnings("unchecked")
    static void remove(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        File path = Utils.join(".gitlet/staging",
                args[1].substring(args[1].lastIndexOf("/") + 1));
        String commit = "";
        HashSet<String> marked = null;
        try {
            ObjectInputStream obj = new ObjectInputStream(new FileInputStream(
                    new File(".gitlet/dataStructs/list")));
            LinkedList<String> list = (LinkedList<String>) obj.readObject();
            commit = list.get(list.size() - 1);
            obj.close();

            ObjectInputStream mark = new ObjectInputStream(new FileInputStream(
                    new File(".gitlet/dataStructs/mark")));
            marked = (HashSet<String>) mark.readObject();
            mark.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        if (new File(String.format(
                ".gitlet/commits/%s/%s", commit,
                args[1].substring(args[1].lastIndexOf("/") + 1))).exists()) {
            marked.add(args[1].substring(args[1].lastIndexOf("/") + 1));
            try {
                ObjectOutputStream remark = new ObjectOutputStream(
                        new FileOutputStream(new File(
                                ".gitlet/dataStructs/mark")));
                remark.writeObject(marked);
                remark.close();
                new File(args[1]).delete();
            } catch (IOException ex) {
                ex.fillInStackTrace();
            }
        } else if (!path.exists()) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (path.exists()) {
            path.delete();
        }
    }

    /** Prints id's of commits with given message.
     * @param args list of arguments*/
    @SuppressWarnings("unchecked")
    static void find(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        int count = 0;
        try {
            ObjectInputStream hash = new ObjectInputStream(
                    new FileInputStream(".gitlet/dataStructs/hash"));
            HashSet<Commit> hashed = (HashSet<Commit>) hash.readObject();
            hash.close();
            for (Commit c : hashed) {
                if (c.getMessage().equals(args[1])) {
                    System.out.println(c.encrypt());
                    count++;
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        if (count == 0) {
            System.out.print("Found no commit with that message.");
            System.exit(0);
        }
    }

    /** Displays information about each commit starting
     * from the head of the branch.
     * @param args list of arguments */
    @SuppressWarnings("unchecked")
    static void log(String[] args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        LinkedList<String> lol = null;
        HashMap<String, Commit> map2 = null;
        try {
            ObjectInputStream linked = new ObjectInputStream(
                    new FileInputStream(".gitlet/dataStructs/list"));
            lol = (LinkedList<String>) linked.readObject();
            linked.close();
            ObjectInputStream mapped2 = new ObjectInputStream(
                    new FileInputStream(".gitlet/dataStructs/connector"));
            map2 = (HashMap<String, Commit>) mapped2.readObject();
            mapped2.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        for (int i = lol.size() - 1; i >= 0; i--) {
            Commit commit = map2.get(lol.get(i));
            if (commit.getMergeParent().size() != 0) {
                mergePrinter(commit);
            } else {
                printer(commit);
            }
        }
    }

    /** Displays information about all commits ever made.
     * @param args list of arguments */
    @SuppressWarnings("unchecked")
    static void globalLog(String[] args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        try {
            ObjectInputStream hash = new ObjectInputStream(
                    new FileInputStream(".gitlet/dataStructs/hash"));
            HashSet<Commit> allHashes = (HashSet<Commit>) hash.readObject();
            hash.close();
            for (Commit c: allHashes) {
                if (c.getMergeParent().size() != 0) {
                    mergePrinter(c);
                } else {
                    printer(c);
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }

    }

    /** Displays branches, staged and removed file.
     * Also shows modifications not staged for commit
     * and untracked files.
     * @param args list of arguments */
    @SuppressWarnings("unchecked")
    static void status(String[] args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        String branch = "";
        try {
            ObjectInputStream br = new ObjectInputStream(new FileInputStream(
                    new File(".gitlet/dataStructs/branch")));
            branch = (String) br.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        System.out.println("=== " + "Branches" + " ===");
        System.out.println("*" + branch);
        LinkedList<String> toCheck = null;
        try {
            ObjectInputStream map = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/map")));
            HashMap<String, LinkedList<String>> mapped = (HashMap<String,
                    LinkedList<String>>) map.readObject();
            TreeMap<String, LinkedList<String>> treeMap = new TreeMap<>(mapped);
            for (String k : treeMap.keySet()) {
                if (!k.equals(branch)) {
                    System.out.println(k);
                }
            }
            ObjectInputStream link = new ObjectInputStream(new FileInputStream(
                    new File(".gitlet/dataStructs/list")));
            toCheck = (LinkedList<String>) link.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        System.out.println();
        stagedFiles();
        System.out.println("=== Removed Files ===");
        String k = toCheck.get(toCheck.size() - 1);
        try {
            ObjectInputStream mark = new ObjectInputStream(
                    new FileInputStream(new File(markLoc)));
            HashSet<String> marked = (HashSet<String>) mark.readObject();
            for (String rm: marked) {
                if (!new File(rm).exists() && new File(String.format(
                        ".gitlet/commits/%s/%s", k, rm)).exists()) {
                    System.out.println(rm);
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        extraStatus(args);
    }
    /** Helper for status to print staged files. */
    private static void stagedFiles() {
        System.out.println("=== Staged Files ===");
        File[] stages = new File(".gitlet/staging").listFiles();
        if (stages != null) {
            if (stages.length != 0) {
                Arrays.sort(stages);
                for (File file : stages) {
                    System.out.println(file.getName());
                }
            }
        }
        System.out.println();
    }

    /** Extra credit for status, checks tracked and modified files.
     * @param args list of arguments */
    @SuppressWarnings("unchecked")
    private static void extraStatus(String[] args) {
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        try {
            ObjectInputStream readLink = new ObjectInputStream(
                    new FileInputStream(new File(listLoc)));
            LinkedList<String> linked = (LinkedList<String>)
                    readLink.readObject();
            ObjectInputStream mark = new ObjectInputStream(
                    new FileInputStream(new File(markLoc)));
            HashSet<String> marked = (HashSet<String>) mark.readObject();
            String curr = linked.get(linked.size() - 1);
            File[] listCom = new File(String.format(
                    ".gitlet/commits/%s", curr)).listFiles();
            if (listCom != null) {
                for (File file : listCom) {
                    if (!new File(".gitlet/staging/%s").exists()
                            && new File(file.getName()).exists()) {
                        if (!Utils.readContentsAsString(new File(
                                file.getName())).equals(
                                        Utils.readContentsAsString(file))) {
                            System.out.println(file.getName() + " (modified)");
                        }
                    } else if (!new File(file.getName()).exists()
                            && !marked.contains(file.getName())) {
                        System.out.println(file.getName() + " (deleted)");
                    }
                }
            }
            File[] staged = new File(stagLoc).listFiles();
            if (staged != null) {
                for (File file: staged) {
                    if (!new File(file.getName()).exists()) {
                        System.out.println(file.getName() + " (deleted)");
                    } else if (!Utils.readContentsAsString(file).equals(
                            Utils.readContentsAsString(
                                    new File(file.getName())))) {
                        System.out.println(file.getName() + " (modified)");
                    }
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        System.out.println();
        untracked();
    }
    /** Extra credit status for untracked files. */
    @SuppressWarnings("unchecked")
    private static void untracked() {
        System.out.println("=== Untracked Files ===");
        File dir = new File(".");
        File[] list = dir.listFiles();
        LinkedList<String> link = null;
        String curr = "";
        try {
            ObjectInputStream onj = new ObjectInputStream(
                    new FileInputStream(new File(listLoc)));
            link = (LinkedList<String>) onj.readObject();
            curr = link.get(link.size() - 1);
            onj.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        if (list != null) {
            for (File file : list) {
                if (file.isFile() && !file.getName().substring(
                        0, 1).equals(".")
                        && file.getName().contains(".")
                        && !file.getName().contains("proj3")) {
                    if (!new File(String.format(".gitlet/commits/%s/%s",
                            curr, file.getName())).exists()
                            && !Utils.join(stagLoc,
                            file.getName()).exists()) {
                        System.out.println(file.getName());
                    }
                }
            }
        }
        System.out.println();
    }

    /** Checks that no error are caused by calling reset.
     * @param  args list of arguments */
    private static void resetTester(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else if (!new File(String.format(
                ".gitlet/commits/%s", args[1])).exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } else if (trackTester() == -1) {
            System.out.println("There is an untracked file in the way;"
                    + " delete it or add it first.");
            System.exit(0);
        }
    }

    /** Checks out an arbitrary commit and changes the current branches head.
     * @param args list of arguments */
    @SuppressWarnings("unchecked")
    static void reset(String[] args) {
        resetTester(args);
        deleteTester();
        File[] resetFiles = new File(String.format(
                ".gitlet/commits/%s", args[1])).listFiles();
        if (resetFiles != null) {
            for (File fl : resetFiles) {
                Utils.writeContents(new File(fl.getName()),
                        Utils.readContentsAsString(fl));
            }
        }
        try {
            ObjectInputStream readerLink = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/list")));
            LinkedList<String> linked = (LinkedList<String>)
                    readerLink.readObject();
            if (linked.contains(args[1])) {
                while (!linked.get(linked.size() - 1).equals(args[1])) {
                    linked.removeLast();
                }
            } else {
                linked.add(args[1]);
            }
            readerLink.close();
            ObjectInputStream br = new ObjectInputStream(new FileInputStream(
                        new File(".gitlet/dataStructs/branch")));
            String branch = (String) br.readObject();
            br.close();
            ObjectOutputStream writerLink = new ObjectOutputStream(
                    new FileOutputStream(new File(
                            ".gitlet/dataStructs/list")));
            writerLink.writeObject(linked);
            writerLink.close();
            ObjectInputStream readMap = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/map")));
            HashMap<String, LinkedList<String>> mapp = (HashMap<String,
                    LinkedList<String>>) readMap.readObject();
            mapp.put(branch, linked);
            readMap.close();
            ObjectOutputStream writeMap = new ObjectOutputStream(
                    new FileOutputStream(new File(
                            ".gitlet/dataStructs/map")));
            writeMap.writeObject(mapp);
            writeMap.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        File[] fl = new File(".gitlet/staging").listFiles();
        if (fl != null) {
            for (File f : fl) {
                f.delete();
            }
        }

    }
    /** Checks that there are no errors in arguments.
     * @param args list of arguments */
    private static void mergeChecker(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else if (new File(stagLoc).listFiles().length != 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
    }
    /** There are uncommited changes, errors and exists. */
    private static void notEmptyError() {
        System.out.println("You have uncommitted changes.");
        System.exit(0);
    }
    /** Attempts to merge with unknown branch, errors. */
    private static void mergeUnknown() {
        System.out.println("A branch with that name does not exist.");
        System.exit(0);
    }
    /** Attempts to merge with itself, error. */
    private static void mergeItself() {
        System.out.println("Cannot merge a branch with itself.");
        System.exit(0);
    }
    /** Merges the current branch with the given branch.
     * @param args list of arguments */
    @SuppressWarnings("unchecked")
    static void merge(String[] args) {
        mergeChecker(args);
        try {
            ObjectInputStream readStage = new ObjectInputStream(
                    new FileInputStream(new File(markLoc)));
            HashSet<String> marks = (HashSet<String>) readStage.readObject();
            ObjectInputStream readMap = new ObjectInputStream(
                    new FileInputStream(new File(mapLoc)));
            HashMap<String, LinkedList<String>> map = (HashMap<String,
                    LinkedList<String>>) readMap.readObject();
            ObjectInputStream br = new ObjectInputStream(
                    new FileInputStream(new File(branchLoc)));
            String branch = (String) br.readObject();
            if (!marks.isEmpty()) {
                notEmptyError();
            } else if (!map.containsKey(args[1])) {
                mergeUnknown();
            } else if (args[1].equals(branch)) {
                mergeItself();
            } else if (trackTester() == -1) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it or add it first.");
                System.exit(0);
            }
            LinkedList<String> curr = map.get(branch);
            LinkedList<String> prev = map.get(args[1]);
            String splitter = splitPoint(args[1]);
            if (splitter.equals(curr.get(curr.size() - 1))) {
                curr = (LinkedList<String>) prev.clone();
                map.put(branch, curr);
                ObjectOutputStream writeList = new ObjectOutputStream(
                        new FileOutputStream(new File(listLoc)));
                writeList.writeObject(curr);
                ObjectOutputStream writeMap = new ObjectOutputStream(
                        new FileOutputStream(new File(mapLoc)));
                writeMap.writeObject(map); specialDelete();
                System.out.println("Current branch fast-forwarded.");
                System.exit(0);
            } else if (splitter.equals(prev.get(prev.size() - 1))) {
                System.out.println("Given branch is an ancestor"
                        + " of the current branch.");
                System.exit(0);
            }
            int conflict = 0;
            if (prevFiles(curr, prev, splitter) < 0) {
                conflict = -1;
            }
            if (currFiles(curr, prev, splitter) < 0) {
                conflict = -1;
            }
            String[] commit = {"commit",
                    String.format("Merged %s into %s.", args[1], branch)};
            commit(commit);
            if (conflict == -1) {
                System.out.println("Encountered a merge conflict.");
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
    }
    /** Deletes files in the current direcotry after fast-forwarding. */
    @SuppressWarnings("unchecked")
    private static void specialDelete() {
        File dir = new File(".");
        File[] list = dir.listFiles();
        LinkedList<String> link = null;
        String curr = "";
        try {
            ObjectInputStream onj = new ObjectInputStream(
                    new FileInputStream(new File(".gitlet/dataStructs/list")));
            link = (LinkedList<String>) onj.readObject();
            curr = link.get(link.size() - 1);
            onj.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        if (list != null) {
            for (File file : list) {
                if (file.isFile() && !file.getName().substring(0, 1).equals(".")
                        && file.getName().contains(".")
                        && !file.getName().contains("proj3")) {
                    if (!new File(String.format(".gitlet/commits/%s/%s",
                            curr, file.getName())).exists()) {
                        file.delete();
                    }
                }
            }
        }
    }
    /** Helper function for merge for current files.
     * @param curr current linkedlist
     * @param prev given linkedlist
     * @param splitter split point
     * @return negative integer if conflict exists*/
    private static int currFiles(LinkedList<String> curr,
                                  LinkedList<String> prev, String splitter) {
        int conflict = 0;
        File[] currFiles = new File(
                String.format(".gitlet/commits/%s",
                        curr.get(curr.size() - 1))).listFiles();
        for (File file: currFiles) {
            File prevCom = new File(
                    String.format(".gitlet/commits/%s/%s",
                            prev.get(prev.size() - 1), file.getName()));
            File splitCom = new File(
                    String.format(".gitlet/commits/%s/%s",
                            splitter, file.getName()));
            if (splitCom.exists()) {
                if (Utils.readContentsAsString(
                        file).equals(Utils.readContentsAsString(splitCom))
                        && !prevCom.exists()) {
                    if (new File(file.getName()).exists()) {
                        String[] rm = {"rm", file.getName()};
                        remove(rm);
                    }
                } else if (!prevCom.exists()) {
                    writeMerged(file, Utils.readContentsAsString(file), "");
                    String[] add = {"add", file.getName()};
                    Gitlet.add(add);
                    conflict = -1;
                }
            } else {
                if (!prevCom.exists()) {
                    continue;
                }
            }
        }
        return conflict;
    }
    /** Helper function for merge for given files.
     * @param curr current linkedlist
     * @param prev given linkedlist
     * @param splitter split point
     * @return negative integer if conflict exists */
    private static int prevFiles(LinkedList<String> curr,
                                 LinkedList<String> prev, String splitter) {
        int conflict = 0;
        File[] branchFiles = new File(String.format(".gitlet/commits/%s",
                prev.get(prev.size() - 1))).listFiles();
        for (File file: branchFiles) {
            File currCom = new File(String.format(".gitlet/commits/%s/%s",
                    curr.get(curr.size() - 1), file.getName()));
            File splitCom = new File(String.format(".gitlet/commits/%s/%s",
                    splitter, file.getName()));
            String givenFile = Utils.readContentsAsString(file);
            if (currCom.exists()) {
                String currentFile = Utils.readContentsAsString(currCom);
                if (splitCom.exists()) {
                    String splitFile = Utils.readContentsAsString(splitCom);
                    if (!splitFile.equals(givenFile)
                            && splitFile.equals(currentFile)) {
                        String[] checkout = {"checkout",
                            prev.get(prev.size() - 1), "--", file.getName()};
                        checkout(checkout);
                        String[] add = {"add", file.getName()};
                        Gitlet.add(add);
                    } else if (splitFile.equals(givenFile)
                            && !splitFile.equals(currentFile)
                            && !givenFile.equals(currentFile)) {
                        writeMerged(file, currentFile, givenFile);
                        String[] add = {"add", file.getName()}; add(add);
                        conflict = -1;
                    }
                } else {
                    if (!currentFile.equals(givenFile)) {
                        writeMerged(file, currentFile, givenFile);
                        String[] add = {"add", file.getName()};
                        Gitlet.add(add);
                        conflict = -1;
                    }
                }
            } else {
                if (!splitCom.exists()) {
                    String[] checkout = {"checkout",
                        prev.get(prev.size() - 1), "--", file.getName()};
                    Gitlet.checkout(checkout);
                    String[] add = {"add", file.getName()};
                    add(add);
                } else {
                    String splitFile = Utils.readContentsAsString(splitCom);
                    if (givenFile.equals(splitFile)) {
                        if (new File(file.getName()).exists()) {
                            new File(file.getName()).delete();
                        }
                    } else if (!givenFile.equals(splitFile)) {
                        writeMerged(file, "", givenFile);
                        String[] add = {"add", file.getName()};
                        add(add);
                        conflict = -1;
                    }
                }
            }
        }
        return conflict;
    }
    /** Helper function that wrties merged files.
     * @param file where to write
     * @param first file to write
     * @param second file to write */
    private static void writeMerged(File file, String first, String second) {
        Utils.writeContents(new File(file.getName()),
                "<<<<<<< HEAD\n" + first
                                + "=======\n" + second
                                + ">>>>>>>\n");
    }
    /** Creates a new branch with a given name.
     * @param args list of arguments passed */
    @SuppressWarnings("unchecked")
    static void branch(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
        }
        LinkedList<String> branchLink = null;
        try {
            ObjectInputStream readMap = new ObjectInputStream(
                    new FileInputStream(new File(".gitlet/dataStructs/map")));
            HashMap<String, LinkedList<String>> mapped =
                    (HashMap<String, LinkedList<String>>) readMap.readObject();
            readMap.close();
            if (mapped.containsKey(args[1])) {
                System.out.println("A branch with that name already exists.");
                System.exit(0);
            }
            ObjectInputStream firstLink = new ObjectInputStream(
                    new FileInputStream(new File(".gitlet/dataStructs/list")));
            LinkedList<String> linked = (LinkedList<String>)
                    firstLink.readObject();
            branchLink = (LinkedList<String>) linked.clone();
            firstLink.close();

            ObjectInputStream br = new ObjectInputStream(new FileInputStream(
                        new File(".gitlet/dataStructs/branch")));
            String branch = (String) br.readObject();
            br.close();

            mapped.put(branch, linked);
            mapped.put(args[1], branchLink);

            ObjectOutputStream writeMap =
                    new ObjectOutputStream(new FileOutputStream(
                            new File(".gitlet/dataStructs/map")));
            writeMap.writeObject(mapped);
            writeMap.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
    }
    /** Removed the branch with the given name, does not affect commits.
     * @param args list of arguments passed */
    @SuppressWarnings("unchecked")
    static void removeBranch(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        String branch = "";
        try {
            ObjectInputStream br = new ObjectInputStream(new FileInputStream(
                    new File(".gitlet/dataStructs/branch")));
            branch = (String) br.readObject();
            br.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        if (branch.equals(args[1])) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        try {
            ObjectInputStream readerMap
                    = new ObjectInputStream(new FileInputStream(
                    new File(".gitlet/dataStructs/map")));
            HashMap<String, LinkedList<String>> map
                    = (HashMap<String, LinkedList<String>>)
                    readerMap.readObject();
            readerMap.close();

            if (!map.containsKey(args[1])) {
                System.out.println("A branch with that name does not exist.");
                System.exit(0);
            } else {
                map.remove(args[1]);
            }

            ObjectOutputStream writeMap =
                    new ObjectOutputStream(new FileOutputStream(
                    new File(".gitlet/dataStructs/map")));
            writeMap.writeObject(map);
            writeMap.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }

    }
    /** Adds a remote name to remote repository.
     * @param args list of arguments passed */
    @SuppressWarnings("unchecked")
    static void addRemote(String[] args) {
        if (args.length != 3) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        try {
            ObjectInputStream obj = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/remote")));
            HashMap<String, String> rems = (HashMap<String, String>) obj.readObject();
            if (rems.containsKey(args[1])) {
                System.out.println("A remote with that name already exists.");

                System.exit(0);
            }
            rems.put(args[1], args[2]);
            obj.close();
            ObjectOutputStream rbj = new ObjectOutputStream(
                    new FileOutputStream(new File(
                            ".gitlet/dataStructs/remote")));
            rbj.writeObject(rems);
            rbj.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
    }
    /** Removes, gives remote name from remote repository.
     * @param args list of arguments passed */
    @SuppressWarnings("unchecked")
    static void removeRemote(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        try {
            ObjectInputStream obj = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/remote")));
            HashMap<String, String> rems = (HashMap<String, String>) obj.readObject();
            obj.close();
            if (!rems.containsKey(args[1])) {
                System.out.println("A remote with that name does not exist.");

                System.exit(0);
            }
            rems.remove(args[1]);
            ObjectOutputStream writeMap = new ObjectOutputStream(new FileOutputStream(new File(".gitlet/dataStructs/remote")));
            writeMap.writeObject(rems);
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
    }
    /** Pushed changes up to remote repository.
     * @param args list of arguments passed */
    @SuppressWarnings("unchecked")
    static void push(String[] args) {
        if (args.length != 3) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        try {
            ObjectInputStream readLocal = new ObjectInputStream(
                    new FileInputStream(new File(".gitlet/dataStructs/list")));
            LinkedList<String> locList = (LinkedList<String>)
                    readLocal.readObject();
            //String path = Utils.readContentsAsString(new File(".gitlet/remote.txt"));
            ObjectInputStream remote = new ObjectInputStream(
                    new FileInputStream(new File(".gitlet/dataStructs/remote")));
            HashMap<String, String> remoteMap = (HashMap<String, String>) remote.readObject();
            if (!new File(remoteMap.get(args[1])).exists()) {
                System.out.println("Remote directory not found.");

                System.exit(0);
            }
            ObjectInputStream rMap = new ObjectInputStream(
                    new FileInputStream(new File(remoteMap.get(args[1]) + "/dataStructs/map")));
            HashMap<String, LinkedList<String>> map
                    = (HashMap<String,
                    LinkedList<String>>) rMap.readObject();
            LinkedList<String> rmList = map.get(args[2]);
            if (!locList.contains(rmList.get(rmList.size() - 1))) {
                System.out.println("Please pull down"
                        + " remote changes before pushing.");
                System.exit(0);
            }

            int index = locList.indexOf(rmList.get(rmList.size() - 1));
            for (int i = index + 1; i < locList.size(); i++) {
                rmList.add(locList.get(i));
                File rewritten = new File(map.get(args[1])
                        + String.format("/commits/%s", locList.get(i)));
                for (File f : new File(String.format(".gitlet/commits/%s",
                        locList.get(i))).listFiles()) {
                    File fl = new File(rewritten.getPath() + f.getName());
                    Utils.writeContents(fl, Utils.readContentsAsString(f));
                }
            }
            map.put(args[2], rmList);
            ObjectOutputStream writeRemote = new ObjectOutputStream(
                    new FileOutputStream(
                            new File(map.get(args[1]) + "/dataStructs/map")));
            writeRemote.writeObject(map);
            writeRemote.close();

        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
    }
    /** Fetches given branch into local repository.
     * @param args list of arguments passed */
    @SuppressWarnings("unchecked")
    static void fetch(String[] args) {
        if (args.length != 3) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        try {
            ObjectInputStream remote = new ObjectInputStream(
                    new FileInputStream(new File(".gitlet/dataStructs/remote")));
            HashMap<String, String> remoteMap = (HashMap<String, String>) remote.readObject();
            if (!new File(remoteMap.get(args[1])).exists()) {
                System.out.println("Remote directory not found.");
                System.exit(0);
            }
            ObjectInputStream reader = new ObjectInputStream(
                    new FileInputStream(new File(remoteMap.get(args[1])
                            + "/dataStructs/map")));
            HashMap<String, LinkedList<String>> map =
                    (HashMap<String, LinkedList<String>>) reader.readObject();
            if (!map.containsKey(args[2])) {
                System.out.println("The remote does not have that branch.");
                System.exit(0);
            }
            ObjectInputStream locReader = new ObjectInputStream(
                    new FileInputStream(new File(".gitlet/dataStructs/map")));
            HashMap<String, LinkedList<String>> locMap = (HashMap<String,
                            LinkedList<String>>) locReader.readObject();
            locMap.put(String.format("%s/%s", args[1], args[2]), map.get(args[2]));
            locReader.close();
            LinkedList<String> branched = map.get(args[2]);
            for (int i = branched.size() - 1; i > 0; i--) {
                File[] fls = new File(String.format("%s/commits/%s", remoteMap.get(args[1]), branched.get(i))).listFiles();
                //File [] files = new File(remoteMap.get(args[1]) + "/commits/" + branched.get(i)).listFiles();
                    for (File file: fls) {
                        if (!new File(String.format(".gitlet/remotedComs/%s", branched.get(i))).exists()) {
                            File rec = new File(String.format(".gitlet/remotedComs/%s", branched.get(i)));
                            rec.mkdir();
                        }
                        File str = Utils.join(new File(".gitlet"), "remotedComs");
                        File str2 = Utils.join(str, branched.get(i));
                        File str3 = Utils.join(str2, file.getName());
                        File f = new File(
                                String.format(".gitlet/commits/%s/%s", branched.get(i), file.getName()));
                        Utils.writeContents(f, Utils.readContentsAsString(file));
                }
            }
            ObjectOutputStream locWriter = new ObjectOutputStream(
                    new FileOutputStream(new File(".gitlet/dataStructs/map")));
            locWriter.writeObject(locMap);
            locWriter.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }

    }
    /** Substitute for old fetch.
     * @param args list of arguments. */
    @SuppressWarnings("unchecked")
    static void newFetch(String[] args) {
        if (args.length != 3) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        try {
            ObjectInputStream remoteName = new ObjectInputStream(new FileInputStream(new File(".gitlet/dataStructs/remote")));
            HashMap<String, String> remoteAddress = (HashMap<String, String>) remoteName.readObject();
            remoteName.close();
            if (!new File(remoteAddress.get(args[1])).exists()) {
                System.out.println("Remote directory not found.");
                System.exit(0);
            }

            String loc = remoteAddress.get(args[1]);

            ObjectInputStream remoteMap = new ObjectInputStream(new FileInputStream(new File(loc + "/dataStructs/map")));
            HashMap<String, LinkedList<String>> remoteBranch = (HashMap<String, LinkedList<String>>) remoteMap.readObject();
            LinkedList<String> branch = remoteBranch.get(args[2]);

            ObjectInputStream localMap = new ObjectInputStream(new FileInputStream(new File(".gitlet/dataStructs/map")));
            HashMap<String, LinkedList<String>> locMap = (HashMap<String, LinkedList<String>>) localMap.readObject();
            localMap.close();
            ObjectInputStream br = new ObjectInputStream(new FileInputStream(new File(".gitlet/dataStructs/branch")));
            String currBranch = (String) br.readObject();
            br.close();
            LinkedList<String> currList = locMap.get(currBranch);
            if (!remoteBranch.containsKey(args[2])) {
                System.out.println("The remote does not have that branch.");
                System.exit(0);
            }
            remoteMap.close();
            ObjectInputStream mapped2 = new ObjectInputStream(
                    new FileInputStream(".gitlet/dataStructs/connector"));
            HashMap<String, Commit> map2 = (HashMap<String, Commit>) mapped2.readObject();
            mapped2.close();

            ObjectInputStream mapped3 = new ObjectInputStream(
                    new FileInputStream(loc + "/dataStructs/connector"));
            HashMap<String, Commit> map3 = (HashMap<String, Commit>) mapped2.readObject();
            mapped3.close();
            for (int i = branch.size() - 1; i >= 0; i--) {
                File[] files = new File(loc + "/commits/" + branch.get(i)).listFiles();
                map2.put(branch.get(i), map3.get(branch.get(i)));
                for (File f : files) {
                    Utils.writeContents(new File(".gitlet/commits/" + branch.get(i) + f.getName()),
                            Utils.readContentsAsString(f));
                }
            }

            ObjectOutputStream fin = new ObjectOutputStream(new FileOutputStream(
                    new File(".gitlet/dataStructs/connector")));
            fin.writeObject(map2);
            fin.close();
            branch.add(currList.get(currList.size() - 1));
            locMap.put(args[1].concat("/").concat(args[2]), branch);
            ObjectOutputStream locWriter = new ObjectOutputStream(
                    new FileOutputStream(new File(".gitlet/dataStructs/map")));
            locWriter.writeObject(locMap);
            locWriter.close();

        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
    }

    /** Pulls from remote repository and then merges with current branch.
     * @param args passed command*/
    static void pull(String[] args) {
        if (args.length != 3) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        String[] newArgs = {"fetch", args[1], args[2]};
        fetch(newArgs);
        String[] forMerge = {"merge", args[2]};
        merge(forMerge);
    }

    /** Special print for merged Commits.
     * @param commit Merged commit */
    private static void mergePrinter(Commit commit) {
        System.out.println("===");
        System.out.println(String.format("commit %s", commit.encrypt()));
        System.out.println("Merge: "
                + commit.getMergeParent().get(0)[1].substring(0, 7) + " "
                + commit.getMergeParent().get(1)[1].substring(0, 7));
        System.out.println("Date: " + commit.getTime());
        System.out.println("Merged "
                + commit.getMergeParent().get(1)[0] + " into "
                + commit.getMergeParent().get(0)[0] + ".");
        System.out.println();
    }

    /** Regular print for a commit.
     * @param commit given commit */
    private static void printer(Commit commit) {
        System.out.println("===");
        System.out.println(String.format("commit %s", commit.encrypt()));
        System.out.println("Date: " + commit.getTime());
        System.out.println(commit.getMessage());
        System.out.println();
    }

    /** Gives time in current timezone.
     * @param date given date
     * @return formatted date */
    static String timefy(Date date) {
        DateFormat dateF2 = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        dateF2.setTimeZone(TimeZone.getTimeZone("PST"));
        return dateF2.format(date);
    }

    /** Checks if all the files are tracked in the working directory.
     * Returns 1 if everything is tracked, -1 otherwise. */
    @SuppressWarnings("unchecked")
    private static int trackTester() {
        File dir = new File(".");
        File[] list = dir.listFiles();
        LinkedList<String> link = null;
        String curr = "";
        try {
            ObjectInputStream onj = new ObjectInputStream(
                    new FileInputStream(new File(".gitlet/dataStructs/list")));
            link = (LinkedList<String>) onj.readObject();
            curr = link.get(link.size() - 1);
            onj.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        if (list != null) {
            for (File file : list) {
                if (file.isFile() && !file.getName().substring(0, 1).equals(".")
                        && file.getName().contains(".")
                        && !file.getName().contains("proj3")) {
                    if (!new File(String.format(".gitlet/commits/%s/%s",
                            curr, file.getName())).exists()
                            && !new File(String.format(".gitlet/staging/%s",
                            file.getName())).exists()) {
                        return -1;
                    }
                }
            }
        }
        return 1;
    }
    /** Deletes all the files that are tracked in the current commit
     * from the working directory. */
    @SuppressWarnings("unchecked")
    private static void deleteTester() {
        File dir = new File(".");
        File[] list = dir.listFiles();
        LinkedList<String> link = null;
        String curr = "";
        try {
            ObjectInputStream onj = new ObjectInputStream(new FileInputStream(
                    new File(".gitlet/dataStructs/list")));
            link = (LinkedList<String>) onj.readObject();
            curr = link.get(link.size() - 1);
            onj.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        if (list != null) {
            for (File file : list) {
                if (file.isFile() && !file.getName().substring(0, 1).equals(".")
                        && file.getName().contains(".")
                        && !file.getName().contains("proj3")) {
                    if (new File(String.format(".gitlet/commits/%s/%s",
                            curr, file.getName())).exists()
                            || new File(String.format(
                                    ".gitlet/staging/%s",
                            file.getName())).exists()) {
                        file.delete();
                    }
                }
            }
        }
    }

    /** Returns the Commit that is a latest common ancestor, a.k.a. split point.
     * @param br branch to find the split for.  */
    @SuppressWarnings("unchecked")
    private static String splitPoint(String br) {
        try {
            ObjectInputStream readMap = new ObjectInputStream(
                    new FileInputStream(new File(".gitlet/dataStructs/map")));
            HashMap<String, LinkedList<String>> map = (HashMap<String,
                    LinkedList<String>>) readMap.readObject();
            readMap.close();
            String branch = "";
            try {
                ObjectInputStream bra = new ObjectInputStream(
                        new FileInputStream(new File(
                                ".gitlet/dataStructs/branch")));
                branch = (String) bra.readObject();
                bra.close();
            } catch (IOException | ClassNotFoundException ex) {
                ex.fillInStackTrace();
            }
            LinkedList<String> curr = map.get(branch);
            LinkedList<String> merge = map.get(br);
            File[] allCommits = new File(
                    ".gitlet/commits").listFiles();
            for (File file: allCommits) {
                if (curr.indexOf(file.getName()) + 1
                        >= curr.size() && curr.contains(file.getName())
                        && merge.contains(file.getName())) {
                    return file.getName();
                } else if (merge.indexOf(file.getName()) + 1
                        >= merge.size() && curr.contains(file.getName())
                        && merge.contains(file.getName())) {
                    return file.getName();
                } else if (curr.contains(file.getName())
                        && merge.contains(file.getName())
                    && !curr.get(curr.indexOf(file.getName()) + 1).equals(
                            merge.get(merge.indexOf(file.getName()) + 1))) {
                    return file.getName();
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        return null;
    }

    /** HashSet for all commits ever made. */
    private static HashSet<Commit> _hashedCommits = new HashSet<>();
    /** LinkedList containing current sequence of commits. */
    private static LinkedList<String> _linkedList = new LinkedList<>();
    /** HashMap connecting SHA1 value to Commit object. */
    private static HashMap<String, Commit> _connector = new HashMap<>();
    /** HashMap that connects branch name to sequence of commits. */
    private static HashMap<String,
            LinkedList<String>> _allConnections = new HashMap<>();
    /** HashSet containing all files marked to be removed. */
    private static HashSet<String> _marked = new HashSet<>();
    /** Path to remote repository if any. */
    private static File remote;
    /** Location of map data structure. */
    private static String mapLoc = ".gitlet/dataStructs/map";
    /** Location of list data structure. */
    private static String listLoc = ".gitlet/dataStructs/list";
    /** Location of stagining area. */
    private static String stagLoc = ".gitlet/staging";
    /** Location of marked files. */
    private static String markLoc = ".gitlet/dataStructs/mark";
    /** Location of the current branch. */
    private static String branchLoc = ".gitlet/dataStructs/branch";
    /** HashSet that would presumably store all remote names. */
    private static HashSet<String> remotes = new HashSet<>();

    private static HashMap<String, String> _remote = new HashMap<>();

}
