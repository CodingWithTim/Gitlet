package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Tim Li
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> ....
     *  Java gitlet.Main add hello.txt
     *  If a user doesn't input any arguments, print the message
     *  Please enter a command. and exit.
     *  If a user inputs a command that doesn't exist, print the
     *  message No command with that name
     *  exists. and exit.
     *  If a user inputs a command with the wrong number or format
     *  of operands, print the message
     *  Incorrect operands. and exit.
     *  If a user inputs a command that requires being in an
     *  initialized Gitlet working directory
     *  (i.e., one containing a .gitlet subdirectory), but is not
     *  in such a directory, print the
     *  message Not in an initialized Gitlet directory. */
    public static void main(String... args) {
        Gitlet git = new Gitlet();
        try {
            if (args.length == 0) {
                throw new GitletException("Please enter a command.");
            }
            if (args[0].compareTo("init") == 0) {
                git.init();
            } else if (git.initiaized()) {
                execute(args, git);
            } else {
                throw new GitletException("Not in an initialized "
                        + "Gitlet directory.");
            }
        } catch (GitletException g) {
            System.out.println(g.getMessage());
        }
    }

    private static void execute(String[] args, Gitlet git) {
        switch (args[0]) {
        case "add":
            git.add(args[1]);
            break;
        case "commit":
            git.commit(args[1]);
            break;
        case "rm":
            git.remove(args[1]);
            break;
        case "log":
            git.log();
            break;
        case "global-log":
            git.globalLog();
            break;
        case "find":
            git.find(args[1]);
            break;
        case "status":
            git.status();
            break;
        case "checkout":
            if (args[1].compareTo("--") == 0) {
                git.checkoutFile(args[2]);
            } else if (args.length == 4
                    && args[2].compareTo("--") == 0) {
                git.checkoutCommit(args[1], args[3]);
            } else if (args.length == 2) {
                git.checkoutBranch(args[1]);
            } else {
                throw new GitletException("Incorrect operands.");
            }
            break;
        case "branch":
            git.branch(args[1]);
            break;
        case "rm-branch":
            git.removeBranch(args[1]);
            break;
        case "reset":
            git.reset(args[1]);
            break;
        case "merge":
            git.merge(args[1]);
            break;
        default:
            throw new GitletException("No command with that "
                    + "name exist.");
        }
    }
}
