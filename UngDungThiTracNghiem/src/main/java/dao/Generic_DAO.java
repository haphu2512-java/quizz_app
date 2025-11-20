package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import util.EntityManagerFactoryUtil;
import java.util.List;

public abstract class Generic_DAO <T, ID>{

    protected EntityManager em;
    protected Class<T> clazz;

    public Generic_DAO(Class<T> clazz) {
        this.clazz = clazz;
        this.em = EntityManagerFactoryUtil.getEntityManager();
    }

    public Generic_DAO(EntityManager em, Class<T> clazz) {
        this.em = em;
        this.clazz = clazz;
    }


    public T findById(ID id){
        return em.find(clazz, id);
    }

    public List<T> getAll(){
        return em.createQuery("from " + clazz.getSimpleName(), clazz)
                .getResultList();
    }

    public boolean save(T t){
        EntityTransaction tr = em.getTransaction();
        try{
            tr.begin();
            em.persist(t);
            tr.commit();
            return true;
        }catch (Exception ex){
            if(tr.isActive())
                tr.rollback();
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    public boolean update(T t){
        EntityTransaction tr = em.getTransaction();
        try{
            tr.begin();
            em.merge(t);
            tr.commit();
            return true;
        }catch (Exception ex){
            if(tr.isActive())
                tr.rollback();
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    public boolean delete(ID id){
        EntityTransaction tr = em.getTransaction();
        try{
            tr.begin();
            T t = em.find(clazz, id);
            if(t != null){
                em.remove(t);
                tr.commit();
                return true;
            }
        }catch (Exception ex){
            if(tr.isActive())
                tr.rollback();
            throw new RuntimeException(ex.getMessage(), ex);
        }

        return false;
    }

    public T mergeIfDetached(T entity) {
        if (!em.contains(entity)) {
            return em.merge(entity); // Hợp nhất đối tượng vào persistence context
        }
        return entity; // Nếu đối tượng đã được quản lý, trả về đối tượng nguyên gốc
    }
}