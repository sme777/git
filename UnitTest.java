package gitlet;
import ucb.junit.textui;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;


/** The suite of all JUnit tests for the gitlet package.
 *  @author
 */
public class UnitTest implements Serializable {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void initTest() {
        Gitlet git = new Gitlet();

        assertTrue(new File(".gitlet").exists());
    }

    @Test
    public void addNCommitTest() {
        String[] strings = {"add", "woof.txt"};

        Gitlet.add(strings);

        assertTrue(new File(".gitlet/staging/woof.txt").exists());
        String[] commit = {"commit", "Updated to Woof"};
        Commit c = new Commit(commit[1], Gitlet.timefy(new Date()), "master");
        Gitlet.commit(commit);
        String  c1 = c.encrypt();
        System.out.print(String.format("hello %s", c1));

        assertTrue(new File(String.format(".gitlet/commits/%s/woof.txt",
                c1.substring(0, 5))).exists());

        String[] strings2 = {"add", "auf.txt"};
        Gitlet.add(strings2);

        String[] commit2 = {"commit", "Updated to Auf"};
        Gitlet.commit(commit2);

        String[] strings3 = {"add", "no.txt"};
        String[] string4 = {"add", "oops.txt"};
        Gitlet.add(string4);
        Gitlet.add(strings3);
        String[] com = {"commit", "Major test"};
        Gitlet.commit(com);


    }


    @Test
    public void logChecker() {

        String[] s1 = {"add", "wug.txt"};
        File f1 = new File("testing/src/wug.txt");
        boolean e = f1.exists();

        Gitlet.add(s1);
        String[] c1 = {"commit", "add wug"};
        Gitlet.commit(c1);

        String[] j = {"log"};
        Gitlet.log(j);
    }

    @Test
    public void dumbTests() {
        HashMap<String, String> k = new HashMap<>();
        assertTrue(k.size() == 0);
    }

    @Test
    public void checkoutTest() {

        String[] newWug = {"add", "wug.txt"};
        Gitlet.add(newWug);
        String[] newCo = {"commit", "added wug"};
        Gitlet.commit(newCo);
        try {
            BufferedWriter change = new BufferedWriter(
                    new FileWriter(new File("wug.txt")));
            change.write("Sample bullshit");
            change.close();
            String[] test = {"checkout", "wug.txt"};
            Gitlet.checkout(test);
        } catch (IOException ex) {
            System.out.println("wtf");
        }

    }

    @Test
    public void commitCheck() {

        String[] a = {"add", "wug.txt"};
        Gitlet.add(a);
        String[] f = {"commit", "added Wug"};
        Gitlet.commit(f);
    }

    @Test
    public void globalTest() {
        String[] s1 = {"add", "gitlet/auf.txt"};
        Gitlet.add(s1);

        int index = s1[1].lastIndexOf("/");
        String f = s1[1].substring(index + 1);
        String[] s2 = {"add", "gitlet/oops.txt"};
        Gitlet.add(s2);

        String[] c1 = {"commit", "first test"};
        Gitlet.commit(c1);

        String[] s3 = {"add", "gitlet/no.txt"};
        Gitlet.add(s1);
        String[] s4 = {"add", "gitlet/oops.txt"};
        Gitlet.add(s2);

        String[] c2 = {"commit", "second test"};
        Gitlet.commit(c2);
        String[] gl = {"global-log"};
        Gitlet.globalLog(gl);

    }

    @Test
    public void removeTest() {
        String[] str = {"add", "gitlet/auf.txt"};
        Gitlet.add(str);

        String[] rm = {"rm", "auf.txt"};
        Gitlet.remove(rm);

        assertTrue(!new File(".gitlet/staging/auf.txt").exists());
    }

    @Test
    public void findTest() {

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        String[] find = {"find", "third test"};
        Gitlet.find(find);

        assertEquals("Found no commit with that message.",
                outContent.toString());


    }

