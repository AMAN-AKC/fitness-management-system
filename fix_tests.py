import os
import re

directory = r"C:\Users\amanc\Desktop\fitness-project\group17-backend\fitness-management-system\src\test\java\com\fitness\controller"

for filename in os.listdir(directory):
    if filename.endswith("Test.java"):
        filepath = os.path.join(directory, filename)
        with open(filepath, "r", encoding="utf-8") as f:
            content = f.read()

        # Check if already modified
        if "excludeAutoConfiguration" in content:
            continue

        # Find the WebMvcTest annotation
        pattern = r"@WebMvcTest\((.*\.class)\)"
        replacement = r"@WebMvcTest(controllers = \1, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})"
        
        new_content = re.sub(pattern, replacement, content)
        
        if new_content != content:
            with open(filepath, "w", encoding="utf-8") as f:
                f.write(new_content)
            print(f"Updated {filename}")
