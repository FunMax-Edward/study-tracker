import os

content = """Categories:
  - Science & Education
License: Apache-2.0
WebSite: https://github.com/FunMax-Edward/study-tracker
SourceCode: https://github.com/FunMax-Edward/study-tracker
IssueTracker: https://github.com/FunMax-Edward/study-tracker/issues

AutoName: study tracker

RepoType: git
Repo: https://github.com/FunMax-Edward/study-tracker.git

Builds:
  - versionName: 1.0.0
    versionCode: 1
    commit: b75b440bb908b1196f6ab5c71f5809e71ec39b17
    subdir: app
    gradle:
      - yes

AllowedAPKSigningKeys: eb8f525f275d96a249c451aa46c802d104995224eeeed31ae30a3ba1b374d4c8

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 1.0.0
CurrentVersionCode: 1
"""

# Ensure the directory exists
os.makedirs("metadata", exist_ok=True)

# Write with explicit newline='\n' for LF
with open("metadata/com.edward.studytracker.yml", "w", newline='\\n', encoding='utf-8') as f:
    f.write(content)

print("Created metadata/com.edward.studytracker.yml with LF line endings.")