    @Test
    public void addChecker() {
        String[] add = {"add", "gitlet/auf.txt"};

        Utils.writeContents(new File("gitlet/auf.txt"), "Hi there");
        Gitlet.add(add);
        Utils.writeContents(new File("gitlet/auf.txt"), "Hello there");
        Gitlet.add(add);

        String[] com = {"commit", "added"};
        Gitlet.commit(com);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void newCommitChecker() {
        Gitlet g = new Gitlet();

        String[] c = {"add", "gitlet/auf.txt"};
        String[] c2 = {"add", "gitlet/oops.txt"};
        String[] c3 = {"add", "gitlet/no.txt"};
        String[] com = {"commit", "first try"};
        Gitlet.add(c);
        Gitlet.add(c2);
        Gitlet.commit(com);
        Gitlet.add(c3);
        String[] com2 = {"commit", "second try"};
        Gitlet.commit(com2);
        Utils.writeContents(new File("gitlet/auf.txt"), "I'm not confused");

        Gitlet.add(c);
        String[] com3 = {"commit", "third try"};
        Gitlet.commit(com3);
        try {
            ObjectInputStream obj = new ObjectInputStream(
                    new FileInputStream(
                            ".gitlet/dataStructs/list"));
            LinkedList<String> bj = (LinkedList<String>) obj.readObject();
            assertEquals(4, bj.size());

            File path = Utils.join(".gitlet/commits",
                    bj.get(bj.size() - 1).substring(0, 5));
            assertTrue(path.exists());
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }


    }

    @Test
    public void anotherCommit() {
        String[] add = {"add", "wug.txt"};
        Gitlet.add(add);
        String[] commit = {"commit", "new commit"};
        Gitlet.commit(commit);
        String[] log = {"log"};
        Gitlet.log(log);
    }

    @Test
    public void remove2Test() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "wug.txt"};
        Gitlet.add(add);
        String[] commit = {"commit", "new commit"};
        Gitlet.commit(commit);
        String[] add3 = {"add", "test1.txt"};
        Gitlet.add(add3);
        String[] com = {"commit", "test 1"};
        Gitlet.commit(com);
        String[] rm = {"rm", "test1.txt"};
        Gitlet.remove(rm);
        String[] add2 = {"add", "woof.txt"};
        Gitlet.add(add2);
        String[] com2 = {"commit", "test 2"};
        Gitlet.commit(com2);

