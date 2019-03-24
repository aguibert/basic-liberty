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
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

public class PersonServiceTest {

    private static final String APP_PATH = "/myservice";
    private static final String MONGO_HOST = "testmongo";
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonServiceTest.class);

    @ClassRule
    public static Network network = Network.newNetwork();

    @ClassRule
    public static GenericContainer<?> mongodb = new GenericContainer<>("mongo:3.4")
                    .withNetwork(network)
                    .withNetworkAliases(MONGO_HOST)
                    .withLogConsumer(new Slf4jLogConsumer(LOGGER));

    @ClassRule
    public static LibertyContainer libertyContainer = new LibertyContainer("my-service")
                    .withExposedPorts(9080)
                    .waitingFor(Wait.forHttp(APP_PATH))
                    .withNetwork(network)
                    .withLogConsumer(new Slf4jLogConsumer(LOGGER))
                    .withEnv("MONGO_HOSTNAME", MONGO_HOST)
                    .withEnv("MONGO_PORT", "27017");

    @Rule
    public TestName testName = new TestName();

    private static PersonService personSvc;

    @BeforeClass
    public static void setupClass() {
        personSvc = libertyContainer.createRestClient(PersonService.class, APP_PATH);
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

    @Test
    public void testUpdateAge() {
        Long personId = personSvc.createPerson("newAgePerson", 1);

        Person originalPerson = personSvc.getPerson(personId);
        assertEquals("newAgePerson", originalPerson.name);
        assertEquals(1, originalPerson.age);
        assertEquals(personId, Long.valueOf(originalPerson.id));

        personSvc.updatePerson(personId, new Person(originalPerson.name, 2, originalPerson.id));
        Person updatedPerson = personSvc.getPerson(personId);
        assertEquals("newAgePerson", updatedPerson.name);
        assertEquals(2, updatedPerson.age);
        assertEquals(personId, Long.valueOf(updatedPerson.id));
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