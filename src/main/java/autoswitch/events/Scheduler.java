package autoswitch.events;

import autoswitch.AutoSwitch;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Scheduler {
    private final Set<Task> schedule = new CopyOnWriteArraySet<>();

    public void schedule(SwitchEvent event, double deltaTimeSec, int initTickTime) {
        int deltaTimeTicks = (int) Math.floor(deltaTimeSec * 20);

        if (deltaTimeTicks == 0) {
            // Schedules for the next tick to make sure there is no lock on switching
            schedule.add(new Task(event, initTickTime, initTickTime + 1));
            return;
        }

        schedule.add(new Task(event, initTickTime, initTickTime + deltaTimeTicks));

    }

    public void execute(int currentTick) {

        schedule.forEach(task -> {
            if (task.finalTickTime <= currentTick && task.event.handlePreSwitchTasks()) {
                task.event.invoke();
                schedule.remove(task);
            }
        });
    }

    protected void remove(SwitchEvent event) {
        schedule.forEach(task -> {
            if (task.event.equals(event)) schedule.remove(task);
        });
    }

    static class Task {
        public int initTickTime;
        public int finalTickTime;
        public SwitchEvent event;

        Task(SwitchEvent event, int initTickTime, int finalTickTime) {
            this.event = event;
            this.finalTickTime = finalTickTime;
            this.initTickTime = initTickTime;
        }

    }

}
