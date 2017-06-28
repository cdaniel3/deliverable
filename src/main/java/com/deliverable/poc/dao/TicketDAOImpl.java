package com.deliverable.poc.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.deliverable.model.Ticket;

public class TicketDAOImpl implements TicketDAO {

	private SessionFactory sessionFactory;

	@Override
	public void save(Ticket t) {
		Session session = this.sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		session.persist(t);
		tx.commit();
		session.close();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Ticket> list() {
		Session session = this.sessionFactory.openSession();
		List<Ticket> ticketList = session.createQuery("from Ticket").list();
		return ticketList;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Ticket> list(String query) {
		Session session = this.sessionFactory.openSession();
		List<Ticket> ticketList = session.createQuery(query).list();
		return ticketList;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	
}
