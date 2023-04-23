import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public class BaseTest {
    static final String api_key = "dbdc5543afaa7d6931d1630bdddefd90";
    @BeforeAll
    public static void setBaseUri() {
        RestAssured.baseURI = "https://api.openweathermap.org/data/2.5";
    }
}
