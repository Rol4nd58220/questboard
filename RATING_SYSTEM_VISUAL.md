# Rating System - Visual Flow

## Complete Rating Update Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    EMPLOYER REVIEW PROCESS                       │
└─────────────────────────────────────────────────────────────────┘

Step 1: Job Seeker Confirms Job Completion
┌──────────────────────────────┐
│  Job Seeker                  │
│  ┌────────────────────────┐  │
│  │ ✓ Job Completed        │  │
│  │ □ Has Issues           │  │
│  │ Notes: ...             │  │
│  └────────────────────────┘  │
│  [Confirm Completion]        │
└──────────────────────────────┘
           ↓
    Saves to jobCompletions
           ↓

Step 2: Employer Sees "Review Completion" Button
┌──────────────────────────────┐
│  Employer Applicants Tab     │
│  ┌────────────────────────┐  │
│  │ John Smith             │  │
│  │ Status: Completed      │  │
│  │ [Review Completion]    │  │
│  └────────────────────────┘  │
└──────────────────────────────┘
           ↓
    Clicks Review Button
           ↓

Step 3: Employer Reviews Job
┌──────────────────────────────────────────┐
│  Review Completion                        │
│  ┌────────────────────────────────────┐  │
│  │ Job Seeker: John Smith             │  │
│  │ Job: House Cleaning                │  │
│  │ Status: ✓ Completed                │  │
│  ├────────────────────────────────────┤  │
│  │ ☑ I confirm job completed          │  │
│  │                                    │  │
│  │ Feedback:                          │  │
│  │ ┌────────────────────────────────┐ │  │
│  │ │ Excellent work! Very thorough  │ │  │
│  │ │ and professional.              │ │  │
│  │ └────────────────────────────────┘ │  │
│  │                                    │  │
│  │ Rating:  ★ ★ ★ ★ ★                │  │
│  │                                    │  │
│  │ Payment: ● GCash  ○ Cash          │  │
│  │ ☑ Payment confirmed                │  │
│  │                                    │  │
│  │ [Submit Review]                    │  │
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
           ↓
      Employer clicks Submit
           ↓

Step 4: System Processes Review
┌──────────────────────────────────────────┐
│  Backend Processing                       │
│                                           │
│  1. Save review to jobCompletions        │
│     ├─ employerRating: 5.0               │
│     ├─ employerFeedback: "Excellent..."  │
│     ├─ reviewedByEmployer: true          │
│     └─ paymentReleased: true             │
│                                           │
│  2. Update application status            │
│     └─ status: "Reviewed"                │
│                                           │
│  3. Query all job seeker's reviews       │
│     ├─ Find: 3 previous reviews          │
│     └─ Ratings: [5, 4, 5]                │
│                                           │
│  4. Calculate new average                │
│     ├─ Total: 5 + 4 + 5 + 5 = 19        │
│     ├─ Count: 4 reviews                  │
│     └─ Average: 19 / 4 = 4.75           │
│                                           │
│  5. Update job seeker profile            │
│     ├─ averageRating: 4.75               │
│     ├─ totalReviews: 4                   │
│     ├─ jobsCompleted: 4                  │
│     └─ lastRatingUpdate: now()           │
└──────────────────────────────────────────┘
           ↓
      Success!
           ↓

Step 5: Confirmation Shown to Employer
┌──────────────────────────────────────────┐
│  Review Submitted ✓                      │
│                                           │
│  Your review has been submitted!          │
│                                           │
│  Rating: ★★★★★                           │
│  Feedback: "Excellent work! Very..."      │
│                                           │
│  Job seeker's rating updated.             │
│                                           │
│  [OK]                                     │
└──────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────┐
│                JOB SEEKER PROFILE - UPDATED RATING               │
└─────────────────────────────────────────────────────────────────┘

BEFORE Review:
┌──────────────────────────────┐
│  Credibility                 │
│                              │
│  ★ 4.5 /5.0    (3 reviews)  │
│  ★★★★☆                      │
│  Jobs Completed: 3           │
└──────────────────────────────┘

