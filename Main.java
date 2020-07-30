package gitlet;

import java.io.File;

/** Driver class for Gitlet, the tiny version-control system.
 *  @author sm3
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        if (args[0].equals("init")) {
            if (args.length == 1) {
                Gitlet g = new Gitlet();
            } else {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
        } else if (!new File(".gitlet").exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        } else if (args[0].equals("add")) {
            Gitlet.add(args);
        } else if (args[0].equals("commit")) {
            Gitlet.commit(args);
        } else if (args[0].equals("checkout")) {
            Gitlet.checkout(args);
        } else if (args[0].equals("log")) {
            Gitlet.log(args);
        } else if (args[0].equals("global-log")) {
            Gitlet.globalLog(args);
        } else if (args[0].equals("rm")) {
            Gitlet.remove(args);
        } else if (args[0].equals("find")) {
            Gitlet.find(args);
        } else if (args[0].equals("status")) {
            Gitlet.status(args);
        } else if (args[0].equals("branch")) {
            Gitlet.branch(args);
        } else if (args[0].equals("rm-branch")) {
            Gitlet.removeBranch(args);
        } else if (args[0].equals("reset")) {
            Gitlet.reset(args);
        } else if (args[0].equals("merge")) {
            Gitlet.merge(args);
        } else if (args[0].equals("add-remote")) {
            Gitlet.addRemote(args);
        } else if (args[0].equals("rm-remote")) {
            Gitlet.removeRemote(args);
        } else if (args[0].equals("push")) {
            Gitlet.push(args);
        } else if (args[0].equals("fetch")) {
            Gitlet.newFetch(args);
        } else if (args[0].equals("pull")) {
            Gitlet.pull(args);
        } else {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }

}
