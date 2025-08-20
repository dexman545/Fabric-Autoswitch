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

    /**
     * Selects a tool for the given action and target.
     *
     * @param inventory the player inventory to select from
     * @param action the action to select for
     * @param target the target to select for
     */
    public void select(PlayerInventory<?> inventory, Action action, Object target) {
        findSlot(inventory, action, target).ifPresent(inventory::selectSlot);
    }

    /**
     * Finds the slot for the given action and target.
     *
     * @param inventory the player inventory to select from
     * @param action the action to select for
     * @param target the target to select for
     * @return the slot to select, empty if no slot was found
     */
    public OptionalInt findSlot(PlayerInventory<?> inventory, Action action, Object target) {
        return findIdealSlot(inventory, configuration.get(action), new SelectionContext(action, target));
    }

    /**
     * Identifies the ideal slot in a player's inventory based on the provided selectors and context.
     * The selection prioritizes target priority, target rating, tool priority, tool rating,
     * and whether the slot is currently selected (reversed slot order as a tie-breaker).
     *
     * @see ToolOrder ToolOrder for the sort order
     * @param inventory the player's inventory to evaluate for selection
     * @param selectors a mapping of target selectors and their associated tool selectors
     * @param context the context that includes the action and target for which the tools and slots are being evaluated
     * @return the ideal slot to select, wrapped in an {@code OptionalInt}. If no suitable slot is found, returns an empty {@code OptionalInt}.
     */
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
        //noinspection ConstantConditions
        if (false) {
            // A version to print information for debugging
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
            // Start at the current selected slot so it has priority
            var slot = inventory.currentSelectedSlot();
            Match toolMatch;
            do {
                toolMatch = fallback.fallback().matches(context, inventory.getTool(slot));

                if (toolMatch.matches()) {
                    break;
                }

                // Wrap around if we reach the end of the inventory
                slot++;
                slot %= inventory.slotCount();
            } while (slot != inventory.currentSelectedSlot());

            if (toolMatch.matches()) {
                return OptionalInt.of(slot);
            }
        }

        return OptionalInt.empty();
    }

    /**
     * Represents a candidate slot in a player's inventory, used for selecting tools optimized for a specific target
     * and context. This data structure is used to evaluate and prioritize slots based on multiple criteria such as
     * target and tool match priorities, ratings, and whether the slot is currently selected.
     * <p>
     * The evaluation process considers the following:
     * - Target priority: Determines the importance of the target match.
     * - Tool priority: Determines the importance of the tool match.
     * - Match ratings for both the target and tool, which indicate suitability for selection.
     * - Slot index, with the currently selected slot having priority.
     * <p>
     * Fields:
     * - tar: The matcher for the target, determines compatibility with the target context.
     * - tool: The matcher for the tool, evaluates compatibility with the tool selectors.
     * - targetPriority: The priority value for the target match.
     * - targetMatch: The result of the matching process for the target.
     * - toolPriority: The priority value for the tool match.
     * - toolMatch: The result of the matching process for the tool.
     * - slot: The index of the slot in the player's inventory.
     * - isSelected: A flag indicating whether this slot is the currently selected one.
     * @see ToolOrder ToolOrder for the sort order
     */
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
     *     <li>Tool Priority</li>
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
     * Prefers more specific entries over more general ones, so non-groups are preferred over groups,
     * and those with data are prioritized over those without.
     * <p>
     * Type rating is a measure of the type within the given context, such as weapon DPS or mining level.
     * <p>
     * Data rating is the measure of the data within the given context, such as normalized enchantment level.
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
     * @see dex.autoswitch.config.data.tree.ExpressionTree#matches
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

            // Prefer the currently selected slot
            diff = Boolean.compare(c1.isSelected, c2.isSelected);
            if (diff != 0) return diff;

            // Slot id ascending
            return Integer.compare(c2.slot(), c1.slot());
        }
    }
}
