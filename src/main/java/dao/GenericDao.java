package dao;

import jakarta.persistence.*;
import util.JPAUtil;
import java.util.List;

public class GenericDao <T>{
    
    private Class<T> entityClass;

    public GenericDao(Class<T> entityClass){
        this.entityClass = entityClass;
    }

    public void save(T entity){
        EntityManager em = JPAUtil.getEntityManager();
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();
        em.close();
    }

    public List<T> findAll(){
        EntityManager em = JPAUtil.getEntityManager();

        List<T> list = em.createQuery("FROM " + entityClass.getSimpleName(), entityClass).getResultList();
        em.close();
        return list;
    }

    public void delete(Long id){
        EntityManager em = JPAUtil.getEntityManager();
        em.getTransaction().begin();
        T entity = em.find(entityClass, id);
        if (entity != null) em.remove(entity);
        em.getTransaction().commit();
        em.close();
    }
}
