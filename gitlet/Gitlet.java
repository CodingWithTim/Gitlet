package gitlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.LinkedList;

/** The main management Gitlet class that manages Gitlet commands and
 * information.
 * @author Tim Li
 */
public class Gitlet {

    /** Gitlet folder location. */
    public static final File GITLET_FOLDER = new File(".gitlet");
    /** Commit folder location. */
    public static final File COMMIT_FOLDER = new File(".gitlet/commit");
    /** Blob folder location. */
    public static final File BLOB_FOLDER = new File(".gitlet/blob");
    /** Reference file location. */
    private static final File REF = Reference.FILE;
    /** Stage file location. */
    private static final File STAGE = Stage.FILE;

    public Gitlet() { }

    /** Set up the persistence folders and file. */
    public void setupPersistence() {
        try {
            if (!GITLET_FOLDER.exists()) {
                GITLET_FOLDER.mkdir();
            }
            if (!COMMIT_FOLDER.exists()) {
                COMMIT_FOLDER.mkdir();
            }
            if (!BLOB_FOLDER.exists()) {
                BLOB_FOLDER.mkdir();
            }
            if (!REF.exists()) {
                REF.createNewFile();
            }
            if (!STAGE.exists()) {
                STAGE.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Initializes the git repository. */
    public void init() {
        try {
            if (GITLET_FOLDER.exists()) {
                throw new GitletException();
            }
        } catch (GitletException g) {
            System.out.println("A Gitlet version-control system already "
                    + "exists in the current directory.");
        }

        setupPersistence();

        Reference ref = new Reference(System.getProperty("user.dir"));
        Stage stage = new Stage();

        Commit first = new Commit("initial commit", null,
                new Date(0));
        String hash = hash(first);
        ref.setHead(hash);
        ref.setCurrent("master");
        ref.addBranch("master", hash);

        first.saveFile(hash);

        ref.saveFile();
        stage.saveFile();
    }

    /** Add a file to the Gitlet repo.
     * @param name name of the file needs to be added
     */
    public void add(String name) {
        Reference ref = Reference.fromFile();
        Stage stage = Stage.fromFile();

        if (!Utils.join(ref.getCwd(), name).exists()) {
            throw new GitletException("File does not exist.");
        }

        Commit head = Commit.fromFile(ref.getHead());
        File file = Utils.join(ref.getCwd(), name);
        String hash = hash(file);

        if (stage.getRemovals().containsKey(name)) {
            stage.getRemovals().remove(name);
        } else if (!head.containsFile(name) || head.getHash(name)
                .compareTo(hash) != 0) {
            stage.add(name, file, hash);
        }

        ref.saveFile();
        stage.saveFile();
    }

    /** Commit to the Gitlet repo with a commit message.
     * @param message message of the commit
     */
    public void commit(String message) {
        Reference ref = Reference.fromFile();
        Stage stage = Stage.fromFile();

        if (stage.getAdditions().size() == 0
                && stage.getRemovals().size() == 0) {
            throw new GitletException("No changes added to the commit.");
        }

        if (message.length() == 0) {
            throw new GitletException("Please enter a commit message.");
        }

        Commit head = Commit.fromFile(ref.getHead());
        Commit newCommit = new Commit(message, ref.getHead(),
                new Date());

        HashMap<String, String> blobs = head.getBlobs();
        HashMap<String, String> map = stage.getAdditions();
        for (String name : map.keySet()) {
            if (blobs.containsKey(name)) {
                blobs.replace(name, map.get(name));
            } else {
                blobs.put(name, map.get(name));
            }
        }
        map = stage.getRemovals();
        for (String name : map.keySet()) {
            blobs.remove(name);
        }

        newCommit.setBlobs(blobs);
        stage.clear();

        String hash = hash(newCommit);
        ref.setHead(hash);
        newCommit.saveFile(hash);

        ref.saveFile();
        stage.saveFile();
    }

    /** Remove a file from the Gitlet Repo.
     * @param name name of the file to be removed
     */
    public void remove(String name) {
        Reference ref = Reference.fromFile();
        Stage stage = Stage.fromFile();

        Commit head = Commit.fromFile(ref.getHead());

        if (!head.containsFile(name) && !stage.getAdditions()
                .containsKey(name)) {
            throw new GitletException("No reason to remove the file.");
        }

        stage.remove(Utils.join(ref.getCwd(), name), head);

        ref.saveFile();
        stage.saveFile();
    }

    /** Prints out the log of Git. */
    public void log() {
        Reference ref = Reference.fromFile();

        Commit current = Commit.fromFile(ref.getHead());
        while (current != null) {
            System.out.println("===");
            System.out.println("commit " + current.hash());

            SimpleDateFormat fmt = new SimpleDateFormat(
                    "EEE MMM d HH:mm:ss yyyy Z");
            System.out.println("Date: " + fmt.format(
                    current.getTimestamp()));

            System.out.println(current.getMessage());
            System.out.println();

            current = Commit.fromFile(current.getParent());
        }

    }


    /** Like log, except displays information about all commits
     * ever made. The order of the commits does not matter.*/
    public void globalLog() {
        for (String file : Utils.plainFilenamesIn(COMMIT_FOLDER)) {
            Commit current = Commit.fromFile(file);

            System.out.println("===");
            System.out.println("commit " + current.hash());

            SimpleDateFormat fmt = new SimpleDateFormat(
                    "EEE MMM d HH:mm:ss yyyy Z");
            System.out.println("Date: " + fmt.format(
                    current.getTimestamp()));

            System.out.println(current.getMessage());
            System.out.println();
        }
    }


    /** Given commit message, find the commit id.
     * @param message message of the commit
     */
    public void find(String message) {
        try {
            Boolean found = false;

            for (String file : Utils.plainFilenamesIn(COMMIT_FOLDER)) {
                Commit current = Commit.fromFile(file);

                if (current.getMessage().compareTo(message) == 0) {
                    System.out.println(current.hash());
                    found = true;
                }
            }

            if (!found) {
                throw new GitletException("Found no commit with "
                        + "that message.");
            }
        } catch (GitletException g) {
            System.out.println(g.getMessage());
        }
    }


    /** Prints out the repo status. */
    public void status() {
        Reference ref = Reference.fromFile();
        Stage stage = Stage.fromFile();
        Commit head = Commit.fromFile(ref.getHead());
        System.out.println("=== Branches ===");
        System.out.println("*" + ref.getCurrent());
        for (String branch : ref.getBranches().keySet()) {
            if (branch.compareTo(ref.getCurrent()) != 0) {
                System.out.println(branch);
            }
        }
        HashSet<String> modified = new HashSet<>();
        HashSet<String> deleted = new HashSet<>();
        HashSet<String> untracked = new HashSet<>();
        for (String file : Utils.plainFilenamesIn(ref.getCwd())) {
            String content = Utils.readContentsAsString(Utils.join(
                    ref.getCwd(), file));
            if (head.containsFile(file)) {
                File blob = Utils.join(BLOB_FOLDER, head.getHash(file));
                if (content.compareTo(Utils.readContentsAsString(blob))
                        != 0) {
                    modified.add(file);
                }
            } else if (!stage.getAdditions().containsKey(file)) {
                untracked.add(file);
            }
        }
        for (String file : head.getBlobs().keySet()) {
            if (!Utils.join(ref.getCwd(), file).exists()
                    && !stage.getRemovals().containsKey(file)) {
                deleted.add(file);
            }
        }
        System.out.println("\n=== Staged Files ===");
        for (String file : stage.getAdditions().keySet()) {
            if (Utils.join(ref.getCwd(), file).exists()) {
                if (!modified.contains(file)) {
                    System.out.println(file);
                }
            } else {
                deleted.add(file);
            }
        }
        System.out.println("\n=== Removed Files ===");
        for (String file : stage.getRemovals().keySet()) {
            System.out.println(file);
        }
        System.out.println("\n=== Modifications Not Staged "
                + "For Commit ===");
        for (String file : deleted) {
            System.out.println(file + " (deleted)");
        }
        for (String file : modified) {
            System.out.println(file + " (modified)");
        }
        System.out.println("\n=== Untracked Files ===");
        for (String file : untracked) {
            System.out.println(file);
        }
    }


    /** Checkout a file in the current head commit.
     * @param name file name
     */
    public void checkoutFile(String name) {
        Reference ref = Reference.fromFile();
        Commit head = Commit.fromFile(ref.getHead());

        if (head.containsFile(name)) {
            File file = Utils.join(ref.getCwd(), name);

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            File blob = Utils.join(BLOB_FOLDER, head.getBlobs().get(name));
            Utils.writeContents(file, Utils.readContents(blob));
        } else {
            throw new GitletException("File does not "
                    + "exist in that commit");
        }
    }

    /** Checkout a file in the given commit.
     * @param id the hash of a specific commit
     * @param name file name
     */
    public void checkoutCommit(String id, String name) {
        Reference ref = Reference.fromFile();
        File commitFile = Utils.join(COMMIT_FOLDER, id);
        Boolean noCommit = false;

        if (!commitFile.exists()) {
            noCommit = true;

            for (String hash : Utils.plainFilenamesIn(COMMIT_FOLDER)) {
                if (hash.contains(id)) {
                    noCommit = false;
                    id = hash;
                }
            }

            if (noCommit) {
                throw new GitletException("No commit with "
                        + "that id exists.");
            }
        }

        Commit commit = Commit.fromFile(id);
        if (commit.containsFile(name)) {
            File file = Utils.join(ref.getCwd(), name);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            File blob = Utils.join(BLOB_FOLDER, commit.getBlobs().get(name));
            Utils.writeContents(file, Utils.readContents(blob));
        } else {
            throw new GitletException("File does not "
                    + "exist in that commit.");
        }
    }


    /** Checkout a branch.
     * @param name branch name
     */
    public void checkoutBranch(String name) {
        Reference ref = Reference.fromFile();
        Stage stage = Stage.fromFile();

        if (!ref.getBranches().containsKey(name)) {
            throw new GitletException("No such branch exists.");
        }

        if (name.compareTo(ref.getCurrent()) == 0) {
            throw new GitletException("No need to check out the "
                    + "current branch.");
        }

        String branch = ref.getBranches().get(name);

        Commit commit = Commit.fromFile(branch);
        Commit head = Commit.fromFile(ref.getHead());

        for (String file : Utils.plainFilenamesIn(ref.getCwd())) {
            if (!head.containsFile(file)
                    && commit.containsFile(file)) {
                throw new GitletException("There is an untracked "
                        + "file in the way; delete it, or add and"
                        + " commit it first.");
            }
        }

        for (Map.Entry<String, String> entry
                : commit.getBlobs().entrySet()) {
            File file = Utils.join(ref.getCwd(), entry.getKey());

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            File blob = Utils.join(BLOB_FOLDER, entry.getValue());
            Utils.writeContents(file, Utils.readContents(blob));
        }

        for (String file : head.getBlobs().keySet()) {
            if (!commit.containsFile(file)) {
                Utils.restrictedDelete(Utils.join(
                        ref.getCwd(), file));
            }
        }

        ref.setCurrent(name);
        ref.setHead(branch);
        stage.clear();

        ref.saveFile();
        stage.saveFile();
    }


    /** Add a new branch to the Gitlet repo.
     * @param name name of the new branch
     */
    public void branch(String name) {
        Reference ref = Reference.fromFile();

        if (ref.getBranches().containsKey(name)) {
            throw new GitletException("A branch with that name already "
                    + "exists.");
        }
        ref.addBranch(name, ref.getHead());

        ref.saveFile();
    }


    /** Remove a branch from the Gitlet repo.
     * @param name name of the branch
     */
    public void removeBranch(String name) {
        Reference ref = Reference.fromFile();

        if (!ref.getBranches().containsKey(name)) {
            throw new GitletException("A branch with that name does "
                    + "not exist.");
        }

        if (name.compareTo(ref.getCurrent()) == 0) {
            throw new GitletException("Cannot remove the current "
                    + "branch.");
        }

        ref.removeBranch(name);

        ref.saveFile();
    }


    /** Check out the given commit and reset the repo to it.
     * @param id commit id
     */
    public void reset(String id) {
        Reference ref = Reference.fromFile();
        Stage stage = Stage.fromFile();

        if (!Utils.plainFilenamesIn(COMMIT_FOLDER).contains(id)) {
            throw new GitletException("No commit with that id exists.");
        }

        Commit commit = Commit.fromFile(id);
        Commit head = Commit.fromFile(ref.getHead());

        for (String file : Utils.plainFilenamesIn(ref.getCwd())) {
            if (!head.containsFile(file)
                    && commit.containsFile(file)) {
                throw new GitletException("There is an untracked "
                        + "file in the way; delete it, or add and"
                        + " commit it first.");
            }
        }

        for (Map.Entry<String, String> entry
                : commit.getBlobs().entrySet()) {
            File file = Utils.join(ref.getCwd(), entry.getKey());

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            File blob = Utils.join(BLOB_FOLDER, entry.getValue());
            Utils.writeContents(file, Utils.readContents(blob));
        }

        for (String file : head.getBlobs().keySet()) {
            if (!commit.containsFile(file)) {
                Utils.restrictedDelete(Utils.join(
                        ref.getCwd(), file));
            }
        }

        ref.setHead(commit.hash());
        for (String name : ref.getBranches().keySet()) {
            if (ref.getBranches().get(name)
                    .compareTo(ref.getHead()) == 0) {
                ref.modifyBranch(name, commit.hash());
            }
        }

        stage.clear();
        ref.saveFile();
        stage.saveFile();
    }


    public void merge(String name) {
        Reference ref = Reference.fromFile();
        Stage stage = Stage.fromFile();
        mergeExceptions1(ref, stage, name);
        Commit head = Commit.fromFile(ref.getHead());
        Commit other = Commit.fromFile(ref.getBranches().get(name));
        Commit splitPoint = getSplitPoint(head, other);
        mergeExceptions2(ref, stage, name, splitPoint, head, other);
        assert splitPoint != null;
        for (String fileName : getFiles(head, other, splitPoint)) {
            String s = splitPoint.getHash(fileName);
            String h = head.getHash(fileName);
            String o = other.getHash(fileName);
            if (s == null && h == null && o != null) {
                checkoutCommit(other.hash(), fileName);
                stage.getAdditions().put(fileName, o);
            } else if (s == null && h != null && o == null) {
                checkoutCommit(head.hash(), fileName);
                stage.getAdditions().put(fileName, h);
            } else if (s != null && h != null) {
                if (s.compareTo(h) == 0 && o == null) {
                    stage.remove(Utils.join(ref.getCwd(), fileName), head);
                }
                if (o != null) {
                    if (s.compareTo(h) == 0 && s.compareTo(o) != 0) {
                        checkoutCommit(other.hash(), fileName);
                        stage.getAdditions().put(fileName, o);
                    }
                }
            }
            stage.saveFile();
            if (s != null && h != null && o != null && s.compareTo(h) != 0
                    && s.compareTo(o) != 0 && h.compareTo(o) != 0) {
                createConflictFile(fileName, h, o);
                System.out.println("Encountered a merge conflict.");
            } else if (s == null && h != null && o != null
                    && h.compareTo(o) != 0) {
                createConflictFile(fileName, h, o);
                System.out.println("Encountered a merge conflict.");
            } else if (s != null && h != null && s.compareTo(h) != 0
                    && o == null) {
                String content = Utils.readContentsAsString(
                        Utils.join(BLOB_FOLDER, h));
                createConflictFileWithEmptyFile(fileName, content, "");
                System.out.println("Encountered a merge conflict.");
            } else if (s != null && o != null && s.compareTo(o) != 0
                    && h == null) {
                String content = Utils.readContentsAsString(
                        Utils.join(BLOB_FOLDER, o));
                createConflictFileWithEmptyFile(fileName, "", content);
                System.out.println("Encountered a merge conflict.");
            }
        }
        stage = Stage.fromFile();
        createMergeCommit(ref, stage, head, other, name);
        ref.saveFile();
        stage.saveFile();
    }


    private void createConflictFile(String fileName,
                                    String hash1,
                                    String hash2) {
        Reference ref = Reference.fromFile();
        Stage stage = Stage.fromFile();

        File from = Utils.join(ref.getCwd(), fileName);
        String content1 = Utils.readContentsAsString(Utils.join(
                BLOB_FOLDER, hash1));
        String content2 = Utils.readContentsAsString(Utils.join(
                BLOB_FOLDER, hash2));

        String content = "<<<<<<< HEAD\n".concat(content1)
                .concat("=======\n")
                .concat(content2)
                .concat(">>>>>>>\n");
        Utils.writeContents(from, content);

        stage.add(fileName, from, Utils.sha1(content));
        stage.saveFile();
    }

    private void mergeExceptions1(Reference ref,
                                 Stage stage,
                                 String name) {
        if (name.compareTo(ref.getCurrent()) == 0) {
            throw new GitletException("Cannot merge a branch with "
                    + "itself.");
        }

        if (!ref.getBranches().containsKey(name)) {
            throw new GitletException("A branch with that name does "
                    + "not exist.");
        }

        if (stage.getAdditions().size() > 0
                || stage.getRemovals().size() > 0) {
            throw new GitletException("You have uncommitted "
                    + "changes.");
        }
    }

    private void mergeExceptions2(Reference ref,
                                  Stage stage,
                                  String name,
                                  Commit splitPoint,
                                  Commit head,
                                  Commit other) {
        if (ref.getHead().compareTo(splitPoint.hash()) == 0) {
            checkoutBranch(name);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        if (other.hash().compareTo(splitPoint.hash()) == 0) {
            throw new GitletException("Given branch is an ancestor"
                    + " of the current branch.");
        }


        for (String file : Utils.plainFilenamesIn(ref.getCwd())) {
            if (!head.containsFile(file)
                    && other.containsFile(file)) {
                throw new GitletException("There is an untracked "
                        + "file in the way; delete it, or add and"
                        + " commit it first.");
            }
        }
    }

    private void createConflictFileWithEmptyFile(String fileName,
                                                 String content1,
                                                 String content2) {
        Reference ref = Reference.fromFile();
        Stage stage = Stage.fromFile();

        File from = Utils.join(ref.getCwd(), fileName);

        String content = "<<<<<<< HEAD\n".concat(content1)
                .concat("=======\n")
                .concat(content2)
                .concat(">>>>>>>\n");
        Utils.writeContents(from, content);

        stage.add(fileName, from, Utils.sha1(content));
        stage.saveFile();
    }

    /** Helper method creates merge commit.
     * @param ref reference object
     * @param stage stage object
     * @param head head commit
     * @param other other commit
     * @param name name of the given branch
     */
    private void createMergeCommit(Reference ref,
                                   Stage stage,
                                   Commit head,
                                   Commit other,
                                   String name) {
        MergeCommit mergeCommit = new MergeCommit(head.hash(),
                other.hash(), ref.getCurrent(), name);
        String hash = hash(mergeCommit);

        HashMap<String, String> blobs = head.getBlobs();
        HashMap<String, String> map = stage.getAdditions();
        for (String key : map.keySet()) {
            if (blobs.containsKey(key)) {
                blobs.replace(key, map.get(key));
            } else {
                blobs.put(key, map.get(key));
            }
        }
        map = stage.getRemovals();
        for (String key : map.keySet()) {
            blobs.remove(key);
        }

        mergeCommit.setBlobs(blobs);

        stage.clear();
        mergeCommit.saveFile(hash);
        ref.setHead(hash);
    }

    /** Helper method that get all the files present.
     * @param head head commit
     * @param other other commit we want to merge with
     * @param splitPoint split point of both commit
     * @return all file names
     */
    private HashSet<String> getFiles(Commit head,
                                     Commit other,
                                     Commit splitPoint) {
        HashSet<String> allFiles = new HashSet<>(
                head.getBlobs().keySet());
        allFiles.addAll(other.getBlobs().keySet());
        allFiles.addAll(splitPoint.getBlobs().keySet());
        return allFiles;
    }

    /** Helper method that finds the closest common ancestors of 2 branches.
     * @param branch1 first branch's hash
     * @param branch2 second branch's hash
     * @return split point commit object
     */
    private Commit getSplitPoint(Commit branch1, Commit branch2) {
        ArrayList<String> h1 = getAllHashes(branch1);

        ArrayList<String> h2 = getAllHashes(branch2);

        for (String hash : h1) {
            if (h2.contains(hash)) {
                return Commit.fromFile(hash);
            }
        }

        return null;
    }


    /** Helper method that gest all the hashes using Breath-first Search.
     * @param root root commit
     * @return all hashes
     */
    private ArrayList<String> getAllHashes(Commit root) {
        Queue<Commit> q = new LinkedList<Commit>();
        ArrayList<String> hashes = new ArrayList<>();
        q.add(root);

        while (!q.isEmpty()) {
            Commit next = q.poll();
            hashes.add(next.hash());

            if (next.getParent() != null) {
                q.add(Commit.fromFile(next.getParent()));
            }

            if (next.isMerge()) {
                q.add(Commit.fromFile(next.getSecondParent()));
            }
        }

        return hashes;
    }

    /** Helper hashing function for commit.
     * @param commit commit object
     * @return hashcode of given commit object
     */
    private String hash(Commit commit) {
        return Utils.sha1(Utils.serialize(commit));
    }

    /** Helper hashing function for file.
     * @param file file location
     * @return hashcode of the content of the given file
     */
    public static String hash(File file) {
        return Utils.sha1(Utils.readContents(file));
    }


    /** Is gitlet initialized.
     * @return yes or no
     */
    public boolean initiaized() {
        return Gitlet.GITLET_FOLDER.exists();
    }
}
