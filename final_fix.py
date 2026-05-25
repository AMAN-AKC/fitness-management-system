import os
import re

directory = r"C:\Users\amanc\Desktop\fitness-project\group17-backend\fitness-management-system\src\test\java\com\fitness\controller"

for filename in os.listdir(directory):
    if filename.endswith("Test.java"):
        filepath = os.path.join(directory, filename)
        with open(filepath, "r", encoding="utf-8") as f:
            content = f.read()

        # Remove JwtFilter mock
        pattern_jwt = r"\s*@org\.springframework\.boot\.test\.mock\.mockito\.MockBean\s*private com\.fitness\.config\.JwtFilter jwtFilter;"
        content = re.sub(pattern_jwt, "", content)

        # Fix AuditLogControllerTest
        if filename == "AuditLogControllerTest.java":
            pattern_audit = r"\s*@org\.springframework\.boot\.test\.mock\.mockito\.MockBean\s*private AuditLogService auditService;"
            content = re.sub(pattern_audit, "", content)

        # Fix AttendanceControllerTest URLs
        if filename == "AttendanceControllerTest.java":
            content = content.replace("/api/v1/attendance/check-in", "/api/v1/attendance/checkin")
            content = content.replace("/api/v1/attendance/class", "/api/v1/attendance/class/1/mark")

        with open(filepath, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"Cleaned up {filename}")
