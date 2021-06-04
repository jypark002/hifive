package hifive;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="conferences", path="conferences")
public interface ConferenceRepository extends PagingAndSortingRepository<Conference, Long>{
    //CRUD. Repository Pattern 자동 생성
    
}
