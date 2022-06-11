package autoswitch.events;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Scheduler {
    private final Set<Task> schedule = new CopyOnWriteArraySet<>();

    /**
     * Add an event to the schedule to take place X seconds after the provided tick time.
     *
     * @param event        the event to schedule
     * @param deltaTimeSec the time in seconds the event should run at
     * @param initTickTime the current time in ticks
     */
    public void schedule(SwitchEvent event, double deltaTimeSec, int initTickTime) {
        int deltaTimeTicks = (int) Math.floor(deltaTimeSec * 20);

        // Clean old event
        remove(event);

        schedule.add(new Task(event, initTickTime + deltaTimeTicks));

    }

    /**
     * Run all scheduled tasks.
     *
     * @param currentTick the current time in ticks
     */
    public void execute(int currentTick) {

        schedule.forEach(task -> {
            if (task.finalTickTime <= currentTick && task.event.handlePreSwitchTasks()) {
                // Failure to invoke is an important part of switchback operation
                if (task.event.invoke()) schedule.remove(task);
            }
        });
    }

    /**
     * Reset the event schedule.
     */
    public void resetSchedule() {
        schedule.clear();
    }

    /**
     * Remove the specified event from the schedule.
     *
     * @param event event to remove
     */
    protected void remove(SwitchEvent event) {
        schedule.removeIf(task -> task.event.equals(event));
    }

    public boolean isEventScheduled(SwitchEvent event) {
        return schedule.stream().anyMatch(task -> task.event.equals(event));
    }

    /**
     * Internal representation of an event that includes the scheduled tick time to execute.
     */
    private static class Task {
        private final int finalTickTime;
        private final SwitchEvent event;

        private Task(SwitchEvent event, int finalTickTime) {
            this.event = event;
            this.finalTickTime = finalTickTime;
        }

        @Override
        public String toString() {
            return "Task{" + "finalTickTime=" + finalTickTime + ", event=" + event + '}';
        }

    }

}
