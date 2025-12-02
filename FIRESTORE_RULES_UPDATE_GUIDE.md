# Firestore Security Rules Update - PERMISSION_DENIED Fix

## Date: December 2, 2025

## Problem
When employers try to send messages to applicants, the app fails with:
```
PERMISSION_DENIED: Missing or insufficient permissions.
```

## Root Cause
The Firestore security rules don't allow:
1. Employers to read user data (needed to get employer name)
2. Users to create/read conversations they are part of
3. Users to send messages in conversations

## Solution

### Step 1: Access Firebase Console
1. Go to https://console.firebase.google.com/
2. Select your project: `questboard-78a7a`
3. Click on **Firestore Database** in the left menu
4. Click on the **Rules** tab at the top

### Step 2: Replace Existing Rules
Copy the entire content from `firestore.rules` file in your project and paste it into the Firebase Console Rules editor.

**OR** manually copy this:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function to check if user is authenticated
    function isSignedIn() {
      return request.auth != null;
    }
    
    // Helper function to check if user is the document owner
    function isOwner(userId) {
      return request.auth.uid == userId;
    }
    
    // Users collection - users can read/write their own data
    match /users/{userId} {
      allow read: if isSignedIn();
      allow write: if isSignedIn() && isOwner(userId);
    }
    
    // Jobs collection
    match /jobs/{jobId} {
      allow read: if isSignedIn();
      allow create: if isSignedIn();
      allow update, delete: if isSignedIn() && resource.data.employerId == request.auth.uid;
    }
    
    // Applications collection
    match /applications/{applicationId} {
      allow read: if isSignedIn() && (
        resource.data.applicantId == request.auth.uid || 
        resource.data.employerId == request.auth.uid
      );
      allow create: if isSignedIn();
      allow update: if isSignedIn() && (
        resource.data.applicantId == request.auth.uid || 
        resource.data.employerId == request.auth.uid
      );
      allow delete: if isSignedIn() && resource.data.applicantId == request.auth.uid;
    }
    
    // Conversations collection - users can access conversations they are part of
    match /conversations/{conversationId} {
      // Allow read if document exists and user is participant, OR if checking for existence
      allow read: if isSignedIn() && (
        !exists(/databases/$(database)/documents/conversations/$(conversationId)) ||
        request.auth.uid in resource.data.participants
      );
      allow create: if isSignedIn() && request.auth.uid in request.resource.data.participants;
      allow update: if isSignedIn() && request.auth.uid in resource.data.participants;
      allow delete: if isSignedIn() && request.auth.uid in resource.data.participants;
      
      // Messages subcollection
      match /messages/{messageId} {
        allow read: if isSignedIn() && request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants;
        allow create: if isSignedIn() && request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants;
        allow update: if isSignedIn() && request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants;
        allow delete: if isSignedIn() && request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants;
      }
    }
    
    // Notifications collection
    match /notifications/{notificationId} {
      allow read: if isSignedIn() && resource.data.userId == request.auth.uid;
      allow create: if isSignedIn();
      allow update, delete: if isSignedIn() && resource.data.userId == request.auth.uid;
    }
    
    // Saved jobs collection
    match /savedJobs/{savedJobId} {
      allow read, write: if isSignedIn() && resource.data.userId == request.auth.uid;
      allow create: if isSignedIn();
    }
    
    // Default deny all other collections
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

### Step 3: Publish Rules
1. Click the **Publish** button in the Firebase Console
2. Wait for confirmation that rules were updated

### Step 4: Test the Feature
1. Open your app
2. As an employer, go to Applicants tab
3. Click "View Details" on an applicant
4. Click "Contact" → "Send Message"
5. The chat should now open successfully ✅

## What These Rules Do

### Users Collection
- **Read:** Any authenticated user can read user profiles (needed to get names)
- **Write:** Users can only update their own profile

### Conversations Collection
- **Read/Write:** Only users who are participants in the conversation can access it
- **Create:** Users can create conversations if they are listed as participants
- **Delete:** Users can delete conversations they are part of
- **Special Note:** The read rule allows checking if a document exists (even if it doesn't) to enable the "get or create" pattern. This prevents permission errors when checking for existing conversations before creating new ones.

### Messages Subcollection
- **Read/Write:** Only participants of the parent conversation can read/write messages
- Uses `get()` function to check parent conversation's participants list

### Applications Collection
- **Read/Write:** Only the applicant or employer can access the application
- Ensures privacy between parties

## Security Features

✅ Users must be authenticated to access any data
✅ Users can only access conversations they are part of
✅ Users can only read profiles, not modify others' data
✅ Messages are private to conversation participants
✅ Applications are private to applicant and employer

## Important Notes

⚠️ **DO NOT** use these rules for production without reviewing them:
```
allow read, write: if true;  // NEVER USE THIS
```

✅ **DO** use the rules provided above - they are secure and appropriate for your app

## Troubleshooting

### If you still get permission errors:

**Error: "PERMISSION_DENIED: Missing or insufficient permissions" when creating conversation:**

This error occurs if the rules don't allow checking for conversation existence. The fix is already included in the rules above:

```
allow read: if isSignedIn() && (
  !exists(/databases/$(database)/documents/conversations/$(conversationId)) ||
  request.auth.uid in resource.data.participants
);
```

This rule allows:
- Reading existing conversations if user is a participant
- Checking if a conversation exists (even if it doesn't) before creating it

**Make sure you published the UPDATED rules (not the old version)!**

1. **Check if rules were published:**
   - Go to Firebase Console → Firestore → Rules
   - Look for the timestamp of last publish
   - Should be recent (just now)

2. **Check the collection name:**
   - Rules use exact collection names
   - Verify your collections match: `conversations`, `users`, `applications`

3. **Check user authentication:**
   - Make sure user is logged in
   - Check: `FirebaseAuth.getInstance().currentUser != null`

4. **Check participant IDs:**
   - The conversation's `participants` array must contain the user's UID
   - Verify in Firestore console that the conversation document has correct participant IDs

5. **Clear app data and retry:**
   - Sometimes cached permissions cause issues
   - Uninstall and reinstall the app

## Testing Checklist

After updating rules, test:
- ✅ Employer can view applicant details
- ✅ Employer can click "Send Message"
- ✅ Chat opens successfully
- ✅ Employer can send messages
- ✅ Job seeker can see the conversation in Messages tab
- ✅ Job seeker can reply to messages
- ✅ Both parties can swipe to delete conversations
- ✅ Unread counts update correctly

---

**Status:** Rules created - Ready to deploy to Firebase Console
**Action Required:** You need to manually copy and paste these rules into Firebase Console

