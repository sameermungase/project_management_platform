# GitHub Setup Guide

This guide will help you securely upload your project to GitHub.

## ⚠️ Important Security Notes

Before pushing to GitHub, make sure:
1. ✅ All sensitive credentials have been removed from committed files
2. ✅ `.gitignore` is properly configured
3. ✅ `application.yml` is excluded (contains database passwords and JWT secrets)
4. ✅ `pom.xml` no longer contains hardcoded credentials

## Step-by-Step Instructions

### Step 1: Verify Git is Installed

Open PowerShell and check if Git is installed:
```powershell
git --version
```

If not installed, download from: https://git-scm.com/download/win

### Step 2: Initialize Git Repository

Navigate to your project directory and initialize Git:
```powershell
cd "C:\Users\snowp\OneDrive\Desktop\Projects\Smart Project and Collaboration Platform"
git init
```

### Step 3: Configure Git (if not already done)

Set your name and email (replace with your GitHub credentials):
```powershell
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```

### Step 4: Create Application Configuration File

Since `application.yml` is excluded from Git, create it from the template:
```powershell
Copy-Item "backend\src\main\resources\application.yml.template" "backend\src\main\resources\application.yml"
```

Then edit `backend\src\main\resources\application.yml` and update:
- Database password
- JWT secret (generate a new one for production)

### Step 5: Stage Files for Commit

Add all files to staging (the `.gitignore` will automatically exclude sensitive files):
```powershell
git add .
```

Verify what will be committed (check that sensitive files are NOT included):
```powershell
git status
```

**IMPORTANT**: Make sure you don't see:
- ❌ `application.yml` (should be ignored)
- ❌ `node_modules/` (should be ignored)
- ❌ `target/` (should be ignored)
- ❌ `dist/` (should be ignored)

### Step 6: Create Initial Commit

```powershell
git commit -m "Initial commit: Smart Project and Collaboration Platform"
```

### Step 7: Create GitHub Repository

1. Go to https://github.com and sign in
2. Click the **"+"** icon in the top right → **"New repository"**
3. Repository name: `smart-project-platform` (or your preferred name)
4. Description: "Enterprise-grade Project Management System built with Spring Boot and Angular"
5. Choose **Public** or **Private** (Private recommended for now)
6. **DO NOT** initialize with README, .gitignore, or license (we already have these)
7. Click **"Create repository"**

### Step 8: Connect Local Repository to GitHub

After creating the repository, GitHub will show you commands. Use these (replace `YOUR_USERNAME` and `YOUR_REPO_NAME`):

```powershell
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
git branch -M main
git push -u origin main
```

**Note**: If you're using HTTPS, GitHub may prompt for credentials. For better security, consider using SSH or a Personal Access Token.

### Step 9: Verify Upload

1. Go to your GitHub repository page
2. Verify all files are present
3. Confirm that sensitive files are NOT visible:
   - ✅ `application.yml` should NOT be in the repository
   - ✅ `application.yml.template` SHOULD be in the repository
   - ✅ `node_modules/` should NOT be in the repository
   - ✅ `target/` should NOT be in the repository

## 🔐 Security Best Practices

### For Team Members / Future Setup

When someone clones the repository, they need to:

1. **Create `application.yml` from template:**
   ```powershell
   Copy-Item "backend\src\main\resources\application.yml.template" "backend\src\main\resources\application.yml"
   ```

2. **Update `application.yml` with their local credentials:**
   - Database username and password
   - JWT secret (generate a new secure key)

3. **For Flyway Maven plugin**, use environment variables or Maven properties:
   ```powershell
   mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/smart_project_db -Dflyway.user=postgres -Dflyway.password=your_password
   ```

### Generate Secure JWT Secret

Use this command to generate a secure JWT secret:
```powershell
# Using OpenSSL (if available)
openssl rand -base64 64

# Or use PowerShell
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 64 | ForEach-Object {[char]$_})
```

## 📝 Future Updates

To push future changes:

```powershell
git add .
git commit -m "Description of your changes"
git push
```

## 🚨 If You Accidentally Committed Sensitive Data

If you accidentally committed sensitive files:

1. **Remove from Git history** (use with caution):
   ```powershell
   git rm --cached backend/src/main/resources/application.yml
   git commit -m "Remove sensitive configuration file"
   git push
   ```

2. **If already pushed**, you'll need to:
   - Rotate all exposed secrets (database passwords, JWT secrets)
   - Consider using `git filter-branch` or BFG Repo-Cleaner to remove from history
   - Or create a new repository and start fresh

## ✅ Checklist Before First Push

- [ ] `.gitignore` is created and properly configured
- [ ] `application.yml` is NOT in the repository (check with `git status`)
- [ ] `application.yml.template` exists and is committed
- [ ] Hardcoded credentials removed from `pom.xml`
- [ ] `node_modules/` is excluded
- [ ] `target/` and `dist/` are excluded
- [ ] All sensitive data is removed from committed files
- [ ] README.md is updated (if needed)

## 📚 Additional Resources

- [GitHub Documentation](https://docs.github.com/)
- [Git Basics](https://git-scm.com/book/en/v2/Getting-Started-Git-Basics)
- [GitHub Security Best Practices](https://docs.github.com/en/code-security)

---

**Remember**: Never commit passwords, API keys, or secrets to version control!
