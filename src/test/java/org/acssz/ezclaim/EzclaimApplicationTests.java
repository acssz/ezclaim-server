package org.acssz.ezclaim;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.security.jwt.secret=test-secret-32-bytes-minimum-1234567890",
        "app.security.jwt.algorithm=HS256",
        "app.security.jwt.ttl=PT1H"
})
class EzclaimApplicationTests {

	@Test
	void contextLoads() {
	}

}
