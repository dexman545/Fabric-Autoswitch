package dex.autoswitch.engine.events;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

import dex.autoswitch.engine.state.SwitchContext;

public class Scheduler {
    private static final int QUEUE_SIZE_WARNING_THRESHOLD = 64;
    private static final Logger LOGGER = Logger.getLogger("AutoSwitch-Engine");
    private final Object lock = new Object();
    private final Map<SwitchEvent, Task> taskMap = new EnumMap<>(SwitchEvent.class);
    private int tickTime;

    /**
     * Schedule {@code event} to fire deltaTicks after initTickTime.
     * If it was already scheduled, remove the old one first.
     */
    public void schedule(SwitchEvent event, SwitchContext context, int deltaTicks) {
        schedule(event, context, deltaTicks, tickTime);
    }

    /**
     * Schedule {@code event} to fire deltaTicks after initTickTime.
     * If it was already scheduled, remove the old one first.
     */
    protected void schedule(SwitchEvent event, SwitchContext context, int deltaTicks, int initTickTime) {
        int scheduledTick = initTickTime + deltaTicks;

        synchronized (lock) {
            var task = new Task(event, context, scheduledTick);
            taskMap.put(event, task);
        }
    }

    public void tick() {
        execute(tickTime++);
    }

    /**
     * Execute all tasks whose scheduled tick ≤ currentTick.
     *
     * @param currentTick the “now” tick
     */
    protected void execute(int currentTick) {
        synchronized (lock) {
            //noinspection ConstantValue
            if (true) {
                taskMap.values().removeIf(task -> task.finalTickTime <= currentTick && task.event.perform(task.context));
            } else {
                var it = taskMap.values().iterator();
                //noinspection Java8CollectionRemoveIf
                while (it.hasNext()) {
                    var task = it.next();
                    if (task.finalTickTime <= currentTick && task.event.perform(task.context)) {
                        it.remove();
                    }
                }
            }
        }

        resetTickIfLargeTime();
    }

    /**
     * Clears everything.
     */
    public void reset() {
        synchronized (lock) {
            taskMap.clear();
            tickTime = 0;
        }
    }

    public boolean isEventScheduled(SwitchEvent event) {
        synchronized (lock) {
            return taskMap.containsKey(event);
        }
    }

    private void resetTickIfLargeTime() {
        synchronized (lock) {
            if (taskMap.isEmpty() && tickTime >= Integer.MAX_VALUE / 2) {
                //LOGGER.log(Level.INFO, "Resetting global tick counter from %s → 0".formatted(tickTime));
                tickTime = 0;
            }
        }
    }

    public void cancel(SwitchEvent event) {
        synchronized (lock) {
            taskMap.remove(event);
        }
    }

    /**
     * Internal representation of an event that includes the scheduled tick time to execute.
     */
    private record Task(SwitchEvent event, SwitchContext context, int finalTickTime) {}
}
