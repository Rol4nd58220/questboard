# Edit Profile Screen - Visual Layout

```
┌─────────────────────────────────────────┐
│  ← Back     Edit Profile                │  Header
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│         ┌────────────────┐              │
│         │ [Profile Photo]│              │  Profile Photo
│         │    120x120     │   ┌─┐        │  with Add/Change
│         └────────────────┘   │+│        │  button overlay
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  First Name *                           │
│  ┌───────────────────────────────────┐  │
│  │ John                              │  │
│  └───────────────────────────────────┘  │
│                                         │
│  Middle Name (Optional)                 │
│  ┌───────────────────────────────────┐  │
│  │ Michael                           │  │
│  └───────────────────────────────────┘  │
│                                         │
│  Last Name *                            │
│  ┌───────────────────────────────────┐  │
│  │ Smith                             │  │
│  └───────────────────────────────────┘  │
│                                         │
│  About Me                               │
│  ┌───────────────────────────────────┐  │
│  │ Experienced handyman with 5       │  │
│  │ years of experience in home       │  │  Multi-line
│  │ repairs and maintenance...        │  │  text field
│  │                                   │  │
│  └───────────────────────────────────┘  │
│                                         │
│  Location                               │
│  ┌───────────────────────────────────┐  │
│  │ Manila, Philippines               │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │      Save Changes                 │  │  Primary Button
│  └───────────────────────────────────┘  │  (Orange)
│  ┌───────────────────────────────────┐  │
│  │      Cancel                       │  │  Secondary Button
│  └───────────────────────────────────┘  │  (Gray)
└─────────────────────────────────────────┘
```

## Field Details

### First Name *
- **Type**: Single-line text input
- **Required**: Yes
- **Validation**: Must not be empty
- **Capitalization**: Auto-capitalize words
- **Autofill**: Person name
- **Hint**: "Enter your first name"

### Middle Name (Optional)
- **Type**: Single-line text input
- **Required**: No
- **Validation**: None
- **Capitalization**: Auto-capitalize words
- **Autofill**: Person name
- **Hint**: "Enter your middle name"

### Last Name *
- **Type**: Single-line text input
- **Required**: Yes
- **Validation**: Must not be empty
- **Capitalization**: Auto-capitalize words
- **Autofill**: Person name
- **Hint**: "Enter your last name"

### About Me
- **Type**: Multi-line text input (4 lines)
- **Required**: No
- **Min Height**: 100dp
- **Capitalization**: Auto-capitalize sentences
- **Hint**: "Tell us about yourself, your skills, and experience"

### Location
- **Type**: Single-line text input
- **Required**: No
- **Capitalization**: Auto-capitalize words
- **Autofill**: Postal address
- **Hint**: "Enter your location (e.g., Manila, Philippines)"

## Button Behaviors

### Save Changes
- **Action**: Validates and saves profile
- **States**:
  - Normal: "Save Changes" (enabled, orange)
  - Loading: "Saving..." (disabled, orange)
  - Success: Shows toast, returns to profile
  - Error: Shows toast, re-enables button

### Cancel
- **Action**: Discards changes and returns to profile
- **No confirmation**: Immediately returns
- **Color**: Gray

### Back Button (←)
- **Action**: Same as Cancel button
- **Location**: Top-left corner

## Validation Messages

### First Name Empty
```
Error: "First name is required"
Focus: First name field
```

### Last Name Empty
```
Error: "Last name is required"
Focus: Last name field
```

## Success Messages

### Profile Saved
```
Toast: "Profile updated successfully"
Action: Return to profile with result
```

### Photo Upload (Coming Soon)
```
Toast: "Photo upload coming soon"
```

## Error Messages

### Save Failed
```
Toast: "Failed to save profile"
Log: Error details
Button: Re-enabled for retry
```

### Load Failed
```
Toast: "Failed to load profile"
Action: Finish activity
```

## Color Scheme
Same as main profile page:
- Background: #1A1A1A (dark)
- Input fields: Card background with border
- Text: White (primary), Gray (hints)
- Buttons: Orange (primary), Gray (secondary)

## Keyboard Behavior
- **adjustResize**: Layout adjusts when keyboard appears
- **Enter key**: Moves to next field
- **Done button**: On last field (Location)

## Data Flow

```
1. User clicks "Edit Profile" on Profile page
2. EditProfileActivity opens
3. Loads current data from Firestore
4. Pre-fills all fields
5. User edits fields
6. User clicks "Save Changes"
7. Validates required fields
8. Builds fullName from parts
9. Updates Firestore:
   - firstName
   - middleName
   - lastName
   - fullName (auto-built)
   - bio
   - location
   - updatedAt (timestamp)
10. Returns to profile with RESULT_OK
11. Profile reloads data
12. User sees updated information
```

## Testing Scenarios

✅ Edit only first name
✅ Edit only last name
✅ Add/remove middle name
✅ Edit bio with long text
✅ Edit location
✅ Leave all optional fields empty
✅ Cancel without saving
✅ Back button without saving
✅ Save with validation errors
✅ Save successfully
✅ Network error during save

