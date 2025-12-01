# XML Layout Cleanup and Renaming Plan

## Analysis Complete - Action Plan

### USED LAYOUTS (Keep and Rename)
1. ✅ `activity_applicants.xml` → KEEP (EmployerApplicantsFragment, ApplicantsActivity)
2. ✅ `activity_email_password_setup.xml` → KEEP (EmailPasswordSetupActivity)
3. ✅ `activity_employer_dashboard.xml` → RENAME to `fragment_employer_my_jobs.xml` (used in fragment)
4. ✅ `activity_jobseeker_jobs.xml` → KEEP (JobSeekerJobsActivity standalone)
5. ✅ `activity_main_employer.xml` → KEEP (EmployerDashboardActivity)
6. ✅ `activity_main_jobseeker.xml` → KEEP (MainActivity)
7. ✅ `choose_account_type_activity.xml` → RENAME to `activity_choose_account_type.xml`
8. ✅ `em_register_form_activity.xml` → RENAME to `activity_employer_register.xml`
9. ✅ `fragment_employer_post_job.xml` → KEEP
10. ✅ `fragment_employer_profile.xml` → KEEP
11. ✅ `fragment_jobseeker_home_simple.xml` → RENAME to `fragment_jobseeker_home.xml`
12. ✅ `fragment_jobseeker_jobs_simple.xml` → RENAME to `fragment_jobseeker_jobs.xml`
13. ✅ `fragment_jobseeker_messages_simple.xml` → RENAME to `fragment_jobseeker_messages.xml`
14. ✅ `fragment_jobseeker_profile_simple.xml` → RENAME to `fragment_jobseeker_profile.xml`
15. ✅ `item_active_job_post.xml` → KEEP
16. ✅ `item_applicant.xml` → KEEP
17. ✅ `item_job_applicants_post.xml` → RENAME to `item_job_post.xml`
18. ✅ `jb_register_form_activity.xml` → RENAME to `activity_jobseeker_register.xml`
19. ✅ `login_activity.xml` → RENAME to `activity_login.xml`
20. ✅ `onboarding.xml` → RENAME to `activity_onboarding.xml`
21. ✅ `bottom_nav_jobseeker.xml` → KEEP (used via include)

### UNUSED/DUPLICATE LAYOUTS (DELETE)
1. ❌ `activity_applicant.xml` - NOT USED
2. ❌ `activity_community.xml` - NOT USED
3. ❌ `activity_home_jobseeker.xml` - DUPLICATE (fragments use simple versions)
4. ❌ `activity_job_details.xml` - NOT USED
5. ❌ `activity_messages_jobseeker.xml` - DUPLICATE
6. ❌ `activity_profile_jobseeker.xml` - DUPLICATE
7. ❌ `email_otp_activity.xml` - NOT USED
8. ❌ `item_available_job.xml` - NOT USED
9. ❌ `item_applicant_job.xml` - NOT USED
10. ❌ `item_pending_application.xml` - NOT USED
11. ❌ `item_recent_job.xml` - NOT USED
12. ❌ `top_app_bar_jobseeker.xml` - NOT USED (was removed from fragments)

### KOTLIN FILES TO UPDATE
1. Choose_Account_Type.kt
2. EmailPasswordSetupActivity.kt
3. EmployerRegister.kt
4. EmployerMyJobsFragment.kt
5. EmployerApplicantsFragment.kt (uses activity_applicants - needs fragment)
6. EmployerMessagesFragment.kt (uses activity_messages - MISSING FILE)
7. JobSeekerHomeFragment.kt
8. JobSeekerJobsFragment.kt
9. JobSeekerMessagesFragment.kt
10. JobSeekerProfileFragment.kt
11. JobSeekerRegister.kt
12. JobPostsAdapter.kt
13. OnboardingActivity.kt
14. SignIn.kt

