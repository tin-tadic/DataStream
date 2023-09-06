package com.fsre.streamerapplication.streamer.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
@Component
public interface ResumeTokenRepository extends MongoRepository<ResumeToken, String> {

}
