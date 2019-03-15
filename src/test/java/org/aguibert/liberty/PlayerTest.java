package org.aguibert.liberty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import org.aguibert.testcontainers.framework.LibertyContainer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

public class PlayerTest {

    private static final String APP_PATH = "/basic-liberty-1.0-SNAPSHOT";
    private static final String MONGO_HOST = "testmongo";
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerTest.class);

    public static Network network = Network.newNetwork();

    public static GenericContainer<?> mongodb = new GenericContainer<>("mongo:3.4")
                    .withExposedPorts(27017)
                    .withNetwork(network)
                    .withNetworkAliases(MONGO_HOST)
                    .waitingFor(Wait.forListeningPort());

    public static LibertyContainer playerContainer = new LibertyContainer("basic-liberty")
                    .withExposedPorts(9080)
                    //.waitForMPHealth(); // TODO: this won't be ready until we have "readiness" checks in MP Health 2.0
                    .waitingFor(Wait.forHttp(APP_PATH))
                    .withNetwork(network)
                    .withEnv("MONGO_HOSTNAME", MONGO_HOST)
                    .withEnv("MONGO_PORT", "27017");

    @ClassRule
    public static RuleChain classRules = RuleChain.outerRule(network)
                    .around(mongodb)
                    .around(playerContainer);

    @Rule
    public TestName testName = new TestName();

    private static PersonService personSvc;

    @BeforeClass
    public static void setupClass() {
        playerContainer.followOutput(new Slf4jLogConsumer(LOGGER));
        mongodb.followOutput(new Slf4jLogConsumer(LOGGER));
        personSvc = playerContainer.createRestClient(PersonService.class, APP_PATH);
    }

    @Before
    public void setUp() {
        System.out.println("BEGIN TEST: " + testName.getMethodName());
    }

    @Test
    public void testCreatePerson() {
        Long createId = personSvc.createPerson("Hank", 42);
        assertNotNull(createId);
    }

    @Test
    public void testMinSizeName() {
        Long minSizeNameId = personSvc.createPerson("Ha", 42);
        assertEquals(new Person("Ha", 42, minSizeNameId),
                     personSvc.getPerson(minSizeNameId));
    }

    @Test
    public void testMinAge() {
        Long minAgeId = personSvc.createPerson("Newborn", 0);
        assertEquals(new Person("Newborn", 0, minAgeId),
                     personSvc.getPerson(minAgeId));
    }

    @Test
    public void testGetPerson() {
        Long bobId = personSvc.createPerson("Bob", 24);
        Person bob = personSvc.getPerson(bobId);
        assertEquals("Bob", bob.name);
        assertEquals(24, bob.age);
        assertNotNull(bob.id);
    }

    @Test
    public void testGetAllPeople() {
        Long person1Id = personSvc.createPerson("Person1", 1);
        Long person2Id = personSvc.createPerson("Person2", 2);

        Person expected1 = new Person("Person1", 1, person1Id);
        Person expected2 = new Person("Person2", 2, person2Id);

        Collection<Person> allPeople = personSvc.getAllPeople();
        assertTrue("Expected at least 2 people to be registered, but there were only: " + allPeople,
                   allPeople.size() >= 2);
        assertTrue("Did not find person " + expected1 + " in all people: " + allPeople,
                   allPeople.contains(expected1));
        assertTrue("Did not find person " + expected2 + " in all people: " + allPeople,
                   allPeople.contains(expected2));
    }

    @Test(expected = NotFoundException.class)
    public void testGetUnknownPerson() {
        personSvc.getPerson(-1L);
    }

    @Test(expected = BadRequestException.class)
    public void testCreateBadPersonNullName() {
        personSvc.createPerson(null, 5);
    }

    @Test(expected = BadRequestException.class)
    public void testCreateBadPersonNegativeAge() {
        personSvc.createPerson("NegativeAgePersoN", -1);
    }

    @Test(expected = BadRequestException.class)
    public void testCreateBadPersonNameTooLong() {
        personSvc.createPerson("NameTooLongPersonNameTooLongPersonNameTooLongPerson", 5);
    }
}