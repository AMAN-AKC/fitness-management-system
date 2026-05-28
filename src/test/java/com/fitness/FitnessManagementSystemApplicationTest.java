package com.fitness;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
public class FitnessManagementSystemApplicationTest {

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully.
    }

    @Test
    void main() {
        // Use Mockito to mock the static SpringApplication.run method so it doesn't actually start a server
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(FitnessManagementSystemApplication.class, new String[]{}))
                    .thenReturn(null);

            assertDoesNotThrow(() -> FitnessManagementSystemApplication.main(new String[]{}));

            mocked.verify(() -> SpringApplication.run(FitnessManagementSystemApplication.class, new String[]{}));
        }
    }
}
