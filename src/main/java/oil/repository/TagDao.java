package oil.repository;

import oil.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

/**
 * Created by  waiter on 18-9-19  下午12:20.
 *
 * @author waiter
 */
public interface TagDao extends JpaRepository<Tag,Integer> {
    ArrayList<Tag> findTop10ByIsExistOrderByCases(boolean b);
    ArrayList<Tag> findAllByIsExistAndNameContaining(boolean b,String search);
    Tag findByName(String name);
}
