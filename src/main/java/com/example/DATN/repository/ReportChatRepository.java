package com.example.DATN.repository;

import com.example.DATN.entity.document.Report;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportChatRepository extends MongoRepository<Report,String> {
}
