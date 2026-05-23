package dao;

import jakarta.persistence.EntityManager;
import model.PublicServer;
import util.JPAUtil;
import java.util.List;

public class PublicServerDAO extends GenericDao<PublicServer>{

    public PublicServerDAO(){
        super(PublicServer.class);
    }

    public PublicServer findByIdNumber(String idNumber){
        EntityManager em = JPAUtil.getEntityManager();

        List<PublicServer> list = em.createQuery(
            "SELECT s FROM PublicServer s WHERE s.idNumber = :idNumber",
            PublicServer.class)
            .setParameter("idNumber", idNumber)
            .getResultList();
        
        em.close();

        return list.isEmpty() ? null : list.get(0);
    }
}
