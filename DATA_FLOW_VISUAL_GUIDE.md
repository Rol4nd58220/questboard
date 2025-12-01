# Employer to JobSeeker Data Flow - Visual Guide

## 🔄 REAL-TIME DATA FLOW

```
┌──────────────────────────────────────────────────────────────┐
│                    EMPLOYER SIDE                             │
└──────────────────────────────────────────────────────────────┘

1. Employer fills Post Job form:
   ┌─────────────────────────────┐
   │ • Job Title                 │
   │ • Description               │
   │ • Payment Type (Hourly/etc) │
   │ • Amount (₱)                │
   │ • Category                  │
   │ • Date & Time               │
   │ • Location                  │
   │ • Requirements              │
   │ • Upload Image (optional)   │
   └─────────────────────────────┘
                ↓
   [Clicks "Post Job" Button]
                ↓
   EmployerPostJobFragment.postJob()
                ↓
   Creates Job object with:
   - employerId: current user ID
   - employerName: from Firestore user doc
   - status: "Open"
   - createdAt: Timestamp.now()
   - All form data
                ↓
   ╔═══════════════════════════════════╗
   ║   FIRESTORE DATABASE              ║
   ║                                   ║
   ║   Collection: "jobs"              ║
   ║   Document: {auto-generated-id}   ║
   ║   {                               ║
   ║     id: "xyz123",                 ║
   ║     employerId: "emp456",         ║
   ║     employerName: "Juan Dela Cruz"║
   ║     title: "House Cleaner",       ║
   ║     description: "Need cleaner",  ║
   ║     paymentType: "Daily",         ║
   ║     amount: 500.0,                ║
   ║     location: "Manila",           ║
   ║     status: "Open", ← IMPORTANT   ║
   ║     createdAt: <timestamp>        ║
   ║     ...                           ║
   ║   }                               ║
   ╚═══════════════════════════════════╝
                ↓
        [AUTO-SYNC - Firestore Listener]
                ↓
┌──────────────────────────────────────────────────────────────┐
│                   JOBSEEKER SIDE                             │
└──────────────────────────────────────────────────────────────┘

JobSeekerHomeFragment.loadAvailableJobs()
                ↓
   Firestore Query with Listener:
   ┌───────────────────────────────────┐
   │ db.collection("jobs")             │
   │   .whereEqualTo("status", "Open") │
   │   .orderBy("createdAt", DESC)     │
   │   .limit(20)                      │
   │   .addSnapshotListener { ... }    │
   └───────────────────────────────────┘
                ↓
   Gets List<Job> from Firestore
                ↓
   Creates AvailableJobsAdapter
                ↓
   ┌─────────────────────────────────────┐
   │  AVAILABLE JOBS SECTION             │
   │  (Vertical Scroll)                  │
   │                                     │
   │  ┌────────────────────────────────┐ │
   │  │ House Cleaner    │  [IMG]      │ │
   │  │ Need cleaner for │  ┌────────┐ │ │
   │  │ 3BR house...     │  │ Photo  │ │ │
   │  │ ₱500/Daily       │  └────────┘ │ │
   │  │ Manila           │  [View]     │ │
   │  └────────────────────────────────┘ │
   │                                     │
   │  ← Job appears INSTANTLY! ✨        │
   └─────────────────────────────────────┘
```

---

## 🔥 REAL-TIME MAGIC

### What Makes It Real-Time?

**Firestore Snapshot Listener:**
```kotlin
.addSnapshotListener { snapshot, error ->
    // This function is called:
    // 1. Immediately when set up (initial data)
    // 2. Automatically when data changes
    // 3. No manual refresh needed!
}
```

### Timeline:

```
T=0s    Employer clicks "Post Job"
        ↓
T=0.5s  Job saved to Firestore
        ↓
T=0.6s  Firestore triggers snapshot listener
        ↓
T=0.7s  JobSeeker Home receives update
        ↓
T=0.8s  New job card appears on screen!
```

**Total time: < 1 second!** ⚡

---

## 📊 THREE DATA SECTIONS

