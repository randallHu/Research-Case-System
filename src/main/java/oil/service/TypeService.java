package oil.service;

import oil.model.Type;
import oil.repository.TypeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by  waiter on 18-9-19  下午8:10.
 *
 * @author waiter
 */
@Service
public class TypeService {
    @Autowired
    private TypeDao typeDao;

    @Cacheable(value = "TypeService_findAll")
    public ArrayList<Type> findAll(){
        return typeDao.findAllByIsExistOrderByGradeAsc(true);
    }

    @CacheEvict(value = "TypeService_findAll",allEntries=true)
    public void save(Type type){
        typeDao.save(type);
    }

    public Type findById(Integer id){
        return typeDao.getById(id);
    }

}
