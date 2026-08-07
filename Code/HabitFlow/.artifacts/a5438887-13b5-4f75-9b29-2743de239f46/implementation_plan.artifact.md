# Implementation Plan - UI Performance Optimization

The goal is to eliminate lag in the application, particularly in the "Statistics" tab where the calendar grid currently performs expensive calculations during UI rendering.

## Proposed Changes

### [Component] UI Optimization

#### [MODIFY] [MainActivity.kt](file:///C:/Users/lieuv/Downloads/HabitFlow/app/src/main/kotlin/com/habitflow/app/MainActivity.kt)
- **StatisticsScreen Optimization**:
    - Move heavy data processing out of the calendar grid loop.
    - **Step 1**: Use `remember` to pre-calculate a `Map<Long, List<OccurrenceEntity>>` (occurrences grouped by day).
    - **Step 2**: Use `remember` to pre-calculate a `Map<Int, List<HabitEntity>>` (habits grouped by day-of-week activity).
    - **Step 3**: Simplify the `Box` and `Surface` hierarchy within each calendar cell to reduce the number of nodes the Compose compiler needs to track.
    - **Step 4**: Replace `split(",")` calls inside the loop with a single pre-calculation for all 7 days of the week.

#### [MODIFY] [Calculators.kt](file:///C:/Users/lieuv/Downloads/HabitFlow/app/src/main/kotlin/com/habitflow/app/Calculators.kt)
- Ensure the `calculate` method doesn't perform redundant operations. (Current O(N log N) is likely acceptable, but I will double-check for redundant filtering).

## Verification Plan

### Automated Tests
- Build the project using `./gradlew :app:assembleDebug`.

### Manual Verification
1.  **Smooth Scrolling**: Open the "Thống kê" tab and scroll through the content. There should be no stutter or lag.
2.  **Fast Navigation**: Switch between tabs (Today -> Stats -> Habits). The transitions should be instantaneous.
3.  **Data Consistency**: Verify the calendar colors still accurately reflect habit completion after the grouping optimization.
