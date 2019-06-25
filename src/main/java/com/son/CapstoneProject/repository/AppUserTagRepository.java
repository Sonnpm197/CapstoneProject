package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.AppUserTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserTagRepository extends JpaRepository<AppUserTag, Long> {
}
