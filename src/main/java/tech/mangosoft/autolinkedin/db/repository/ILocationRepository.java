package tech.mangosoft.autolinkedin.db.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import tech.mangosoft.autolinkedin.db.entity.Location;

import java.util.List;

public interface ILocationRepository extends CrudRepository<Location, Long> {

    List<Location> findAll();

    Location getById(Long id);

    Location getLocationByLocation(@Param("location") String location);

    Location getLocationByLocationLike(String location);
}
