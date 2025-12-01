__# Firestore Boolean AccountType - Quick Reference

## Database Field
```
Field Name: isJobSeeker
Type: Boolean
Values: true | false
```

## Boolean Logic
```kotlin
isJobSeeker = true  → Job Seeker  → MainActivity
isJobSeeker = false → Employer    → EmployerDashboardActivity
```

## Code Examples

### Saving to Firestore (Registration)
```kotlin
val userData = hashMapOf(
    "isJobSeeker" to isJobSeeker, // Boolean
    // ... other fields
)
db.collection("users").document(userId).set(userData)
```

### Reading from Firestore (Login)
```kotlin
db.collection("users").document(userId)
    .get()
    .addOnSuccessListener { document ->
        val isJobSeeker = document.getBoolean("isJobSeeker") ?: true
        
        val intent = if (isJobSeeker) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, EmployerDashboardActivity::class.java)
        }
        startActivity(intent)
    }
```

### Passing Between Activities
```kotlin
// Job Seeker Registration
intent.putExtra("IS_JOB_SEEKER", true)

// Employer Registration
intent.putExtra("IS_JOB_SEEKER", false)

// Receiving in EmailPasswordSetupActivity
isJobSeeker = intent.getBooleanExtra("IS_JOB_SEEKER", true)
```

## Files Modified
1. ✅ EmailPasswordSetupActivity.kt
2. ✅ SignIn.kt
3. ✅ JobSeekerRegister.kt
4. ✅ EmployerRegister.kt

## Status: READY TO USE 🚀

