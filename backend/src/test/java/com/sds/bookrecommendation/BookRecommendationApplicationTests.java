package com.sds.bookrecommendation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "jwt.secret=VGhpc0lzQVRlc3RTZWNyZXRLZXlGb3JCb29rUmVjb21tZW5kYXRpb24=",
        "jwt.expiration=86400000"
})
class BookRecommendationApplicationTests {

    @Test
    void contextLoads() {
    }
}