# Glide Integration Guide for Profile Page

## Current Status
Glide dependency has been added to `app/build.gradle.kts` but not yet integrated into the code to avoid compilation errors before Gradle sync.

## Dependency Added
```kotlin
implementation("com.github.bumptech.glide:glide:4.16.0")
```

## Integration Steps

### 1. After Gradle Sync Completes
Once the Gradle sync finishes successfully, add the Glide import to `JobSeekerProfileFragment.kt`:

```kotlin
import com.bumptech.glide.Glide
```

### 2. Update Profile Photo Loading
Replace the TODO comment in `loadUserProfile()` with actual Glide code:

```kotlin
// Load profile photo if available
if (profilePhotoUrl != null) {
    view?.findViewById<ImageView>(R.id.imgProfilePhoto)?.let { imageView ->
        Glide.with(this)
            .load(profilePhotoUrl)
            .placeholder(R.drawable.avatar_placeholder)
            .error(R.drawable.avatar_placeholder)
            .circleCrop()
            .into(imageView)
    }
}
```

### 3. Enable Photo Upload Feature
Uncomment or add Firebase Storage integration:

```kotlin
import com.google.firebase.storage.FirebaseStorage

// In class
private lateinit var storage: FirebaseStorage

// In onViewCreated
storage = FirebaseStorage.getInstance()

// Update uploadProfilePhoto function
private fun uploadProfilePhoto(uri: Uri) {
    val userId = auth.currentUser?.uid ?: return
    val storageRef = storage.reference.child("profile_photos/$userId.jpg")

    Toast.makeText(context, "Uploading photo...", Toast.LENGTH_SHORT).show()

    storageRef.putFile(uri)
        .addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                db.collection("users").document(userId)
                    .update("profilePhotoUrl", downloadUrl.toString())
                    .addOnSuccessListener {
                        Toast.makeText(context, "Profile photo updated", Toast.LENGTH_SHORT).show()
                        loadUserProfile() // Reload to show new photo
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProfileFragment", "Error updating photo URL: ${e.message}", e)
                        Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        .addOnFailureListener { e ->
            Log.e("ProfileFragment", "Error uploading photo: ${e.message}", e)
            Toast.makeText(context, "Failed to upload photo", Toast.LENGTH_SHORT).show()
        }
}
```

### 4. Enable Change Photo Button
In the layout, change the visibility of the change photo button:

```xml
<ImageView
    android:id="@+id/btnChangePhoto"
    android:visibility="visible" /> <!-- Change from "gone" to "visible" -->
```

### 5. Update imagePickerLauncher
Change the callback to enable upload:

```kotlin
private val imagePickerLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        result.data?.data?.let { uri ->
            selectedImageUri = uri
            view?.findViewById<ImageView>(R.id.imgProfilePhoto)?.setImageURI(uri)
            uploadProfilePhoto(uri) // Enable this line
        }
    }
}
```

## Testing After Integration

1. **Build the project**: `./gradlew build`
2. **Run the app** and navigate to Profile tab
3. **Click change photo button** - should open image picker
4. **Select an image** - should upload to Firebase Storage
5. **Verify upload** - check Firebase Console > Storage
6. **Reload profile** - image should load from URL

## Firebase Storage Rules

Ensure Firebase Storage rules allow profile photo uploads:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /profile_photos/{userId} {
      allow read: if true; // Anyone can view profile photos
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## Error Handling

The implementation includes:
- ✅ Placeholder image while loading
- ✅ Error image if load fails
- ✅ Toast messages for user feedback
- ✅ Logging for debugging
- ✅ Null safety checks

## Performance Optimization

Glide automatically handles:
- Image caching (memory + disk)
- Resizing to fit ImageView
- Background loading
- Lifecycle awareness (won't load after fragment destroyed)

## Alternative: Use Coil Instead

If Glide causes issues, consider using Coil (Kotlin-first):

```kotlin
// In build.gradle.kts
implementation("io.coil-kt:coil:2.5.0")

// In code
imageView.load(profilePhotoUrl) {
    placeholder(R.drawable.avatar_placeholder)
    error(R.drawable.avatar_placeholder)
    transformations(CircleCropTransformation())
}
```

