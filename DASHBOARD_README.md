# QuestBoard - Employer Dashboard UI

This design recreates the employer dashboard screen for the QuestBoard job posting application.

## Files Created

### Layouts
1. **activity_employer_dashboard.xml** - Main dashboard layout with:
   - Top bar with menu, QuestBoard logo, and notification bell
   - Overview section with 4 statistics cards (Total Jobs Posted, Active Jobs, Pending Applicants, Completed Jobs)
   - "View All Job Posts" button
   - Active Job Posts section with RecyclerView
   - Bottom navigation

2. **item_active_job_post.xml** - Job post card layout with:
   - Posted time
   - Job title
   - Status and applicants count
   - "View Applicants" button with arrow
   - Job image thumbnail

### Drawables
1. **stat_card_background.xml** - Rounded card background for statistics
2. **job_card_background.xml** - Background for job post cards
3. **button_view_applicants.xml** - Button background style
4. **ic_arrow_right.xml** - Arrow icon for buttons
5. **ic_bell.xml** - Notification bell icon

### Colors (colors.xml)
- `background_dark` (#1A1A1A) - Main background
- `card_background` (#252525) - Statistics cards
- `card_background_darker` (#1E1E1E) - Job post cards
- `text_primary` (#FFFFFF) - Main text
- `text_secondary` (#B0B0B0) - Secondary text
- `text_tertiary` (#808080) - Tertiary text
- `accent_orange` (#FF9800) - Brand color
- `button_dark` (#2A2A2A) - Button background
- `border_card` (#2A2A2A) - Card borders
- `bottom_nav_background` (#1E1E1E) - Bottom nav background

### Color Selector
**bottom_nav_color.xml** - Color state selector for bottom navigation items

### Menu
**bottom_nav_menu_employer.xml** - Bottom navigation menu with 5 items:
- My Jobs
- Applicants
- Post Job
- Messages
- Profile

### Kotlin Classes
1. **EmployerDashboardActivity.kt** - Main activity for the dashboard
   - Initializes all views
   - Sets up RecyclerView
   - Handles bottom navigation
   - Uses UIHelper to style the QuestBoard title

2. **ActiveJobPostsAdapter.kt** - RecyclerView adapter for job posts
   - JobPost data class
   - ViewHolder pattern
   - Click listener for "View Applicants" button

3. **UIHelper.kt** - Helper class for UI utilities
   - Sets colored text for "QuestBoard" (orange "Q", white rest)

### Strings (strings.xml)
Added strings for:
- Dashboard labels
- Button text
- Bottom navigation items
- Formatted strings with placeholders

## Design Features

### Dark Theme
The entire design uses a dark color scheme with:
- Dark backgrounds (#1A1A1A)
- Card-based layout with subtle borders
- White text for contrast
- Orange accent color (#FF9800) for branding

### Statistics Cards
- 2x2 grid layout
- Rounded corners (16dp)
- Large numbers (32sp) for statistics
- Small labels (12sp) for descriptions

### Job Post Cards
- Horizontal layout with image on the right
- Posted time, title, and status information
- Action button with arrow icon
- 100dp square image thumbnail

### Bottom Navigation
- 5 items with icons and labels
- Always visible labels
- Color state selector for active/inactive states

## Usage

### In Activity
```kotlin
class EmployerDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employer_dashboard)
        
        // Style the app name with orange "Q"
        UIHelper.setQuestBoardTitle(findViewById(R.id.tvAppName))
        
        // Setup RecyclerView with sample data
        val jobPosts = listOf(
            JobPost("1", "Errand Runner", "2 days ago", "Open", 3),
            JobPost("2", "Event Setup Crew", "5 days ago", "Open", 3),
            JobPost("3", "Construction Helper", "1 week ago", "Open", 10)
        )
        
        val adapter = ActiveJobPostsAdapter(jobPosts) { jobPost ->
            // Handle view applicants click
        }
        
        findViewById<RecyclerView>(R.id.rvActiveJobPosts).apply {
            layoutManager = LinearLayoutManager(this@EmployerDashboardActivity)
            this.adapter = adapter
        }
    }
}
```

## Notes

- The design uses Material Design 3 components (CardView, BottomNavigationView)
- RecyclerView is used for efficient scrolling of job posts
- All text uses string resources for internationalization
- Images can be loaded using Glide, Picasso, or Coil
- The layout is responsive and uses ConstraintLayout and CoordinatorLayout
- Bottom navigation is fixed at the bottom with the main content scrollable above it

## Requirements

Add these dependencies to your app's `build.gradle.kts`:
```kotlin
implementation("androidx.cardview:cardview:1.0.0")
implementation("com.google.android.material:material:1.11.0")
implementation("androidx.recyclerview:recyclerview:1.3.2")
implementation("androidx.constraintlayout:constraintlayout:2.1.4")
```

