package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

/** A class that manages commit.
 *  @author Tim Li */
public class Commit implements Serializable {

    /** hash of the commit object. */
    private String _hash;
    /** the message of the commit. */
    private String _message;
    /** the time the commit was created. */
    private Date _timestamp;
    /** the parent of this commit object. */
    private String _parent;
    /** a HashMap of blobs, with the file name as keys and hash as values. */
    private HashMap<String, String> _blobs;

    public Commit(String message, String parent) {
        _message = message;
        _parent = parent;
        _blobs = new HashMap<>();
    }

    public Commit(String message, String parent, Date timestamp) {
        _message = message;
        _parent = parent;
        _timestamp = timestamp;
        _blobs = new HashMap<>();
    }

    public Commit(String message, String parent, Date timestamp,
                  HashMap<String, String> blobs) {
        _message = message;
        _parent = parent;
        _timestamp = timestamp;
        _blobs = new HashMap<>(blobs);
    }

    /** persistence function that loads the commit object from files.
     *  @param name name of the file
     *  @return the commit object */
    public static Commit fromFile(String name) {
        if (name == null) {
            return null;
        }

        File inFile = Utils.join(Gitlet.COMMIT_FOLDER, name);
        return Utils.readObject(inFile, Commit.class);
    }

    /** persistance function that saves the commit object to files.
     *  @param name name of the file */
    public void saveFile(String name) {
        File newFile = Utils.join(Gitlet.COMMIT_FOLDER, name);

        try {
            newFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        _hash = name;
        Utils.writeObject(newFile, this);
    }

    /** checks if track a file by name.
     *  @param name name of the file
     *  @return true of tracked, false otherwise
     */
    public Boolean containsFile(String name) {
        return _blobs.containsKey(name);
    }

    /** check if track a file by hash.
     *  @param hash hash of the file
     *  @return true if tracked, false otherwise
     */
    public Boolean containsHash(String hash) {
        return _blobs.containsValue(hash);
    }

    /** get the hash of a blob.
     * @param name of the blob
     * @return hash of the blob
     */
    public String getHash(String name) {
        if (_blobs.containsKey(name)) {
            return _blobs.get(name);
        }
        return null;
    }


    /** is this a merge commit.
     * @return false
     */
    public boolean isMerge() {
        return false;
    }

    /** SecondParent getter.
     * @return null
     */
    public String getSecondParent() {
        return null;
    }

    /** Message getter.
     *  @return message
     */
    public String getMessage() {
        return _message;
    }

    /** Timestamp getter.
     *  @return timestamp
     */
    public Date getTimestamp() {
        return _timestamp;
    }

    /** Parent getter.
     *  @return parent
     */
    public String getParent() {
        return _parent;
    }

    /** Blobs getter.
     *  @return blobs
     */
    public HashMap<String, String> getBlobs() {
        return _blobs;
    }

    /** Blobs setter.
     *  @param blobs HashMap of Blobs
     */
    public void setBlobs(HashMap<String, String> blobs) {
        _blobs = new HashMap<>(blobs);
    }

    /** Hash getter.
     *  @return hash
     */
    public String hash() {
        return _hash;
    }
}
