package autoswitch.events;

import autoswitch.AutoSwitch;
import net.minecraft.util.ActionResult;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Scheduler {
    private Set<Task> schedule = new CopyOnWriteArraySet<>();

    public ActionResult schedule(SwitchEvent event, double deltaTimeSec, int initTickTime) {
        int deltaTimeTicks = (int) Math.floor(deltaTimeSec * 20);

        if (deltaTimeTicks == 0) {
            // Schedules for the next tick to make sure there is no lock on switching
            schedule.add(new Task(event, initTickTime, initTickTime + 1));
            return ActionResult.PASS;
        }

        schedule.add(new Task(event, initTickTime, initTickTime + deltaTimeTicks));

        return ActionResult.PASS;
    }

    public void execute(int currentTick) {

        schedule.forEach(task -> {
            //task.event.handlePreSwitchTasks() TODO check
            if (task.finalTickTime <= currentTick) {
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

    public boolean isCurrentlyTargeted(Object protoTarget) {
        AutoSwitch.logger.error(schedule);
        for (Task task : schedule) {
            AutoSwitch.logger.error("Has: {}; Got: {}", task.event.getProtoTarget(), protoTarget);
            if (task.event.getProtoTarget().equals(protoTarget)) return true;
        }

        return false;
    }

    class Task {
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
