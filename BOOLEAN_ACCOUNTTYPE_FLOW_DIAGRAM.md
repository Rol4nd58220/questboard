# Boolean AccountType Flow Diagram

## Registration Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                     USER CHOOSES ACCOUNT TYPE                    │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                ┌───────────┴───────────┐
                │                       │
                ▼                       ▼
    ┌───────────────────┐   ┌───────────────────┐
    │  Job Seeker Form  │   │  Employer Form    │
    │ JobSeekerRegister │   │ EmployerRegister  │
    └─────────┬─────────┘   └─────────┬─────────┘
              │                       │
              │ IS_JOB_SEEKER=true    │ IS_JOB_SEEKER=false
              │                       │
              └───────────┬───────────┘
                          │
                          ▼
            ┌─────────────────────────┐
            │ EmailPasswordSetup      │
            │ Activity                │
            │                         │
            │ User creates email/pass │
            └────────────┬────────────┘
                         │
                         ▼
            ┌─────────────────────────┐
            │  Save to Firestore      │
            │  {                      │
            │    isJobSeeker: Boolean │
            │    firstName: "..."     │
            │    ...                  │
            │  }                      │
            └────────────┬────────────┘
                         │
         ┌───────────────┴───────────────┐
         │                               │
         ▼                               ▼
    isJobSeeker = true            isJobSeeker = false
         │                               │
         ▼                               ▼
┌──────────────────┐          ┌──────────────────────┐
│   MainActivity   │          │ EmployerDashboard    │
│ (Job Seeker UI)  │          │ Activity             │
└──────────────────┘          └──────────────────────┘
```

## Login Flow

```
┌─────────────────────────┐
│   User Enters Email     │
│   & Password            │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  Firebase Auth          │
│  signInWithEmail...     │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  Fetch User Document    │
│  from Firestore         │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  Read isJobSeeker field │
│  (Boolean)              │
└────────────┬────────────┘
             │
  ┌──────────┴──────────┐
  │                     │
  ▼                     ▼
true                  false
  │                     │
  ▼                     ▼
┌──────────┐      ┌─────────────┐
│MainActivity│    │ Employer    │
│           │      │ Dashboard   │
└──────────┘      └─────────────┘
```

## Firestore Document Structure

### Job Seeker Document
```json
{
  "userId": "abc123...",
  "isJobSeeker": true,          ← BOOLEAN
  "firstName": "John",
  "middleName": "M",
  "lastName": "Doe",
  "phone": "+1234567890",
  "address1": "123 Main St",
  "address2": "Apt 4B",
  "birthday": "01/15/1990",
  "idType": "National ID",
  "createdAt": 1701388800000
}
```

### Employer Document
```json
{
  "userId": "xyz789...",
  "isJobSeeker": false,         ← BOOLEAN
  "firstName": "Jane",
  "middleName": "L",
  "lastName": "Smith",
  "phone": "+0987654321",
  "address1": "456 Business Ave",
  "address2": "Suite 200",
  "birthday": "03/20/1985",
  "idType": "Driver's License",
  "businessPermitType": "DTI Registration",
  "createdAt": 1701388800000
}
```

## Decision Logic

### Registration
```kotlin
if (isJobSeeker) {
    // Navigate to Job Seeker Dashboard
    startActivity(Intent(this, MainActivity::class.java))
} else {
    // Navigate to Employer Dashboard
    startActivity(Intent(this, EmployerDashboardActivity::class.java))
}
```

### Login
```kotlin
val isJobSeeker = document.getBoolean("isJobSeeker") ?: true

if (isJobSeeker) {
    startActivity(Intent(this, MainActivity::class.java))
} else {
    startActivity(Intent(this, EmployerDashboardActivity::class.java))
}
```

## Key Benefits

✅ **Simple**: Boolean is easier to understand than string comparisons
✅ **Fast**: Boolean comparisons are more efficient
✅ **Safe**: No typos like "job_seeker" vs "jobseeker"
✅ **Clean**: Less code, clearer logic
✅ **Scalable**: Easy to query in Firestore

---
**Implementation Date**: December 1, 2025
**Status**: ✅ COMPLETE

