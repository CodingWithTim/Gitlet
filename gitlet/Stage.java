package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/** A data structure that manages the files need to be added or removed.
 * @author Tim Li */
public class Stage implements Serializable {

    /** file location. */
    public static final File FILE = new File(".gitlet/stage");
    /** blobs staged for addition, file name as keys and hash as values. */
    private HashMap<String, String> _additions;
    /** blobs staged for remove, file name as keys and hash as values. */
    private HashMap<String, String> _removals;

    public Stage() {
        _additions = new HashMap<>();
        _removals = new HashMap<>();
    }

    /** Clears the staging area. */
    public void clear() {
        _additions.clear();
        _removals.clear();
    }

    /** Persistence function that loads the stage object.
     *  @return the stage object */
    public static Stage fromFile() {
        return Utils.readObject(FILE, Stage.class);
    }

    /** Persistence function that saves the stage object. */
    public void saveFile() {
        Utils.writeObject(FILE, this);
    }

    /** Adds file to staging area.
     *  @param name file name
     *  @param from file location
     *  @param hash file hash */
    public void add(String name, File from, String hash) {
        File to = Utils.join(Gitlet.BLOB_FOLDER, hash);

        if (_additions.containsKey(name)) {
            File blob = Utils.join(Gitlet.BLOB_FOLDER, _additions.get(name));
            if (blob.exists()) {
                blob.delete();
            }

            try {
                to.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Utils.writeContents(to, Utils.readContents(from));
            _additions.replace(name, hash);

        } else {
            try {
                to.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Utils.writeContents(to, Utils.readContents(from));
            _additions.put(name, hash);
        }
    }

    /** Add file to be removed in staging area.
     *  @param file file location
     *  @param head the head commit object */
    public void remove(File file, Commit head) {
        _additions.remove(file.getName());

        if (head.containsFile(file.getName())) {
            String hash = head.getBlobs().get(file.getName());

            if (file.exists()) {
                hash = Gitlet.hash(file);
                Utils.restrictedDelete(file);
            }

            _removals.put(file.getName(), hash);
        }
    }

    /** Additions getter.
     * @return additions */
    public HashMap<String, String> getAdditions() {
        return _additions;
    }

    /** Removals getter.
     *  @return removals */
    public HashMap<String, String> getRemovals() {
        return _removals;
    }
}
