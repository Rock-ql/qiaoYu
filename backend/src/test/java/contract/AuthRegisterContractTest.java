package contract;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户注册接口合约测试 - 这个测试必须失败
 * 测试 POST /api/auth/register 接口是否符合API规范
 * 
 * 作者: xiaolei
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.data.redis.host=localhost", 
    "spring.data.redis.port=6379"
})
public class AuthRegisterContractTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    public void testRegisterContractWithValidData() {
        // 这个测试现在必须失败，因为接口还未实现
        String requestBody = """
            {
                "phone": "13800138001",
                "nickname": "测试用户",
                "password": "123456"
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        // 发送请求到未实现的接口 - 这应该失败
        ResponseEntity<String> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/auth/register",
            request,
            String.class
        );

        // 期望失败，因为接口未实现
        fail("注册接口尚未实现，此测试应该失败");
    }
}