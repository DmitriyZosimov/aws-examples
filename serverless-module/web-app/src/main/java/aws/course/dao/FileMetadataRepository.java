package aws.course.dao;

import aws.course.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findByName(String name);

    void deleteByName(String name);

    @Query(value = "SELECT * FROM image_metadata im ORDER BY RAND() LIMIT 1", nativeQuery = true)
    FileMetadata findRandom();

}
