# Firebase Rules Deployment Guide - Job Completion Feature

## Overview
This guide helps you deploy the necessary Firebase rules for the job completion feature.

## What Needs to be Updated

1. ✅ **Firestore Database Rules** - Already created in `firestore.rules`
2. ✅ **Firebase Storage Rules** - Created in `storage.rules`

## Step-by-Step Deployment

### Part 1: Firestore Database Rules

#### 1. Open Firebase Console
Go to: https://console.firebase.google.com/project/questboard-78a7a/firestore/rules

#### 2. Copy Rules
Open the file: `firestore.rules` in your project

#### 3. Replace Rules in Console
- Select ALL text in the Firebase Console rules editor
- Delete it
- Paste the complete rules from `firestore.rules`
- Click **"Publish"** button

#### 4. Verify
- Check for "Published" confirmation
- Timestamp should show current time

---

### Part 2: Firebase Storage Rules

#### 1. Open Firebase Console
Go to: https://console.firebase.google.com/project/questboard-78a7a/storage/rules

#### 2. Copy Rules
Open the file: `storage.rules` in your project

#### 3. Replace Rules in Console
- Select ALL text in the Firebase Console rules editor
- Delete it
- Paste the complete rules from `storage.rules`
- Click **"Publish"** button

#### 4. Verify
- Check for "Published" confirmation
- Timestamp should show current time

---

## What These Rules Do

### Firestore Rules Update

**New Addition: jobCompletions Collection**

```javascript
match /jobCompletions/{completionId} {
  allow read: if isSignedIn() && (
    resource.data.jobSeekerId == request.auth.uid || 
    resource.data.employerId == request.auth.uid
  );
  allow create: if isSignedIn();
  allow update: if isSignedIn() && (
    resource.data.jobSeekerId == request.auth.uid || 
    resource.data.employerId == request.auth.uid
  );
  allow delete: if isSignedIn() && resource.data.jobSeekerId == request.auth.uid;
}
```

**Purpose:**
- Job seekers can create completion forms
- Both job seeker and employer can read their related completions
- Both can update (for reviews/feedback)
- Only job seeker can delete

---

### Storage Rules (NEW)

**Purpose:** Allow job seekers to upload work photos

**work_photos/ folder:**
- Read: Any authenticated user
- Write: Any authenticated user
- Max size: 5MB per image
- Only image files allowed (jpg, png, etc.)

**Security:**
- ✅ Prevents large file uploads (saves storage costs)
- ✅ Prevents non-image file uploads
- ✅ Requires authentication (no anonymous uploads)
- ✅ Auto-validates file type and size

---

## Testing After Deployment

### Test 1: Firestore Rules
1. Open app as job seeker
2. Go to Active Jobs
3. Click "Confirm Completion"
4. Fill form and submit
5. **Expected:** Success ✅
6. **Check:** Document appears in Firestore → jobCompletions collection

### Test 2: Storage Rules
1. Open app as job seeker
2. Go to Active Jobs
3. Click "Confirm Completion"
4. Click "Upload Photo"
5. Select an image
6. Submit form
7. **Expected:** Image uploads successfully ✅
8. **Check:** Image appears in Firebase Storage → work_photos/ folder

### Test 3: Permission Denied Errors
If you see permission errors:
1. Check that rules are published (look for recent timestamp)
2. Wait 30 seconds for rules to propagate
3. Close and restart app
4. Try again

---

## Common Issues

### Issue: "PERMISSION_DENIED" when saving completion

**Solution:**
- Make sure you published Firestore rules
- Verify jobCompletions collection rules are included
- Wait 30 seconds and try again

### Issue: "Upload failed" when uploading photo

**Solution:**
- Make sure you published Storage rules
- Check file size is under 5MB
- Verify file is an image (jpg, png, gif, etc.)

### Issue: Rules won't publish

**Solution:**
- Check for syntax errors in rules
- Make sure you copied the COMPLETE rules file
- Verify you're in the correct Firebase project

---

## Files Reference

**In your project folder:**
- `firestore.rules` - Database security rules
- `storage.rules` - Storage security rules
- `JOB_COMPLETION_FEATURE_COMPLETE.md` - Full feature documentation

**In Firebase Console:**
- Firestore Rules: https://console.firebase.google.com/project/questboard-78a7a/firestore/rules
- Storage Rules: https://console.firebase.google.com/project/questboard-78a7a/storage/rules

---

## Quick Checklist

Before testing the job completion feature:

- [ ] Opened Firestore Rules in Firebase Console
- [ ] Copied rules from `firestore.rules` file
- [ ] Pasted into console and published
- [ ] Confirmed "Published" message
- [ ] Opened Storage Rules in Firebase Console
- [ ] Copied rules from `storage.rules` file
- [ ] Pasted into console and published
- [ ] Confirmed "Published" message
- [ ] Waited 30 seconds for propagation
- [ ] Closed and restarted app
- [ ] Ready to test!

---

## Support

If issues persist:
1. Check Firebase Console logs
2. Verify user is authenticated
3. Check Firestore and Storage quotas
4. Review error messages in Android logcat

**Status:** Rules ready for deployment
**Estimated time:** 5 minutes

