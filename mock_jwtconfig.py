import os

directory = r"C:\Users\amanc\Desktop\fitness-project\group17-backend\fitness-management-system\src\test\java\com\fitness\controller"

for filename in os.listdir(directory):
    if filename.endswith("Test.java"):
        filepath = os.path.join(directory, filename)
        with open(filepath, "r", encoding="utf-8") as f:
            content = f.read()

        mock_config = """    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fitness.config.JwtConfig jwtConfig;
"""
        if "JwtConfig jwtConfig" not in content:
            # Insert after the JwtFilter mock
            insert_str = "private com.fitness.config.JwtFilter jwtFilter;\n"
            insert_pos = content.find(insert_str)
            if insert_pos != -1:
                insert_pos += len(insert_str)
                content = content[:insert_pos] + mock_config + content[insert_pos:]
                
                with open(filepath, "w", encoding="utf-8") as f:
                    f.write(content)
                print(f"Updated {filename}")
