# 🔥 Firebase Setup Guide - Complete Instructions

## 📋 Overview

You need to set up **two Firebase services** for your QuestBoard app:
1. **Firebase Authentication** - For user login/signup
2. **Cloud Firestore** - For storing user profile data

---

## 🚀 Step-by-Step Setup

### STEP 1: Access Firebase Console

1. **Open your browser** and go to: https://console.firebase.google.com
2. **Sign in** with your Google account
3. You should see your project or create a new one

---

### STEP 2: Find Your Project

**If you already have a Firebase project:**
- Look for your project name in the Firebase Console
- Click on your project to open it

**If you DON'T have a Firebase project yet:**
1. Click **"Add project"** or **"Create a project"**
2. **Enter project name:** `QuestBoard` (or any name you prefer)
3. Click **"Continue"**
4. **Google Analytics:** You can disable it for now (toggle OFF)
5. Click **"Create project"**
6. Wait for setup to complete
7. Click **"Continue"**

---

### STEP 3: Enable Firebase Authentication

1. **In your Firebase project**, look at the left sidebar
2. Click **"Build"** → **"Authentication"**
3. Click **"Get started"** button
4. You'll see **"Sign-in method"** tab at the top
5. Click on **"Email/Password"** in the providers list
6. **Toggle the first switch to ENABLE** (Email/Password)
7. Leave "Email link (passwordless sign-in)" DISABLED
8. Click **"Save"**

**✅ You should see:** "Email/Password" now shows as "Enabled"

---

### STEP 4: Enable Cloud Firestore Database

1. **In the left sidebar**, click **"Build"** → **"Firestore Database"**
2. Click **"Create database"** button
3. **Choose location:**
   - Select a location close to you (e.g., `us-central`, `asia-southeast1`)
   - Click **"Next"**
4. **Security rules:** Select **"Start in test mode"**
   - ⚠️ This allows read/write for testing (we'll secure it later)
   - Click **"Enable"**
5. **Wait** for the database to be created (takes ~30 seconds)

**✅ You should see:** Empty Firestore database console with "Start collection" button

---

### STEP 5: Set Up Firestore Security Rules (Important!)

By default, test mode rules expire in 30 days. Let's set up proper rules:

1. **In Firestore Database**, click the **"Rules"** tab at the top
2. **Replace** the existing rules with this:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection - users can only read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow authenticated users to read all users (for job matching)
    match /users/{userId} {
      allow read: if request.auth != null;
    }
  }
}
```

3. Click **"Publish"** button
4. **Confirm** by clicking **"Publish"** again

**✅ Rules are now set:** Users can only modify their own data, but can view others (for job matching)

---

### STEP 6: Verify Your google-services.json File

Your app already has the `google-services.json` file at:
```
app/google-services.json
```

**To verify it's correct:**

1. **In Firebase Console**, click the **gear icon** (⚙️) next to "Project Overview"
2. Click **"Project settings"**
3. Scroll down to **"Your apps"** section
4. You should see an **Android app** listed
5. Click on the Android app
6. **Check the package name:** Should be `com.example.questboard`
7. If it matches, your `google-services.json` is correct!

**If you DON'T see an Android app or need to download the file:**

1. Click **"Add app"** → Select **Android** (robot icon)
2. **Android package name:** Enter `com.example.questboard`
3. **App nickname:** `QuestBoard` (optional)
4. **Debug signing certificate:** Leave blank for now
5. Click **"Register app"**
6. **Download** the `google-services.json` file
7. **Replace** the existing file in your project:
   - Copy the downloaded file
   - Paste it to: `D:\App_Project\questBoard\app\google-services.json`
8. Click **"Next"** → **"Next"** → **"Continue to console"**

---

### STEP 7: Test Your Firebase Connection

Let's verify everything is working:

1. **Run your app** on the emulator/device
2. **Register a test account:**
   - Click "Register"
   - Choose "Job Seeker"
   - Fill in basic info (name, phone, address, birthday)
   - Skip all document uploads
   - Click "Sign Up"
   - Enter email: `test@example.com`
   - Enter password: `test123`
   - Click "Continue"

3. **Check Firebase Console - Authentication:**
   - Go to **Build → Authentication → Users** tab
   - **You should see:** Your test account listed with the email

4. **Check Firebase Console - Firestore:**
   - Go to **Build → Firestore Database → Data** tab
   - **You should see:**
     - Collection: `users`
     - Document: (auto-generated ID)
     - Fields: firstName, lastName, email, accountType, etc.

**✅ If you see your data in both places, Firebase is working perfectly!**

---

## 🗄️ Understanding Your Firestore Structure

Your app saves data in this structure:

```
Firestore Database
└── users (collection)
    ├── {userId1} (document - auto-generated ID)
    │   ├── firstName: "John"
    │   ├── middleName: "A"
    │   ├── lastName: "Doe"
    │   ├── phone: "1234567890"
    │   ├── address1: "123 Main St"
    │   ├── address2: "City, State"
    │   ├── birthday: "01/15/1990"
    │   ├── idType: "Select ID Type"
    │   ├── accountType: "job_seeker"
    │   └── createdAt: 1733097600000
    │
    └── {userId2} (document)
        ├── firstName: "Jane"
        ├── middleName: "B"
        ├── lastName: "Smith"
        ├── phone: "0987654321"
        ├── address1: "456 Business Ave"
        ├── address2: "City, State"
        ├── birthday: "05/20/1985"
        ├── idType: "Select ID Type"
        ├── accountType: "employer"
        ├── businessPermitType: "Select Permit Type"
        └── createdAt: 1733097600100
