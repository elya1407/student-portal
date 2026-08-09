package com.studentportal.repository;

import com.studentportal.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();

    @Query("select p from Post p where p.forSubgroup is null or p.forSubgroup = :subgroup order by p.createdAt desc")
    List<Post> findVisibleForSubgroup(@Param("subgroup") Integer subgroup);
}
