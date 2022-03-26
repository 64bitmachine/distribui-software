package com.example;


//#test-top
import akka.http.javadsl.testkit.JUnitRouteTest;
import org.junit.*;
import org.junit.runners.MethodSorters;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;


//#set-up
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeliveryRoutesTest extends JUnitRouteTest {

    @ClassRule
    public static TestKitJunitResource testkit = new TestKitJunitResource();

    //#test-top
    // shared registry for all tests
    // private static ActorRef<UserRegistry.Command> userRegistry;
    // private TestRoute appRoute;

    @BeforeClass
    public static void beforeClass() {
        // userRegistry = testkit.spawn(UserRegistry.create());
    }

    @Before
    public void before() {
        // DeliveryRoutes userRoutes = new DeliveryRoutes(testkit.system(), userRegistry);
        // appRoute = testRoute(userRoutes.userRoutes());
    }

    @AfterClass
    public static void afterClass() {
        // testkit.stop(userRegistry);
    }

    //#set-up
    //#actual-test
    // @Test
    // public void test1NoUsers() {
    //     appRoute.run(HttpRequest.GET("/users"))
    //             .assertStatusCode(StatusCodes.OK)
    //             .assertMediaType("application/json")
    //             .assertEntity("{\"users\":[]}");
    // }

    // //#actual-test
    // //#testing-post
    // @Test
    // public void test2HandlePOST() {
    //     appRoute.run(HttpRequest.POST("/users")
    //             .withEntity(MediaTypes.APPLICATION_JSON.toContentType(),
    //                     "{\"name\": \"Kapi\", \"age\": 42, \"countryOfResidence\": \"jp\"}"))
    //             .assertStatusCode(StatusCodes.CREATED)
    //             .assertMediaType("application/json")
    //             .assertEntity("{\"description\":\"User Kapi created.\"}");
    // }
    // //#testing-post

    // @Test
    // public void test3Remove() {
    //     appRoute.run(HttpRequest.DELETE("/users/Kapi"))
    //             .assertStatusCode(StatusCodes.OK)
    //             .assertMediaType("application/json")
    //             .assertEntity("{\"description\":\"User Kapi deleted.\"}");

    // }
    //#set-up
}
//#set-up
