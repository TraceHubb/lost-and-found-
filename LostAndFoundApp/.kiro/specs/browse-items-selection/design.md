# Design Document: Browse Items Selection

## Overview

This design implements a selection dialog for the Browse feature in the Lost and Found Android app. When users tap the Browse button on the HomeScreen, they will see an AlertDialog offering two options: "Browse Lost Items" or "Browse Found Items". This enhancement mirrors the existing Report feature's dialog pattern, providing a consistent user experience.

The implementation is straightforward and leverages existing Jetpack Compose patterns already used in the HomeScreen for the Report dialog. No new navigation routes or data models are required—the feature simply adds UI state management and reuses existing navigation callbacks.

**Key Design Principles:**
- **Consistency**: Match the existing Report dialog pattern for familiar UX
- **Simplicity**: Minimal code changes, maximum reuse of existing components
- **Composability**: Follow Compose best practices for state management and UI composition

## Architecture

### Component Overview

The feature involves modifications to a single screen component:

```
HomeScreen (Modified)
├── showBrowseDialog: Boolean (new state)
├── BrowseSelectionDialog (new composable)
│   ├── AlertDialog
│   │   ├── Title: "Browse Items"
│   │   ├── Text: "What would you like to browse?"
│   │   ├── ConfirmButton: "Browse Lost Items" → onBrowseLost()
│   │   └── DismissButton: "Browse Found Items" → onBrowseFound()
└── QuickActionIcon (Browse) → onClick sets showBrowseDialog = true
```

### Architectural Pattern

This feature follows the **Unidirectional Data Flow** pattern common in Jetpack Compose:

1. **State**: `showBrowseDialog` boolean managed by `remember { mutableStateOf(false) }`
2. **Events**: User taps Browse button → state changes to `true`
3. **UI Update**: Dialog becomes visible when state is `true`
4. **Actions**: User selects option → callback invoked, state resets to `false`

### Integration Points

- **HomeScreen.kt**: Add dialog state and composable
- **AppNavGraph.kt**: No changes (existing routes handle navigation)
- **ItemsListScreen.kt**: No changes (already supports both ItemType.LOST and ItemType.FOUND)

## Components and Interfaces

### Modified Component: HomeScreen

**Location**: `app/src/main/java/com/lostandfound/presentation/home/HomeScreen.kt`

**New State Variable:**
```kotlin
var showBrowseDialog by remember { mutableStateOf(false) }
```

**Modified QuickActionIcon (Browse):**
```kotlin
QuickActionIcon(
    icon = Icons.Default.Search,
    label = "Browse",
    color = Color(0xFF9E9E9E),
    onClick = { showBrowseDialog = true }  // Changed from onBrowseLost
)
```

**New Composable Function:**
```kotlin
@Composable
private fun BrowseSelectionDialog(
    onDismiss: () -> Unit,
    onBrowseLost: () -> Unit,
    onBrowseFound: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Browse Items") },
        text = { Text("What would you like to browse?") },
        confirmButton = {
            Button(onClick = {
                onDismiss()
                onBrowseLost()
            }) {
                Text("Browse Lost Items")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                onDismiss()
                onBrowseFound()
            }) {
                Text("Browse Found Items")
            }
        }
    )
}
```

**Dialog Invocation:**
```kotlin
if (showBrowseDialog) {
    BrowseSelectionDialog(
        onDismiss = { showBrowseDialog = false },
        onBrowseLost = onBrowseLost,
        onBrowseFound = onBrowseFound
    )
}
```

### Component Responsibilities

| Component | Responsibility |
|-----------|---------------|
| HomeScreen | Manage dialog visibility state, render dialog when needed |
| BrowseSelectionDialog | Present options, handle user selection, invoke callbacks |
| AppNavGraph | Navigate to appropriate ItemsListScreen (existing functionality) |
| ItemsListScreen | Display filtered items (existing functionality) |

### Interface Contracts

**HomeScreen Parameters (unchanged):**
```kotlin
@Composable
fun HomeScreen(
    onReportLost: () -> Unit,
    onReportFound: () -> Unit,
    onBrowseLost: () -> Unit,      // Existing callback
    onBrowseFound: () -> Unit,     // Existing callback
    onLogout: () -> Unit
)
```

**BrowseSelectionDialog Parameters:**
```kotlin
@Composable
private fun BrowseSelectionDialog(
    onDismiss: () -> Unit,         // Called when dialog is dismissed
    onBrowseLost: () -> Unit,      // Called when "Browse Lost Items" selected
    onBrowseFound: () -> Unit      // Called when "Browse Found Items" selected
)
```

## Data Models

**No new data models required.**

The feature uses existing types:
- `ItemType.LOST` and `ItemType.FOUND` (already defined in `com.lostandfound.data.models.ItemType`)
- Navigation routes: `"items/${ItemType.LOST.name}"` and `"items/${ItemType.FOUND.name}"` (already configured in AppNavGraph)

## Error Handling

### Error Scenarios and Mitigation

| Scenario | Mitigation | Implementation |
|----------|-----------|----------------|
| User taps outside dialog | Dialog dismisses gracefully | `onDismissRequest = { showBrowseDialog = false }` |
| Rapid button taps | State resets prevent multiple navigations | Dialog closes before callback invoked |
| Navigation callback is null | Compile-time safety via required parameters | Kotlin's type system enforces non-null callbacks |
| State not reset after navigation | Explicit state reset in all code paths | `onDismiss()` called before navigation callbacks |

