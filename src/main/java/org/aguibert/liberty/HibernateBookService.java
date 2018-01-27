package org.aguibert.liberty;

import java.net.URL;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;
import javax.persistence.SynchronizationType;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.resource.transaction.spi.TransactionStatus;

@Path("/")
@ApplicationScoped
public class HibernateBookService {

	@Resource(name = "jdbc/derby", lookup = "jdbc/derby")
	DataSource ds;

	@Resource
	UserTransaction tx;

	// @PersistenceUnit(unitName = "test_pu")
	// EntityManagerFactory emf;

	@GET
	public String getBook() throws Exception {
		try {
			Class.forName("org.hibernate.jpa.HibernatePersistenceProvider");
			log("Loaded hibernate provider class");
			
			URL pXml = getClass().getClassLoader().getResource("META-INF/persistence.xml");
			log("Found p.xml: " + pXml);

			EntityManagerFactory emf = null;
			PersistenceProviderResolver resolver = PersistenceProviderResolverHolder.getPersistenceProviderResolver();
			for (PersistenceProvider provider : resolver.getPersistenceProviders()) {
				log("Found provider: " + provider);
				emf = provider.createEntityManagerFactory("test_pu", null);
				log("Created emf: " + emf);
			}

			if (emf == null) {
				log("emf still null, trying agian...");
				emf = Persistence.createEntityManagerFactory("test_pu");
			}
			log("Emf props: " + emf.getProperties());

			log("");

			EntityManager em = emf.createEntityManager(SynchronizationType.SYNCHRONIZED);

			log("Is in a tran already? " + em.isJoinedToTransaction());

//			tx.begin();

			log("Joined? " + em.isJoinedToTransaction());
			em.joinTransaction();
			log("Joined? " + em.isJoinedToTransaction());

			// EntityTransaction tx = em.getTransaction();
			// log("Using tx: " + tx);

			Widget b = new Widget();
			b.id = 1;
			em.persist(b);
			log("Persisted book: " + b);

			// tx.commit();
			// log("committed tx");

//			tx.rollback();
//			log("rolled back");

			// Book test = em.find(Book.class, 1);
			// if (test == null) {
			// log("didn't find book");
			// test = new Book();
			// test.id = 1;
			// test.title = "Book A";
			//
			//// tx.begin();
			// em.persist(test);
			//// tx.commit();
			// }

			em.close();
			emf.close();
		} catch (Exception e) {
			log("Hit exception: " + e.getMessage());
			e.printStackTrace(System.out);
		}
		return writeLog();
	}

	@GET
	@Path("/hibernate")
	public String createBook() {
		StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
		        // "jdbc" is the default, but for explicitness
		        .applySetting( AvailableSettings.TRANSACTION_COORDINATOR_STRATEGY, "jta" )
		        .applySetting(AvailableSettings.DATASOURCE, ds)
		        .applySetting(AvailableSettings.DIALECT, "org.hibernate.dialect.DerbyTenSevenDialect")
		        //.applySetting(AvailableSettings.JTA_PLATFORM, "org.aguibert.liberty.WebSphereLibertyJtaPlatform")
		        //.applySetting(AvailableSettings.JTA_PLATFORM, "org.hibernate.engine.transaction.jta.platform.internal.WebSphereExtendedJtaPlatform")
		        .applySetting(AvailableSettings.JPA_JTA_DATASOURCE, "java:comp/env/jdbc/derby")
		        .applySetting("javax.persistence.schema-generation.database.action", "drop-and-create")
		        .build();

		Metadata metadata = new MetadataSources( serviceRegistry )
		        .addAnnotatedClass( Widget.class )
		        .getMetadataBuilder()
		        .build();

		SessionFactory sessionFactory = metadata.getSessionFactoryBuilder()
		        .build();

		// Note: depending on the JtaPlatform used and some optional settings,
		// the underlying transactions here will be controlled through either
		// the JTA TransactionManager or UserTransaction

		Session session = sessionFactory.openSession();
		try {
		    // Since we are in CMT, a JTA transaction would
		    // already have been started.  This call essentially
		    // no-ops
		    session.getTransaction().begin();

		    Number customerCount = (Number) session.createQuery( "select count(w) from Widget w" ).uniqueResult();

		    // Since we did not start the transaction ( CMT ),
		    // we also will not end it.  This call essentially
		    // no-ops in terms of transaction handling.
		    session.getTransaction().commit();
		}
		catch ( Exception e ) {
		    // again, the rollback call here would no-op (aside from
		    // marking the underlying CMT transaction for rollback only).
		    if ( session.getTransaction().getStatus() == TransactionStatus.ACTIVE
		            || session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK ) {
		        session.getTransaction().rollback();
		    }
		    // handle the underlying error
		    log(e.getMessage());
		    e.printStackTrace();
		}
		finally {
		    session.close();
		    sessionFactory.close();
		}
		return writeLog();
	}
	
	private StringBuilder log = new StringBuilder();
	
	private String writeLog() {
		String result = log.toString();
		log = new StringBuilder();
		return result;
	}

	private void log(String msg) {
		System.out.println(msg);
		log.append(msg);
		log.append("<br>");
		// log.append(" \n");
	}

}
