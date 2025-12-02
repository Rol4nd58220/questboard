# Re-enabling Image Upload - Quick Guide

## Current Status
Image upload functionality is **disabled** but all code is preserved with comments for easy re-enabling later.

## How to Re-enable Image Upload

### Step 1: Update Layout XML
**File:** `res/layout/dialog_job_completion.xml`

Find the "Work Photos Section" and update visibility:

```xml
<!-- Change from visibility="gone" to visibility="visible" -->
<TextView
    android:id="@+id/tvWorkPhotosLabel"
    android:visibility="visible"/>  <!-- Change this -->

<TextView
    android:visibility="visible"/>  <!-- Change this -->

<Button
    android:id="@+id/btnUploadPhoto"
    android:visibility="visible"    <!-- Change this -->
    android:enabled="true"/>         <!-- Change this -->
```

### Step 2: Uncomment Imports
**File:** `JobSeekerJobsFragment.kt`

Uncomment these imports at the top:
```kotlin
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
```

### Step 3: Uncomment Properties
In the class declaration, uncomment:
```kotlin
private lateinit var storage: FirebaseStorage
private var selectedImageUri: Uri? = null
private var currentCompletionDialog: AlertDialog? = null

// Image picker launcher
private val imagePickerLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        result.data?.data?.let { uri ->
            selectedImageUri = uri
            updateImagePreview(uri)
        }
    }
}
```

### Step 4: Initialize Firebase Storage
In `onViewCreated()`, uncomment:
```kotlin
storage = FirebaseStorage.getInstance()
```

### Step 5: Reset Image URI
In `showJobCompletionForm()`, uncomment:
```kotlin
selectedImageUri = null
```

### Step 6: Uncomment UI Elements
In `showJobCompletionForm()`, uncomment these view references:
```kotlin
val btnUploadPhoto = dialogView.findViewById<Button>(R.id.btnUploadPhoto)
val cvImagePreview = dialogView.findViewById<CardView>(R.id.cvImagePreview)
val ivWorkPhoto = dialogView.findViewById<ImageView>(R.id.ivWorkPhoto)
val btnRemovePhoto = dialogView.findViewById<ImageButton>(R.id.btnRemovePhoto)
```

### Step 7: Uncomment Button Handlers
Uncomment the entire block:
```kotlin
// Handle image upload
btnUploadPhoto.setOnClickListener {
    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    imagePickerLauncher.launch(intent)
}

btnRemovePhoto.setOnClickListener {
    selectedImageUri = null
    cvImagePreview.visibility = View.GONE
    ivWorkPhoto.setImageURI(null)
}
```

### Step 8: Update Submission Logic
In `submitJobCompletion()`, replace the current logic with the commented block:
```kotlin
// Remove this:
saveCompletionData(
    application, job, currentUser.uid, isCompleted, hasIssues, 
    concerns, "", additionalNotes, progressDialog, dialog
)

// Uncomment this:
if (selectedImageUri != null) {
    uploadWorkPhoto(selectedImageUri!!) { imageUrl ->
        saveCompletionData(
            application, job, currentUser.uid, isCompleted, hasIssues, 
            concerns, imageUrl, additionalNotes, progressDialog, dialog
        )
    }
} else {
    saveCompletionData(
        application, job, currentUser.uid, isCompleted, hasIssues, 
        concerns, "", additionalNotes, progressDialog, dialog
    )
}
```

### Step 9: Uncomment Helper Functions
Uncomment these two complete functions:
```kotlin
private fun updateImagePreview(uri: Uri) { ... }
private fun uploadWorkPhoto(imageUri: Uri, onSuccess: (String) -> Unit) { ... }
```

### Step 10: Deploy Firebase Storage Rules
Deploy the `storage.rules` file to Firebase Console:
https://console.firebase.google.com/project/questboard-78a7a/storage/rules

### Step 11: Test
1. Clean and rebuild the app
2. Test image selection
3. Test image upload
4. Verify image appears in Firebase Storage

---

## Summary

All the code is preserved in comments. Just:
1. Change XML visibility from "gone" to "visible"
2. Uncomment all the marked sections in JobSeekerJobsFragment.kt
3. Deploy storage rules
4. Test!

**Estimated Time:** 10-15 minutes