        Utils.writeContents(new File("test1.txt"), "Test 2");
        String[] add4 = {"add", "test1.txt"};
        Gitlet.add(add4);
        String[] com4 = {"commit", "final test"};
        Gitlet.commit(com4);


    }

    @Test
    @SuppressWarnings("unchecked")
    public void branchTest() {
        String[] branch = {"branch", "branch1"};
        Gitlet.branch(branch);

        String[] branch2 = {"branch", "branch2"};
        Gitlet.branch(branch2);

        try {
            ObjectInputStream br = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/map")));
            HashMap<String, LinkedList<String>> map = (HashMap<String,
                    LinkedList<String>>) br.readObject();
            br.close();
            map.remove(null);
            assertEquals(map.keySet().size(), 3);


        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void removeBranchTest() {

        String[] master = {"branch", "branch2"};
        Gitlet.branch(master);
        try {
            ObjectInputStream br = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/map")));
            HashMap<String, LinkedList<String>> map = (HashMap<String,
                    LinkedList<String>>) br.readObject();
            br.close();
            assertEquals(map.keySet().size(), 3);

        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }

        String[] s2 = {"rm-branch", "branch2"};
        Gitlet.removeBranch(s2);

        try {
            ObjectInputStream br = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/map")));
            HashMap<String, LinkedList<String>> map = (HashMap<String,
                    LinkedList<String>>) br.readObject();
            br.close();
            assertEquals(map.keySet().size(), 2);

        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
    }
    @Test
    @SuppressWarnings("unchecked")
    public void fileChecker() {
        File dir = new File(".");
        File[] list = dir.listFiles();
        LinkedList<String> link = null;
        String curr = "";
        try {
            ObjectInputStream onj = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/list")));
            link = (LinkedList<String>) onj.readObject();
            curr = link.get(link.size() - 1);
            onj.close();
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("whoops");
        }
        if (list != null) {
            for (File file : list) {
                if (file.isFile() && !file.getName().substring(0, 1).equals(".")
                        && file.getName().contains(".")
                        && !file.getName().contains("proj3")) {

                    if (!new File(String.format(".gitlet/commits/%s/%s",
                            curr.substring(0, 5), file.getName())).exists()) {
                        System.out.println(file.getName());
                    }
                }
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkoutBranch() {

        String[] add = {"add", "gitlet/auf.txt"};
        Gitlet.add(add);

        HashMap<String, LinkedList<String>> mapped = null;
        try {
            ObjectInputStream map = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/map")));
            mapped = (HashMap<String, LinkedList<String>>) map.readObject();
            map.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }

        String[] co = {"checkout", "branch1"};
        Gitlet.checkout(co);

        String[] add2 = {"add", "gitlet/no.txt"};
        Gitlet.add(add2);

        String[] com2 = {"commit", "branch tetser"};
        Gitlet.commit(com2);

        try {
            ObjectInputStream map = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/map")));
            mapped = (HashMap<String, LinkedList<String>>) map.readObject();
            map.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
    }

    @Test
    public void statusChecker() {
        String[] add2 = {"add", "gitlet/auf.txt"};
        String[] add3 = {"add", "gitlet/oops.txt"};
        Gitlet.add(add2);
        Gitlet.add(add3);
        String[] com = {"commit", "lets see"};
        Gitlet.commit(com);
        String[] rm = {"rm", "gitlet/auf.txt"};
        Gitlet.remove(rm);
        String[] st = {"status"};
        Gitlet.status(st);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void resetTest() {

        HashMap<String, LinkedList<String>> mapped = null;
        HashMap<String, LinkedList<String>> mapped2 = null;
        try {
            ObjectInputStream map = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/map")));
            mapped = (HashMap<String, LinkedList<String>>) map.readObject();
            map.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        String[] reset = {"reset", "1eb073e304372b49852b6645d72ab234a8958917"};
        Gitlet.reset(reset);

        String[] reset2 = {"reset", "2d6901b328484b445fc067180ca106e39c853328"};
        Gitlet.reset(reset2);

        try {
            ObjectInputStream map2 = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/map")));
            mapped2 = (HashMap<String, LinkedList<String>>) map2.readObject();
            map2.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }

        System.out.println(mapped.get("master").size());

    }

    @Test
    public void file2Checker() {
        File fl = new File("bye.txt");
        assertFalse(fl.exists());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void mergeTest() {

        String[] add = {"add", "auf.txt"};
        Gitlet.add(add);
        String[] commit = {"commit", "try1"};
        Gitlet.commit(commit);
        try {
            ObjectInputStream link = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/list")));
            LinkedList<String> list = (LinkedList<String>) link.readObject();
            link.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
    }

    @Test
    public void log() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "auf.txt"};
        Gitlet.add(add);
        String[] com = {"commit", "test"};
        Gitlet.commit(com);
        String[] log = {"log"};
        Gitlet.log(log);
    }

    @Test
    public void addRemove() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "wug.txt"};
        String[] add2 = {"add", "woof.txt"};
        Gitlet.add(add);
        Gitlet.add(add2);
        String[] rm = {"rm", "wug.txt"};
        Gitlet.remove(rm);
        String[] s = {"status"};
        Gitlet.status(s);

    }

    @Test
    public void addStatus() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "wug.txt"};
        String[] add2 = {"add", "woof.txt"};
        Gitlet.add(add);
        Gitlet.add(add2);
        String[] com = {"commit", "Two files"};
        Gitlet.commit(com);
        String[] rm = {"rm", "wug.txt"};
        Gitlet.remove(rm);
        Utils.writeContents(new File("wug.txt"), "Woofy");
        String[] add3 = {"add", "wug.txt"};
        Gitlet.add(add3);
        String[] s = {"status"};
        Gitlet.status(s);
    }

    @Test
    public void emptyStatus() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "wug.txt"};
        String[] add2 = {"add", "woof.txt"};
        Gitlet.add(add);
        Gitlet.add(add2);
        String[] com = {"commit", ""};
        Gitlet.commit(com);
    }

    @Test
    public void commitAfter() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "wug.txt"};
        String[] add2 = {"add", "woof.txt"};
        Gitlet.add(add);
        Gitlet.add(add2);
        String[] com = {"commit", "first"};
        Gitlet.commit(com);
        String[] s = {"status"};
        Gitlet.status(s);
        String[] rm = {"rm", "wug.txt"};
        Gitlet.remove(rm);
        String[] com2 = {"commit", "removed"};
        Gitlet.commit(com2);
        Gitlet.status(s);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkout1() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "wug.txt"};
        String[] add2 = {"add2", "notwug.txt"};
        Gitlet.add(add);
        Gitlet.add(add2);
        String[] com = {"commit", "version 1 of wug.txt"};
        Gitlet.commit(com);
        Utils.writeContents(new File("wug.txt"),
                Utils.readContentsAsString(new File("notwug.txt")));
        Gitlet.add(add);
        String[] com2 = {"commit", "version 2 of wug.txt"};
        Gitlet.commit(com2);
        String[] log = {"log"};
        Gitlet.log(log);
        String c = "";
        try {
            ObjectInputStream obj = new ObjectInputStream(
                    new FileInputStream(new File(".gitlet/dataStructs/list")));
            LinkedList<String> l = (LinkedList<String>) obj.readObject();
            c = l.get(1);
            obj.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
        String[] co = {"checkout", c, "--", "wug.txt"};
        Gitlet.checkout(co);
        String[] st = {"status"};
        Gitlet.status(st);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkout2() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "wug.txt"};
        String[] add2 = {"add2", "notwug.txt"};
        Gitlet.add(add);
        String[] com = {"commit", "version 1 of wug.txt"};
        Gitlet.commit(com);
        Utils.writeContents(new File("wug.txt"),
                Utils.readContentsAsString(new File("notwug.txt")));
        Gitlet.add(add);
        String[] com2 = {"commit", "version 2 of wug.txt"};
        Gitlet.commit(com2);
        String[] log = {"log"};
        Gitlet.log(log);
        String c = "";
        try {
            ObjectInputStream obj = new ObjectInputStream(
                    new FileInputStream(new File(".gitlet/dataStructs/list")));
            LinkedList<String> l = (LinkedList<String>) obj.readObject();
            c = l.get(2);
            obj.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }

        String[] co1 = {"checkout", c, "--", "warg.txt"};
        Gitlet.checkout(co1);
        String[] co2 = {
            "checkout",
            "5d0bc169a1737e955f9cb26b9e7aa21e4afd4d12",
            "--",
            "wug.txt"};
        Gitlet.checkout(co2);
        String[] co3 = {"checkout", c, "++", "wug.txt"};
        Gitlet.checkout(co3);
        String[] co4 = {"checkout", "foobar"};
        Gitlet.checkout(co4);
        String[] co5 = {"checkout", "master"};
        Gitlet.checkout(co5);

    }

    @Test
    public void branchTest2() {
        Gitlet g = new Gitlet();
        String[] br = {"branch", "other"};
        Gitlet.branch(br);

        String[] add = {"add", "testing/src/wug.txt"};
        Gitlet.add(add);
        String[] add2 = {"add", "testing/src/notwug.txt"};
        Gitlet.add(add2);

        String[] c = {"commit", "Main two files"};
        Gitlet.commit(c);

        String[] co = {"checkout", "other"};
        Gitlet.checkout(co);

        Utils.writeContents(new File("wug.txt"),
                Utils.readContentsAsString(
                new File("testing/src/notwug.txt")));
        String[] add3 = {"add", "wug.txt"};
        Gitlet.add(add3);
        String[] c2 = {"commit", "Alternative file"};
        Gitlet.commit(c2);
        String[] m = {"checkout", "master"};
        Gitlet.checkout(m);

    }

    @Test
    public void adRem() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "wug.txt"};
        Gitlet.add(add);
        String[] add2 = {"add", "notwug.txt"};
        Gitlet.add(add2);
        String[] r = {"rm", "wug.txt"};
        Gitlet.remove(r);
        String[] s = {"status"};
        Gitlet.status(s);
    }

    @Test
    public void mergeTest1() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "wug.txt"};
        Gitlet.add(add);
        String[] add2 = {"add", "notwug.txt"};
        Gitlet.add(add2);
        String[] c = {"commit", "Two files"};
        Gitlet.commit(c);
        String[] br = {"branch", "other"};
        Gitlet.branch(br);
        File wug2 = new File("notwug.txt");
        Utils.writeContents(wug2, "wug2");
        String[] add3 = {"add", "wug.txt"};
        Gitlet.add(add3);
        String[] rm = {"rm", "notwug.txt"};
        Gitlet.remove(rm);

        String[] cm = {"commit", "Add h.txt and remove g.txt"};

        Gitlet.commit(cm);


        String[] co = {"checkout", "other"};
        Gitlet.checkout(co);
        String[] rm2 = {"remove", "wug.txt"};
        Gitlet.remove(rm2);
        File wug3 = new File("wug3.txt");
        Utils.writeContents(wug3, "wug3");
        String[] add4 = {"add", "wug3.txt"};
        Gitlet.add(add4);
        String[] com = {"commit", "Add k.txt and remove f.txt"};
        Gitlet.commit(com);

        String[] co2 = {"checkout", "master"};
        Gitlet.checkout(co2);

        String[] m = {"merge", "other"};
        Gitlet.merge(m);
        String[] log = {"log"};
        Gitlet.log(log);
        String[] s = {"status"};
        Gitlet.status(s);
    }

    @Test
    public void logT() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "wug.txt"};
        Gitlet.add(add);
        String[] c1 = {"commit", "version 1 of wug.txt"};
        Gitlet.commit(c1);
        Utils.writeContents(new File("wug.txt"),
                Utils.readContentsAsString(new File("testing/src/notwug.txt")));
        String[] add2 = {"add", "wug.txt"};
        Gitlet.add(add2);
        String[] c2 = {"commit", "version2 of wug.txt"};
        Gitlet.commit(c2);
        String[] l = {"log"};
        Gitlet.log(l);

    }

    @Test
    public void merge2() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "f.txt"};
        Gitlet.add(add);
        String[] add2 = {"add", "f.txt"};
        Gitlet.add(add2);
        String[] c = {"commit", "Two files"};
        Gitlet.commit(c);
        String[] br = {"branch", "other"};
        Gitlet.branch(br);
        Utils.writeContents(new File("h.txt"),
                Utils.readContentsAsString(new File("wug3.txt")));
        String[] add3 = {"add", "h.txt"};
        Gitlet.add(add3);
        String[] rm = {"remove", "notwug.txt"};
        Gitlet.remove(rm);
        Utils.writeContents(new File("f.txt"),
                Utils.readContentsAsString(new File("wug3.txt")));
        String[] add4 = {"add", "f.txt"};
        Gitlet.add(add4);
        String[] cc = {"commit", "a lot of"};
        Gitlet.commit(cc);
        String[] co = {"checkout", "other"};
        Gitlet.checkout(co);
        Utils.writeContents(new File("f.txt"),
                Utils.readContentsAsString(new File("notwug.txt")));
        Gitlet.add(add4);
        Utils.writeContents(new File("k.txt"),
                Utils.readContentsAsString(new File("wug3.txt")));
        String[] a = {"add", "k.txt"};
        Gitlet.add(a);
        String[] ccc = {"commit", "more"};
        Gitlet.commit(ccc);
        String[] cooo = {"checkout", "master"};
        Gitlet.checkout(cooo);
        String[] log = {"log"};
        Gitlet.log(log);



    }

    @Test
    public void status1() {
        Gitlet g = new Gitlet();
        Utils.writeContents(new File("f.txt"),
                Utils.readContentsAsString(new File("testing/src/wug.txt")));
        String[] add = {"add", "f.txt"};
        Gitlet.add(add);
        Utils.writeContents(new File("h.txt"),
                Utils.readContentsAsString(new File("testing/src/notwug.txt")));
        String[] add2 = {"add", "h.txt"};
        Gitlet.add(add2);
        String[] rm = {"rm", "f.txt"};
        Gitlet.remove(rm);
        String[] s = {"status"};
        Gitlet.status(s);
        assertEquals(Utils.readContentsAsString(
                new File("testing/src/wug.txt")),
                Utils.readContentsAsString(new File("f.txt")));
    }

    @Test
    public void ec1() {
        Gitlet g = new Gitlet();
        String[] s = {"status"};
        Gitlet.status(s);
        String[] add = {"add", "f.txt"};
        String[] com = {"commit", "Add f"};
        Gitlet.add(add);
        Gitlet.commit(com);
        Utils.writeContents(new File("f.txt"), "some funky shit");
        Gitlet.status(s);
        new File("f.txt").delete();
        Gitlet.status(s);
    }

    @Test
    public void mergeError() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "wug.txt"};
        Gitlet.add(add);
        String[] add2 = {"add", "notwug.txt"};
        Gitlet.add(add2);
        String[] c = {"commit", "Two files"};
        Gitlet.commit(c);
        String[] br = {"branch", "b1"};
        Gitlet.branch(br);
        String[] add3 = {"add", "wug3.txt"};
        Gitlet.add(add3);
        String[] com2 = {"commit", "Add h.txt"};
        Gitlet.commit(com2);
        String[] br2 = {"branch", "b2"};
        Gitlet.branch(br2);
        String[] rm = {"rm", "wug.txt"};
        Gitlet.remove(rm);
        String[] com3 = {"commit", "remove wug.txt"};
        Gitlet.commit(com3);
        String[] m = {"merge", "b1"};
        Gitlet.merge(m);
        String[] ch = {"checkout", "b2"};
        Gitlet.checkout(ch);
        String[] m2 = {"merge", "master"};
        Gitlet.merge(m2);
    }

    @Test
    public void addNR() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "f.txt"};
        String[] add2 = {"add", "h.txt"};
        Gitlet.add(add);
        Gitlet.add(add2);
        String[] rm = {"rm", "f.txt"};
        Gitlet.remove(rm);
        String[] st = {"status"};
        Gitlet.status(st);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void abriv() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "f.txt"};
        Gitlet.add(add);
        String[] com = {"commit", "vers1"};
        Gitlet.commit(com);
        Utils.writeContents(new File("f.txt"), "WAZUP");
        Gitlet.add(add);
        String[] com2 = {"commit", "vers12"};
        Gitlet.commit(com2);
        String[] lg = {"log"};
        Gitlet.log(lg);
        try {
            ObjectInputStream obj = new ObjectInputStream(
                    new FileInputStream(new File(".gitlet/dataStructs/list")));
            LinkedList<String> list = (LinkedList<String>) obj.readObject();
            String k = list.get(list.size() - 2);
            String[] c = {"checkout", k.substring(0, 6), "--", "f.txt"};
            Gitlet.checkout(c);
        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void resetTester() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "f.txt"};
        String[] add2 = {"add", "g.txt"};
        Gitlet.add(add);
        Gitlet.add(add2);
        String[] cm = {"commit", "Two files"};
        Gitlet.commit(cm);
        String[] br = {"branch", "other"};
        Gitlet.branch(br);
        Utils.writeContents(new File("h.txt"), "SMASS");
        String[] add3 = {"add", "h.txt"};
        Gitlet.add(add3);
        String[] rm = {"rm", "g.txt"};
        Gitlet.remove(rm);
        String[] cm2 = {"commit", "Add h.txt and remove g.txt"};
        Gitlet.commit(cm2);
        String[] co = {"checkout", "other"};
        Gitlet.checkout(co);
        String[] rm2 = {"rm", "f.txt"};
        Gitlet.remove(rm2);
        Utils.writeContents(new File("k.txt"), "DEEZNUTS");
        String[] add4 = {"add", "k.txt"};
        Gitlet.add(add4);
        String[] cm3 = {"commit", "Add k.txt and remove f.txt"};
        Gitlet.commit(cm3);
        String[] log = {"log"};
        Gitlet.log(log);
        String[] co2 = {"checkout", "master"};
        Gitlet.checkout(co2);
        Gitlet.log(log);
        Utils.writeContents(new File("m.txt"), "Wow");
        String[] add5 = {"add", "m.txt"};
        Gitlet.add(add5);
        try {
            ObjectInputStream obj = new ObjectInputStream(
                    new FileInputStream(new File(
                            ".gitlet/dataStructs/list")));
            LinkedList<String> list = (LinkedList<String>) obj.readObject();
            String second = list.get(1);
            String thrid = list.get(2);
            String[] reset = {"reset", second};
            Gitlet.reset(reset);
            String[] st = {"status"};
            Gitlet.status(st);
            Gitlet.log(log);
            String[] co3 = {"checkout", "other"};
            Gitlet.checkout(co3);
            Gitlet.log(log);
            Gitlet.checkout(co2);
            Gitlet.log(log);
            String[] reset2 = {"reset", thrid};
            Gitlet.reset(reset2);
            Gitlet.log(log);

        } catch (IOException | ClassNotFoundException ex) {
            ex.fillInStackTrace();
        }
    }

    @Test
    public void conflictTester() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "f.txt"};
        String[] add2 = {"add", "g.txt"};
        Gitlet.add(add);
        Gitlet.add(add2);
        String[] cm = {"commit", "Two files"};
        Gitlet.commit(cm);
        String[] br = {"branch", "other"};
        Gitlet.branch(br);
        Utils.writeContents(new File("h.txt"), "SMASS");
        String[] add3 = {"add", "h.txt"};
        Gitlet.add(add3);
        String[] rm = {"rm", "g.txt"};
        Gitlet.remove(rm);
        Utils.writeContents(new File("f.txt"), "Ouchy");
        Gitlet.add(add);
        String[] cc = {"commit", "a couple of things"};
        Gitlet.commit(cc);
        String[] co = {"checkout", "other"};
        Gitlet.checkout(co);
        String[] rm2 = {"rm", "f.txt"};
        Gitlet.remove(rm2);
        Utils.writeContents(new File("k.txt"), "Wooah");
        String[] add5 = {"add", "k.txt"};
        Gitlet.add(add5);
        String[] ccc = {"commit", "totally forgot"};
        Gitlet.commit(ccc);
        String[] co2 = {"checkout", "master"};
        Gitlet.checkout(co2);
        String[] log = {"log"};
        Gitlet.log(log);
        String[] merge = {"merge", "other"};
        Gitlet.merge(merge);
        Gitlet.log(log);
        String[] st = {"status"};
        Gitlet.status(st);

    }

    @Test
    public void addRemote() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "f.txt"};
        String[] add2 = {"add", "h.txt"};
        Gitlet.add(add);
        Gitlet.add(add2);
        String[] cm = {"commit", "Two files"};
        Gitlet.commit(cm);
        String[] ar = {"add-remote", "some", "data/.gitlet"};
        Gitlet.addRemote(ar);
        String[] ar2 = {"add-remote", "some", "data2/.gitlet"};
        Gitlet.addRemote(ar2);
        String[] fetch = {"fetch", "some", "master"};
        Gitlet.fetch(fetch);
        String[] push = {"push", "some", "master"};
        Gitlet.push(push);
    }

    @Test
    public void fetchTester() {
        Gitlet g = new Gitlet();
        String[] add = {"add", "f.txt"};
        String[] add2 = {"add", "k.txt"};
        Gitlet.add(add);
        Gitlet.add(add2);
        String[] cm = {"commit", "Two files"};
        Gitlet.commit(cm);
        String[] branch = {"branch", "B1/master"};
        Gitlet.branch(branch);
        String[] addRemote = {"add-remote", "Steve", "/Users/Downloads/test/.gitlet"};
        Gitlet.addRemote(addRemote);
        String[] fetch = {"fetch", "Steve", "master"};
        Gitlet.fetch(fetch);

    }
}




