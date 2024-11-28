package vn.fs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.fs.entities.Comment;
import vn.fs.entities.Product;

import java.util.List;
import java.util.Optional;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>{
    Optional<List<Comment>> findByProduct_ProductId(Long productId);
}