```

---

## 🔒 Security Best Practices

### Current Setup (Development):
- ✅ Test mode enabled for easy development
- ✅ Basic authentication required
- ⚠️ Users can read all other user profiles (needed for job matching)

### For Production (Later):
You should enhance security rules to:
- Limit read access based on user roles
- Add data validation
- Implement rate limiting
- Add more granular permissions

---

## 🎯 Quick Checklist

Use this to verify your Firebase setup:

**Firebase Console:**
- [ ] Project created/accessed
- [ ] Authentication enabled (Email/Password)
- [ ] Firestore Database created
- [ ] Security rules updated
- [ ] Android app registered
- [ ] google-services.json downloaded (if needed)

**In Your App:**
- [ ] google-services.json in `app/` folder
- [ ] Build successful
- [ ] App runs without crashes
- [ ] Can navigate to registration

**Testing:**
- [ ] Register test account successfully
- [ ] User appears in Firebase Authentication
- [ ] User data appears in Firestore Database
- [ ] Can log in with test account

---

## 📊 Viewing Your Data in Firebase Console

### To View Users (Authentication):
```
Firebase Console → Build → Authentication → Users tab
```
You'll see:
- User Identifier (UID)
- Providers (email)
- Created date
- Sign-in date
- User UID (copy this to find in Firestore)

### To View User Profiles (Firestore):
```
Firebase Console → Build → Firestore Database → Data tab
```
You'll see:
- Collection: users
- Click on a document to see all fields
- Can edit, delete, or add fields manually
- Can filter and search

---

## 🔧 Common Issues & Solutions

### Issue: "FirebaseApp initialization unsuccessful"
**Solution:**
- Check that `google-services.json` is in the correct location (`app/` folder)
- Verify package name matches in the file
- Clean and rebuild project
- Sync Gradle files

### Issue: User created but no data in Firestore
**Solution:**
- Check Firestore security rules
- Verify internet connection
- Check Logcat for errors
- Make sure you clicked "Continue" after entering email/password

### Issue: "Permission denied" in Firestore
**Solution:**
- Go to Firestore → Rules tab
- Make sure rules allow authenticated users to write
- Check that user is logged in before writing

### Issue: Can't see data in Firebase Console
**Solution:**
- Refresh the Firebase Console page
- Click on the "Data" tab
- Make sure you're looking at the right project
- Wait a few seconds and refresh

---

## 📱 Testing Different Scenarios

### Test Job Seeker Registration:
```
1. Register as Job Seeker
2. Check Firestore → accountType should be "job_seeker"
3. businessPermitType should NOT exist
```

### Test Employer Registration:
```
1. Register as Employer
2. Check Firestore → accountType should be "employer"
3. businessPermitType should be "Select Permit Type"
```

### Test Login:
```
1. Register an account
2. Close and reopen app
3. Click "Log In"
4. Enter same email/password
5. Should navigate to dashboard
```

---

## 🎉 You're All Set!

Once you complete these steps, your Firebase backend is fully configured and your app will:

✅ **Create user accounts** in Firebase Authentication
✅ **Store user profiles** in Cloud Firestore
✅ **Authenticate users** on login
✅ **Sync data** in real-time
✅ **Secure user data** with proper rules

---

## 📝 Next Steps After Setup

1. **Test thoroughly** with multiple accounts
2. **Monitor usage** in Firebase Console
3. **Review security rules** before production
4. **Set up Firebase Storage** (for document uploads later)
5. **Enable additional features** as needed

---

## 🆘 Need Help?

If you encounter any issues:

1. **Check Logcat** in Android Studio for error messages
2. **Verify Firebase Console** shows your project
3. **Review security rules** in Firestore
4. **Check internet connection** on emulator/device
5. **Share specific error messages** for troubleshooting

---

**Your Firebase is ready to use!** 🚀

Follow these steps in order and you'll have a fully functional backend for your QuestBoard app.

