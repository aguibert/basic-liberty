/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.aguibert.liberty;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAResource;

import org.hibernate.HibernateException;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatformException;

/**
 * JTA platform implementation intended for use with WebSphere Liberty and OpenLiberty
 *
 * @author Andrew Guibert
 * @author Nathan Rauh
 */
public class WebSphereLibertyJtaPlatform extends AbstractJtaPlatform {
	
	static {
		System.out.println("@AGG loaded WebSphereLibertyJtaPlatform");
	}
	
	public static final String TMF_CLASS_NAME = "com.ibm.tx.jta.TransactionManagerFactory";
	
	public static final String UT_NAME = "java:comp/UserTransaction";
	
	public WebSphereLibertyJtaPlatform() {
		System.out.println("@AGG made new WebSphereLibertyJtaPlatform");
	}
	
	@Override
	protected TransactionManager locateTransactionManager() {
		System.out.println("@AGG locateTransactionManager");
		try {
			final Class<?> TransactionManagerFactory = serviceRegistry()
					.getService( ClassLoaderService.class )
					.classForName( TMF_CLASS_NAME );
			return (TransactionManager) TransactionManagerFactory.getMethod("getTransactionManager").invoke(null);
		}
		catch ( Exception e ) {
			throw new JtaPlatformException( "Could not obtain WebSphere Liberty transaction manager instance", e );
		}
	}

	@Override
	protected UserTransaction locateUserTransaction() {
		System.out.println("@AGG locateUserTransaction");
		return (UserTransaction) jndiService().locate( UT_NAME );
	}

	public boolean canRegisterSynchronization() {
		try {
			return getCurrentStatus() == Status.STATUS_ACTIVE;
		} 
		catch (SystemException x) {
			throw new RuntimeException(x);
		}
	}

	public int getCurrentStatus() throws SystemException {
		return retrieveTransactionManager().getStatus();
	}

	public Object getTransactionIdentifier(Transaction transaction) {
		return transaction;
	}

	public void registerSynchronization(Synchronization synchronization) {
		try {
			retrieveTransactionManager().getTransaction().registerSynchronization(synchronization);
		} 
		catch (IllegalStateException x) {
			throw new RuntimeException(x);
		} 
		catch (RollbackException x) {
			throw new RuntimeException(x);
		} 
		catch (SystemException x) {
			throw new RuntimeException(x);
		}
	}

}