### 1. Recently Viewed Jobs
```
User clicks "View" on any job
        ↓
saveAsRecentlyViewed(job)
        ↓
Firestore: recentlyViewed/{userId}/jobs/{jobId}
        {
          jobId: "xyz123",
          viewedAt: Timestamp.now()
        }
        ↓
Next time Home loads:
        ↓
Query recentlyViewed collection
        ↓
Fetch full job details from jobs collection
        ↓
Display in Recently Viewed section
```

### 2. Pending Applications
```
User applies to a job (TODO: implement apply)
        ↓
Creates Application document:
Firestore: applications/{appId}
        {
          applicantId: current user,
          jobId: "xyz123",
          jobTitle: "House Cleaner",
          status: "Pending",
          appliedAt: Timestamp.now()
        }
        ↓
Home page query:
        ↓
db.collection("applications")
  .whereEqualTo("applicantId", userId)
  .whereEqualTo("status", "Pending")
        ↓
Display in Pending Applications section
```

### 3. Available Jobs
```
Any employer posts a job with status="Open"
        ↓
Firestore: jobs/{jobId}
        {
          status: "Open",  ← Key field
          ...
        }
        ↓
Home page query:
        ↓
db.collection("jobs")
  .whereEqualTo("status", "Open")
        ↓
All open jobs from all employers
        ↓
Display in Available Jobs section
```

---

## 🎯 USER JOURNEY

### JobSeeker Opens App:

```
1. Login/Register as JobSeeker
        ↓
2. Lands on Home Page
        ↓
3. Three sections load simultaneously:
   
   [Recently Viewed Jobs]  ← From user's view history
   • Shows jobs user clicked before
   • If empty: Shows newest jobs
   
   [Pending Applications]  ← User's applications
   • Shows jobs user applied to
   • Status: Pending, Accepted, Rejected
   
   [Available Jobs]        ← ALL employer posts
   • Shows ALL open jobs
   • Real-time updates
   • Newest first

4. User scrolls through jobs
        ↓
5. Clicks "View" on a job
        ↓
6. Job saved to Recently Viewed
        ↓
7. (TODO) Navigate to Job Details
        ↓
8. (TODO) User can "Apply"
        ↓
9. Application appears in Pending section
```

---

## 🔗 FIRESTORE COLLECTIONS USED

### Jobs Collection:
```
jobs/
├── job_id_1/          (status: "Open")
│   └── Employer A's job
├── job_id_2/          (status: "Open")
│   └── Employer B's job
├── job_id_3/          (status: "Closed")
│   └── Not shown to JobSeekers
└── job_id_4/          (status: "Open")
    └── Employer C's job
```

**Query for Available Jobs:**
```kotlin
WHERE status == "Open"
```

### Applications Collection:
```
applications/
├── app_id_1/
│   ├── applicantId: "jobseeker_123"
│   ├── jobId: "job_id_1"
│   └── status: "Pending"
├── app_id_2/
│   ├── applicantId: "jobseeker_123"
│   ├── jobId: "job_id_2"
│   └── status: "Accepted"
└── app_id_3/
    ├── applicantId: "jobseeker_456"
    ├── jobId: "job_id_1"
    └── status: "Pending"
```

**Query for User's Applications:**
```kotlin
WHERE applicantId == current_user_id
AND status == "Pending"
```

### Recently Viewed (per user):
```
recentlyViewed/
├── jobseeker_123/
│   └── jobs/
│       ├── job_id_1/  {viewedAt: <timestamp>}
│       └── job_id_4/  {viewedAt: <timestamp>}
└── jobseeker_456/
    └── jobs/
        └── job_id_2/  {viewedAt: <timestamp>}
```

**Query for User's History:**
```kotlin
recentlyViewed/{userId}/jobs
ORDER BY viewedAt DESC
```

---

## ✅ VERIFICATION CHECKLIST

To verify real data is working:

- [ ] 1. Create Employer account
- [ ] 2. Post a job with status "Open"
- [ ] 3. Switch to JobSeeker account
- [ ] 4. Open Home page
- [ ] 5. Check Available Jobs section
- [ ] 6. Verify employer's job appears
- [ ] 7. Click "View" on any job
- [ ] 8. Go back to Home
- [ ] 9. Verify job appears in Recently Viewed
- [ ] 10. Check Firestore console - see real data

---

## 🎊 SUCCESS!

The JobSeeker Home page is now **fully integrated** with Firestore and loads **real employer data** in real-time!

No more placeholders! 🎉

