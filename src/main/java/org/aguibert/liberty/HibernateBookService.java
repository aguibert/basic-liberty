package org.aguibert.liberty;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
@ApplicationScoped
public class HibernateBookService {
	
	@Resource
	UserTransaction tx;
	
	@GET
	public String getBook() throws Exception {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("test");  
		EntityManager em = emf.createEntityManager();
		
		tx.begin();
		System.out.println("Is in a tran already? " + em.isJoinedToTransaction());
		em.joinTransaction();
		
//		EntityTransaction tx = em.getTransaction();
		System.out.println("Using tx: " + tx);

		Book test = em.find(Book.class, 1);                                         
		if (test == null) {                       
			System.out.println("didn't find book");
		  test = new Book();                                                        
		  test.id = 1;
		  test.title = "Book A";

//		  tx.begin();                                                               
		  em.persist(test);                                                         
//		  tx.commit();                                                              
		}                                                                           

		System.out.format("Test{id=%s, data=%s}\n", test.id, test.title);            

		em.close(); 
		
		tx.commit();
		emf.close();
		
		return test.toString();
	}
	
	@GET
	@Path("/create")
	public String createBook() {
		return "bye";
	}
	
}
