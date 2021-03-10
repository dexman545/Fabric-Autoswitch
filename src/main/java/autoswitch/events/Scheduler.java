package autoswitch.events;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import autoswitch.AutoSwitch;

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
        schedule.forEach(task -> {
            if (task.event.equals(event)) schedule.remove(task);
        });
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
