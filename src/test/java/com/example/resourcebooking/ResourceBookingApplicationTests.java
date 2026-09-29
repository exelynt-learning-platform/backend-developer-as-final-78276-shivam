package com.example.resourcebooking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-key-for-resource-booking-system-2026-testing-only-123456",
        "app.jwt.expiration=3600000",

        "spring.datasource.url=jdbc:h2:mem:resource_booking_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",

        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class ResourceBookingApplicationTests {

    @Test
    void contextLoads() {
    }
}