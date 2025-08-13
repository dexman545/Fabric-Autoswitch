package dex.autoswitch.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import dex.autoswitch.config.data.FallbackSelector;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.PlayerInventory;

public class SelectionEngine {
    private final Map<Action, Map<Selector, Set<Selector>>> configuration;
    /**
     * The fallback tool selector to use in case no tools matched
     */
    private final FallbackSelector fallback;

    public SelectionEngine(Map<Action, Map<Selector, Set<Selector>>> configuration, FallbackSelector fallback) {
        this.configuration = Collections.unmodifiableMap(configuration);
        this.fallback = fallback;
    }

    public void select(PlayerInventory<?> inventory, Action action, Object target) {
        findSlot(inventory, action, target).ifPresent(inventory::selectSlot);
    }

    public OptionalInt findSlot(PlayerInventory<?> inventory, Action action, Object target) {
        return findIdealSlot(inventory, configuration.get(action), new SelectionContext(action, target));
    }

    // Sort the slots in this order: targetPriority -> targetRatingN -> toolPriority -> toolRatingN -> slot(reversed)
    // Higher priority is weighted better in all cases, same with rating.
    private OptionalInt findIdealSlot(PlayerInventory<?> inventory, Map<Selector, Set<Selector>> selectors, SelectionContext context) {
        // Build a list of all (targetSel, toolSel, slot) triples that match
        List<Candidate> candidates = new ArrayList<>();
        for (var e : selectors.entrySet()) {
            var tgtSel = e.getKey();
            var targetMatch = tgtSel.matches(context, context.target());
            if (targetMatch.matches()) {
                for (var toolSel : e.getValue()) {
                    for (int slot = 0; slot < inventory.slotCount(); slot++) {
                        var toolMatch = toolSel.matches(context, inventory.getTool(slot));
                        if (toolMatch.matches()) {
                            candidates.add(
                                    new Candidate(tgtSel.matcher(), toolSel.matcher(), tgtSel.priority(),
                                            targetMatch, toolSel.priority(), toolMatch,
                                            slot, slot == inventory.currentSelectedSlot()));
                        }
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            return pickFallbackSlot(inventory, context);
        }

        // Pick the max candidate by our comparator
        Optional<Candidate> best;
        if (false) {
            // A version to print informaion for debugging
            candidates.sort(new ToolOrder(inventory.currentSelectedSlot()).reversed());

            for (var c : candidates) {
                System.out.println(c);
            }

            best = candidates.isEmpty() ? Optional.empty() : Optional.of(candidates.getFirst());
            assert best.equals(candidates.stream().max(new ToolOrder(inventory.currentSelectedSlot())));
        } else {
            best = candidates.stream().max(new ToolOrder(inventory.currentSelectedSlot()));
        }

        return best.map(candidate -> OptionalInt.of(candidate.slot)).orElseGet(OptionalInt::empty);
    }

    /**
     * @return the fallback slot if available, otherwise don't switch
     */
    private OptionalInt pickFallbackSlot(PlayerInventory<?> inventory, SelectionContext context) {
        if (fallback.match(context.action())) {
            var slot = inventory.currentSelectedSlot();
            Match toolMatch;
            do {
                toolMatch = fallback.fallback().matches(context, inventory.getTool(slot));

                if (toolMatch.matches()) {
                    break;
                }

                slot++;
                slot %= inventory.slotCount();
            } while (slot != inventory.currentSelectedSlot());

            if (toolMatch.matches()) {
                return OptionalInt.of(slot);
            }
        }

        return OptionalInt.empty();
    }

    private record Candidate(Matcher tar, Matcher tool, int targetPriority, Match targetMatch,
                             int toolPriority, Match toolMatch,
                             int slot, boolean isSelected) {}

    /**
     * Unless specified otherwise, favors higher valued numbers and {@code true} over {@code false}.
     * <p>
     * Sorts the tools in the order of:
     * <p><ol>
     *     <li>Target Priority</li>
     *     <li>Target Rating
     *       <ol>
     *         <li>level 0</li>
     *         <li>....</li>
     *         <li>level {@code maxTargetRatingLevel}</li>
     *       </ol>
     *     </li>
     *     <li>Target Priority</li>
     *     <li>Tool Rating
     *       <ol>
     *         <li>level 0</li>
     *         <li>....</li>
     *         <li>level {@code maxToolRatingLevel}</li>
     *       </ol>
     *     </li>
     *     <li>isSlotCurrentlySelected</li>
     *     <li>smallest slot</li>
     * </ol>
     *
     * <p>
     * Ratings levels are generally a recursive sort of:
     * <p><ol>
     *     <li>isGroup</li>
     *     <li>hasData</li>
     *     <li>typeRating</li>
     *     <li>dataRating</li>
     * </ol>
     *
     * @param currentSlot the currently selected slot to prefer
     */
    private record ToolOrder(int currentSlot) implements Comparator<Candidate> {
        @Override
        public int compare(Candidate c1, Candidate c2) {
            // Target priority
            int diff = Integer.compare(c1.targetPriority, c2.targetPriority);
            if (diff != 0) return diff;

            // Target rating levels
            var maxTargetRatingLevel = Math.max(c1.targetMatch().getMaxLevel(), c2.targetMatch.getMaxLevel());
            for (int i = 0; i <= maxTargetRatingLevel; i++) {
                var r1 = c1.targetMatch.getRating(i);
                var r2 = c2.targetMatch.getRating(i);
                diff = Double.compare(r1, r2);
                if (diff != 0) return diff;
            }

            // Tool priority
            diff = Integer.compare(c1.toolPriority, c2.toolPriority);
            if (diff != 0) return diff;

            // Tool rating levels
            var maxToolRatingLevel = Math.max(c1.toolMatch().getMaxLevel(), c2.toolMatch.getMaxLevel());
            for (int i = 0; i <= maxToolRatingLevel; i++) {
                var r1 = c1.toolMatch.getRating(i);
                var r2 = c2.toolMatch.getRating(i);
                diff = Double.compare(r1, r2);
                if (diff != 0) return diff;
            }

            // Prefer currently selected slot
            diff = Boolean.compare(c1.isSelected, c2.isSelected);
            if (diff != 0) return diff;

            // Slot id ascending
            return Integer.compare(c2.slot(), c1.slot());
        }
    }
}
