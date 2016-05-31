package io.hakansson.dynamicjar.frameworkprovider.db.test;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.UnboundLiteral;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-05-28.
 * Copyright 2016
 */
@RequestScoped
public class MainClass {

    @PersistenceContext(unitName = "defaultPersistenceUnit")
    private EntityManager em;

    public void init() {
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setTestColumn("test");
        em.getTransaction().begin();
        em.persist(sampleEntity);
        em.flush();
        em.getTransaction().commit();

        sampleEntity = (SampleEntity) em.createQuery("from SampleEntity").getSingleResult();
        System.out.println("Id: " + sampleEntity.getId() + ", TestColumn: " + sampleEntity.getTestColumn());
    }

    public static void main(String[] args) throws InterruptedException {
        RequestContext requestContext = CDI.current().select(RequestContext.class, UnboundLiteral.INSTANCE).get();
        requestContext.activate();
        MainClass managedInstance = CDI.current().select(MainClass.class).get();
        Thread t = new Thread() {
            public void run() {
                RequestContext requestContext2 = CDI.current().select(RequestContext.class, UnboundLiteral.INSTANCE).get();
                requestContext2.activate();
                managedInstance.init();
                requestContext2.deactivate();
            }
        };
        t.start();
        t.join();
        Thread.sleep(5000);
        requestContext.deactivate();
    }

}
