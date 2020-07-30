package gitlet;
import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
/** Makes a new commit in the current repository.
 * @author sm3*/
public class Commit implements Serializable {

    /** Initialize a new commit with.
     * @param message passed message
     * @param time creation time
     * @param branch this commit belongs to*/
    public Commit(String message, String time, String branch) {
        _message = message;
        _time = time;
        _branch = branch;
    }

    /** Return SHA1 for this file.
     * @return SHA1*/
    String encrypt() {
        return Utils.sha1(_time + _message);
    }

    /** Returns time of this commit.
     * @return time */
    String getTime() {
        return _time;
    }

    /** Returns message of this commit.
     * @return message */
    String getMessage() {
        return _message;
    }

    /** Returns parent commit.
     * @return identification of the commit */
    @SuppressWarnings("unchecked")
    String getParent() {
        String parent = "";
        try {
            ObjectInputStream reader = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/list")));
            LinkedList<String> list = (LinkedList<String>) reader.readObject();
            if (list.size() == 0) {
                return null;
            }
            parent = list.get(list.size() - 1);
            reader.close();
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("No Parent for this Commit");
        }
        return parent;
    }

    /** Returns a list of files in the current commit.
     * @param sha identification of a commit
     * @return list of files in the commit. */
    File[] parentFiles(String sha) {

        return new File(String.format(".gitlet/commits/%s", sha)).listFiles();

    }

    /** Serializes and makes and empty copy for initial commit. */
    void serialize() {

        File commit = new File(".gitlet/commits");
        File staged = new File(".gitlet/staging");

        File[] toBeAdded = staged.listFiles();
        File toBeSaved = Utils.join(commit.getPath(), encrypt());
        toBeSaved.mkdir();

        for (File f : toBeAdded) {
            String k = Utils.readContentsAsString(f);
            Utils.writeContents((Utils.join(toBeSaved, f.getName())), k);
            f.delete();
        }
    }
    /** Puts multiple parents into a HashMap.
     * @param merge list of strings with names
     *              and commits id's */
    void mergeParent(String[] merge) {
        String[] f = {merge[0], merge[1]};
        sortedParents.put(0, f);
        String[] f2 = {merge[2], merge[3]};
        sortedParents.put(1, f2);

    }
    /** Returns HashMap of multiple parents.
     * @return map of parents */

    HashMap<Integer, String[]> getMergeParent() {
        return sortedParents;
    }
    /** Serializes, copies and adds files
     * to the directory of this commit.
     * @return negative number if encountered an error*/
    @SuppressWarnings("unchecked")
    int serialize2() {

        File commit = new File(".gitlet/commits");
        File staged = new File(".gitlet/staging");

        File[] toBeAdded = staged.listFiles();
        HashSet<String> marks = null;
        try {
            ObjectInputStream reader = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/mark")));
            marks = (HashSet<String>) reader.readObject();
            reader.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        if (toBeAdded == null || toBeAdded.length == 0 && marks.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return -1;

        } else {
            File toBeSaved = Utils.join(commit.getPath(), encrypt());
            toBeSaved.mkdir();
            if (getParent() != null && parentFiles(getParent()) != null) {
                for (File f: parentFiles(getParent())) {
                    String w = Utils.readContentsAsString(f);
                    String l = f.getName();
                    if (marks.contains(l)) {
                        marks.remove(l);
                        try {
                            ObjectOutputStream obj = new ObjectOutputStream(
                                    new FileOutputStream(
                                    new File(".gitlet/dataStructs/mark")));
                            obj.writeObject(marks);
                        } catch (IOException ex) {
                            ex.fillInStackTrace();
                        }
                        continue;
                    }
                    File saved = Utils.join(toBeSaved, l);
                    Utils.writeContents(saved, w);
                }
            }
            for (File f : toBeAdded) {
                String k = Utils.readContentsAsString(f);
                Utils.writeContents((Utils.join(toBeSaved, f.getName())), k);
                f.delete();
            }
        }

        return 1;
    }
    /** HashMap containing multiple parents, only for
     * commits created as a result of a merge. */
    private HashMap<Integer, String[]> sortedParents
            = new HashMap<Integer, String[]>();
    /** Branch this Commit belongs to, if one parent. */
    private String _branch;
    /** Message this commit was created with. */
    private String _message;
    /** Time at which this commit was created. */
    private String _time;
}
