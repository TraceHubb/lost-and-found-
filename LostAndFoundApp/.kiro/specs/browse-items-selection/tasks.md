# Implementation Plan: Browse Items Selection

## Overview

This implementation adds a selection dialog to the Browse feature in the Lost and Found Android app. When users tap the Browse button on the HomeScreen, they will see an AlertDialog offering two options: "Browse Lost Items" or "Browse Found Items". This mirrors the existing Report feature's dialog pattern for a consistent user experience.

The implementation involves modifying only the HomeScreen.kt file to add dialog state management and a new composable function. No changes are needed to navigation or data layers as existing infrastructure already supports both item types.

## Tasks

- [x] 1. Add dialog state management to HomeScreen
  - Add `showBrowseDialog` boolean state variable using `remember { mutableStateOf(false) }`
  - Initialize state to `false` (dialog hidden by default)
  - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 2. Create BrowseSelectionDialog composable function
  - [x] 2.1 Implement BrowseSelectionDialog composable
    - Create private composable function with three parameters: `onDismiss`, `onBrowseLost`, `onBrowseFound`
    - Use `AlertDialog` component with title "Browse Items"
    - Add dialog text "What would you like to browse?"
    - Implement confirm button labeled "Browse Lost Items" that calls `onDismiss()` then `onBrowseLost()`
    - Implement dismiss button labeled "Browse Found Items" that calls `onDismiss()` then `onBrowseFound()`
    - Set `onDismissRequest` to call `onDismiss` parameter
    - _Requirements: 1.2, 1.3, 4.1, 4.2, 4.3, 4.4_
  
  - [ ]* 2.2 Write unit tests for BrowseSelectionDialog
    - Test that dialog displays correct title and text
    - Test that "Browse Lost Items" button invokes `onBrowseLost` callback
    - Test that "Browse Found Items" button invokes `onBrowseFound` callback
    - Test that `onDismiss` is called before navigation callbacks
    - Test that dialog dismisses when `onDismissRequest` is triggered
    - _Requirements: 1.2, 1.3, 1.4, 1.5_

- [x] 3. Modify Browse QuickActionIcon onClick behavior
  - Update the Browse `QuickActionIcon` onClick handler from `onBrowseLost` to `{ showBrowseDialog = true }`
  - Verify the icon, label, and color remain unchanged
  - _Requirements: 1.1, 5.1_

- [x] 4. Add dialog invocation logic to HomeScreen
  - Add conditional rendering: `if (showBrowseDialog) { ... }`
  - Invoke `BrowseSelectionDialog` with three callbacks:
    - `onDismiss = { showBrowseDialog = false }`
    - `onBrowseLost = onBrowseLost` (pass through existing parameter)
    - `onBrowseFound = onBrowseFound` (pass through existing parameter)
  - Place dialog invocation after the existing Report dialog code
  - _Requirements: 1.1, 5.2, 6.1, 6.2_

- [x] 5. Checkpoint - Verify compilation and basic functionality
  - Ensure all tests pass
  - Build the app and verify no compilation errors
  - Manually test that Browse button opens the dialog
  - Manually test that both dialog options navigate correctly
  - Ask the user if questions arise

- [ ]* 6. Write comprehensive UI tests
  - [ ]* 6.1 Write UI test for dialog appearance
    - Test that clicking Browse button displays the dialog
    - Test that dialog contains correct title "Browse Items"
    - Test that dialog contains correct text "What would you like to browse?"
    - Test that both button options are visible
    - _Requirements: 1.1, 1.2, 1.3_
  
  - [ ]* 6.2 Write UI test for Lost Items navigation
    - Test that selecting "Browse Lost Items" navigates to ItemsListScreen
    - Test that ItemsListScreen displays "Lost Items" title
    - Test that dialog closes after selection
    - _Requirements: 2.1, 2.2, 2.3_
  
  - [ ]* 6.3 Write UI test for Found Items navigation
    - Test that selecting "Browse Found Items" navigates to ItemsListScreen
    - Test that ItemsListScreen displays "Found Items" title
    - Test that dialog closes after selection
    - _Requirements: 3.1, 3.2, 3.3_
  
  - [ ]* 6.4 Write UI test for dialog dismissal
    - Test that pressing back button dismisses dialog without navigation
    - Test that tapping outside dialog dismisses it without navigation
    - Test that HomeScreen remains visible after dismissal
    - _Requirements: 1.4, 1.5_
  
  - [ ]* 6.5 Write UI test for dialog reusability
    - Test that dialog can be reopened after dismissal
    - Test that dialog can be reopened after navigation and back
    - _Requirements: 5.2_

- [ ]* 7. Write integration tests for complete navigation flows
  - Test complete flow: HomeScreen → Browse dialog → Lost Items → Back to HomeScreen
  - Test complete flow: HomeScreen → Browse dialog → Found Items → Back to HomeScreen
  - Test that state is properly reset between multiple dialog openings
  - _Requirements: 2.4, 3.4, 5.2_

- [ ] 8. Final checkpoint - Ensure all tests pass
  - Run all unit tests and verify they pass
  - Run all UI tests and verify they pass
  - Verify no regressions in existing Report dialog functionality
  - Ensure all tests pass, ask the user if questions arise

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- The implementation reuses existing navigation callbacks and routes
- No changes needed to AppNavGraph, ItemsListScreen, or data models
- Dialog pattern matches existing Report dialog for UI consistency
- Checkpoints ensure incremental validation and user feedback opportunities
