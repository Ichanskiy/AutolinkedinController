package tech.mangosoft.autolinkedin.db.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import tech.mangosoft.autolinkedin.db.entity.Location;

public interface ILocationRepository extends CrudRepository<Location, Long> {

    Location getLocationByLocation(@Param("location") String location);

    Location getLocationByLocationLike(String location);
}
