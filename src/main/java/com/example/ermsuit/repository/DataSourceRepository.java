package com.example.ermsuit.repository;

import com.example.ermsuit.entity.DataSource;
import com.example.ermsuit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataSourceRepository extends JpaRepository<DataSource, Long> {
    List<DataSource> findByUploadedBy(User user);
    List<DataSource> findByFormat(DataSource.DataFormat format);
}
