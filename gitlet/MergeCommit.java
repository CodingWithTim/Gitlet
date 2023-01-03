package gitlet;

import java.util.Date;

public class MergeCommit extends Commit {

    /** Points to the parent from the current branch. */
    private String _secondParent;

    /** name of the current branch during merge. */
    private String _firstBranch;

    /** name of the given branch during merge. */
    private String _secondBranch;

    /** log message. */
    private String _logMessage;

    public MergeCommit(String firstParent, String secondParent,
                       String firstBranch, String secondBranch) {
        super("Merge", firstParent, new Date());
        _secondParent = secondParent;
        _firstBranch = firstBranch;
        _secondBranch = secondBranch;
        _logMessage = "Merged " + _secondBranch + " into " + _firstBranch + ".";
    }

    /** Get the log message.
     * @return log message
     */
    @Override
    public String getMessage() {
        return _logMessage;
    }

    /** is this a merge commit.
     * @return true
     */
    @Override
    public boolean isMerge() {
        return true;
    }

    /** SecondParent getter.
     * @return secondParent
     */
    @Override
    public String getSecondParent() {
        return _secondParent;
    }
}
