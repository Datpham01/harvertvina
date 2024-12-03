package vn.fs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.fs.entities.Category;
import vn.fs.entities.Origins;

public interface OriginsRepository extends JpaRepository<Origins, Long> {

}
