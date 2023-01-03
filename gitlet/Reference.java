package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

/** A serializable class that stores references to things that Gitlet class
 * needs.
 * @author Tim Li
 */
public class Reference implements Serializable {

    /** File location. */
    public static final File FILE = new File(".gitlet/ref");
    /** Current working directory. */
    private File _cwd;
    /** Head commit file hash. */
    private String _head;
    /** Master commit hash. */
    private String _current;
    /** All branches, name of branch as keys and hash as value. */
    private HashMap<String, String> _branches;

    public Reference(String directory) {
        _cwd = new File(directory);
        _branches = new HashMap<>();
    }

    /** Persistence function that loads the Reference class.
     * @return reference object
     */
    public static Reference fromFile() {
        assert (FILE.exists());
        return Utils.readObject(FILE, Reference.class);
    }

    /** Adds branch to Branches.
     * @param name branch name
     * @param hash branch head commit hash
     */
    public void addBranch(String name, String hash) {
        if (!_branches.containsKey(name)) {
            _branches.put(name, hash);
        }
    }


    /** Change the head pointer of a given branch.
     * @param name name of the branch
     * @param hash the new hash pointer
     */
    public void modifyBranch(String name, String hash) {
        _branches.replace(name, hash);
    }

    /** Remove branch from Branches.
     * @param name branch name
     */
    public void removeBranch(String name) {
        _branches.remove(name);
    }

    /** Persistence function that saves the Reference object. */
    public void saveFile() {
        Utils.writeObject(FILE, this);
    }

    /** Cwd getter.
     * @return current working directory
     */
    public File getCwd() {
        return _cwd;
    }

    /** Head getter.
     * @return head
     */
    public String getHead() {
        return _head;
    }

    /** Head setter and update current branch.
     * @param head head commit hash
     */
    public void setHead(String head) {
        _head = head;
        _branches.replace(_current, head);
    }

    /** Current setter.
     * @param current set the current branch name
     */
    public void setCurrent(String current) {
        _current = current;
    }

    /** Current getter.
     * @return the name of current branch
     */
    public String getCurrent() {
        return _current;
    }

    /** Branches getter.
     * @return Branches
     */
    public HashMap<String, String> getBranches() {
        return _branches;
    }
}
