import os
import re

BASE_DIR = r"c:\Users\amanc\Desktop\fitness-project\group17-backend\fitness-management-system\src\main\java\com\fitness"
TEST_DIR = r"c:\Users\amanc\Desktop\fitness-project\group17-backend\fitness-management-system\src\test\java\com\fitness"

def ensure_dir(path):
    if not os.path.exists(path):
        os.makedirs(path)

ensure_dir(os.path.join(TEST_DIR, "service"))
ensure_dir(os.path.join(TEST_DIR, "controller"))

# 1. Generate Service Tests
service_dir = os.path.join(BASE_DIR, "service")
for file in os.listdir(service_dir):
    if file.endswith("Service.java"):
        class_name = file.replace(".java", "")
        test_class_name = class_name + "Test"
        test_file_path = os.path.join(TEST_DIR, "service", test_class_name + ".java")
        
        if not os.path.exists(test_file_path):
            with open(test_file_path, "w") as f:
                f.write(f"""package com.fitness.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class {test_class_name} {{

    @InjectMocks
    private {class_name} {class_name[:1].lower() + class_name[1:]};

    @Test
    void testContextLoads() {{
        assertNotNull({class_name[:1].lower() + class_name[1:]});
    }}
}}
""")

# 2. Generate Controller Tests
controller_dir = os.path.join(BASE_DIR, "controller")
for file in os.listdir(controller_dir):
    if file.endswith("Controller.java"):
        class_name = file.replace(".java", "")
        test_class_name = class_name + "Test"
        test_file_path = os.path.join(TEST_DIR, "controller", test_class_name + ".java")
        
        if not os.path.exists(test_file_path):
            with open(test_file_path, "w") as f:
                f.write(f"""package com.fitness.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({class_name}.class)
public class {test_class_name} {{

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testContextLoads() throws Exception {{
        // Basic context load test
    }}
}}
""")

print("Generated test skeletons!")
