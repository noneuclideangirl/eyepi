package net.noneuclideangirl.data;

public class ProcessData {
    public final long pid;
    public final long spawnTime;

    public ProcessData(long pid, long spawnTime) {
        this.pid = pid;
        this.spawnTime = spawnTime;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ProcessData) {
            var rhs = (ProcessData) other;
            return pid == rhs.pid && spawnTime == rhs.spawnTime;
        } else {
            return false;
        }
    }
}