### State Management Edge Cases

**Scenario**: User taps Browse, dialog appears, user presses back button
- **Behavior**: Dialog dismisses via `onDismissRequest`
- **State**: `showBrowseDialog` set to `false`
- **Result**: User remains on HomeScreen

**Scenario**: User selects an option, then immediately taps Browse again
- **Behavior**: Dialog state resets to `false` after selection, can be reopened
- **State**: No lingering state issues
- **Result**: Dialog functions correctly on subsequent uses

## Testing Strategy

This feature requires a combination of **UI tests** and **unit tests** to ensure correctness.

### Unit Testing Approach

**Focus Areas:**
1. **State Management**: Verify dialog visibility state changes correctly
2. **Callback Invocation**: Verify correct callbacks are invoked for each selection
3. **Edge Cases**: Verify dialog dismissal without selection

**Test Cases:**

```kotlin
class HomeScreenTest {
    
    @Test
    fun `when Browse button clicked, dialog state becomes true`() {
        // Arrange: Initial state with showBrowseDialog = false
        // Act: Simulate Browse button click
        // Assert: showBrowseDialog == true
    }
    
    @Test
    fun `when Browse Lost Items selected, onBrowseLost callback invoked`() {
        // Arrange: Dialog visible
        // Act: Select "Browse Lost Items"
        // Assert: onBrowseLost was called, showBrowseDialog == false
    }
    
    @Test
    fun `when Browse Found Items selected, onBrowseFound callback invoked`() {
        // Arrange: Dialog visible
        // Act: Select "Browse Found Items"
        // Assert: onBrowseFound was called, showBrowseDialog == false
    }
    
    @Test
    fun `when dialog dismissed without selection, no navigation occurs`() {
        // Arrange: Dialog visible
        // Act: Dismiss dialog (tap outside or back button)
        // Assert: Neither callback invoked, showBrowseDialog == false
    }
    
    @Test
    fun `dialog can be reopened after dismissal`() {
        // Arrange: Dialog opened and dismissed
        // Act: Click Browse button again
        // Assert: showBrowseDialog == true
    }
}
```

### UI Testing Approach

**Focus Areas:**
1. **Visual Verification**: Dialog appears with correct text and buttons
2. **Navigation Flow**: End-to-end navigation to ItemsListScreen
3. **Consistency**: Dialog matches Report dialog styling

**Test Cases:**

```kotlin
@RunWith(AndroidJUnit4::class)
class BrowseSelectionUITest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun browseButton_whenClicked_showsDialog() {
        // Arrange: Launch HomeScreen
        // Act: Click Browse button
        // Assert: Dialog with "Browse Items" title is displayed
    }
    
    @Test
    fun browseDialog_displaysCorrectOptions() {
        // Arrange: Open browse dialog
        // Assert: "Browse Lost Items" button exists
        // Assert: "Browse Found Items" button exists
        // Assert: Dialog title is "Browse Items"
        // Assert: Dialog text is "What would you like to browse?"
    }
    
    @Test
    fun browseLostItems_navigatesToLostItemsList() {
        // Arrange: Open browse dialog
        // Act: Click "Browse Lost Items"
        // Assert: ItemsListScreen displayed with "Lost Items" title
    }
    
    @Test
    fun browseFoundItems_navigatesToFoundItemsList() {
        // Arrange: Open browse dialog
        // Act: Click "Browse Found Items"
        // Assert: ItemsListScreen displayed with "Found Items" title
    }
    
    @Test
    fun browseDialog_dismissesOnBackPress() {
        // Arrange: Open browse dialog
        // Act: Press back button
        // Assert: Dialog not displayed, HomeScreen still visible
    }
}
```

### Integration Testing

**Focus**: Verify the complete flow from HomeScreen → Dialog → ItemsListScreen → Back to HomeScreen

```kotlin
@Test
fun completeNavigationFlow_browseLostAndReturn() {
    // 1. Start on HomeScreen
    // 2. Click Browse button
    // 3. Verify dialog appears
    // 4. Click "Browse Lost Items"
    // 5. Verify ItemsListScreen with LOST type
    // 6. Click back button
    // 7. Verify returned to HomeScreen
}
```

### Manual Testing Checklist

- [ ] Browse button opens dialog
- [ ] Dialog title and text are correct
- [ ] "Browse Lost Items" navigates to Lost items list
- [ ] "Browse Found Items" navigates to Found items list
- [ ] Dialog dismisses when tapping outside
- [ ] Dialog dismisses on back button press
- [ ] No navigation occurs when dialog is dismissed without selection
- [ ] Dialog can be reopened after dismissal
- [ ] Dialog styling matches Report dialog
- [ ] Back button from ItemsListScreen returns to HomeScreen

### Test Coverage Goals

- **Unit Tests**: 100% coverage of state management logic
- **UI Tests**: All user interaction paths covered
- **Integration Tests**: Complete navigation flows verified

### Testing Tools

- **JUnit 4**: Unit test framework
- **Compose Testing Library**: UI component testing
- **Mockito/MockK**: Mocking navigation callbacks
- **Espresso**: Android UI testing (if needed for integration tests)

**Note**: Property-based testing is not applicable for this feature as it involves UI interactions and navigation logic rather than data transformations or algorithms with universal properties.

