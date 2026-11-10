# Database Cleanup Rule: Test & Guest Accounts

## Mandatory Policy
After completion of any tests, debugging sessions, validations, or automated runs, **ALWAYS** remove all test accounts, guest accounts, and temporary verification accounts from the database completely.

## Strict Guidelines:
1. **Zero Residue:** No guest accounts (e.g. `guest_*`, `guest@*`), temporary testers (e.g. `temp_*`, `test_*`, `testuser*`), or disposable mock accounts may remain in the remote or local database.
2. **Cascading Cleanups:** When removing test/guest accounts, ensure all associated records across child tables (`user_sessions`, `friendships`, `messages`, `dm_participants`, `server_members`, etc.) are also cleanly purged so database integrity is strictly maintained.
3. **Automatic Post-Test Execution:** Any workflow or test execution script that creates temporary users for testing must include a cleanup step in its `finally` block or tear-down routine.
4. **Audit Requirement:** Always query and verify the `users` table after testing to ensure only authorized, real user accounts persist.
