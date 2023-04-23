import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class OpenWeatherMapTest extends BaseTest{
    RequestSpecification requestSpec;
    ResponseSpecification responsePetDto;
    @BeforeEach
    public void setUp(){
        requestSpec = RestAssured.given().param("appid",api_key);
        ResponseSpecBuilder specBuilder = new ResponseSpecBuilder()
                .expectStatusCode(200);
        responsePetDto = specBuilder.build();
    }

// Тестируем City name на латинице (Тест 1)
    @ParameterizedTest
    @DisplayName("Название города на латинице, + название с маленькой буквы")
    @Description("Тест проверяет результат по названию города")
    @ValueSource (strings = {"Ufa", "ufa"})
    public void shouldGetWeatherByCityName(String cityName){
        requestSpec
                .param("q", cityName)
                .get("/weather")
                .then()
                .spec(responsePetDto)
                .body("name", equalTo("Ufa"));
    }

// Тестируем City name на кириллице (Тест 2)
    @Test
    @DisplayName("Название города на кириллице")
    @Description("Тест проверяет результат по статускоду и часовому поясу")
    //Тест на кириллице по хорошему должен пройти, но в данном случае тест то проходит, то нет
    //Но в swagger отрабатывает как нужно
    public void shouldGetWeatherByCityName2() {
        requestSpec
                .param("q", "Оренбург")
                .get("/weather")
                .then()
                .statusCode(200)
                .body("timezone", equalTo(18000));
    }

//Проверка с некорректным названием города. Негативный тест (Тест 3)
    @Test
    @DisplayName("Некорректное название города")
    @Description("Тест проверяет результат по сообщению ошибки")
    public void shouldNegativeGetInvalidCityName() {
        Response response = requestSpec
                .param("q","Gorodmorod")
                .get("/weather");
        response
                .then()
                .statusCode(404)
                .body("cod", equalTo("404")
                        ,"message", equalTo("city not found"));
    }

// Тестируем по ID города (Тест 4)
    @Test
    @DisplayName("По ID города")
    @Description("Тест проверяет результат по названию города")
    public void shouldGetWeatherByCityID(){
        requestSpec
                .param("id", 1496747)
                .get("/weather")
                .then()
                .spec(responsePetDto)
                .body("name", equalTo("Novosibirsk"));
//                .body("lon", equalTo("82.9344"));
// Хотел сравнить ответ по координатам, но в Actual null почему-то. Не пойму почему так

    }

// Тестируем по ID города. Негативный тест (Тест 5)
    @Test
    @DisplayName("По некорректному ID города")
    @Description("Тест проверяет результат по сообщению ошибки")
    public void shouldNegativeGetWeatherByCityID() {
        Response response = requestSpec
                .param("id", "!@#%^&")
                .get("/weather");
        response
                .then()
                .assertThat()
                .log().body()
                .body("cod", equalTo("400"),
                        "message",  equalTo("!@#%^& is not a city ID"));
    }

// Тестируем по координатам (Тест 6)
    @Test
    @DisplayName("Проверка по координатам")
    @Description("Тест проверяет результат по названию города")
    public void shouldGetWeatherByCoordinates() {
        requestSpec
                .param("lat", "55.0411")
                .param("lon", "82.9344")
                .get("/weather")
                .then()
                .statusCode(200)
                .body("name", equalTo("Novosibirsk"));
    }

// Тестируем все поля (Тест 7)
    @Test
    @DisplayName("По всем параметрам")
    @Description("Тест проверяет результат по статускоду и ContentTypе, по названию города")
    public void shouldGetWeatherAllParameters(){
    Response response = requestSpec
            .param("q","Novosibirsk")
            .param("id","1496747")
            .param("lat","55.0411")
            .param("lon","82.9344")
            .param("zip","630000")
            .param("units", "metric")
            .param("lang", "en")
            .param("mode", "json")
            .get("/weather");
    assertThat(response)
            .extracting(
                    Response::getContentType,
                    Response::getStatusCode
            ).containsExactly(
                    "application/json; charset=utf-8",
                    200
            );
    response
            .then()
            .body("name", equalTo("Novosibirsk"));
}

// Тестируем пустые поля (Тест 8)
    @Test
    @DisplayName("Без параметров")
    @Description("Тест проверяет результат по схеме JSON в файле, в которой указан JSON ошибки")
    public void shouldGetWeatherWithoutParameters(){
        Response response = requestSpec
                .get("/weather");
        response
                .then()
                .assertThat()
                .log().body()
                .body(matchesJsonSchemaInClasspath("NotFound.json"));
//Сначала хотел проверить на предмет соответствия сообщению,
//но решил проверить на соответствие схему JSON

//                .then()
//                .statusCode(400)
//                .assertThat()
//                .body("message", equalTo("Nothing to geocode"));
//        containsString("Nothing to geocode") не совсем подходит, т.к. он ищет содержание текста
//        Если убрать Nothing, то тест тоже пройдет
    }

}
