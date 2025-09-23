package unit.util;

import cn.badminton.util.JwtUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

public class JwtUtilTest {

    @Test
    public void testGenerateAndParseToken() throws Exception {
        JwtUtil jwt = new JwtUtil();
        Field secret = JwtUtil.class.getDeclaredField("secret");
        secret.setAccessible(true);
        secret.set(jwt, "unit-test-secret-key-1234567890-ABCDEF-XYZ");
        Field expire = JwtUtil.class.getDeclaredField("expireMinutes");
        expire.setAccessible(true);
        expire.set(jwt, 10L);

        String userId = "u-123";
        String token = jwt.generateToken(userId);
        Assertions.assertNotNull(token);
        String parsed = jwt.parseUserId(token);
        Assertions.assertEquals(userId, parsed);
    }
}
