# QuestBoard Authentication & Registration Setup

## Summary

I've created a complete authentication and registration flow for your QuestBoard app with separate flows for Job Seekers and Employers.

## Files Created/Updated

### 1. Choose_Account_Type.kt
- **Layout**: `choose_account_type_activity.xml`
- **Purpose**: Allows users to choose between Job Seeker and Employer account types
- **Navigation**: 
  - Job Seeker → JobSeekerRegister
  - Employer → EmployerRegister

### 2. JobSeekerRegister.kt
- **Layout**: `jb_register_form_activity.xml`
- **Purpose**: Collects job seeker profile information
- **Fields**:
  - First Name, Middle Name, Last Name
  - Phone Number
  - Address Line 1 & 2
  - Birthday
  - Valid ID Type (Spinner)
  - Front ID Image
  - Back ID Image
- **Navigation**: After validation → EmailPasswordSetupActivity

### 3. EmployerRegister.kt
- **Layout**: `em_register_form_activity.xml`
- **Purpose**: Collects employer profile information
- **Fields**:
  - First Name, Middle Name, Last Name
  - Phone Number
  - Address Line 1 & 2
  - Birthday
  - Business Permit Type (Spinner)
  - Business Permit Image
  - Valid ID Type (Spinner)
  - Front ID Image
  - Back ID Image
- **Navigation**: After validation → EmailPasswordSetupActivity

### 4. EmailPasswordSetupActivity.kt
- **Layout**: `activity_email_password_setup.xml`
- **Purpose**: Creates Firebase account and saves profile data
- **Fields**:
  - Email
  - Password
  - Confirm Password
- **Functionality**:
  - Creates Firebase Authentication account
  - Saves user profile to Firestore
  - Navigates to appropriate dashboard based on account type

### 5. SignIn.kt (Updated)
- Changed signup navigation to point to `Choose_Account_Type` activity

### 6. AndroidManifest.xml (Updated)
- Registered all new activities

## User Flow

```
Login Screen (SignIn.kt)
    |
    ├─ Login → MainActivity or EmployerDashboard
    |
    └─ Sign Up → Choose Account Type (Choose_Account_Type.kt)
                    |
                    ├─ Job Seeker → Job Seeker Registration (JobSeekerRegister.kt)
                    |                   |
                    |                   └─ Email/Password Setup → MainActivity
                    |
                    └─ Employer → Employer Registration (EmployerRegister.kt)
                                      |
                                      └─ Email/Password Setup → EmployerDashboard
```

## Firestore Data Structure

### For Job Seekers:
```json
{
  "firstName": "string",
  "middleName": "string",
  "lastName": "string",
  "phone": "string",
  "address1": "string",
  "address2": "string",
  "birthday": "string",
  "idType": "string",
  "accountType": "job_seeker",
  "createdAt": timestamp
}
```

### For Employers:
```json
{
  "firstName": "string",
  "middleName": "string",
  "lastName": "string",
  "phone": "string",
  "address1": "string",
  "address2": "string",
  "birthday": "string",
  "businessPermitType": "string",
  "idType": "string",
  "accountType": "employer",
  "createdAt": timestamp
}
```

## Next Steps

1. **Sync Gradle**: The IDE needs to sync to recognize all the new classes
2. **Test the Flow**: Try the complete registration flow
3. **Image Upload**: Currently images are selected but not uploaded to Firebase Storage. You may want to add Firebase Storage integration
4. **Validation**: Add more robust validation as needed
5. **Error Handling**: Add more user-friendly error messages

## Dependencies Already Configured

- Firebase Authentication (via firebase-bom)
- Firebase Firestore (via firebase-bom)
- Firebase Analytics
- Material Design Components

## Notes

- All activities use the correct XML layouts you specified
- The flow validates all required fields before proceeding
- Passwords must be at least 6 characters
- Email validation is performed
- Firebase creates the auth account first, then saves profile data to Firestore