AFTER Review:
┌──────────────────────────────┐
│  Credibility                 │
│                              │
│  ★ 4.75 /5.0   (4 reviews)  │  ← Updated!
│  ★★★★★                      │  ← Updated!
│  Jobs Completed: 4           │  ← Updated!
└──────────────────────────────┘


┌─────────────────────────────────────────────────────────────────┐
│                    RATING CALCULATION EXAMPLE                    │
└─────────────────────────────────────────────────────────────────┘

Job Seeker: John Smith

Past Reviews:
  Job 1: House Cleaning    → 5 stars
  Job 2: Lawn Mowing       → 4 stars
  Job 3: Painting          → 5 stars

New Review:
  Job 4: Garden Work       → 5 stars

Calculation:
  Total = 5 + 4 + 5 + 5 = 19
  Count = 4 reviews
  Average = 19 ÷ 4 = 4.75 stars

Result:
  ✓ Profile updated with 4.75 average
  ✓ Shows 4 total reviews
  ✓ Shows 4 jobs completed
  ✓ Visual rating bar shows 4.75 stars


┌─────────────────────────────────────────────────────────────────┐
│                        DATA STRUCTURE                            │
└─────────────────────────────────────────────────────────────────┘

Firestore Collections:

jobCompletions/
  └─ {completionId}
       ├─ applicationId: "abc123"
       ├─ jobId: "job456"
       ├─ jobSeekerId: "user789"          ← Links to job seeker
       ├─ employerId: "emp012"
       ├─ employerRating: 5.0             ← Rating given
       ├─ employerFeedback: "Great!"
       ├─ reviewedByEmployer: true
       ├─ employerReviewed: true          ← Used in query
       └─ submittedAt: Timestamp

users/
  └─ {userId}  (Job Seeker)
       ├─ firstName: "John"
       ├─ middleName: "Michael"
       ├─ lastName: "Smith"
       ├─ averageRating: 4.75             ← Calculated average
       ├─ totalReviews: 4                 ← Count of reviews
       ├─ jobsCompleted: 4                ← Total jobs done
       └─ lastRatingUpdate: Timestamp     ← When last updated


┌─────────────────────────────────────────────────────────────────┐
│                      QUERY OPTIMIZATION                          │
└─────────────────────────────────────────────────────────────────┘

Old Approach (Slow):
  Profile loads → Query all jobCompletions → Calculate rating
  Every profile view = Database query

New Approach (Fast):
  Profile loads → Read averageRating from user profile
  No extra query needed!

Fallback:
  If averageRating not found → Calculate from jobCompletions
  (For backward compatibility with old data)


┌─────────────────────────────────────────────────────────────────┐
│                    ERROR HANDLING FLOW                           │
└─────────────────────────────────────────────────────────────────┘

Review Submission Success:
  ✓ Review saved
  ✓ Rating calculated
  ✓ Profile updated
  → Show: "Review submitted! Job seeker's rating updated."

Review Saved, Rating Update Failed:
  ✓ Review saved
  ✓ Rating calculated
  ✗ Profile update failed
  → Show: "Review submitted! (Rating update pending)"
  → Log error for debugging

Rating Calculation Failed:
  ✓ Review saved
  ✗ Rating calculation failed
  → Show: "Review submitted! (Rating calculation pending)"
  → Log error for debugging


┌─────────────────────────────────────────────────────────────────┐
│                    BENEFITS SUMMARY                              │
└─────────────────────────────────────────────────────────────────┘

For Job Seekers:
  ✓ Build credible reputation
  ✓ Higher ratings = More job opportunities
  ✓ Transparent feedback from employers
  ✓ Verified track record

For Employers:
  ✓ See job seeker's performance history
  ✓ Make informed hiring decisions
  ✓ Read feedback from other employers
  ✓ Trust in platform quality

For Platform:
  ✓ Automatic updates (no manual work)
  ✓ Accurate calculations
  ✓ Scalable architecture
  ✓ Enhanced trust and credibility
```

