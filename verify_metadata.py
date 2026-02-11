import yaml
import sys

filename = "metadata/com.edward.studytracker.yml"

try:
    with open(filename, 'rb') as f:
        content = f.read()

    # Check for CRLF
    if b'\r\n' in content:
        print("FAILURE: File contains CRLF line endings!")
        sys.exit(1)
    
    if b'\n' not in content:
         print("WARNING: File seems to have no newlines or is empty.")

    # Check for valid YAML
    with open(filename, 'r', encoding='utf-8') as f:
        yaml.safe_load(f)

    print("SUCCESS: File has LF line endings and valid YAML.")

except FileNotFoundError:
    print(f"FAILURE: {filename} not found.")
    sys.exit(1)
except yaml.YAMLError as exc:
    print(f"FAILURE: Invalid YAML: {exc}")
    sys.exit(1)
except Exception as e:
    print(f"FAILURE: Unexpected error: {e}")
    sys.exit(1)
