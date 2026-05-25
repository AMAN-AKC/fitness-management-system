import os
import re

directory = r"C:\Users\amanc\Desktop\fitness-project\group17-backend\fitness-management-system\src\test\java\com\fitness\controller"

for filename in os.listdir(directory):
    if filename.endswith("Test.java"):
        filepath = os.path.join(directory, filename)
        with open(filepath, "r", encoding="utf-8") as f:
            content = f.read()

        # Revert WebMvcTest
        pattern = r"@WebMvcTest\(controllers = (.*\.class), excludeAutoConfiguration = \{.*\}\)"
        content = re.sub(pattern, r"@WebMvcTest(\1)", content)
        
        # Insert MockBeans
        mock_beans = """
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fitness.config.JwtFilter jwtFilter;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
"""
        if "JwtFilter jwtFilter" not in content:
            # Insert before the first @Test or @Autowired
            insert_pos = content.find("@Autowired")
            if insert_pos == -1:
                insert_pos = content.find("@Test")
            
            if insert_pos != -1:
                content = content[:insert_pos] + mock_beans + "\n    " + content[insert_pos:]

        with open(filepath, "w", encoding="utf-8") as f:
            f.write(content)
