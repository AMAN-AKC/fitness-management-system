import os
import re

controllers_dir = r"C:\Users\amanc\Desktop\fitness-project\group17-backend\fitness-management-system\src\main\java\com\fitness\controller"
tests_dir = r"C:\Users\amanc\Desktop\fitness-project\group17-backend\fitness-management-system\src\test\java\com\fitness\controller"

for filename in os.listdir(tests_dir):
    if filename.endswith("Test.java"):
        test_filepath = os.path.join(tests_dir, filename)
        controller_filename = filename.replace("Test.java", ".java")
        controller_filepath = os.path.join(controllers_dir, controller_filename)
        
        if not os.path.exists(controller_filepath):
            continue
            
        with open(controller_filepath, "r", encoding="utf-8") as f:
            controller_content = f.read()
            
        fields = re.findall(r"private\s+final\s+([A-Z]\w+)\s+(\w+)\s*;", controller_content)
        
        with open(test_filepath, "r", encoding="utf-8") as f:
            test_content = f.read()
            
        mock_additions = ""
        imports_additions = set()
        
        for field_type, field_name in fields:
            if f"{field_type} {field_name}" not in test_content:
                mock_additions += f"    @org.springframework.boot.test.mock.mockito.MockBean\n    private {field_type} {field_name};\n"
                import_match = re.search(r"import\s+([\w\.]+{}\b);".format(field_type), controller_content)
                if import_match:
                    import_str = import_match.group(1)
                    if import_str not in test_content:
                        imports_additions.add(f"import {import_str};")
        
        if mock_additions:
            if imports_additions:
                pkg_end = test_content.find(";") + 1
                test_content = test_content[:pkg_end] + "\n" + "\n".join(imports_additions) + test_content[pkg_end:]
            
            insert_pos = test_content.find("@Autowired\n    private MockMvc mockMvc;")
            if insert_pos == -1:
                insert_pos = test_content.find("@Test")
            
            if insert_pos != -1:
                test_content = test_content[:insert_pos] + mock_additions + "\n    " + test_content[insert_pos:]
                
                with open(test_filepath, "w", encoding="utf-8") as f:
                    f.write(test_content)
                print(f"Added mocks to {filename}")
