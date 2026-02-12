import os
import sys

# Define content explicitly
content = """Categories:
  - Science & Education
License: Apache-2.0
WebSite: https://github.com/FunMax-Edward/study-tracker
SourceCode: https://github.com/FunMax-Edward/study-tracker
IssueTracker: https://github.com/FunMax-Edward/study-tracker/issues

AutoName: study tracker

RepoType: git
Repo: https://github.com/FunMax-Edward/study-tracker.git
Binaries:
  https://github.com/FunMax-Edward/study-tracker/releases/download/v%v/app-release.apk

Builds:
  - versionName: 1.0.2
    versionCode: 3
    commit: 61d3289abd10d0970dab6ae7fd84b3721d75ca5f
    subdir: app
    gradle:
      - yes

AllowedAPKSigningKeys: eb8f525f275d96a249c451aa46c802d104995224eeeed31ae30a3ba1b374d4c8

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 1.0.2
CurrentVersionCode: 3
"""

file_path = "metadata/com.edward.studytracker.yml"

print(f"Preparing to write to {file_path}...")

try:
    # Ensure directory
    os.makedirs("metadata", exist_ok=True)
    
    # Write file with LF forcing
    with open(file_path, "w", newline='\n', encoding='utf-8') as f:
        f.write(content)
        f.flush()
        os.fsync(f.fileno())

    print("Write complete.")

    # Validation
    stat = os.stat(file_path)
    if stat.st_size == 0:
        print("ERROR: File is empty after writing!")
        sys.exit(1)
        
    with open(file_path, "rb") as f:
        raw_bytes = f.read()
        
    if b'\r\n' in raw_bytes:
        print("ERROR: CRLF detected!")
        sys.exit(1)
        
    if b'\n' not in raw_bytes:
        print("ERROR: NO LF detected (strange for multiline)!")
        sys.exit(1)

    print(f"SUCCESS: File generated. Size: {stat.st_size} bytes. No CRLF detected.")

except Exception as e:
    print(f"EXCEPTION: {e}")
    sys.exit(1)
